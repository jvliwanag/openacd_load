package com.ezuce.oacdlt;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;

public class PhoneFactory {
	private Config baseConfig;
	private Logger logger;

	public PhoneFactory(Config baseConfig, Logger logger) {
		this.baseConfig = baseConfig;
		this.logger = logger;
	}

	public Phone createPhone(String username, String password,
			PhoneListener listener) {
		Config copy = fromBaseConfig(baseConfig, username, password);

		return new Phone(copy, logger, listener);
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

		// specific to user
		config.setUserPart(username);
		config.setPassword(password);

		return config;
	}
}
