package trends;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import processing.core.PApplet;

public class Graph {
	PApplet p; // parent PApplet
	List<Stat> statsList = new ArrayList<Stat>();
	Stat activeStat;
	private Integer size, heightMax, padding, viewsMax, viewsMin, fillColor, mean, colorArrayIndex;
	public boolean renderDates, renderGraph = true;
	private String searchTerm, wikiTerm;
	private ArrayList<int[]> colorArray = new ArrayList<int[]>();

	Graph(PApplet p, int heightMax, List<Stat> statsList, String searchTerm, String wikiTerm) {
		this.p = p;
		this.heightMax = heightMax;
		this.statsList = statsList;
		this.viewsMax = calcViewsMax()[0];
		this.viewsMin = calcViewsMax()[1];
		this.searchTerm = searchTerm;
		this.wikiTerm = wikiTerm;
		this.padding = 180;
		this.colorArrayIndex = ((Trends)p).currentColorScheme;
		initColorArray();
	}

	private void initColorArray() {
		colorArray.add(new int[] { 0xffffffd9, 0xffedf8b1, 0xffc7e9b4, 0xff7fcdbb, 0xff41b6c4, 0xff1d91c0, 0xff225ea8, 0xff253494, 0xff081d58 });
		colorArray.add(new int[] { 0xffffffcc, 0xffffeda0, 0xfffed976, 0xfffeb24c, 0xfffd8d3c, 0xfffc4e2a, 0xffe31a1c, 0xffbd0026, 0xff800026 });
		colorArray.add(new int[] { 0xfff7fcfd, 0xffe0ecf4, 0xffbfd3e6, 0xff9ebcda, 0xff8c96c6, 0xff8c6bb1, 0xff88419d, 0xff810f7c, 0xff4d004b });
		colorArray.add(new int[] { 0xfff7fcf0, 0xffe0f3db, 0xffccebc5, 0xffa8ddb5, 0xff7bccc4, 0xff4eb3d3, 0xff2b8cbe, 0xff0868ac, 0xff084081 });
		colorArray.add(new int[] { 0xfffff7ec, 0xfffee8c8, 0xfffdd49e, 0xfffdbb84, 0xfffc8d59, 0xffef6548, 0xffd7301f, 0xffb30000, 0xff7f0000 });
		colorArray.add(new int[] { 0xfffff7fb, 0xffece7f2, 0xffd0d1e6, 0xffa6bddb, 0xff74a9cf, 0xff3690c0, 0xff0570b0, 0xff045a8d, 0xff023858 });
		colorArray.add(new int[] { 0xfffff7f3, 0xfffde0dd, 0xfffcc5c0, 0xfffa9fb5, 0xfff768a1, 0xffdd3497, 0xffae017e, 0xff7a0177, 0xff49006a });
		colorArray.add(new int[] { 0xfff7f4f9, 0xffe7e1ef, 0xffd4b9da, 0xffc994c7, 0xffdf65b0, 0xffe7298a, 0xffce1256, 0xff980043, 0xff67001f });
		colorArray.add(new int[] { 0xffffffe5, 0xfffff7bc, 0xfffee391, 0xfffec44f, 0xfffe9929, 0xffec7014, 0xffcc4c02, 0xff993404, 0xff662506 });
	}
	
	private List<Double> getAllViews() {
		ArrayList<Double> viewsList = new ArrayList<Double>();
		for (Stat s : statsList) {
			viewsList.add((double)s.getViews());
		}
		return viewsList;
	}
	
	private Integer mean() {
		//adjusts the mean so that only values within ~2 standard deviations from
		//the unfiltered mean get entered into the list for the returned filtered mean
		
		double[] viewsList = new double[90];
		double sum = 0;
		//load all the (double)views into an array
		for (int i = 0; i < statsList.size(); i++) {
			viewsList[i] = (double)statsList.get(i).getViews();
		}
		//sum all the views and compute unfiltered mean
	    for (int i = 0; i < viewsList.length; i++) {
	        sum += viewsList[i];
	    }
	    double unfilteredMean = sum / viewsList.length;
	    //throw the unfiltered mean in with the views to find the standard deviation
		StandardDeviation sd = new StandardDeviation();
		Integer deviation = (int)sd.evaluate(viewsList, unfilteredMean);
		//reset sum for calculation of filtered mean
		sum = 0; int amountRemoved = 0;	
	    for (int i = 0; i < viewsList.length; i++) {
	    	if (Math.abs(viewsList[i] - unfilteredMean) < 10 * deviation){
	    		sum += viewsList[i];
	    	}
	    	else { amountRemoved++; PApplet.println(viewsList[i] + " not counted"); }
	    } 
	    PApplet.println("got mean: " + sum / (viewsList.length - amountRemoved));
	    return (int) sum / (viewsList.length - amountRemoved);
	}
	
	
	
	private int[] calcViewsMax() {

		List<Stat> sorted = new ArrayList<Stat>();

		for (Stat s : statsList) {
			sorted.add(s.copy());
		}

		Collections.sort(sorted, new ViewsComparator());
		int[] minmax = new int[2];
		minmax[0] = sorted.get(0).getViews();
		minmax[1] = sorted.get(sorted.size() - 1).getViews();
		return minmax;

	}
	
