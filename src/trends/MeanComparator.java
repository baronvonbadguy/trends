package trends;

import java.util.Comparator;

public class MeanComparator implements Comparator<Graph> {
	  
	  public int compare(Graph g1, Graph g2) {
	    return g2.getMean().compareTo(g1.getMean());
	  }
	  
	}
