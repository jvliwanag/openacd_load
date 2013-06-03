package com.ezuce.oacdlt;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AgentWebConnectionFactory implements AgentConnectionFactory {
	private URI loginURI;
	private URI conURI;

	private ScheduledExecutorService exec;

	public AgentWebConnectionFactory(URI loginURI, URI conURI) {
		this.loginURI = loginURI;
		this.conURI = conURI;

		this.exec = Executors.newScheduledThreadPool(10);
	}

	@Override
	public AgentConnection createConnection(String username, String password,
			Phone phone, AgentConnectionListener listener) {
		return new AgentWebConnection(username, password, listener, phone,
				loginURI, conURI, exec);
	}
}
