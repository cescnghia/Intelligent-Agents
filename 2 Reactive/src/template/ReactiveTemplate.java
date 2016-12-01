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
/**
 * 
 * 
 * @author
 * Stephane Cayssials (272048)
 * Anh Nghia Khau (223613)
 *
 */

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;



	private HashMap<State, Double> probaEachState = new HashMap<State, Double>(); // probability for each state
	private HashMap<City, Double> probaMoveAtCity = new HashMap<City, Double>(); // probability for MOVE at each city
	private HashMap<AgentAction, Double> actionReward = new HashMap<AgentAction, Double>(); //reward of an action
	private HashMap<State, List<AgentAction>> allActionsOfState = new HashMap<State, List<AgentAction>>(); // all possible actions at a state
	private HashMap<City, List<State>> allStateOfCity = new HashMap<Topology.City, List<State>>(); // all possible states at a city
	private HashMap<State, TupleActionValue> bestChoiceOfState = new HashMap<State, TupleActionValue>(); // store the best Action and best Value given a state
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
			// probability has a task at city a
			double probaHasTask = 0;
			double cost;
			for (City b : allCities) {
				List<State> states = new ArrayList<State>();
				if (a != b) { 
					State state = new State(a, b);
					states.add(state);
					// probability having a task at city b that you are from city a
					probaEachState.put(state, td.probability(a, b));
					probaHasTask += td.probability(a, b);
					
					List<AgentAction> actions = new ArrayList<AgentAction>();
					// First choice of action, there is a task in b
					AgentAction action1 = new AgentAction(b, template.AgentAction.Action.PICKUP);
					cost = a.distanceTo(b)*agent.vehicles().get(0).costPerKm();
					//Store all informations into a hashtable
					actions.add(action1);
					actionReward.put(action1, td.reward(a, b) - cost);
					// Suppose at the beginning, best value of an action is a smallest number in double
					bestChoiceOfState.put(state, new TupleActionValue(action1, -Double.MIN_VALUE));
					// Second choice of action, we can be move to the neighbor 
					for (City neighbor : a.neighbors()){
						AgentAction action2 = new AgentAction(neighbor, template.AgentAction.Action.MOVE);
						cost = a.distanceTo(neighbor)*agent.vehicles().get(0).costPerKm();
						actions.add(action2);
						actionReward.put(action2, -cost);
					}
					allActionsOfState.put(state, actions);
				}
				allStateOfCity.put(a, states);
			}
			// probaNoTask = 1- probaHasTask
			probaMoveAtCity.put(a, (1-probaHasTask));
			
			// all states State(city, null) at the beginning
			State stateNull = new State(a,null);
			List<AgentAction> actions = new ArrayList<AgentAction>();
			// Have to move to neighbor
			for (City neighbor : a.neighbors()){
				AgentAction action3 = new AgentAction(neighbor, template.AgentAction.Action.MOVE);
				cost = a.distanceTo(neighbor)*agent.vehicles().get(0).costPerKm();
				actions.add(action3);
				actionReward.put(action3, -cost);
				bestChoiceOfState.put(stateNull, new TupleActionValue(action3, -Double.MIN_VALUE));
			}
			// probaNoTask = 1- probaHasTask : there is no task at state null
			probaEachState.put(stateNull, (1-probaHasTask));
			allActionsOfState.put(stateNull, actions);		
		}
		reinforcementLearning();	
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		State state = new State(vehicle.getCurrentCity(), null);
		if (availableTask != null) {
			state.setDestinationCity(availableTask.deliveryCity);
		}
		AgentAction bestAction = bestChoiceOfState.get(state).getBestAction();
		if (bestAction.isMove()) {
			action = new Move(bestAction.getDestination());
		} else {
			action = new Pickup(availableTask);
		}
	
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;

	}
	
	public void reinforcementLearning(){
		boolean goodEnough;

		do {
			goodEnough = true;
			for (City city : allCities){
				List<State> allStates = allStateOfCity.get(city);
				for (State oldState : allStates){
					// There are best Action and best Value of State state in this tuple
					TupleActionValue tuple = bestChoiceOfState.get(oldState);
					List<AgentAction> allActions = allActionsOfState.get(oldState);
					for (AgentAction action : allActions){
						double qValue = actionReward.get(action);
						for (State nextState: allStateOfCity.get(action.getDestination())){
							qValue += (this.pPickup * getProba(nextState, action) * bestChoiceOfState.get(nextState).getBestValue());
						}
						if (tuple.getBestValue() < qValue){
							tuple.setBestValue(qValue);
							tuple.setBestAction(action);
							// There is a change of vector V(S), have to re-calculate all value of V(S)
							goodEnough = false;
						}
					}
				}
			}
		} while (! goodEnough);
	}
	
	public double getProba(State nextState, AgentAction a){
		if (a.isPickup()){
			return probaEachState.get(nextState);
		} else { //MOVE
			return probaMoveAtCity.get(nextState.getCurrentCity());
		}
	}
}
