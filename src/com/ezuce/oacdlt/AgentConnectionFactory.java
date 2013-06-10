package com.ezuce.oacdlt;

public interface AgentConnectionFactory {

	public AgentConnection createConnection(String username, String password,
			Phone phone, AgentConnectionListener listener);
}
