package io.github.profilr.passwordportal;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.mysql.cj.jdbc.MysqlDataSource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabasePasswordResetHandler implements PasswordResetHandler {

	private static final String DUMMY_PASSWORD = RedeployPasswordResetHandler.DUMMY_PASSWORD;
	private static final String HIBERNATE_PROPERTIES_PATH = RedeployPasswordResetHandler.HIBERNATE_PROPERTIES_PATH;
	private static final String PROPERTY_DATASOURCE_NAME = "hibernate.hikari.dataSourceClassName";
	private static final String PROPERTY_PASSWORD = "hibernate.hikari.password";
	private static final String PROPERTY_URL = "hibernate.hikari.dataSource.url";

	private MysqlDataSource datasource;

	@Override
	@SneakyThrows(IOException.class)
	public void init(ServletContext context) throws InvalidConfigurationException {
		log.info("Reading in hibernate.properties");
		String path = context.getRealPath(HIBERNATE_PROPERTIES_PATH);
		Properties properties = new Properties();
		try (FileInputStream inputStream = new FileInputStream(path)) {
			properties.load(inputStream);
			String datasourceName = properties.getProperty(PROPERTY_DATASOURCE_NAME),
					password = properties.getProperty(PROPERTY_PASSWORD), url = properties.getProperty(PROPERTY_URL);
			if (!MysqlDataSource.class.getName().equals(datasourceName))
				throw new InvalidConfigurationException(
						String.format("Invalid DataSource specified. The value of property `%s` must be `%s`",
								PROPERTY_DATASOURCE_NAME, MysqlDataSource.class.getName()));
			if (url == null)
				throw new InvalidConfigurationException(
						String.format("No URL specified. The value of property `%s` must be a valid JDBC URL",
								PROPERTY_URL, MysqlDataSource.class.getName()));
			if (!DUMMY_PASSWORD.equals(password))
				throw new InvalidConfigurationException(
						String.format("Incorrect dummy password. The value of property `%s` must be `%s`",
								PROPERTY_PASSWORD, DUMMY_PASSWORD));
			datasource = new MysqlDataSource();
			datasource.setUrl(url);
			log.info("Successfully initialized");
		}
	}

	@Override
	public void checkPassword(String username, String oldPassword) throws IncorrectPasswordException {
		log.info("Testing password");
		datasource.setUser(username);
		datasource.setPassword(oldPassword);
		try (Connection c = datasource.getConnection();
				Statement s = c.createStatement();
				ResultSet rs = s.executeQuery("SELECT 1")) {
			log.info("Connection successful");
			rs.first();
			if (rs.getInt(1) != 1) {
				log.error("SELECT 1 returns {} ??", rs.getInt(1));
				throw new SQLException("Weird Return Value");
			}
			log.info("Password correct");
		} catch (SQLException e) {
			throw new IncorrectPasswordException(e);
		}
	}

	@Override
	@SneakyThrows(SQLException.class)
	public void resetPassword(String username, String oldPassword, String newPassword) {
		log.info("Changing database password");
		datasource.setUser(username);
		datasource.setPassword(oldPassword);
		try (Connection c = datasource.getConnection();
				PreparedStatement ps = c.prepareStatement("UPDATE USER ? IDENTIFIED BY ?")) {
			ps.setString(1, username);
			ps.setString(2, newPassword);
			ps.executeUpdate();
			log.info("Database Password Update Success");
		}
	}

}
