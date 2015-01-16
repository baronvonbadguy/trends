package trends;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import trends.Trends;

public class NetRequestThread extends Thread {

	PApplet p;
	private boolean running; // Is the thread running? Yes or no?
	private int wait; // How many milliseconds should we wait in between
						// executions?
	private String response;
	private List<Parameters> paramQueue = new ArrayList<Parameters>();
	public Parameters params = new Parameters();
	public boolean scraping = false;
	private int batchSize;

	// Constructor, create the thread
	// It is not running by default
	NetRequestThread(PApplet p, int w) {
		this.p = p;
		wait = w;
		running = false;
	}

	// Overriding "start()"
	public void start() {
		// Set running equal to true
		running = true;
		// Print messages
		System.out.println("Starting thread (will execute every " + wait
				+ " milliseconds.)");
		// Do whatever start does in Thread, don't forget this!
		super.start();
	}

	// We must implement run, this gets triggered by start()
	public void run() {
		while (running) {
			if (paramQueue.size() > 0)
				batchSize = paramQueue.size();
			while (paramQueue.size() > 0) {
				scraping = true;
				Parameters param = paramQueue.get(0);
				if (param != null) {
					PApplet.println("views = " + param.getTerm());
					((Trends) p).getWikiStats(param.getTerm());
					paramQueue.remove(0);
				}
			}
			batchSize = 0;
			scraping = false;
			// Ok, let's wait for however long we should wait
			try {
				sleep((long) (wait));
			} catch (Exception e) {
			}
		}
		System.out.println("thread is done!"); // The thread is done when we get
												// to the end of run()
	}

	// Our method that quits the thread
	public void quit() {
		System.out.println("Quitting.");
		running = false; // Setting running to false ends the loop in run()
		interrupt(); // in case the thread is waiting. . .
	}


	public String getResponse() {
		return response;
	}
	
	public void queueParams() {
		paramQueue.add(params);
		params = new Parameters();
	}

	public int[] getCurrentScrapingProgress() {
		return new int[] {(this.batchSize - this.paramQueue.size()), this.batchSize};
	}
	
	class Parameters {
		private Stat stat;
		private String term;

		Parameters(Stat stat) {
			this.stat = stat;
		}

		Parameters() {
		}

		public Stat getStat() {
			return stat;
		}

		public String getTerm() {
			return term;
		}

		public void setStat(Stat stat) {
			this.stat = stat;
		}

		public void setTerm(String term) {
			this.term = term;
		}

	}

}
