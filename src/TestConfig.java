import java.util.Properties;

public class TestConfig {
	private final String domain;
	private final String outboundProxy;
	private final String localInetAddress;
	private final String publicInetAddress;
	private final String[] lines;
	private final int agentExtStart;
	private final int agentExtEnd;
	private final String agentPassword;
	private final int callerExtStart;
	private final int callerExtEnd;
	private final String callerPwd;
	private final int agentAnsMin;
	private final int agentAnsMax;
	private final int agentCallMin;
	private final int agentCallMax;
	private final int callerIdleMin;
	private final int callerIdleMax;
	private final int callerCallMin;
	private final int callerCallMax;

	public static TestConfig fromProperties(Properties p) {

		String domain = p.getProperty("sip.domain", "oacdlt.3zuce.com");
		String outboundProxy = getString(p, "sip.outbound_proxy", null);

		String localInetAddress = getString(p, "sip.local_inet_address", null);
		String publicInetAddress = getString(p, "sip.public_inet_address", null);

		String[] lines = p.getProperty("lines").split(",");

		int agentExtStart = getInt(p, "agent.ext.start", 1100);
		int agentExtEnd = getInt(p, "agent.ext.end", 1200);
		String agentPassword = getString(p, "agent.password", "1234");

		int callerExtStart = getInt(p, "caller.ext.start", 1090);
		int callerExtEnd = getInt(p, "caller.ext.end", callerExtStart + 10);
		String callerPwd = getString(p, "caller.password", "1234");

		int agentAnsMin = getInt(p, "agent.ans.min", 500);
		int agentAnsMax = getInt(p, "agent.ans.max", 1000);
		int agentCallMin = getInt(p, "agent.call.min", 3000);
		int agentCallMax = getInt(p, "agent.call.max", 10000);

		int callerIdleMin = getInt(p, "caller.idle.min", 1000);
		int callerIdleMax = getInt(p, "caller.idle.max", 2000);
		int callerCallMin = getInt(p, "caller.call.min", 10000);
		int callerCallMax = getInt(p, "caller.call.max", 20000);

		return new TestConfig(domain, outboundProxy, localInetAddress,
				publicInetAddress, lines, agentExtStart, agentExtEnd,
				agentPassword, callerExtStart, callerExtEnd, callerPwd,
				agentAnsMin, agentAnsMax, agentCallMin, agentCallMax,
				callerIdleMin, callerIdleMax, callerCallMin, callerCallMax);
	}

	private static int getInt(Properties p, String key, int defaultVal) {
		try {
			return Integer.parseInt(p.getProperty(key));
		} catch (NumberFormatException ex) {
			return defaultVal;
		}
	}

	private static String getString(Properties p, String key, String defaultVal) {
		String t = p.getProperty(key, "");
		return t.equals("") ? defaultVal : t;
	}

	public TestConfig(String domain, String outboundProxy,
			String localInetAddress, String publicInetAddress, String[] lines,
			int agentExtStart, int agentExtEnd, String agentPassword,
			int callerExtStart, int callerExtEnd, String callerPwd,
			int agentAnsMin, int agentAnsMax, int agentCallMin,
			int agentCallMax, int callerIdleMin, int callerIdleMax,
			int callerCallMin, int callerCallMax) {
		super();
		this.domain = domain;
		this.outboundProxy = outboundProxy;
		this.localInetAddress = localInetAddress;
		this.publicInetAddress = publicInetAddress;
		this.lines = lines;
		this.agentExtStart = agentExtStart;
		this.agentExtEnd = agentExtEnd;
		this.agentPassword = agentPassword;
		this.callerExtStart = callerExtStart;
		this.callerExtEnd = callerExtEnd;
		this.callerPwd = callerPwd;
		this.agentAnsMin = agentAnsMin;
		this.agentAnsMax = agentAnsMax;
		this.agentCallMin = agentCallMin;
		this.agentCallMax = agentCallMax;
		this.callerIdleMin = callerIdleMin;
		this.callerIdleMax = callerIdleMax;
		this.callerCallMin = callerCallMin;
		this.callerCallMax = callerCallMax;
	}

	public String getDomain() {
		return domain;
	}

	public String getOutboundProxy() {
		return outboundProxy;
	}

	public String getLocalInetAddress() {
		return localInetAddress;
	}

	public String getPublicInetAddress() {
		return publicInetAddress;
	}

	public String[] getLines() {
		return lines;
	}

	public int getAgentExtStart() {
		return agentExtStart;
	}

	public int getAgentExtEnd() {
		return agentExtEnd;
	}

	public String getAgentPassword() {
		return agentPassword;
	}

	public int getCallerExtStart() {
		return callerExtStart;
	}

	public int getCallerExtEnd() {
		return callerExtEnd;
	}

	public String getCallerPwd() {
		return callerPwd;
	}

	public int getAgentAnsMin() {
		return agentAnsMin;
	}

	public int getAgentAnsMax() {
		return agentAnsMax;
	}

	public int getAgentCallMin() {
		return agentCallMin;
	}

	public int getAgentCallMax() {
		return agentCallMax;
	}

	public int getCallerIdleMin() {
		return callerIdleMin;
	}

	public int getCallerIdleMax() {
		return callerIdleMax;
	}

	public int getCallerCallMin() {
		return callerCallMin;
	}

	public int getCallerCallMax() {
		return callerCallMax;
	}
}
