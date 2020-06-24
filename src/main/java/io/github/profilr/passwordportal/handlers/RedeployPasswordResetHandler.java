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
class RedeployPasswordResetHandler implements PasswordResetHandler {

	static final String DUMMY_USERNAME = "{{{USERNAME}}}";
	static final String DUMMY_PASSWORD = "{{{PASSWORD}}}";
	
	private String hibernateProperties;
	private File destination;

	@Override
	public void init() throws InvalidConfigurationException {
		log.info("Reading in redeploy.properties");
		try (InputStream is = getClass().getResourceAsStream("redeploy.properties")) {
			Properties properties = new Properties();
			properties.load(is);
			String destinationPath = properties.getProperty("destination");
			if (destinationPath == null)
				throw new InvalidConfigurationException("No destination file path found. Make sure that `redeploy.properties` has a key `destination`");
			destination = new File(destinationPath);
			if (!destination.isFile())
				throw new InvalidConfigurationException(String.format("File `%s` either does not exist or is not a file", destinationPath));
		} catch (IOException e) {
			throw new InvalidConfigurationException("IOException in reading redeploy.properties file", e);
		}
		log.info("Reading in hibernate.properties");
		try (InputStream is = getClass().getResourceAsStream("hibernate.properties");
			 BufferedReader bf = new BufferedReader(new InputStreamReader(is))) {
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = bf.readLine()) != null)
				sb.append(line);
			hibernateProperties = sb.toString();
		} catch (IOException e) {
			throw new InvalidConfigurationException("IOException in reading hibernate.properties file", e);
		}
	}

	@Override
	public void checkPassword(String username, String oldPassword) {}

	@Override
	@SneakyThrows(IOException.class)
	public void resetPassword(String username, String oldPassword, String newPassword) {
		String output = hibernateProperties.replace(DUMMY_USERNAME, username)
										   .replace(DUMMY_PASSWORD, newPassword);
		try (FileOutputStream fs = new FileOutputStream(destination)) {
			fs.write(output.getBytes());
		}
	}

}