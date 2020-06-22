package io.github.profilr.passwordportal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.profilr.passwordportal.handlers.PasswordResetHandler;
import lombok.SneakyThrows;

@Path("/")
public class PasswordResetResource {

	@Context
	ServletContext context;
	
	private static List<PasswordResetHandler> handlers;
	
	@SneakyThrows(ReflectiveOperationException.class)
	private List<PasswordResetHandler> getHandlers() throws InvalidConfigurationException {
		if (handlers == null) {
//			String packageName = PasswordResetHandler.class.getPackage().getName();
			String packageName = "io.github.profilr.passwordportal.handlerstest";
			String interfaceName = PasswordResetHandler.class.getName();
			handlers = new ArrayList<>();
			try (ScanResult scanResult = new ClassGraph().enableAllInfo()
														 .whitelistPackages(packageName)
														 .scan()) {
				for (ClassInfo info : scanResult.getClassesImplementing(interfaceName)) {
					Class<? extends PasswordResetHandler> clazz = info.loadClass(PasswordResetHandler.class);
					PasswordResetHandler handler = clazz.getConstructor().newInstance();
					try {
						handler.init(context);
					} catch (InvalidConfigurationException e) {
						handlers = null; // reset cached handlers
						throw e;
					}
					handlers.add(handler);
				}

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
			handlers = getHandlers();
			for (PasswordResetHandler handler : handlers)
				handler.checkPassword(username, oldPassword);
			for (PasswordResetHandler handler : handlers)
				handler.resetPassword(username, oldPassword, newPassword);
			return Response.ok("Success")
						   .build();
		} catch (InvalidConfigurationException | IncorrectPasswordException | RuntimeException e) {
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
