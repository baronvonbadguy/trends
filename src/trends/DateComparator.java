package trends;

import java.util.Comparator;

public class DateComparator implements Comparator<Stat> {
	  
	  public int compare(Stat s1, Stat s2) {
	    Integer i1, i2;
	    i1 = Integer.parseInt(s1.getDate());
	    i2 = Integer.parseInt(s2.getDate());
	    return i1.compareTo(i2);
	  }
	  
	}
