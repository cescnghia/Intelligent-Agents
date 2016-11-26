package template;

import java.util.concurrent.ThreadLocalRandom;

public class BidEstimator {

	// Agent's parameters
	private PickupDeliveryProblem mBestPDP;
	private double mBestCost = Double.MAX_VALUE;
	private A mBestPlan = null;
	
	// Parameters intermedia
	// Using to comptute new cost and new plan in askPrice() method
	private PickupDeliveryProblem mNewPDP;
	private double mNewCost = Double.MAX_VALUE;
	private A mNewPlan = null;
	
	public BidEstimator(PickupDeliveryProblem bestPDP, PickupDeliveryProblem newPDP) {
		this.mBestPDP = bestPDP.clone();
		this.mNewPDP = newPDP.clone();
	}
	
	/**
	 * Get the bid
	 * 
	 * @return the bid
	 */
	public long getBid() {
		long lowBound = this.getLowBound();
		long v = ThreadLocalRandom.current().nextLong(100);
		return lowBound + v;
	}
	
	/**
	 * Get the lower bound for the bid if we want to make money
	 * difference between the bestPDP and the NewPDP with the new task
	 * 
	 * @return the lower bound for the bid
	 */
	private long getLowBound() {
		return (long) (this.mNewPDP.getCost() - this.mBestPDP.getCost());
	}
	
}
