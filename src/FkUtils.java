import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.media.MediaMode;

public class FkUtils {

	// normally wouldn't be static if normal app, butt... just a dirty program
	// anyway
	public static Config fromBaseConfig(Config baseConfig, String username,
			String password) {
		Config config = new JavaConfig();

		// copy off baseConfig
		config.setDomain(baseConfig.getDomain());
		config.setLocalInetAddress(baseConfig.getLocalInetAddress());
		config.setPublicInetAddress(baseConfig.getPublicInetAddress());

		// default to none
		config.setMediaMode(MediaMode.none);

		// specific to user
		config.setUserPart(username);
		config.setPassword(password);

		return config;
	}
//
//	public static scheduleBetween(int minMs, int maxMs, Runnable r,
//			ScheduledExecutorService exec) {
//		
//	}
}
