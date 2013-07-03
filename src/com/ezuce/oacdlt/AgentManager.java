package com.ezuce.oacdlt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.peers.sip.transport.SipResponse;

public class AgentManager {
	private Logger logger = LoggerFactory.getLogger(AgentManager.class);
	private List<AgentConnection> agents;

	private int ringMinMs;
	private int ringMaxMs;

	private int callMinMs;
	private int callMaxMs;

	private ScheduledExecutorService exec;

	private Random rand;

	private Map<Phone, Future<?>> phoneActions;
	
	private String[] initCommands = {
			"ouc.get_contact_info",
			"get_connection_info",
			"get_release_codes",
			"get_connection_info",
			"ouc.get_my_rolling_stats",
			"ouc.get_live_stats",
			"ouc.get_clients",
			"get_all_skills",
			"ouc.get_lines",
			"ouc.get_clients",
			"get_all_skills",
			"get_release_codes"
	};
	
	public AgentManager(int extFrom, int extTo, String password,
			String sipPassword, int ringMinMs, int ringMaxMs, int callMinMs,
			int callMaxMs, AgentConnectionFactory connFactory,
			PhoneFactory phoneFactory) {
		this.ringMinMs = ringMinMs;
		this.ringMaxMs = ringMaxMs;
		this.callMinMs = callMinMs;
		this.callMaxMs = callMaxMs;

		this.exec = Executors.newScheduledThreadPool((extTo - extFrom + 1) / 2);
		this.rand = new Random();
		this.phoneActions = new HashMap<>();

		initAgents(extFrom, extTo, password, sipPassword, connFactory,
				phoneFactory);
	}

	public void start() {
		for (AgentConnection agent : agents) {
			Phone p = agent.getPhone();
			p.register();

			agent.connect();
			agent.goAvailable();
			
			for (String c : initCommands) {
				agent.sendRPC(c);
			}
		}
	}

	private void initAgents(int from, int to, String password,
			String sipPassword, AgentConnectionFactory connFactory,
			PhoneFactory phoneFactory) {
		this.agents = new ArrayList<>(to - from + 1);

		AgentPhoneListener phoneListener = new AgentPhoneListener();
		AgentConnectionListener connListener = new CommonAgentConnectionListener();

		for (int i = from; i <= to; i++) {
			String username = Integer.toString(i);
			Phone p = phoneFactory.createPhone(username, sipPassword,
					phoneListener);

			AgentConnection conn = connFactory.createConnection(username,
					password, p, connListener);

			agents.add(conn);
		}
	}

	private void schedulePhoneAnswer(final Phone phone) {
		int delay = randomBetween(ringMinMs, ringMaxMs);
		log(phone, "scheduling answer after %dms", delay);
		Future<?> answer = exec.schedule(new Runnable() {
			@Override
			public void run() {
				log(phone, "answering");
				phone.answer();
				schedulePhoneHangUp(phone);
			}
		}, delay, TimeUnit.MILLISECONDS);

		phoneActions.put(phone, answer);
	}

	private void schedulePhoneHangUp(final Phone phone) {
		int delay = randomBetween(callMinMs, callMaxMs);
		log(phone, "scheduling hangup after %dms", delay);
		Future<?> hangUp = exec.schedule(new Runnable() {
			@Override
			public void run() {
				log(phone, "hanging up");
				phone.hangUp();
			}
		}, delay, TimeUnit.MILLISECONDS);

		phoneActions.put(phone, hangUp);
	}

	private int randomBetween(int min, int max) {
		return rand.nextInt(max - min + 1) + min;
	}

	public List<AgentConnection> getConns() {
		return agents;
	}

	class AgentPhoneListener implements PhoneListener {

		@Override
		public void onIncomingCall(Phone phone) {
			log(phone, "incoming call");
			schedulePhoneAnswer(phone);
		}

		@Override
		public void onRemoteHangup(Phone phone) {
			log(phone, "other end hang up");
			Future<?> action = phoneActions.remove(phone);
			if (action != null) {
				action.cancel(false);
				log(phone, "cancelled pending action");
			}
		}

		@Override
		public void onPickup(Phone phone) {
		}

		@Override
		public void onError(Phone phone, SipResponse sipResponse) {
		}
	}

	class CommonAgentConnectionListener implements AgentConnectionListener {
		@Override
		public void onClose(AgentConnection conn) {
			log(conn, "was disconnected. Reconnecting");
			conn.connect();
		}

		@Override
		public void onConnect(AgentConnection conn) {
			log(conn, "is connected");
			
		}
		
		public void onGreeting(AgentConnection conn) {
			
		}

		@Override
		public void onAvailable(AgentConnection conn) {
			log(conn, "went available");
		}

		@Override
		public void onRelease(AgentConnection conn) {
			log(conn, "was released. Putting back to available");
			conn.goAvailable();
		}

		@Override
		public void onWrapUp(AgentConnection conn) {
			log(conn, "entered wrapup.");
			conn.endWrapup();
		}
	}

	private void log(AgentConnection conn, String fmt, Object... args) {
		String user = conn.getUser();
		log(user, fmt, args);
	}

	private void log(Phone p, String fmt, Object... args) {
		String user = p.getUser();
		log(user, fmt, args);
	}

	private void log(String user, String fmt, Object... args) {
		String msg = String.format(fmt, args);
		
		String l = "[a " + user + "] - " + msg;
		logger.info(l);
		System.out.println(l);
	}
}
