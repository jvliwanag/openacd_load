package com.ezuce.oacdlt;

public class AgentConnectionException extends RuntimeException {
	private static final long serialVersionUID = -210700222048592158L;

	public AgentConnectionException() {
		super();
	}

	public AgentConnectionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AgentConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public AgentConnectionException(String message) {
		super(message);
	}

	public AgentConnectionException(Throwable cause) {
		super(cause);
	}

}
