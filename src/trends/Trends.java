package trends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import org.apache.http.client.HttpClient;
import org.apache.http.params.*;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

import java.awt.event.KeyEvent; 

import http.requests.*;
import controlP5.*;
import processing.core.*;
import processing.data.JSONObject;

public class Trends extends PApplet {
	// TO DO: create full to do

	// globals
	ControlP5 cp5;
	Textfield searchField;
	PFont onramp, ostritch48, ostritch60, dialog;
	RadioButton daysButton = null;
	ControllerGroup graphsButtonGroup;
	JSONObject news, trends;
	List<Graph> graphs = new ArrayList<Graph>();
	NetRequestThread netThread = new NetRequestThread(this, 300);
	boolean calcViewsMax = false;
	Integer viewsMax, currentColorScheme = 0;
	final int DARKGREY = 0xFF58585B, OFFWHITE = 0xFFF1F1F2, LIGHTGREY = 0xFF939597;
	
	public static void main(String args[]) {
		// PApplet.main(new String[] { "--present", "trends.Trends" });
		PApplet.main(new String[] { "trends.Trends" });
	}
	//setup function called once on the initialization of the app
	public void setup() {
		//set size and renderer of processing instance
		size(1280, 720, P3D);
		smooth(5);
		//set the color mode to be used in further calls to methods concerned with color values
		colorMode(HSB, 360, 100, 100, 100);
		//loads fonts
		onramp = loadFont("ONRAMP-20.vlw");
		ostritch48 = loadFont("OstrichSansRounded-Medium-48.vlw");
		ostritch60 = loadFont("OstrichSansRounded-Medium-60.vlw");
		dialog = loadFont("Dialog.plain-9.vlw");
		//initializes GUI elements
		cp5 = new ControlP5(this);
		initCP5();
		//start the network queue thread
		netThread.start();
	}
	//main draw loop, called every FPS
	public void draw() {
		background(color(OFFWHITE));
		//if the network thread is scraping stats, display the loading circle
		if (netThread.scraping){
			fill(color(hue(LIGHTGREY), saturation(LIGHTGREY), brightness(LIGHTGREY), 0));
			stroke(color(OFFWHITE));
			strokeWeight(2);
			ellipse(200, 35, 40, 40);
			
			strokeWeight(3);
			stroke(color(hue(OFFWHITE) + 20, saturation(OFFWHITE) + 20, brightness(OFFWHITE) - 20));
			arc(200, 35, 40, 40, -HALF_PI , map(netThread.getCurrentScrapingProgress()[0], 0, netThread.getCurrentScrapingProgress()[1], -HALF_PI, TWO_PI - HALF_PI));
			
			stroke(color(OFFWHITE));
			strokeWeight(5);
			arc(200, 35, 40, 40, 0 + map((frameCount % 60), 0, 60, 0, TWO_PI), QUARTER_PI + map((frameCount % 60), 0, 60, 0, TWO_PI));
			
			noStroke();
			
			cp5.getController("title").setVisible(false);
			cp5.getController("loading").setVisible(true);
		}
		else {
			cp5.getController("title").setVisible(true);
			cp5.getController("loading").setVisible(false);
		}
		//clears the searchfield's default text when clicked
		if (searchField.isMousePressed())
			if (searchField.getText().equals("enter, search, terms, like, this (then hit enter)"))
				searchField.setText("");
		
		if (graphs.size() > 0) {
			pushMatrix();
			//if needed, find the single greatest view count for all graphs
			//used to resize all the graphs when a new graph is added
			if (calcViewsMax == true) { 
				this.viewsMax = 0; 
				for (Graph g : graphs) { 
					if (g.getViewsMax() > this.viewsMax) this.viewsMax = g.getViewsMax();
				} 
				println("views max: " + this.viewsMax);
				calcViewsMax = false; 
			}
			//graph drawing loop
			for (int i = 0; i < graphs.size(); i++) {
				Graph g = graphs.get(i);
				if (g.renderGraph){
					g.render(i, this.viewsMax);
				}
			}
			popMatrix();
		}
		
		// draw gui elements
		pushMatrix();
		noStroke();
		fill(200, 90);
		rect(0, 0, width, height / 10);
		popMatrix();
		cp5.draw();

	}

	
	public void initCP5() {
		cp5.addTextlabel("title", "wikitrends  : ", 12, 14).setFont(ostritch60).setVisible(true);
		cp5.addTextlabel("loading", "Loading... ", 12, 14).setFont(ostritch60).setVisible(false);
		
		searchField = cp5.addTextfield("searchField")
							.setPosition(250, 7)
								.setText("enter, search, terms, like, this (then hit enter)");

		searchField.getCaptionLabel()
						.setText("");
		
		searchField.setFont(ostritch48)
						.setHeight(57)
							.setWidth(885)
								.setColorActive(color(LIGHTGREY))
									.setColorBackground(color(OFFWHITE))
										.setColorValueLabel(color(LIGHTGREY));
		
		searchField.setAutoClear(false).keepFocus(true);

		cp5.addButton("searchButton")
				.setPosition(1145, 7)
					.setWidth(125)
						.setHeight(57)
							.setColorActive(color(LIGHTGREY))
								.setColorValueLabel(color(OFFWHITE))
									.setColorBackground(color(DARKGREY));
		
		cp5.getController("searchButton")
			.getCaptionLabel()
				.setFont(ostritch48)
					.setText("search")
						.alignX(CENTER);
	}

