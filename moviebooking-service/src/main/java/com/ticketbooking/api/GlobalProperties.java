package com.ticketbooking.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@PropertySource("classpath:moviebooking.properties")
public class GlobalProperties {

//	@Setter
//	@Getter
    @Value("${booking.timeout}")
    private int timeout;

//	@Setter
//	@Getter
    @Value("${booking.seat.limit}")
    private int seatlimit;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getSeatlimit() {
		return seatlimit;
	}

	public void setSeatlimit(int seatlimit) {
		this.seatlimit = seatlimit;
	}
    
    //getters and setters
	
	

}
