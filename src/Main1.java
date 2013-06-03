import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;

import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaMode;

import com.ezuce.oacdlt.AgentManager;
import com.ezuce.oacdlt.AgentWebConnectionFactory;
import com.ezuce.oacdlt.CallerManager;
import com.ezuce.oacdlt.PhoneFactory;
import com.ezuce.oacdlt.SupervisorManager;

public class Main1 {
	public static void main(String... args) throws Exception {

		URI loginURI = URI.create("http://oacddev.ezuce.com:8936/login");
		URI conURI = URI.create("ws://oacddev.ezuce.com:8936/wsock");

		Logger logger = new Logger(null);

		String password = "password";
		String sipPassword = "1234";
		String sipDomain = "oacddev.ezuce.com";

		// caller settings
		int callerFrom = 1002;
		int callerTo = 1002;

		int idleMinMs = 2000;
		int idleMaxMs = 5000;
		int callMinMs = 5000;
		int callMaxMs = 8000;

		ArrayList<String> lines = new ArrayList<>();
		lines.add("90");

		String inetAddress = "10.24.7.1";

		// agent settings
		int agentFrom = 1005;
		int agentTo = 1009;

		int ringMinMs = 1000;
		int ringMaxMs = 5000;
		int agentCallMinMs = 2000;
		int agentCallMaxMs = 10000;

		// supervisor settings
		int supervisorFrom = 1003;
		int supervisorTo = 1004;

		// sip config

		JavaConfig baseConfig = new JavaConfig();
		baseConfig.setLocalInetAddress(InetAddress.getByName(inetAddress));
		// baseConfig.setLocalInetAddress(InetAddress.getLocalHost())
		baseConfig.setDomain(sipDomain);
		baseConfig.setMediaMode(MediaMode.none);

		// phone factory
		PhoneFactory phoneF = new PhoneFactory(baseConfig, logger);
		//
		// // caller manager
		CallerManager callerM = new CallerManager(callerFrom, callerTo,
				sipPassword, idleMinMs, idleMaxMs, callMinMs, callMaxMs, lines,
				phoneF);

		callerM.register();
		callerM.startCalls();

		// conn factory
		AgentWebConnectionFactory connF = new AgentWebConnectionFactory(
				loginURI, conURI);

		// agent Manager
		AgentManager agentM = new AgentManager(agentFrom, agentTo, password,
				sipPassword, ringMinMs, ringMaxMs, agentCallMinMs,
				agentCallMaxMs, connF, phoneF);

		// supervisor Manager
		SupervisorManager supervisorM = new SupervisorManager(supervisorFrom,
				supervisorTo, password, sipPassword, connF, phoneF);

		callerM.register();
		agentM.start();
		supervisorM.start();

		callerM.startCalls();

		// Phone p = phoneF.createPhone("1095", "1234", new
		// DummyPhoneListener());
		// p.register();
		// p.reset();
		// Thread.sleep(5000);
		// System.out.println("ok");
	}
}
