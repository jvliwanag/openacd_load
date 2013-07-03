package com.ezuce.oacdlt;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AgentWsockConnectionFactory implements AgentConnectionFactory {
	private URI loginURI;
	private URI conURI;

	private ScheduledExecutorService exec;

	public AgentWsockConnectionFactory(URI loginURI, URI conURI) {
		this.loginURI = loginURI;
		this.conURI = conURI;

		this.exec = Executors.newScheduledThreadPool(10);
	}

	@Override
	public AgentConnection createConnection(String username, String password,
			Phone phone, AgentConnectionListener listener) {
		return new AgentWsockConnection(username, password, listener, phone,
				loginURI, conURI, exec);
	}
}
