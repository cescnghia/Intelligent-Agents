package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR, NAIVE }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan = null;

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			plan = astarPlan(vehicle, tasks);
			break;
		case BFS:
			// ...
			plan = bfsPlan(vehicle, tasks);
			break;
		case NAIVE:
			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
	
	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
		// time
		long begin = System.currentTimeMillis();  
		
		City currentCity = vehicle.getCurrentCity();
		Plan plan = new Plan(currentCity);
		boolean finalState = false;
		LinkedList<State> Q = new LinkedList<State>();
		ArrayList<State> C = new ArrayList<State>();

		State currentState = new State(vehicle, tasks, currentCity);
		Q.add(currentState);
		
		int iteration = 0;
		while(!finalState){
			iteration ++;
			if(Q.isEmpty()){
				System.out.println("Failure of the bfsPlan because Q is empty -> impossible to reach a final node");
				break;
			}
			State analysedState = Q.poll();
			
			if (analysedState.isFinal()) {
				System.out.println("Final State, the cost is: "+analysedState.mCost);
				plan = analysedState.getPlan();
				finalState = true;
			}
			if (! C.contains(analysedState)){
				C.add(analysedState);
				
				for (State s : analysedState.succ())
					if (! Q.contains(s))
						Q.add(s);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("A-Star takes "+iteration+" iterations in "+(end-begin)+" ms");
		return plan;
	}

	private Plan astarPlan(Vehicle vehicle, TaskSet tasks){
		// time
		long begin = System.currentTimeMillis();
		
		City currentCity = vehicle.getCurrentCity();
		Plan plan = new Plan(currentCity);
		int iteration = 0;
		boolean finalState = false;
		PriorityQueue<State> Q = new PriorityQueue<State>(new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				return o1.compareTo(o2);
			}
		});
		ArrayList<State> C = new ArrayList<State>(); 
		State currentState = new State(vehicle, tasks, currentCity);
		Q.add(currentState);
		
		while (! finalState){
			iteration++;
			if ( Q.isEmpty()) {
				System.out.println("Failure !!!!");
				break;
			}
			
			State analysedState = Q.poll();
			// Final state => return the plan
			if (analysedState.isFinal()) {
				System.out.println("Final State, the cost is: "+analysedState.mCost);
				finalState = true;
				plan = analysedState.getPlan();
			}
			
			boolean hasLowerCost = false;
			if(C.contains(analysedState)){
				hasLowerCost = analysedState.mCost < C.get(C.indexOf(analysedState)).mCost;
			}
			
			if( (!C.contains(analysedState)) || hasLowerCost){

				C.add(analysedState);
				List<State> successorStatesList = analysedState.succ();
				
				//Merge
				for (State s1 : successorStatesList){
					if (! Q.contains(s1))
						Q.add(s1);
				}	
			
			}		
		}
		long end = System.currentTimeMillis();
		System.out.println("A-Star takes "+iteration+" iterations in "+(end-begin)+" ms");
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