	public void render(int graphsIndex, int graphsHeightMax) {
		//-------------
		//----GRAPH----
		//-------------
		int x, y, w;
		float h;
		// bar width
		y = p.height - 50;
		w = (p.width - padding - 10) / statsList.size();
		
		int[] pallette = colorArray.get(colorArrayIndex);
		if (pallette.length - 1 == graphsIndex)
			fillColor = p.color(pallette[graphsIndex % (pallette.length - 1)], 75);
		else{
			fillColor = p.color(pallette[pallette.length - 1 - graphsIndex], 75);
		}

		for (int i = 0; i < statsList.size(); i++) {

			Stat s = statsList.get(i);
			x = padding + (i * w);
			if (i == 0)
				h = -PApplet.map(s.getViews(), 0, graphsHeightMax, 0, heightMax);
			else
			h = -PApplet.map(s.getViews(), 0, graphsHeightMax, 0, heightMax)
					+ PApplet.map(p.noise(i + p.frameCount * (float) .005), -1, 1,
							-10, 10);
			// checks to see if mouse is over bar
			p.noiseDetail(7, (float) 0.05);

			if ((p.mouseX < (x + w)) && (p.mouseX > x) 
			 && (p.mouseY > (y + h)) && (p.mouseY < y)) {

				// set stat to be active, changing color and adding the day views above the bar
				activeStat = s;
				// draws view count
				p.fill(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 60, 100);
				p.text(s.getViews(), x, y + h - 5);
				p.noStroke();
				// if the mouse is pressed while drawing this bar grab the news
				// articles for that day
				if (p.mousePressed) {
					p.fill(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 30, 100);
				}
				else {
					p.fill(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 15, 100);
				}
			}
			else {
				activeStat = null;
				if (renderDates)
					p.fill(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor), 100);
				else p.fill(fillColor);
			}

			//draws each graph bar with date
			p.rect(x, y, w, h, 3, 3, 0, 0);
			if (renderDates)
				p.fill(180);
				p.textFont(((Trends)p).dialog);
				p.text(statsList.get(i).getDate(), x, y + 10, w, 50);
		}
		//------------------
		//------LABEL-------
		//------------------
		int labelPosY = p.height - 10,
			labelWidth = 105 / ((Trends)p).graphs.size(),
			labelPosX =  10 + (graphsIndex * labelWidth),
			labelHeight = - (int) PApplet.map(statsList.get(0).getViews(), 0, ((Trends)p).graphs.get(0).statsList.get(0).getViews(), 0, heightMax) - (p.height - 10 - y);
		if (renderDates)
			p.fill(p.color(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 30, 100));
		else p.fill(p.color(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 30, 75));
		//draws the label
		p.rect(labelPosX, labelPosY, labelWidth * (((Trends)p).graphs.size() - graphsIndex), labelHeight, 10, 0, 0, 10);
		//draws the perspective element connecting the label and the graph
		p.beginShape(PApplet.POLYGON);
		
			if (renderDates)
				p.fill(p.color(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 28, 100));
			else p.fill(p.color(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor) + 28, 75));
			
			p.vertex(114, labelPosY + labelHeight);
			p.vertex(114, labelPosY);
			
			if (renderDates)
				p.fill(p.color(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor), 100));
			else p.fill(p.color(p.hue(fillColor), p.saturation(fillColor), p.brightness(fillColor), 75));
			
			p.vertex(padding, p.height - 50);
			p.vertex(padding, (p.height - 50) - (int) PApplet.map(statsList.get(0).getViews(), 0, graphsHeightMax, 0, heightMax));
			
		p.endShape();
		
		p.fill(p.color(0xFFF1F1F2));
		p.textFont(((Trends)p).onramp);
		//p.textSize(16);
		String wikiSpace = wikiTerm.replace("_", " ");
		/*
		for (int i = 0; i < wikiTerm.length(); i++) {
			p.text(wikiSpace.substring(i,  i + 1), labelPosX + (labelWidth / 2) - 4, labelPosY + (labelHeight / 2) - ((wikiTerm.length() * 20) / 2) + (20 * (i + 1)));
		}*/
		//draws the label title
		p.pushMatrix();
		p.translate(labelPosX + (labelWidth / 2) - 4, labelPosY + (labelHeight / 2));
		p.rotate(PApplet.radians(90));
		p.textAlign(PApplet.CENTER);
		p.text(wikiSpace, 0, 0);
		//p.text(wikiSpace, labelPosX + (labelWidth / 2) - 4, labelPosY + (labelHeight / 2) - ((wikiTerm.length() * 20) / 2));
		p.popMatrix();
	}
		
		
		// get methods
		List<Stat> getStats() {
			return statsList;
		}
		
		public String getSearchTerm() {
			return this.searchTerm;
		}
		
		public String setWikiTerm() {
			return this.wikiTerm;
		}
		
		public Integer getMean() {
			return this.mean;
		}
		
		public Integer getViewsMax() { 
			return this.viewsMax;
		}
		
		// set methods

		public void setStats(List<Stat> statsList) {
			this.statsList = statsList;
		}

		public void setFillColor(int fillColor) {
			this.fillColor = fillColor;
		}

		public void setRenderDates(boolean renderDates) {
			this.renderDates = renderDates;
		}

		public void setSearchTerm(String searchTerm) {
			this.searchTerm = searchTerm;
		}
		
		public void setWikiTerm(String wikiTerm) {
			this.wikiTerm = wikiTerm;
		}
		
		public void calcMean() { 
			this.mean = mean();
		}
		
		public void setColorArrayIndex(Integer colorArrayIndex){
			this.colorArrayIndex = colorArrayIndex;
		}
		

}
