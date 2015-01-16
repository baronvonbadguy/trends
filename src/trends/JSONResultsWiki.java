package trends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JSONResultsWiki {

    private Query query;
    public Query getResponseQuery() { return this.query; }
    public String toString() { return "queryData[" + this.query + "]"; }
    
    static class Query {
    	 private ArrayList<String> pageids = new ArrayList<String>();
    	 private Map<String, Page> pages = new HashMap<String, Page>();
    	 public ArrayList<String> getPageIDList() { return this.pageids; }
    	 public Map<String, Page> getPages() { return this.pages; }
    }
    public static class Page {
    	 private String pageid;
    	 private String title;
    	 private Integer length;
    	 //private ArrayList<Link> links = new ArrayList<Link>();
    	 //private ArrayList<Link> backlinks = new ArrayList<Link>();
    	 //public ArrayList<Link> getLinkList() { return this.links; }
    	 //public ArrayList<Link> getBacklinkList() { return this.backlinks; }
    	 public String getTitle() { return this.title; } 
    	 public String getPageID() { return this.pageid; } 
    	 public Integer getLength() { return this.length; }
    } /*
    static class Link {
         private String title;
         private String pageid;
    	 public String getTitle() { return this.title; }
    	 public String getPageID() { return this.pageid; }
    }*/


}





