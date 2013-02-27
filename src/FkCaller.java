import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class FkCaller {

	private Logger logger;
	private Config config;
	private UserAgent ua;

	private int callNum = 0;

	private int idleMin;
	private int idleMax;
	private int callMin;
	private int callMax;

	private String[] lines;

	private ScheduledExecutorService exec;

	private Random rand;
	private SipRequest callReq;
	private ScheduledFuture<?> hangupFuture;

	public FkCaller(Config baseConfig, String username, String password,
			int idleMin, int idleMax, int callMin, int callMax, String[] lines,
			ScheduledExecutorService exec, Logger logger)
			throws SocketException {
		Config config = FkUtils.fromBaseConfig(baseConfig, username, password);

		// set to echo
		// config.setMediaMode(MediaMode.echo);

		SipListener listener = new FkCallerSipListener();
		UserAgent ua = new UserAgent(listener, config, logger);

		this.logger = logger;
		this.config = config;
		this.ua = ua;

		this.idleMin = idleMin;
		this.idleMax = idleMax;
		this.callMin = callMin;
		this.callMax = callMax;
		
		this.lines = lines;
		
		this.exec = exec;
		this.rand = new Random();
	}

	// once this starts, there's no stopping...
	// should have guard to start only once..
	public void startCalling() {
		scheduleNextCall();
	}

	private void scheduleNextCall() {
		int idle = rand.nextInt(idleMax - idleMin + 1) + idleMin;
		exec.schedule(new Runnable() {
			@Override
			public void run() {
				callLine(lines[rand.nextInt(lines.length)]);
			}
		}, idle, TimeUnit.MILLISECONDS);
	}

	private void scheduleNextHangUp() {
		int hangup = rand.nextInt(callMax - callMin + 1) + callMax;
		hangupFuture = exec.schedule(new Runnable() {

			@Override
			public void run() {
				ua.getUac().terminate(callReq);
				cleanupCurCall();
				scheduleNextCall();
			}
		}, hangup, TimeUnit.MILLISECONDS);
	}

	protected void cleanupCurCall() {
		Future<?> future = hangupFuture;
		if (future != null)
			future.cancel(false);

		hangupFuture = null;
		callReq = null;
	}

	public boolean callLine(String line) {
		// No checking whatsoever
		String uri = "sip:" + line + "@" + config.getDomain();
		String callId = "call-" + config.getUserPart() + "-" + (callNum++)
				+ "-" + line;

		try {
			callReq = ua.getUac().invite(uri, callId);
			scheduleNextHangUp();
			return true;
		} catch (SipUriSyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}

	class FkCallerSipListener implements SipListener {

		@Override
		public void registering(SipRequest sipRequest) {
		}

		@Override
		public void registerSuccessful(SipResponse sipResponse) {
			logger.info("Registered caller " + config.getUserPart());
		}

		@Override
		public void registerFailed(SipResponse sipResponse) {
			logger.error("Registration for caller " + config.getUserPart()
					+ " failed!");
		}

		@Override
		public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
			logger.error("Caller received call... This shouldn't happen!");
		}

		@Override
		public void remoteHangup(SipRequest sipRequest) {
			logger.info("Call hangup.. scheduling another call");
			cleanupCurCall();
			scheduleNextCall();
		}

		@Override
		public void ringing(SipResponse sipResponse) {
			logger.info("Ringing");
		}

		@Override
		public void calleePickup(SipResponse sipResponse) {
		}

		@Override
		public void error(SipResponse sipResponse) {
		}

	}
}
