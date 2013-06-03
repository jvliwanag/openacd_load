package com.ezuce.oacdlt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.transport.SipResponse;

public class CallerManager {
	private static Logger LOG = OACommon.LOG;

	private List<Phone> phones;

	private int idleMinMs, idleMaxMs;
	private int callMinMs, callMaxMs;

	private List<String> lines;

	private ScheduledExecutorService exec;
	private Random rand;

	private PhoneListener listener;

	private Map<Phone, Future<?>> hangupFutures;

	public CallerManager(int from, int to, String password, int idleMinMs,
			int idleMaxMs, int callMinMs, int callMaxMs, List<String> lines,
			PhoneFactory factory) {
		this.idleMinMs = idleMinMs;
		this.idleMaxMs = idleMaxMs;
		this.callMinMs = callMinMs;
		this.callMaxMs = callMaxMs;
		this.lines = lines;

		this.listener = new CallerListener();

		initPhones(from, to, password, factory);

		this.rand = new Random();
		this.hangupFutures = new HashMap<Phone, Future<?>>();
		// just a guess...
		this.exec = Executors.newScheduledThreadPool((to - from) / 2);
	}

	public void register() {
		// Register all phones first...
		for (Phone p : phones) {
			p.register();
		}
	}

	public void startCalls() {
		// Then begin calling
		for (Phone p : phones) {
			startRandomCall(p);
		}
	}

	public List<Phone> getPhones() {
		return phones;
	}

	// internal

	private void initPhones(int from, int to, String password,
			PhoneFactory factory) {
		phones = new ArrayList<>(to - from + 1);
		for (int i = from; i <= to; i++) {
			String username = Integer.toString(i);

			Phone p = factory.createPhone(username, password, listener);
			phones.add(p);
		}
	}

	private void scheduleRandomCall(final Phone p) {
		int delay = randomBetween(idleMinMs, idleMaxMs);
		log(p, "starting random call in %dms", delay);
		exec.schedule(new Runnable() {
			@Override
			public void run() {
				startRandomCall(p);
			}
		}, delay, TimeUnit.MILLISECONDS);
	};

	private void startRandomCall(final Phone p) {
		int ndx = rand.nextInt(lines.size());
		String line = lines.get(ndx);
		log(p, "Calling %s", line);
		try {
			p.dial(line);
			scheduleHangUp(p);
		} catch (Exception ex) {
			log(p, "dial failed, resetting phone");
			p.reset();
			log(p, "phone reset");
			scheduleRandomCall(p);
		}
	}

	private void scheduleHangUp(final Phone p) {
		int delay = randomBetween(callMinMs, callMaxMs);
		log(p, "hanging up in %dms", delay);
		Future<?> hangupFuture = exec.schedule(new Runnable() {
			@Override
			public void run() {
				log(p, "Hanging up");
				p.hangUp();
				scheduleRandomCall(p);
			}
		}, delay, TimeUnit.MILLISECONDS);

		hangupFutures.put(p, hangupFuture);
	}

	private int randomBetween(int min, int max) {
		return rand.nextInt(max - min + 1) + min;
	}

	private void log(Phone p, String fmt, Object... args) {
		String user = p.getUser();
		String msg = String.format(fmt, args);
		
		String l = "[c " + user + "] - " + msg; 
		
		LOG.info(l);
		System.out.println(l);
	}

	class CallerListener implements PhoneListener {

		@Override
		public void onIncomingCall(Phone phone) {
			log(phone, "incoming call");
		}

		@Override
		public void onRemoteHangup(Phone phone) {
			log(phone, "was hangup");
			Future<?> f = hangupFutures.remove(phone);
			if (f != null) {
				f.cancel(false);
			}
			scheduleRandomCall(phone);
		}

		@Override
		public void onPickup(Phone phone) {
			log(phone, "was picked up");
		}

		@Override
		public void onError(Phone phone, SipResponse sipResponse) {
			log(phone, "ring failed");
			Future<?> hangupFuture = hangupFutures.remove(phone);
			if (hangupFuture != null) {
				log(phone, "cancelling hangup");
				hangupFuture.cancel(false);
			}

			scheduleRandomCall(phone);
		}

	}
}
