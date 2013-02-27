import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

public class Main1 {

	private static final int CALLER_EXT_START = 1090;
	private static final int CALLER_EXT_END = 1099;
	private static final String CALLER_PWD = "1234";

	private static final int CALLER_IDLE_MIN_MS = 1000;
	private static final int CALLER_IDLE_MAX_MS = 10000;

	// includes wait time
	private static final int CALLER_TCALL_MIN_MS = 10000;
	private static final int CALLER_TCALL_MAX_MS = 60000;

	private static final int AGENT_EXT_START = 1001;
	private static final int AGENT_EXT_END = 1009;
	private static final String AGENT_PWD = "1234";

	private static final int AGENT_MIN_ANS_MS = 1000;
	private static final int AGENT_MAX_ANS_MS = 5000;

	// does not include ring time
	private static final int AGENT_MIN_CALL_MS = 10000;
	private static final int AGENT_MAX_CALL_MS = 90000;

	private static final String[] LINES = { "90" };

	public static void main(String[] args) throws Exception {

		ScheduledExecutorService exec = Executors
				.newScheduledThreadPool(CALLER_EXT_END - CALLER_EXT_START + 1);
		Logger logger = new Logger(null);

		Config baseConfig = new JavaConfig();

		baseConfig.setDomain("oacddev.ezuce.com");

		// This should be a logical default...
		// baseConfig.setLocalInetAddress(InetAddress.getLocalHost());

		// But since I'm using a vm with a private ip...
		baseConfig.setLocalInetAddress(InetAddress.getByName("10.24.7.1"));

		baseConfig.setPublicInetAddress(baseConfig.getLocalInetAddress());

		initAgents(exec, logger, baseConfig);
		initCallers(exec, logger, baseConfig);
	}

	private static List<FkCaller> initCallers(ScheduledExecutorService exec,
			Logger logger, Config baseConfig) throws SocketException {
		List<FkCaller> callers = new ArrayList<FkCaller>(CALLER_EXT_END
				- CALLER_EXT_START + 1);
		for (int i = CALLER_EXT_START; i <= CALLER_EXT_END; i++) {
			String username = Integer.toString(i);
			FkCaller caller = new FkCaller(baseConfig, username, CALLER_PWD,
					CALLER_IDLE_MIN_MS, CALLER_IDLE_MAX_MS,
					CALLER_TCALL_MIN_MS, CALLER_TCALL_MAX_MS, LINES, exec,
					logger);
			callers.add(caller);

			caller.startCalling();
		}

		return callers;
	}

	private static List<FkAgent> initAgents(ScheduledExecutorService exec,
			Logger logger, Config baseConfig) throws SocketException,
			SipUriSyntaxException {

		List<FkAgent> agents = new ArrayList<FkAgent>(AGENT_EXT_END
				- AGENT_EXT_START + 1);
		for (int i = AGENT_EXT_START; i <= AGENT_EXT_END; i++) {
			String username = Integer.toString(i);
			FkAgent agent = new FkAgent(baseConfig, username, AGENT_PWD,
					AGENT_MIN_ANS_MS, AGENT_MAX_ANS_MS, AGENT_MIN_CALL_MS,
					AGENT_MAX_CALL_MS, exec, logger);
			agent.logIn();
			agents.add(agent);
		}

		return agents;
	}

}
