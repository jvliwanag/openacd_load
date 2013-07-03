import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.ezuce.oacdlt.AgentConnectionFactory;
import com.ezuce.oacdlt.AgentManager;
import com.ezuce.oacdlt.AgentWsockConnectionFactory;
import com.ezuce.oacdlt.CallerManager;
import com.ezuce.oacdlt.PhoneFactory;
import com.ezuce.oacdlt.SupervisorManager;

public class Main1 {
	public static void main(String... args) throws Exception {

		Configuration config = new PropertiesConfiguration("config.properties");

		// sip
		Config baseConfig = getSipConfig(config);

		// phone factory
		PhoneFactory phoneF = new PhoneFactory(baseConfig);

		// caller settings
		CallerManager callerM = getCallerManager(config, phoneF);

		// agent settings
		AgentConnectionFactory connF = getConnectionFactory(config);

		AgentManager agentM = getAgentManager(config, phoneF, connF);
		SupervisorManager supervisorM = getSupervisorManager(config, phoneF,
				connF);

		callerM.register();
		agentM.start();
		supervisorM.start();

		Thread.sleep(3000);
		callerM.startCalls();
	}

	private static SupervisorManager getSupervisorManager(Configuration config,
			PhoneFactory phoneF, AgentConnectionFactory connF) {
		// supervisor settings
		Configuration supervisorConf = config.subset("supervisor");

		int supervisorFrom = supervisorConf.getInt("ext.start");
		int supervisorTo = supervisorConf.getInt("ext.end");
		String supervisorPassword = supervisorConf.getString("password");
		String supervisorSipPassword = supervisorConf.getString("sip_password");

		// supervisor Manager
		SupervisorManager supervisorM = new SupervisorManager(supervisorFrom,
				supervisorTo, supervisorPassword, supervisorSipPassword, connF,
				phoneF);
		return supervisorM;
	}

	private static AgentManager getAgentManager(Configuration config,
			PhoneFactory phoneF, AgentConnectionFactory connF) {
		Configuration agentConf = config.subset("agent");

		int agentFrom = agentConf.getInt("ext.start");
		int agentTo = agentConf.getInt("ext.end");
		String agentPassword = agentConf.getString("password");
		String agentSipPassword = agentConf.getString("sip_password");

		int ringMinMs = agentConf.getInt("ans.min");
		int ringMaxMs = agentConf.getInt("ans.max");
		int agentCallMinMs = agentConf.getInt("call.min");
		int agentCallMaxMs = agentConf.getInt("call.max");

		// agent Manager
		AgentManager agentM = new AgentManager(agentFrom, agentTo,
				agentPassword, agentSipPassword, ringMinMs, ringMaxMs,
				agentCallMinMs, agentCallMaxMs, connF, phoneF);
		return agentM;
	}

	private static AgentConnectionFactory getConnectionFactory(
			Configuration config) {
		URI loginURI = URI.create(config.getString("web.login_uri"));
		URI conURI = URI.create(config.getString("web.wsock_uri"));

		// conn factory
		AgentWsockConnectionFactory connF = new AgentWsockConnectionFactory(
				loginURI, conURI);
		return connF;
	}

	private static CallerManager getCallerManager(Configuration config,
			PhoneFactory phoneF) {
		Configuration callerConf = config.subset("caller");
		int callerFrom = callerConf.getInt("ext.start");
		int callerTo = callerConf.getInt("ext.end");
		String callerSipPassword = callerConf.getString("sip_password");

		int idleMinMs = callerConf.getInt("idle.min");
		int idleMaxMs = callerConf.getInt("idle.max");
		int callMinMs = callerConf.getInt("call.min");
		int callMaxMs = callerConf.getInt("call.max");

		List<String> lines = Arrays.asList(config.getStringArray("lines"));
		// caller manager
		CallerManager callerM = new CallerManager(callerFrom, callerTo,
				callerSipPassword, idleMinMs, idleMaxMs, callMinMs, callMaxMs,
				lines, phoneF);
		return callerM;
	}

	private static Config getSipConfig(Configuration config)
			throws SipUriSyntaxException, UnknownHostException {
		Configuration sipConf = config.subset("sip");
		String sipDomain = sipConf.getString("domain");
		String sipProxy = sipConf.getString("outbound_proxy");
		String sipLocalInetAddress = sipConf.getString("local_inet_address");
		String sipPublicInetAddress = sipConf.getString("public_inet_address");
		String sipPlaybackFile = sipConf.getString("playbackFile");

		JavaConfig baseConfig = new JavaConfig();
		baseConfig.setDomain(sipDomain);

		if (!isEmpty(sipProxy)) {
			SipURI r = new SipURI(sipProxy.startsWith("sip:") ? sipProxy
					: "sip:" + sipProxy);
			baseConfig.setOutboundProxy(r);
		}

		if (!isEmpty(sipLocalInetAddress)) {
			baseConfig.setLocalInetAddress(InetAddress
					.getByName(sipLocalInetAddress));
		} else {
			baseConfig.setLocalInetAddress(InetAddress.getLocalHost());
		}

		if (!isEmpty(sipPublicInetAddress)) {
			baseConfig.setPublicInetAddress(InetAddress
					.getByName(sipPublicInetAddress));
		}

		if (!isEmpty(sipPlaybackFile)) {
			baseConfig.setMediaMode(MediaMode.file);
			baseConfig.setMediaFile("./playback.wav");
		} else {
			baseConfig.setMediaMode(MediaMode.none);
		}
		return baseConfig;
	}

	private static boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}
}
