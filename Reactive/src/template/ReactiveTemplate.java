package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.plan.Action;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	

	private HashMap<State, Double> probEachState = new HashMap<State, Double>();
	private HashMap<AgentAction, Double> actionReward = new HashMap<AgentAction, Double>();
	private HashMap<State, List<AgentAction>> allActionsOfState = new HashMap<State, List<AgentAction>>();
	private ArrayList<City> allCities;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		
		allCities = new ArrayList<City>(topology.cities());
		
		// Create all state
		for (City a : allCities) {
			for (City b : allCities) {
				double cost;
				if (a != b) { 
					State state = new State(a, b);
					// probability having a task at city b that you are from city a
					probEachState.put(state, td.probability(a, b));
					
					List<AgentAction> actions = new ArrayList<AgentAction>();
					// First choice of action, there is a task in b
					AgentAction action1 = new AgentAction(b, template.AgentAction.Action.PICKUP);
					cost = a.distanceTo(b)*agent.vehicles().get(0).costPerKm();
					//Store all informations into a hashtable
					actions.add(action1);
					actionReward.put(action1, td.reward(a, b) - cost);
					
					// Second choice of action, we can be move to the neighbor 
					for (City neighbor : a.neighbors()){
						AgentAction action2 = new AgentAction(neighbor, template.AgentAction.Action.MOVE);
						cost = a.distanceTo(neighbor)*agent.vehicles().get(0).costPerKm();
						actions.add(action2);
						actionReward.put(action2, -cost);
					}
					
					allActionsOfState.put(state, actions);
				
				}
			}
		}
		
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
}
