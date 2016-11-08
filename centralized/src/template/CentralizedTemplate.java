package template;

//the list of imports
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import template.Task_.Action;
import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
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
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
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
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();

        PickupDeliveryProblem pdp = new PickupDeliveryProblem(vehicles, tasks);
        pdp.StochasticLocalSearch();
        A bestA = pdp.getBestA();
        System.out.println("The minimun cost is: "+pdp.getCost());
        
        List<Plan> plans = new ArrayList<Plan>();

        for (Vehicle v : vehicles){
        	LinkedList<Task_> tasks_ = bestA.getTasksOfVehicle(v);
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

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}
