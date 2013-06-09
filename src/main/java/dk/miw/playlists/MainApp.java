package dk.miw.playlists;

import org.apache.camel.main.Main;

public class MainApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.enableHangupSupport();
		main.addRouteBuilder(new PlaylistsFeederRoute());
		main.run();
	}

}
