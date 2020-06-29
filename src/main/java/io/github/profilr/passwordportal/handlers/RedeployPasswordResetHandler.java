package io.github.profilr.passwordportal.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import io.github.profilr.passwordportal.InvalidConfigurationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedeployPasswordResetHandler implements PasswordResetHandler {

	static final String DUMMY_USERNAME = "{{{USERNAME}}}";
	static final String DUMMY_PASSWORD = "{{{PASSWORD}}}";
	
	private String hibernateProperties;
	private File destination;

	@Override
	public void init() throws InvalidConfigurationException {
		log.info("Reading in redeploy.properties");
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("redeploy.properties")) {
			log.debug("Obtained redeploy.properties input stream");
			if (is == null) {
				log.error("Unable to find redeploy.properties, ensure it is present in src/main/resources");
				throw new InvalidConfigurationException("Unable to load redeploy.properties. Please check log file");
			}
			Properties properties = new Properties();
			properties.load(is);
			log.debug("Loaded redeploy.properties into Properties object");
			String destinationPath = properties.getProperty("destination");
			if (destinationPath == null) {
				log.error("No destination file path found. Ensure key 'destination' has a value.");
				throw new InvalidConfigurationException("No destination specified. Please check log file");
			}
			destination = new File(destinationPath);
			if (!destination.isFile()) {
				log.error("Destination file '{}' does not exist", destination.getAbsolutePath());
				throw new InvalidConfigurationException("Specified destination does not exist. Please check log file");
			}
		} catch (IOException e) {
			log.error("Unhandled IOException in reading redeploy.properties file (ensure valid redeploy.properties file in src/main/resources)", e);
			throw new InvalidConfigurationException("Unhandled exception. Please check log file", e);
		}
		log.info("Reading in hibernate.properties");
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("hibernate.properties");
			 BufferedReader bf = new BufferedReader(new InputStreamReader(is))) {
			log.debug("Obtained BufferedReader of hibernate.properties");
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = bf.readLine()) != null)
				sb.append(line).append('\n');
			hibernateProperties = sb.toString();
			log.debug("Read hibernate.properties into String hibernateProperties");
		} catch (NullPointerException e) {
			log.error("File hibernate.properties not found", e);
			throw new InvalidConfigurationException("Unable to load hibernate.properties. Please check log file");
		} catch (IOException e) {
			log.error("Unhandled exception in initialization (ensure valid hibernate.properties file in src/main/resources)", e);
			throw new InvalidConfigurationException("Unhandled exception. Please check log file", e);
		}
		log.info("Successfully initialized");
	}

	@Override
	public void checkPassword(String username, String oldPassword) {}

	@Override
	@SneakyThrows(IOException.class)
	public void resetPassword(String username, String oldPassword, String newPassword) {
		log.info("Redeploying new configuration");
		String output = hibernateProperties.replace(DUMMY_USERNAME, username)
										   .replace(DUMMY_PASSWORD, newPassword);
		try (FileOutputStream fs = new FileOutputStream(destination)) {
			log.debug("Obtained file stream to destination file");
			fs.write(output.getBytes());
		} catch (IOException e) {
			log.error("Unhandled (and unreported) IOException during redploy", e);
			throw e;
		}
		log.info("Redeploy successful");
	}

}