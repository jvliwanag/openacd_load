package com.ezuce.oacdlt;

public abstract class BaseAgentConnection implements AgentConnection {
	protected String username;
	protected String password;
	protected AgentConnectionListener listener;
	protected Phone phone;

	public BaseAgentConnection(String username, String password,
			AgentConnectionListener listener, Phone phone) {
		super();
		this.username = username;
		this.password = password;
		this.listener = listener;
		this.phone = phone;
	}

	public String getUser() {
		return username;
	}

	public AgentConnectionListener getListener() {
		return listener;
	}

	public Phone getPhone() {
		return phone;
	}
}
