package io.github.profilr.passwordportal;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.servlet.ServletProperties;

@ApplicationPath("/reset-handler")
public class ResourceConfig extends org.glassfish.jersey.server.ResourceConfig {
	
	public ResourceConfig(){
		packages("io.github.profilr.passwordportal");
		property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "*\\.(html|css|png|ico)");
	}
	
}
