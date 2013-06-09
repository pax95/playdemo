package dk.miw.playlists;

import java.util.Map;

import org.apache.camel.Converter;

import dk.miw.playlists.model.Track;

@Converter
public class TrackConverter {
	
	@Converter
	public Track toTrack(Map<String, Object> map){
		Track track = new Track();
        track.setPayload(map);
        return track;
	}
}
