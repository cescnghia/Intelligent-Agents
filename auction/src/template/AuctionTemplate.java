package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private long timeout_setup;
    private long timeout_plan;
    
    private double myRatio = 0.85;

	private double maxRatio = 2;
	private double minRatio = 0.5;
    private int round = 0;
    
    private List<Long> myBids = new ArrayList<Long>();
    private List<Long> opponentBids = new ArrayList<Long>();
	
	// Agent's parameters
	private PickupDeliveryProblem myBestPDP;
	private double myBestCost = Double.MAX_VALUE;
	private A myBestPlan = null;
	
	private PickupDeliveryProblem opponentBestPDP;
	private double opponentBestCost = Double.MAX_VALUE;
	private A opponentBestPlan = null;
	
	// Parameters intermedia
	// Using to comptute new cost and new plan in askPrice() method
	private PickupDeliveryProblem myNewPDP;
	private double myNewCost = Double.MAX_VALUE;
	private A myNewPlan = null;
	
	private PickupDeliveryProblem opponentNewPDP;
	private double opponentNewCost = Double.MAX_VALUE;
	private A opponentNewPlan = null;
	
	private Map<Integer, Ennemy> mEnnemies = new HashMap<Integer, Ennemy>();


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
//		this.currentCity = vehicle.homeCity();

//		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
//		this.random = new Random(seed);
		
		ArrayList<Vehicle> myVehicles = new ArrayList<Vehicle>(agent.vehicles());
		
		System.out.println("[setup] myVehicles: " + myVehicles);
		
		this.myBestPDP = new PickupDeliveryProblem(myVehicles);
		this.opponentBestPDP = new PickupDeliveryProblem(myVehicles);
		
		
	}

	// There are only 2 agent (our agent and opponent agent)
	// Check status of opponent
	// Control the actually winner 
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		System.out.println("Number of agent is: "+bids.length);
		long myBid = bids[agent.id()];
		long opponentBid = bids[1-agent.id()];
	
		if (winner == agent.id()) { // We win this task
			System.out.println("[AuctionTemplate.auctionResult] we win the task: " + previous);
			// we win for the task
			// store the value of newCost and newPlan that we have computed in the method askPrice()
			this.myBestPDP = this.myNewPDP;
		    this.myBestPlan = this.myNewPlan;
		    this.myBestCost = this.myNewCost;
		    
		    // we win, we try to increase the ratio for having more profit in next auction
		    myRatio += (double)myBid / (double)opponentBid;

			
		} else { // Do Something
			System.out.println("[AuctionTemplate.auctionResult] we lose the task: " + previous);
			// analyze the plan of opponent
			// do some strategy....
			this.opponentBestPDP = this.opponentNewPDP;
		    this.opponentBestPlan = this.opponentNewPlan;
		    this.opponentBestCost = this.opponentNewCost;
		    
		    myRatio -= (double)myBid / (double)opponentBid;

		}	
		
		// minRatio <= ratio <= maxRatio
		myRatio = Math.min(myRatio,maxRatio);
		myRatio = Math.max(myRatio, minRatio);

	}
	
	
	@Override
	public Long askPrice(Task task) {

		round++ ;
		
		if (vehicle.capacity() < task.weight)
			return null;
		
		// try to add this new task to the old plan
		System.out.println("[AuctionTemplate.askPrice] create myNewPDP");
		
		
			this.myNewPDP = this.myBestPDP.clone().addNewTask(task);
			this.myNewPlan = this.myNewPDP.StochasticLocalSearch();
			this.myNewCost = this.myNewPlan.cost();
			
			this.opponentNewPDP = this.opponentBestPDP.clone().addNewTask(task);
			this.opponentNewPlan = this.opponentNewPDP.StochasticLocalSearch();
			this.opponentNewCost = this.opponentNewPlan.cost();
			
			double myNewMaginalCost = (myBestPlan == null) ? this.myNewCost :this.myNewCost - myBestPlan.cost();
			double opponentNewMaginalCost = (opponentBestPlan == null) ? this.opponentNewCost : this.opponentNewCost - opponentBestPlan.cost();
			
			//Have to update myRatio and opponentRatio in auctionResult
			
			double myBid = myNewMaginalCost * myRatio;
			double opponentBid = myBid / myRatio;
			
			// if (myBid < 0) => good
			
			if (opponentBid < myBid)
				myBid = opponentBid;
			
			return  (long) Math.round(myBid);

	}
	
	
	
    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
    	System.out.println("Generate Plan");
        long time_start = System.currentTimeMillis();
        
        System.out.println("Number of tasks that we won: "+ tasks.size());

        // Have to re-compute the best plan
        List<Task> t = new ArrayList<Task>(tasks);
        PickupDeliveryProblem pdp = new PickupDeliveryProblem(vehicles, t);
        pdp.StochasticLocalSearch();
        A bestPlan = pdp.getBestA();
        
        List<Plan> plans = new ArrayList<Plan>();
        List<City> cities = new ArrayList<City>();

        for (Vehicle v : vehicles){
        	City homeCity = v.homeCity();
        	LinkedList<Task_> tasks_ = bestPlan.getTasksOfVehicle(v);
        	if (tasks_ != null) {
        		Plan plan = makePlan(homeCity, tasks_);
       			plans.add(plan);
        	} else {
        		System.out.println("!!!!!!!!! task is null");
        	}
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        return plans;
    }
    
    private Plan makePlan(City homeCity, LinkedList<Task_> tasks) {
		City currentCity = homeCity;
		Plan plan = new Plan(homeCity);
		
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
