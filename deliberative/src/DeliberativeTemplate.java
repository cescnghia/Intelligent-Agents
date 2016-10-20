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

	enum Algorithm { BFS, ASTAR }
	
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
		City currentCity = vehicle.getCurrentCity();
		Plan plan = new Plan(currentCity);
		boolean finalNode = false;
		LinkedList<State> Q = new LinkedList<State>();
		ArrayList<State> C = new ArrayList<State>();

		State currentState = new State(vehicle, tasks, currentCity);
		Q.add(currentState);
		
		int iteration = 0;
		while(!finalNode){
			iteration ++;
			if(Q.isEmpty()){
				System.out.println("Failure of the bfsPlan because Q is empty -> impossible to reach a final node");
			}
			State analysedState = Q.poll();
			if (analysedState.isFinal) {
				System.out.println("isFinal");
				plan = analysedState.getPlan();
				finalNode = true;
			}
			if(!C.contains(analysedState)){
				C.add(analysedState);
				Q.addAll(analysedState.succ());
			} else {
				//System.out.println("test");
			}
		}
		System.out.println("BFS takes "+iteration+" iterations");
		return plan;
	}
/*	
	private Plan astarPlan(Vehicle vehicle, TaskSet tasks){
		City currentCity = vehicle.getCurrentCity();
		Plan plan = new Plan(currentCity);
		int iteration = 0;
		boolean finalNode = false;
		PriorityQueue<State> Q = new PriorityQueue<State>(new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				return o1.compareTo(o2);
			}
		});
		ArrayList<State> C = new ArrayList<State>(); 
		State currentState = new State(vehicle, tasks, currentCity);
		Q.add(currentState);
		
		while (! finalNode){
			iteration++;
			if ( Q.isEmpty()) {
				System.out.println("Failure !!!!");
			}
			
			State analysedState = Q.poll();
			// Final state => return the plan
			if (analysedState.isFinal()) {
				System.out.println("Final State");
				finalNode = true;
				plan = analysedState.getPlan();
			}
			
			boolean hasLowerCost = false;
			if(C.contains(analysedState)){
				hasLowerCost = analysedState.cost < C.get(C.indexOf(analysedState)).cost;
			}
			
			if(!C.contains(analysedState) || hasLowerCost){
				C.add(analysedState);
				List<State> successorStatesList = analysedState.succ();
				Q.addAll(successorStatesList);
			}		
		}
		System.out.println("A-Star takes "+iteration+" iterations");
		return plan;
	}
*/	
	private Plan astarPlan(Vehicle vehicle, TaskSet tasks) {
		City currentCity = vehicle.getCurrentCity();
		Plan plan = new Plan(currentCity);
		boolean finalNode = false;
		LinkedList<State> Q = new LinkedList<State>();
		ArrayList<State> C = new ArrayList<State>();

		State currentState = new State(vehicle, tasks, currentCity);
		Q.add(currentState);
		int iteration = 0;
		while(!finalNode){
			iteration ++;
			if(Q.isEmpty()){
				System.out.println("Failure of the bfsPlan because Q is empty -> impossible to reach a final node");
			}
			State analysedState = Q.poll();
			if (analysedState.isFinal) {
				System.out.println("isFinal");
				plan = analysedState.getPlan();
				finalNode = true;
			}
			boolean hasLowerCost = false;
			if(C.contains(analysedState)){
				hasLowerCost = analysedState.cost < C.get(C.indexOf(analysedState)).cost;
			}
			if(!C.contains(analysedState) || hasLowerCost){
				C.add(analysedState);
				List<State> successorStatesList = analysedState.succ();
				Collections.sort(successorStatesList);
				Q.addAll(successorStatesList);
				Collections.sort(Q);
			}
		}
		System.out.println("A-Star takes "+iteration+" iterations");
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
