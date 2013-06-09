package dk.miw.playlists.model;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Track {
	private String artist;
	private String title;
	private String time;
	private String channel;
	private String album;
	private String albumImageUrl;
	private Long trackNo;
	private Date since;
	public void setTrackNo(Long trackNo) {
		this.trackNo = trackNo;
	}
	public Long getTrackNo() {
		return trackNo;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channnel) {
		this.channel = channnel;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	@SuppressWarnings("unchecked")
	public void setPayload(Map<String, Object> map) {
		Map<String, Object> now = (Map<String, Object>) map.get("now");
		this.title = (String) now.get("track_title");
		this.time = (String) now.get("start_time");
		this.artist = (String) now.get("display_artist");
		Map<String, Object> info = (Map<String, Object>) map.get("info");
		this.channel = (String) info.get("channel");
		this.album = "";
		this.albumImageUrl = "";
	}
	
	
	
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getAlbumImageUrl() {
		return albumImageUrl;
	}
	public void setAlbumImageUrl(String albumImageUrl) {
		this.albumImageUrl = albumImageUrl;
	}
	@Override
	public String toString() {
		return "Track [artist=" + artist + ", title=" + title + ", time="
				+ time + ", channel=" + channel + ", album=" + album
				+ ", albumImageUrl=" + albumImageUrl + ", trackNo=" + trackNo
				+ ", since=" + since + "]";
	}
	public void setSince(Date since) {
		this.since = since;
		
	}
	
	public Date getSince() {
		return this.since;
	}
}
