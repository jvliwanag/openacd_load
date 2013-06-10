package com.ezuce.oacdlt;

import java.util.ArrayList;
import java.util.List;

public class SupervisorManager {
	
	private List<AgentConnection> agents;
	
	private String[] initCommands = {
		"ouc.get_contact_info",
		"get_connection_info",
		"get_release_codes",
		"get_connection_info",
		"ouc.get_my_rolling_stats",
		"ouc.get_live_stats",
		"ouc_sup.subscribe_queues",
		"ouc_sup.subscribe_queued_calls",
		"ouc.get_clients",
		"get_all_skills",
		"ouc.get_lines",
		"ouc_sup.subscribe_agents",
		"ouc_sup.subscribe_agent_profiles",
		"ouc.get_clients",
		"get_all_skills",
		"get_release_codes"	
	};
	
	public SupervisorManager(int extFrom, int extTo, String password,
			String sipPassword, AgentConnectionFactory connFactory,
			PhoneFactory phoneFactory) {

		initSupervisors(extFrom, extTo, password, sipPassword, connFactory,
				phoneFactory);
	}

	private void initSupervisors(int from, int to, String password,
			String sipPassword, AgentConnectionFactory connFactory,
			PhoneFactory phoneFactory) {
		this.agents = new ArrayList<>(to - from + 1);

		PhoneListener phoneListener = new DummyPhoneListener();
		AgentConnectionListener connListener = new CommonSupervisorConnectionListener();

		for (int i = from; i <= to; i++) {
			String username = Integer.toString(i);
			Phone p = phoneFactory.createPhone(username, sipPassword,
					phoneListener);
			AgentConnection conn = connFactory.createConnection(username,
					password, p, connListener);

			agents.add(conn);
		}
	}
	

	public void start() {
		for (AgentConnection agent : agents) {
			Phone p = agent.getPhone();
			p.register();
			agent.connect();
			
			for (String c : initCommands) {
				agent.sendRPC(c);
			}
		}
	}
	
	class CommonSupervisorConnectionListener implements AgentConnectionListener {

		@Override
		public void onClose(AgentConnection connection) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnect(AgentConnection connection) {
			
		}

		@Override
		public void onAvailable(AgentConnection connection) {
			
		}

		@Override
		public void onRelease(AgentConnection connection) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onWrapUp(AgentConnection connection) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGreeting(AgentConnection connection) {
		}
		
	}
}
