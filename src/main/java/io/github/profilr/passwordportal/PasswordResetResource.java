package io.github.profilr.passwordportal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
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
			String packageName = PasswordResetHandler.class.getPackage().getName();
			String interfaceName = PasswordResetHandler.class.getName();
			handlers = new ArrayList<>();
			try (ScanResult scanResult = new ClassGraph().enableAllInfo()
														 .whitelistPackages(packageName)
														 .scan()) {
				for (ClassInfo info : scanResult.getClassesImplementing(interfaceName)) {
					Class<? extends PasswordResetHandler> clazz = info.loadClass(PasswordResetHandler.class);
					PasswordResetHandler handler = clazz.getConstructor().newInstance();
					handler.init(context);
					handlers.add(handler);
				}

			}
		}
		return handlers;
	}
	
	@POST
	public Response reset(@FormParam("username") String username,
						  @FormParam("oldPassword") String oldPassword,
						  @FormParam("newPassword") String newPassword) {
		try {
			handlers = getHandlers();
			for (PasswordResetHandler handler : handlers)
				handler.checkPassword(username, oldPassword);
			for (PasswordResetHandler handler : handlers)
				handler.resetPassword(username, oldPassword, newPassword);
			return Response.noContent()
						   .build();
		} catch (InvalidConfigurationException | RuntimeException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return Response.serverError()
						   .entity(sw.toString())
						   .build();
		} catch (IncorrectPasswordException e) {
			return Response.status(Status.UNAUTHORIZED)
						   .build();
		}
	}
	
}
