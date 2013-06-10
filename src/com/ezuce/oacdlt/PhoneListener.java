package com.ezuce.oacdlt;

import net.sourceforge.peers.sip.transport.SipResponse;

public interface PhoneListener {
	public void onIncomingCall(Phone phone);

	public void onRemoteHangup(Phone phone);

	public void onPickup(Phone phone);

	public void onError(Phone phone, SipResponse sipResponse);
}
