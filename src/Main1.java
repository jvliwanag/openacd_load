import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

public class Main1 {
	public static void main(String[] args) throws Exception {
		Properties p = new Properties();
		p.load(new FileInputStream("config.properties"));

		TestConfig testConfig = TestConfig.fromProperties(p);

		ScheduledExecutorService exec = Executors.newScheduledThreadPool(10);
		Logger logger = new Logger(null);

		Config baseConfig = new JavaConfig();
		baseConfig.setDomain(testConfig.getDomain());

		if (testConfig.getOutboundProxy() != null)
			baseConfig.setOutboundProxy(new SipURI("sip:" + testConfig
					.getOutboundProxy()));

		if (testConfig.getLocalInetAddress() != null) {
			baseConfig.setLocalInetAddress(InetAddress.getByName(testConfig
					.getLocalInetAddress()));
		} else {
			baseConfig.setLocalInetAddress(InetAddress.getLocalHost());
		}

		if (testConfig.getPublicInetAddress() != null) {
			baseConfig.setPublicInetAddress(InetAddress.getByName(testConfig
					.getPublicInetAddress()));
		} else {
			baseConfig.setPublicInetAddress(baseConfig.getLocalInetAddress());
		}

		initCallers(exec, logger, baseConfig, testConfig);
		initAgents(exec, logger, baseConfig, testConfig);
	}

	private static List<FkCaller> initCallers(ScheduledExecutorService exec,
			Logger logger, Config baseConfig, TestConfig testConfig)
			throws SocketException {
		int extStart = testConfig.getCallerExtStart();
		int extEnd = testConfig.getCallerExtEnd();

		String pwd = testConfig.getCallerPwd();

		int idleMin = testConfig.getCallerIdleMin();
		int idleMax = testConfig.getCallerIdleMax();

		int callMin = testConfig.getCallerCallMin();
		int callMax = testConfig.getCallerCallMax();

		String[] lines = testConfig.getLines();

		List<FkCaller> callers = new ArrayList<FkCaller>(extEnd - extStart + 1);
		for (int i = extStart; i <= extEnd; i++) {
			String username = Integer.toString(i);
			FkCaller caller = new FkCaller(baseConfig, username, pwd, idleMin,
					idleMax, callMin, callMax, lines, exec, logger);
			callers.add(caller);

			caller.startCalling();
		}

		return callers;
	}

	private static List<FkAgent> initAgents(ScheduledExecutorService exec,
			Logger logger, Config baseConfig, TestConfig testConfig)
			throws SocketException, SipUriSyntaxException {

		int extStart = testConfig.getAgentExtStart();
		int extEnd = testConfig.getAgentExtEnd();

		String pwd = testConfig.getAgentPassword();

		int ansMin = testConfig.getAgentAnsMin();
		int ansMax = testConfig.getAgentAnsMax();

		int callMin = testConfig.getAgentCallMin();
		int callMax = testConfig.getAgentCallMax();

		List<FkAgent> agents = new ArrayList<FkAgent>(extEnd - extStart + 1);
		for (int i = extStart; i <= extEnd; i++) {
			String username = Integer.toString(i);
			FkAgent agent = new FkAgent(baseConfig, username, pwd, ansMin,
					ansMax, callMin, callMax, exec, logger);
			// agent.logOut();
			agent.logIn();
			agents.add(agent);
		}

		return agents;
	}

}