	public void searchButton() {
		//graphs.clear();
		boolean getTerm = true;
		//splits up all the terms in the searchfield by ','
		List<String> terms = Arrays.asList(searchField.getText().toString()
				.split("\\s*,\\s*"));
		//queues a job to the network thread to pull info on each search term
		for (String term : terms) {
			//checks to see if we have any graphs, and if any of the terms
			//match previously drawn graphs so we don't have to pull redundant data
			outerLoop:
			if (graphs.size() != 0) {
				for (Graph g : graphs) {
					if (g.getSearchTerm().equals(term)) {
						println(term + " is already displayed");
						getTerm = false;
						break outerLoop;
					}
					else { getTerm = true; }
				}
			}
			//checks if we do indeed need to fetch data for a new term
			if (getTerm == true){
				println("creating " + getTerm + " WIKIVIEWS thread");
				netThread.params.setTerm(term);
				netThread.queueParams();
			}
		}
	}

	public void keyPressed() {
			if (keyCode == ENTER)
					searchButton();
			else if (keyCode == KeyEvent.VK_F1){
				currentColorScheme = currentColorScheme == 8 ? 0 : currentColorScheme + 1;
				println("color changed to: " + currentColorScheme);
				for (Graph g : graphs) {		
					g.setColorArrayIndex(currentColorScheme);
				}	
			}
		
	}
	
	JSONResultsWiki getRequestWikiAPI(ArrayList<String> parameters) {
		String url = "http://en.wikipedia.org/w/api.php?action=query&format=json&indexpageids&prop=info";
		System.out.println("starting wiki API URI build");
		StringBuilder uriBuilder = new StringBuilder(url);
		
		for(String s : parameters) {
			uriBuilder.append(s);
			System.out.println(s);
		}
		GetRequest get;

		get = new GetRequest(uriBuilder.toString());
		println(uriBuilder.toString());
		println("waiting for wiki API connection");
		get.send();
		println("data returned");	
		
		return new Gson().fromJson(get.getContent(), JSONResultsWiki.class);

		
	}
	
	String getProperWikiTitle(String term) {
		String title = "";
		try {

			// instantiates the default http client from apache as well as the
			// client parameters
			HttpClient httpClient = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			// keeps the client from following the I'm feeling lucky redirect
			params.setParameter("http.protocol.handle-redirects", false);

			// instantiates the GET request, applies the redirect parameter to
			// it, then executes it
			HttpGet httpGet = new HttpGet(
					"https://www.google.com/search?sourceid=navclient&btnI=I&q=wikipedia+"
							+ urlEncode(term));
			httpGet.setParams(params);
			HttpResponse response = httpClient.execute(httpGet);

			// grabs the redirect location of the page and returns just the page
			// title from the URL
			title = response.getFirstHeader("location").getValue();
			println("returned title is " + title);

			if (title.startsWith("http://en.wikipedia")) {
				println("url matches criteria");
				title = Arrays.asList(
						title.split("http://en.wikipedia.org/wiki/")).get(1);
			} else {
			}// title = null; }

			println("altered title: " + title);
			// shutsdown the http client so we dont have memory leaks
			httpClient.getConnectionManager().shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return title;
		}
	}
	
	
	void getWikiStats(String searchTerm) {
		// forms and fires GET request
		println("about to get proper title");
		String wikiTerm = getProperWikiTitle(searchTerm);
		if (wikiTerm == null) {
			println(searchTerm + " not found");
			return;
		} else {
			GetRequest get = new GetRequest("http://stats.grok.se/json/en/latest" + "90" + "/" + wikiTerm);
			println("waiting for Stats connection");
			get.send();
			println("data returned");
			// parses the GET request data string into a JSON object
			if (get.getContent() != null){
				JSONObject dv = JSONObject.parse(get.getContent()).getJSONObject("daily_views");
				List<Stat> l = new ArrayList<Stat>();
				Iterator iter = dv.keys().iterator();
	
				// the dates in the json object are stored as keys and the values are the
				// corresponding daily views. this block is responsible for populating
				// the list l with Stat objects created with the StringDate key and
				// the int daily views value
	
				while (iter.hasNext()) {
					String key = (String) iter.next();
					int views = dv.getInt(key);
					Stat s = new Stat(key, views, searchTerm);
					l.add(s);
				}
				println("got " + l.size() + " entries");
				Graph g = new Graph(this, 550, l, searchTerm, wikiTerm);
				g.calcMean();
				Collections.sort(g.statsList,
						new DateComparator());
				graphs.add(g);
				Collections.sort(graphs, new MeanComparator());
				graphs.get(0).setRenderDates(true);
				calcViewsMax = true;
			}
			
		}
	}
	
}