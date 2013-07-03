package com.ezuce.oacdlt;

import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.Synchronized;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Phone {
	private Logger logger = LoggerFactory.getLogger(Phone.class);

	private UserAgent ua;

	private boolean registerSuccess;

	private int callNum;

	private Config config;

	// Latches to make this synchronous
	private CountDownLatch registerLatch;
	private CountDownLatch ringLatch;

	private PhoneListener listener;
	private Phone phone;

	private boolean isIncomingRing;

	private Dialog dialog;
	private SipRequest activeCallReq;

	public Phone(Config config, PhoneListener listener) {
		this.config = config;
		this.listener = listener;
		this.phone = this;

		try {
			ua = new UserAgent(new PhoneSipListener(), config);
		} catch (SocketException e) {
			rethrow("failed to create phone", e);
		}
	}

	@Synchronized
	public void register() {
		registerSuccess = false;
		registerLatch = new CountDownLatch(1);

		boolean completed = false;

		try {
			ua.getUac().register();
			completed = registerLatch.await(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			rethrow("register fail", e);
		}

		if (completed) {
			if (!registerSuccess) {
				rethrow("register fail");
			}
		} else {
			rethrow("register timeout");
		}
	}

	@Synchronized
	public void unregister() {
		try {
			ua.getUac().unregister();
		} catch (SipUriSyntaxException e) {
			rethrow("unregister fail", e);
		}
	}

	@Synchronized
	public void reset() {
		try {
			// unregister();
			ua.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		registerLatch = null;
		ringLatch = null;
		dialog = null;
		activeCallReq = null;

		try {
			ua = new UserAgent(new PhoneSipListener(), config);
			register();
		} catch (Exception e) {
			throw new PhoneException("reset fail", e);
		}
	}

	@Synchronized
	public void answer() {
		if (dialog == null || activeCallReq == null)
			throw new PhoneException("no call to answer");

		ua.getUas().acceptCall(activeCallReq, dialog);
		isIncomingRing = false;
		dialog = null;
	}

	@Synchronized
	public void dial(String extension) {
		String sipAddr = "sip:" + extension + "@" + config.getDomain();
		dialSip(sipAddr);
	}

	@Synchronized
	public void dialSip(String sipAddr) {
		if (activeCallReq != null)
			throw new PhoneException("busy with another call");

		if (!sipAddr.startsWith("sip:")) {
			sipAddr = "sip:" + sipAddr;
		}

		String callId = "call-" + config.getUserPart() + "-" + (callNum++);

		try {
			isIncomingRing = false;
			ringLatch = new CountDownLatch(1);
			activeCallReq = ua.getUac().invite(sipAddr, callId);
			System.out.println("sending invite");
		} catch (SipUriSyntaxException e) {
			e.printStackTrace();
			throw new PhoneException("unable to dial", e);
		}

		try {
			if (!ringLatch.await(5, TimeUnit.SECONDS)) {
				activeCallReq = null;
				ringLatch = null;
				rethrow("dial timeout");
			}
		} catch (InterruptedException e) {
			rethrow("interrupted", e);
		}
	}

	@Synchronized
	public void hangUp() {
		if (activeCallReq != null) {
			if (isIncomingRing) {
				ua.getUas().rejectCall(activeCallReq);
			} else {
				ua.getUac().terminate(activeCallReq);
			}

			activeCallReq = null;
		} else {
			// TODO must throw error
			// rethrow("hangup on no active call");
			logger.warn("Hangup called when no current call is present");
		}
	}

	private void rethrow(String msg) {
		logger.error(msg);
		System.out.println("error -- " + msg);
		throw new PhoneException(msg);
	}

	private void rethrow(String msg, Exception ex) {
		logger.error(msg, ex);
		ex.printStackTrace();
		throw new PhoneException(msg, ex);
	}

	// Getters

	public String getUser() {
		return config.getUserPart();
	}

	public boolean isBusy() {
		return activeCallReq != null;
	}

	public SipRequest getActiveCall() {
		return activeCallReq;
	}

	class PhoneSipListener implements SipListener {

		@Override
		public void registering(SipRequest sipRequest) {
		}

		@Override
		public void registerSuccessful(SipResponse sipResponse) {
			registerSuccess = true;
			registerLatch.countDown();
			logger.debug("register ok");
		}

		@Override
		public void registerFailed(SipResponse sipResponse) {
			registerSuccess = false;
			registerLatch.countDown();
			if (sipResponse != null) {
				logger.debug("register fail -- {}",
						sipResponse.getReasonPhrase());
			}
		}

		@Override
		public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
			isIncomingRing = true;

			activeCallReq = sipRequest;
			dialog = ua.getDialogManager().getDialog(provResponse);

			listener.onIncomingCall(phone);
		}

		@Override
		public void remoteHangup(SipRequest sipRequest) {
			activeCallReq = null;
			System.out.println("remote hangup");
			listener.onRemoteHangup(phone);
		}

		@Override
		public void ringing(SipResponse sipResponse) {
			if (ringLatch != null) {
				System.out.println("is ringing");
				ringLatch.countDown();
			}
		}

		@Override
		public void calleePickup(SipResponse sipResponse) {
			if (ringLatch != null) {
				// may have skipped ringing
				System.out.println("skipped ring, is picked up");
				ringLatch.countDown();
			} else {
				System.out.println("pick up");
			}
			listener.onPickup(phone);
		}

		@Override
		public void error(SipResponse sipResponse) {
			logger.error("error sip!");
			System.out.println("error sip -- " + sipResponse.getReasonPhrase());

			if (ringLatch != null) {
				ringLatch.countDown();
			}

			listener.onError(phone, sipResponse);
			activeCallReq = null;
		}
	}

	@Synchronized
	public void close() {
		this.ua.close();
	}
}

enum State {

}