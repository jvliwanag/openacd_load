package com.ezuce.oacdlt;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;

public class PhoneFactory {
	private Config baseConfig;

	public PhoneFactory(Config baseConfig) {
		this.baseConfig = baseConfig;
	}

	public Phone createPhone(String username, String password,
			PhoneListener listener) {
		Config copy = fromBaseConfig(baseConfig, username, password);

		return new Phone(copy, listener);
	}

	public static Config fromBaseConfig(Config baseConfig, String username,
			String password) {
		Config config = new JavaConfig();

		// copy off baseConfig
		config.setDomain(baseConfig.getDomain());
		config.setLocalInetAddress(baseConfig.getLocalInetAddress());
		config.setPublicInetAddress(baseConfig.getPublicInetAddress());
		config.setOutboundProxy(baseConfig.getOutboundProxy());
		config.setMediaMode(baseConfig.getMediaMode());
		config.setMediaFile(baseConfig.getMediaFile());

		// specific to user
		config.setUserPart(username);
		config.setPassword(password);

		return config;
	}
}
