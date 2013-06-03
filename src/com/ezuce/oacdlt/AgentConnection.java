package com.ezuce.oacdlt;

public interface AgentConnection {

	public void connect();

	public void disconnect();

	public void goAvailable();

	public void goReleased();

	public void hangUp();

	public void endWrapup();

	public String getUser();

	public Phone getPhone();

	public void sendRPC(String method, Object... args);
}