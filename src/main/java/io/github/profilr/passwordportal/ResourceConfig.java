package io.github.profilr.passwordportal;

import org.glassfish.jersey.servlet.ServletProperties;

public class ResourceConfig extends org.glassfish.jersey.server.ResourceConfig {
	
	public ResourceConfig(){
		packages("io.github.profilr.passwordportal");
		property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "*\\.(html|css|png|ico)");
	}
	
}
