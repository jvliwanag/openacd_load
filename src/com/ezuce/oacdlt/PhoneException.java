package com.ezuce.oacdlt;

public class PhoneException extends RuntimeException {

	public PhoneException() {
		super();
	}

	public PhoneException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PhoneException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhoneException(String message) {
		super(message);
	}

	public PhoneException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 5299628538599005606L;

}
