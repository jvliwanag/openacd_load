package com.ezuce.oacdlt;

public interface AgentConnectionListener {
	public void onClose(AgentConnection connection);
	public void onConnect(AgentConnection connection);
	public void onAvailable(AgentConnection connection);
	public void onRelease(AgentConnection connection);
	public void onWrapUp(AgentConnection connection);
	public void onGreeting(AgentConnection connection);
}
