package com.ezuce.oacdlt;

import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class Phone {
	private static Logger LOG = OACommon.LOG;

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
	
	private Logger logger;

	public Phone(Config config, Logger logger, PhoneListener listener) {
		this.config = config;
		this.listener = listener;
		this.phone = this;
		this.logger = logger;

		try {
			ua = new UserAgent(new PhoneSipListener(), config, logger);
		} catch (SocketException e) {
			rethrow("failed to create phone", e);
		}
	}

	public void register() {
		registerSuccess = false;
		registerLatch = new CountDownLatch(1);

		boolean completed = false;

		try {
			ua.getUac().register();
			completed = registerLatch.await(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			rethrow("register fail");
		}

		if (completed) {
			if (!registerSuccess) {
				rethrow("register fail");
			}
		} else {
			rethrow("register timeout");
		}
	}

	public void unregister() {
		try {
			ua.getUac().unregister();
		} catch (SipUriSyntaxException e) {
			rethrow("unregister fail", e);
		}
	}

	public void reset() {
		try {
//			unregister();
			ua.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		registerLatch = null;
		ringLatch = null;
		dialog = null;
		activeCallReq = null;
		
		try {
			ua = new UserAgent(new PhoneSipListener(), config, logger);
			register();
		} catch (Exception e) {
			throw new PhoneException("reset fail", e);
		}
	}
	
	public void answer() {
		if (dialog == null || activeCallReq == null)
			throw new PhoneException("no call to answer");

		ua.getUas().acceptCall(activeCallReq, dialog);
		isIncomingRing = false;
		dialog = null;
	}

	public void dial(String extension) {
		String sipAddr = "sip:" + extension + "@" + config.getDomain();
		dialSip(sipAddr);
	}

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

	public void hangUp() {
		if (activeCallReq != null) {
			if (isIncomingRing) {
				ua.getUas().rejectCall(activeCallReq);
			} else {
				ua.getUac().terminate(activeCallReq);
			}

			activeCallReq = null;
		} else {
			rethrow("hangup on no active call");
		}
	}

	private void rethrow(String msg) {
		LOG.error(msg);
		System.out.println("error -- " + msg);
		throw new PhoneException(msg);
	}

	private void rethrow(String msg, Exception ex) {
		LOG.error(msg, ex);
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
			LOG.debug("register ok");
		}

		@Override
		public void registerFailed(SipResponse sipResponse) {
			registerSuccess = false;
			registerLatch.countDown();
			LOG.debug("register fail");
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
			LOG.error("error sip!");
			System.out.println("error sip -- " + sipResponse.getReasonPhrase());
			
			if (ringLatch != null) {
				ringLatch.countDown();
			}
			
			listener.onError(phone, sipResponse);
			activeCallReq = null;
		}
	}
}

enum State {

}