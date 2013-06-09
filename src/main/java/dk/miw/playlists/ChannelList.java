package dk.miw.playlists;

import java.util.Arrays;
import java.util.List;

public class ChannelList {

	public static List<String> getChannels() {
		String[] c = { "P3", "AR4", "P8J", "DRM", "P6B", "P7M", "RAM", "P5D", "KH4" };
		return Arrays.asList(c);
	}
}
