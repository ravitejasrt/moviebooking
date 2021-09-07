package com.ticketbooking.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "yaml")
@PropertySource(value = "classpath:movie.yml", factory = YamlPropertySourceFactory.class)
public class YamlBookingProperties {

	
    private String timeout;

    private String seatlimit;

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getSeatlimit() {
		return seatlimit;
	}

	public void setSeatlimit(String seatlimit) {
		this.seatlimit = seatlimit;
	}
    
    
    

	
}
