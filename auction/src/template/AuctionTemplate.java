package template;

//the list of imports
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import template.Task_.Action;
import logist.LogistPlatform;
import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
    private long timeout_setup;
    private long timeout_plan;
	
	// Agent's parameters
	private PickupDeliveryProblem myBestPDP;
	private double myBestCost = Double.MAX_VALUE;
	private A myBestPlan = null;
	
	// Parameters intermedia
	// Using to comptute new cost and new plan in askPrice() method
	private PickupDeliveryProblem myNewPDP;
	private double myNewCost = Double.MAX_VALUE;
	private A myNewPlan = null;


	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
		
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
            System.out.println(exc.getMessage());
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
		
		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		
		ArrayList<Vehicle> myVehicles = new ArrayList<Vehicle>(agent.vehicles());
		
		this.myBestPDP = new PickupDeliveryProblem(myVehicles);
		
		
	}

	// There are only 2 agent (our agent and opponent agent)
	// Check status of opponent
	// Control the actually winner 
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		if (winner == agent.id()) { // We win this task
			System.out.println("[AuctionTemplate.auctionResult] we win the task: " + previous);
			// we win for the task
			// store the value of newCost and newPlan that we have computed in the method askPrice()
			this.myBestPDP = this.myNewPDP;
			this.myBestPlan = this.myNewPlan;
			this.myBestCost = this.myNewCost;
			// continue the auction
			
			//Code given by assistant
			currentCity = previous.deliveryCity;
			
		} else { // Do Something
			System.out.println("[AuctionTemplate.auctionResult] we lose the task: " + previous);
			// analyze the plan of opponent
			// do some strategy....
		}
	}
	
	
	@Override
	public Long askPrice(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;
		
		// try to add this new task to the old plan
		System.out.println("[AuctionTemplate.askPrice] create myNewPDP");
		try{
			this.myNewPDP = this.myBestPDP.clone().addNewTask(task);
			
			System.out.println("[AuctionTemplate.askPrice] task number of myNewPDP" + this.myNewPDP.getTasks().size());
			
			System.out.println("[AuctionTemplate.askPrice] calculate plan of myNewPDP");
			this.myNewPlan = this.myNewPDP.StochasticLocalSearch();
			
			// and compute new cost for this new plan
			System.out.println("[AuctionTemplate.askPrice] calculate cost of myNewPDP");
			this.myNewCost = myNewPlan.cost();
			
			System.out.println("[AuctionTemplate.askPrice] estimate the bid for myNewPDP");
			BidEstimator bidEstimator = new BidEstimator(this.myBestPDP, this.myNewPDP);
			
			return bidEstimator.getBid();
		} catch (Exception e) {
			System.out.println(e);
			return Long.MAX_VALUE;
		}
		
		
		/*TO DO*/
		// compute and return a bid in terms of new cost
		// if myNewCost is bigger than myBestCost => no need to take new task
		
		
		// Code given by ASSITANT
//		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
//		long distanceSum = distanceTask
//				+ currentCity.distanceUnitsTo(task.pickupCity);
//		double marginalCost = Measures.unitsToKM(distanceSum
//				* vehicle.costPerKm());
//
//		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
//		double bid = ratio * marginalCost;
//
//		return (long) Math.round(bid);
	}
	
	
	
    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();

       // Plan is already created by the askPrice()
       // Best plan is myBestPlan
       // Best cost is myBestCost
        
        System.out.println("The minimun cost is: "+myBestCost);
        
        List<Vehicle> myVehicles = myBestPlan.getVehicles();
        
        List<Plan> plans = new ArrayList<Plan>();

        for (Vehicle v : myVehicles){
        	LinkedList<Task_> tasks_ = myBestPlan.getTasksOfVehicle(v);
        	if (tasks_ != null) {
        		Plan plan = makePlan(v, tasks_);
       			plans.add(plan);
        	}
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
        return plans;
    }
    
    private Plan makePlan(Vehicle v, LinkedList<Task_> tasks) {
		City currentCity = v.homeCity();
		Plan plan = new Plan(currentCity);
		
		for(Task_ t : tasks){
			if (t.getAction() == Action.PICKUP){
				City nextCity = t.getTask().pickupCity;
				for (City city : currentCity.pathTo(nextCity))
					plan.appendMove(city);
				currentCity = nextCity;
				plan.appendPickup(t.getTask());
			} else {
				City nextCity = t.getTask().deliveryCity;
				for (City city : currentCity.pathTo(nextCity))
					plan.appendMove(city);
				currentCity = nextCity;
				plan.appendDelivery(t.getTask());
			}
		}
		return plan;
	}
}
