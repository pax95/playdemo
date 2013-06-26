package dk.miw.playlists;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import dk.miw.playlists.model.Track;

public class PlaylistsFeederRoute extends RouteBuilder {
	private Map<String, Long> traceInfo = new HashMap<String, Long>();

	private Pubnub pubnub = new Pubnub("pub-c-f414dd3b-48ee-4c9a-b898-1f4c5f7fd46e",
			"sub-c-506c4834-d0ee-11e2-9456-02ee2ddab7fe");

	@Override
	public void configure() throws Exception {
		new PubNubSubscriber().init();
		PropertiesComponent propertiesComponent = new PropertiesComponent();
		propertiesComponent.setLocation("classpath:playlists.properties");
		getContext().addComponent("properties", propertiesComponent);

		from("timer://pollingTimer?fixedRate=true&period=3000")
				.autoStartup(true)
				.split()
				.method(ChannelList.class)
				.parallelProcessing()
				.setHeader(Exchange.HTTP_METHOD, simple("GET"))
				.setHeader(Exchange.HTTP_URI,
						simple("http://www.dr.dk/playlister/feeds/nowNext/nowPrev.drxml?items=0&cid=${body}"))
				.to("http://dummy")
				.id("dummy")
				.filter()
				.simple("${body} != null")
				.unmarshal()
				.json(JsonLibrary.Gson)
				.filter(simple("${body[now][status]} == 'music'"))
				.convertBodyTo(Track.class)
				.setHeader("artist", simple("${body.artist}"))
				.setHeader("title", simple("${body.title}"))
				.idempotentConsumer(simple("${body.time}-${body.channel}"),
						MemoryIdempotentRepository.memoryIdempotentRepository(300)).to("direct:process");

		from("direct:process").enrich("direct:lastfmEnricher", new LastFmAggregationStrategy()).log("${body}")
				.marshal().json(JsonLibrary.Gson, Track.class).process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						traceInfo.put(exchange.getIn().getHeader("title", String.class),
								new Long(System.currentTimeMillis()));
						pubnub.publish("music", new JSONObject(exchange.getIn().getBody(String.class)), new Callback() {

							@Override
							public void successCallback(String channel, Object message) {
								super.successCallback(channel, message);
							}

							@Override
							public void errorCallback(String channel, Object message) {
								System.err.println("failed " + message);
								super.errorCallback(channel, message);
							}
						});

					}
				});

		// enrich flight with weather information
		from("direct:lastfmEnricher")
				.onException(Exception.class)
				.to("log:dk.miw.playlists?showAll=true&multiline=true")
				.handled(true)
				.end()
				.removeHeader(Exchange.HTTP_URI)
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						String artist = exchange.getIn().getHeader("artist", String.class);
						if (artist != null) {
							exchange.getIn().setHeader("artist", URLEncoder.encode(artist, "UTF-8"));
						}
						String title = exchange.getIn().getHeader("title", String.class);
						if (title != null) {
							exchange.getIn().setHeader("title", URLEncoder.encode(title, "UTF-8"));
						}
					}
				})
				.setProperty(Exchange.CHARSET_NAME, constant("UTF-8"))
				.setHeader(
						Exchange.HTTP_QUERY,
						simple("method=track.getInfo&api_key={{lfm-api-key}}&artist=${header.artist}&track=${header.title}"))
				.setBody().simple("1").to("http://ws.audioscrobbler.com/2.0/?throwExceptionOnFailure=false");
	}

	private class PubNubSubscriber {
		public void init() throws PubnubException {
			String[] channels = new String[] { "music" };

			PlaylistsFeederRoute.this.pubnub.subscribe(channels, new Callback() {
				@Override
				public void successCallback(String channel, Object message) {
					super.successCallback(channel, message);
					String title;
					try {
						title = ((JSONObject) message).getString("title");
						Long enqueueTime = traceInfo.get(title);
						System.out.println("Pubnub message turnaround time "
								+ (System.currentTimeMillis() - enqueueTime.longValue()) + " ms.");
					} catch (JSONException e) {
					}
				}
			});
		}
	}

}
