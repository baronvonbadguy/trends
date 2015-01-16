package trends;

import java.util.Comparator;



public class ViewsComparator implements Comparator<Stat> {
  
  public int compare(Stat s1, Stat s2) {
    return s2.getViews().compareTo(s1.getViews());
  }
  
}