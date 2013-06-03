package com.ezuce.oacdlt;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@WebSocket(maxMessageSize=262144)
public class AgentWebConnection extends BaseAgentConnection {
	private URI loginURI;
	private URI conURI;

	private WebSocketClient wsock;
	private Session session;

	private int id;
	private ScheduledExecutorService exec;
	private ScheduledFuture<?> pingFuture;
	private String channelId;

	private JSONParser parser = new JSONParser();

	public AgentWebConnection(String username, String password,
			AgentConnectionListener listener, Phone phone, URI loginURI,
			URI conURI, ScheduledExecutorService exec) {
		super(username, password, listener, phone);
		
		this.username = username;
		this.password = password;
		this.listener = listener;
		this.loginURI = loginURI;
		this.conURI = conURI;

		this.exec = exec;
	}

	@Override
	public void connect() {
		HttpClient http = new HttpClient();
		try {
			http.start();
			tryLogin(http);

			wsock = new WebSocketClient();
			wsock.getPolicy().setMaxMessageSize(262144);
			wsock.setCookieStore(http.getCookieStore());

			wsock.start();
			connectWSock(wsock);

			startPing();
			listener.onConnect(this);
		} catch (Exception ex) {

			ex.printStackTrace();
			throw new AgentConnectionException("connect failed", ex);
		} finally {
			try {
				http.stop();
			} catch (Exception e) {
				e.printStackTrace();
				throw new AgentConnectionException(e);
			}
		}
	}

	@Override
	public void disconnect() {
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void goAvailable() {
		sendRPC("go_available");
	}

	@Override
	public void goReleased() {
		sendRPC("go_released");
	}

	@Override
	public void hangUp() {
		throw new AgentConnectionException("nyi");
	}

	@Override
	public void endWrapup() {
		if (channelId != null) {
			sendRPC("end_wrapup", channelId);
		}
	}

	public void sendPing() {
		sendRPC("ping");
	}

	private void tryLogin(HttpClient http) throws Exception {

		String body = String.format("username=%s&password=%s",
				URLEncoder.encode(username, "utf8"),
				URLEncoder.encode(password, "utf8"));

		System.out.println("body: " + body);
		ContentResponse resp = http.POST(loginURI)
				.header("Content-Type", "application/x-www-form-urlencoded ")
				.content(new StringContentProvider(body)).send();

		if (resp.getStatus() == HttpStatus.UNAUTHORIZED_401)
			throw new AgentConnectionException("Unauthorized");

	}

	private void connectWSock(WebSocketClient wsock) throws Exception {
		wsock.start();
		try {
			this.session = wsock.connect(this, conURI).get();
		} catch (Exception ex) {
			throw new AgentConnectionException("Failed to connect wsock", ex);
		}
	}

	private int nextId() {
		return id++;
	}

	// Not thread safe...
	public void sendRPC(String method, Object... os) {
		RPCMsg msg = new RPCMsg(Integer.toString(nextId()), method, os);
		try {
			session.getRemote().sendString(msg.toString());
		} catch (IOException e) {
			e.printStackTrace();

			// TODO handle
		}
	}

	private void startPing() {
		pingFuture = exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				sendPing();
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	// WSock Listener
	@OnWebSocketMessage
	public void onMessage(String msg) {
		try {
			JSONObject obj = (JSONObject) parser.parse(msg);

			if (obj.containsKey("jsonrpc")) {
				handleResult(obj);
			} else {
				String command = (String) obj.get("command");
				if (command != null) {
					handleCommand(command, obj);
				} else if (obj.get("username") != null) {
					handleGreeting();
				} else if (obj.get("event") != null) {
//					handleEvent();
				} else {
					System.out.printf("Something else: %s\n", msg);
				}
			}

		} catch (Exception e) {
			System.out.println("error on msg -- " + msg);
			e.printStackTrace();
		}
	}

	private void handleGreeting() {
		listener.onGreeting(this);
	}

	private void handleResult(JSONObject obj) {
		// A result
		
		Object result = obj.get("result");

		if (result instanceof JSONObject) {
			JSONObject r = (JSONObject) result;
			if (r.containsKey("pong")) {
				// It's a pong. disregard
			} else {
				// It's something else
				// System.out.println(msg);
			}
		}
	}

	private void handleCommand(String command, JSONObject obj) {
		if (command.equals("arelease")) {
			if (obj.get("releaseData").equals(false)) {
				listener.onAvailable(this);
			} else {
				listener.onRelease(this);
			}
		} else if (command.equals("setchannel")) {
			String state = (String) obj.get("state");
			channelId = (String) obj.get("channelid");
			
			if (state.equals("wrapup")) {
				listener.onWrapUp(this);
			}
		} else if (command.equals("endchannel")) {
			channelId = null;
		}
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
		this.session = null;
		this.pingFuture.cancel(false);

		listener.onClose(this);
	}
}
