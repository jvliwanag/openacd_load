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
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

// not. thread. safe.
// DO NOT call login while there's a call
public class FkAgent {

	// yep, hardcoded
	private static final String LOGOUT_CODE = "*81";
	private static final String LOGIN_CODE = "*80";

	private Logger logger;
	private Config config;
	private UserAgent ua;

	private int callNum = 0;

	private CurCall curCall;

	private int minAnswer, maxAnswer, minCallDur, maxCallDur;
	private Random random;

	private ScheduledExecutorService exec;

	public FkAgent(Config baseConfig, String username, String password,
			int minAnswer, int maxAnswer, int minCallDur, int maxCallDur,
			ScheduledExecutorService exec, Logger logger)
			throws SocketException, SipUriSyntaxException {
		Config config = FkUtils.fromBaseConfig(baseConfig, username, password);

		// set to echo
		// config.setMediaMode(MediaMode.echo);

		SipListener listener = new FkCallerSipListener();
		UserAgent ua = new UserAgent(listener, config, logger);
		ua.getUac().register();

		this.logger = logger;
		this.config = config;
		this.ua = ua;

		this.minAnswer = minAnswer;
		this.maxAnswer = maxAnswer;
		this.minCallDur = minCallDur;
		this.maxCallDur = maxCallDur;

		this.exec = exec;
		this.random = new Random();
	}

	public boolean logIn() {
		return call(LOGIN_CODE);
	}

	public boolean logOut() {
		return call(LOGOUT_CODE);
	}

	public boolean call(String extn) {
		// No checking whatsoever. Trusting valid input
		String uri = "sip:" + extn + "@" + config.getDomain();
		String callId = "call-" + config.getUserPart() + "-" + (callNum++)
				+ "-" + extn;

		try {
			ua.getUac().invite(uri, callId);
			return true;
		} catch (SipUriSyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void scheduleAnswer() {

		final CurCall call = this.curCall;
		int ansDelay = random.nextInt(maxAnswer - minAnswer + 1) + minAnswer;
		logger.info("Agent " + config.getUserPart() + " will answer in "
				+ ansDelay + " ms");

		call.future = exec.schedule(new Runnable() {
			@Override
			public void run() {
				logger.info("Agent " + config.getUserPart() + " answering!");
				ua.getUas().acceptCall(call.callReq, call.dialog);
				scheduleHangup();
			}
		}, ansDelay, TimeUnit.MILLISECONDS);
	}

	private void scheduleHangup() {
		final CurCall call = this.curCall;
		int callDur = random.nextInt(maxCallDur - minCallDur + 1) + minCallDur;
		logger.info("Agent " + config.getUserPart() + " will hangup in "
				+ callDur + " ms");
		
		call.future = exec.schedule(new Runnable() {
			@Override
			public void run() {
				logger.info("Agent " + config.getUserPart() + " hanging up!");
				ua.getUac().terminate(call.callReq);
				cleanupCurCall();
			}
		}, callDur, TimeUnit.MILLISECONDS);
	}

	private void cleanupCurCall() {
		CurCall call = curCall;
		Future<?> future;
		if (call != null && (future = call.future) != null) {
			future.cancel(false);
		}
		curCall = null; // not safe
	}

	class FkCallerSipListener implements SipListener {

		@Override
		public void registering(SipRequest sipRequest) {
		}

		@Override
		public void registerSuccessful(SipResponse sipResponse) {
			logger.info("Registered agent " + config.getUserPart());
		}

		@Override
		public void registerFailed(SipResponse sipResponse) {
			logger.error("Registration for agent " + config.getUserPart()
					+ " failed!");
		}

		// wildly thread unsafe..
		@Override
		public void incomingCall(final SipRequest sipRequest,
				SipResponse provResponse) {
			logger.error("Incoming call!");

			Dialog dialog = ua.getDialogManager().getDialog(provResponse);
			curCall = new CurCall(dialog, sipRequest);

			scheduleAnswer();
		}

		@Override
		public void remoteHangup(SipRequest sipRequest) {
			logger.info("Call hangup");

			cleanupCurCall();
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

class CurCall {
	ScheduledFuture<?> future;
	Dialog dialog;
	SipRequest callReq;

	CurCall(Dialog dialog, SipRequest callReq) {
		this.dialog = dialog;
		this.callReq = callReq;
	}
}
