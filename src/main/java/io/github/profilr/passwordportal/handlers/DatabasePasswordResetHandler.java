package io.github.profilr.passwordportal.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.mysql.cj.jdbc.MysqlDataSource;

import io.github.profilr.passwordportal.IncorrectPasswordException;
import io.github.profilr.passwordportal.InvalidConfigurationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabasePasswordResetHandler implements PasswordResetHandler {

	private static final String DUMMY_USERNAME = RedeployPasswordResetHandler.DUMMY_USERNAME;
	private static final String DUMMY_PASSWORD = RedeployPasswordResetHandler.DUMMY_PASSWORD;
	private static final String PROPERTY_DATASOURCE_NAME = "hibernate.hikari.dataSourceClassName";
	private static final String PROPERTY_USERNAME = "hibernate.hikari.username";
	private static final String PROPERTY_PASSWORD = "hibernate.hikari.password";
	private static final String PROPERTY_URL = "hibernate.hikari.dataSource.url";

	private MysqlDataSource datasource;

	@Override
	public void init() throws InvalidConfigurationException {
		log.info("Initializing DatabasePasswordResetHandler");
		log.debug("Reading in hibernate.properties");
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("hibernate.properties")) {
			log.debug("Obtained hibernate.properties input stream");
			if (is == null) {
				log.error("Unable to find hibernate.properties, ensure it is present in src/main/resources");
				throw new InvalidConfigurationException("Unable to load hibernate.properties. Please check log file");
			}
			Properties properties = new Properties();
			properties.load(is);
			log.debug("Loaded hibernate.properties into Properties object");
			String datasourceName = properties.getProperty(PROPERTY_DATASOURCE_NAME),
				   username = properties.getProperty(PROPERTY_USERNAME),
				   password = properties.getProperty(PROPERTY_PASSWORD),
				   url = properties.getProperty(PROPERTY_URL);
			if (!MysqlDataSource.class.getName().equals(datasourceName)) {
				log.error("Invalid DataSource specified: Property '{}' must be '{}' (only MySQL is supported at the moment)", PROPERTY_DATASOURCE_NAME, MysqlDataSource.class.getName());
				throw new InvalidConfigurationException("Invalid DataSource specified in hibernate.properties. Please check log file");
			}
			if (url == null) {
				log.error("No URL specified: Property '{}' must contain the JDBC URL", PROPERTY_URL);
				throw new InvalidConfigurationException("No URL specified in hibernate.properties. Please check log file");
			}
			if (!DUMMY_PASSWORD.equals(password) || !DUMMY_USERNAME.equals(username)) {
				log.error("Incorrect dummy username and/or dummy password specified: Property '{}' should be '{}' and property '{}' should be '{}'",
						PROPERTY_USERNAME, DUMMY_USERNAME, PROPERTY_PASSWORD, DUMMY_PASSWORD);
				throw new InvalidConfigurationException("Incorrect placeholder credentials specified in configuration. Please check log file");
			}
			log.debug("Read and validated all properties");
			datasource = new MysqlDataSource();
			datasource.setUrl(url);
			log.debug("Created DataSource (without any credentials yet)");
			log.info("Successfully initialized");
		} catch (IOException | RuntimeException e) {
			log.error("Unhandled exception in initialization (ensure valid hibernate.properties file in src/main/resources)", e);
			throw new InvalidConfigurationException("Unhandled exception. Please check log file", e);
		}
	}

	@Override
	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException {
		log.info("Testing password");
		datasource.setUser(username);
		datasource.setPassword(oldPassword);
		log.debug("Attempting connection");
		try (Connection c = datasource.getConnection();
				Statement s = c.createStatement();
				ResultSet rs = s.executeQuery("SELECT 1")) {
			log.debug("Connection successful, testing query return");
			rs.first();
			if (rs.getInt(1) != 1) {
				log.error("'SELECT 1' returns {} from database!!?!", rs.getInt(1));
				throw new SQLException("Weird Return Value");
			}
			log.debug("Query success");
			log.info("Password correct");
		} catch (SQLException e) {
			log.info("Password incorrect");
			throw new IncorrectPasswordException();
		}
	}

	@Override
	@SneakyThrows(SQLException.class)
	public void resetPassword(String username, String oldPassword, String newPassword) {
		log.info("Changing database password");
		datasource.setUser(username);
		datasource.setPassword(oldPassword);
		log.debug("Attempting connection");
		try (Connection c = datasource.getConnection();
				PreparedStatement ps = c.prepareStatement("ALTER USER ? IDENTIFIED BY ?")) {
			log.debug("Connection successful, running 'ALTER USER' update");
			ps.setString(1, username);
			ps.setString(2, newPassword);
			ps.executeUpdate();
			log.info("Database Password Update Success");
		} catch (SQLException e) {
			log.error("Unhandled Exception encountered when changing password", e);
			throw e;
		}
	}

}
