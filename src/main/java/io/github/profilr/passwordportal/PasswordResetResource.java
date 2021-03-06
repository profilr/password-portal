package io.github.profilr.passwordportal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassGraphException;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.profilr.passwordportal.handlers.PasswordResetHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Path("/")
@Slf4j
public class PasswordResetResource {

	private static List<PasswordResetHandler> handlers;
	
	@SneakyThrows(ReflectiveOperationException.class)
	private List<PasswordResetHandler> getHandlers() throws InvalidConfigurationException {
		if (handlers == null) {
			log.info("Initializing handlers list");
			String packageName = PasswordResetHandler.class.getPackage().getName();
			String interfaceName = PasswordResetHandler.class.getName();
			handlers = new ArrayList<>();
			try (ScanResult scanResult = new ClassGraph().enableAllInfo()
														 .whitelistPackages(packageName)
														 .scan()) {
				for (ClassInfo info : scanResult.getClassesImplementing(interfaceName)) {
					try {
						Class<? extends PasswordResetHandler> clazz = info.loadClass(PasswordResetHandler.class);
						log.info("Initializing handler {}", clazz.getSimpleName());
						PasswordResetHandler handler = clazz.getDeclaredConstructor().newInstance();
						handler.init();
						handlers.add(handler);
					} catch (ReflectiveOperationException | IllegalArgumentException | InvalidConfigurationException e) {
						handlers = null; // reset cached handlers
						log.error("Error initializing handler "+info.getSimpleName(), e);
						throw e;
					}
				}

			} catch (ClassGraphException e) {
				log.error("Error collecting handlers", e);
				handlers = null;
				throw e;
			}
		}
		return handlers;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public Response reset(@FormParam("username") String username,
						  @FormParam("oldPassword") String oldPassword,
						  @FormParam("newPassword") String newPassword) {
		try {
			log.info("New request for user '{}'", username);
			handlers = getHandlers();
			log.debug("Obtained handlers");
			for (PasswordResetHandler handler : handlers)
				handler.checkPassword(username, oldPassword);
			log.debug("Checked password");
			for (PasswordResetHandler handler : handlers)
				handler.resetPassword(username, oldPassword, newPassword);
			log.info("Request processed successfully");
			return Response.ok("Success")
						   .build();
		} catch (Exception e) {
			log.debug("Unable to process request because of exception", e);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.printf("%s: %s", e.getClass().getSimpleName(), e.getMessage());
			Throwable c = e.getCause();
			if (c != null)
				pw.printf("%nCaused by %s: %s", c.getClass().getSimpleName(), c.getMessage());
			return Response.status(e instanceof IncorrectPasswordException ?
									Status.UNAUTHORIZED : Status.INTERNAL_SERVER_ERROR)
						   .entity(sw.toString())
						   .build();
		}
	}
	
}
