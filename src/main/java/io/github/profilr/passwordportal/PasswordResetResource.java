package io.github.profilr.passwordportal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class PasswordResetResource {

	@GET
	public String get() {
		return "Hello World";
	}
	
}
