package trends;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stat {

	private String date, term;
	private Integer views;

	Stat(String date, Integer views, String term) {
		this.date = formatDateRemoveDash(date);
		this.views = views;
		this.term = term;
	}

	Stat copy() {
		Stat s = new Stat(date, views, term);
		return s;
	}

	public String getDate() {
		return this.date;
	}

	public String getTerm() {
		return this.term;
	}

	public Integer getViews() {
		return this.views;
	}

	private String formatDateRemoveDash(String _date) {
		Pattern p = Pattern.compile("^(\\d{2})(\\d+)(-)(\\d+)(-)(\\d+)");
		Matcher m = p.matcher(_date);
		StringBuffer result = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(result, m.group(2) + m.group(4) + m.group(6));
		}
		m.appendTail(result);
		return result.toString();
	}

	public String returnDateSearchFormat() {
		Pattern p = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})");
		Matcher m = p.matcher(date);
		StringBuffer result = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(result, m.group(2) + ("/") + m.group(3)
					+ ("/20") + m.group(1));
		}
		m.appendTail(result);
		return result.toString();
	}

}
