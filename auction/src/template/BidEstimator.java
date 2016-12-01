package template;

import java.util.concurrent.ThreadLocalRandom;

public class BidEstimator {
	
	private int auctionNumber = 0;

	// Agent's parameters
	private A mBestPlan = null;
	
	// Parameters intermedia
	// Using to comptute new cost and new plan in askPrice() method
	private A mNewPlan = null;
	
	public BidEstimator(A bestPlan, A newPlan) {
		this.mBestPlan = bestPlan;
		this.mNewPlan = newPlan;
	}
	
	/**
	 * Get the bid
	 * 
	 * @return the bid
	 */
	public long getBid() {
		if(this.auctionNumber == 0) {
			long lowBound = this.getLowBound();
			return lowBound;
		} else {
			long lowBound = this.getLowBound();
			return lowBound;
		}
	}
	
	/**
	 * Get the lower bound for the bid if we want to make money
	 * difference between the bestPDP and the NewPDP with the new task
	 * 
	 * @return the lower bound for the bid
	 */
	private long getLowBound() {
		if(this.mBestPlan == null)
			return (long) this.mNewPlan.cost();
		return (long) (this.mNewPlan.cost() - this.mBestPlan.cost());
	}
	
	public void auctionWin() {
		auctionNumber++;
	}
	
	public void auctionLose() {
		auctionNumber++;
	}
	
}
