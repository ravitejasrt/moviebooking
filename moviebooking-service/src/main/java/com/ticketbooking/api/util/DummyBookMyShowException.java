package com.ticketbooking.api.util;

public class DummyBookMyShowException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DummyBookMyShowException(String errorMsg) {
		super(errorMsg);
	}
}
