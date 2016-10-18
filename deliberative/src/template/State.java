package template;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State {
	
	// This class has to cover all the possible state of the simulation
	// To summarize a step it is:
	// A *vehicle with a weight capacity
	// This vehicle is in a city *currentCity
	// with some *availableTasks of a certain weight
	// This vehicle is carrying some Tasks of a certain weight
	
	private Vehicle mVehicle;
	private TaskSet mAvailableTasks;
	private TaskSet mCarriedTasks;
	private City mCurrentCity;
	private int mWeight;
	private int mFreeWeight;
	private ActionsEnum mActionEnum;
	private Plan mPlan;
	
	public State(Vehicle vehicule, TaskSet availableTasks, City currentCity) {
		this.mActionEnum = ActionsEnum.INITSTATE;
		this.mPlan = new Plan(currentCity);
		this.mPlan.seal();
		
		this.mVehicle = vehicule;
		this.mAvailableTasks = availableTasks;
		this.mCurrentCity = currentCity;
		
		this.mWeight = 0;
		this.mCarriedTasks = this.mVehicle.getCurrentTasks();
		for (Task task: this.mCarriedTasks) {
			this.mWeight += task.weight;
		}
		this.mFreeWeight = this.mVehicle.capacity() - this.mWeight;	
	}
	
	public Plan getPlan() {
		return mPlan;
	}

	public State(Vehicle vehicule, TaskSet availableTasks, City currentCity, int freeWeight, TaskSet carriedTasks, ActionsEnum actionEnum, Plan plan) {
		this.mActionEnum = actionEnum;
		this.mPlan = plan;
		
		this.mVehicle = vehicule;
		this.mAvailableTasks = availableTasks;
		this.mCurrentCity = currentCity;
		
		this.mWeight = 0;
		this.mCarriedTasks = carriedTasks;
		for (Task task: this.mCarriedTasks) {
			this.mWeight += task.weight;
		}
		this.mFreeWeight = this.mVehicle.capacity() - this.mWeight;
		this.mFreeWeight = freeWeight;
	}
	
	public boolean isFinal() {
		// if there is no available task on the map and the vehicle do not carry task
		// this means that the state is final
		if (this.mAvailableTasks.isEmpty() && this.mVehicle.getCurrentTasks().isEmpty()) {
			return true;
		}
		return false;
	}
	
	public List<State> succ() {
		List<State> stateList = new ArrayList<State>();
		// fill the list with all the next state for each action type
		// for PICKUP in a city or for DELIVERY in a city
		for (ActionsEnum action: ActionsEnum.values()) {
			if(action == ActionsEnum.PICKUP) {
				// We check if one of more of all available task can be a next
				// pickup state
				for (Task taskAvailable : this.mAvailableTasks) {
					// we check if the vehicle has enough place for the task
					if (this.mFreeWeight < taskAvailable.weight) {
						// stop here for this task because there is not enough place in the vehicle
						continue;
					}
					// calculate the new freeWeight of the vehicle that has pickup the new task
					int newFreeWeight = this.mFreeWeight + taskAvailable.weight;
					Plan newPlan = this.mPlan;
					for (City city : this.mCurrentCity.pathTo(taskAvailable.pickupCity))
						newPlan.appendMove(city);

					newPlan.appendPickup(taskAvailable);
					// set the new city
					City newCity = taskAvailable.pickupCity;
					// set the new availableTasks Set by removing the picked one
					TaskSet newAvailableTasks = TaskSet.copyOf(this.mAvailableTasks);
					newAvailableTasks.remove(taskAvailable);
					// set the new carriedTask Set
					TaskSet newCarriedTasks = TaskSet.copyOf(this.mCarriedTasks);
					newCarriedTasks.add(taskAvailable);
					// add the new nextState to the nextState List
					stateList.add(new State(this.mVehicle, newAvailableTasks, newCity, newFreeWeight, newCarriedTasks, action, newPlan));
				}
			}
			if (action == ActionsEnum.DELIVERY) {
				// We check if one of more of all carriedTask can be Delivered
				for (Task taskDelivrable : this.mCarriedTasks) {
					// calculate the new freeWeight of the vehicle that has pickup the new task
					int newFreeWeight = this.mFreeWeight - taskDelivrable.weight;
					Plan newPlan = this.mPlan;
					for (City city : this.mCurrentCity.pathTo(taskDelivrable.pickupCity))
						newPlan.appendMove(city);

					newPlan.appendDelivery(taskDelivrable);
					// set the new city
					City newCity = taskDelivrable.deliveryCity;
					// set the new availableTasks which is the same as previously
					TaskSet newAvailableTasks = TaskSet.copyOf(this.mAvailableTasks);
					// set the new carriedTask Set
					TaskSet newCarriedTasks = TaskSet.copyOf(this.mCarriedTasks);
					newCarriedTasks.remove(taskDelivrable);
					// add the new nextState to the nextState List
					stateList.add(new State(this.mVehicle, newAvailableTasks, newCity, newFreeWeight, newCarriedTasks, action, newPlan));
				}
			}
		}
		return stateList;
	}

	public Vehicle getVehicule() {
		return mVehicle;
	}

	public TaskSet getAvailableTasks() {
		return mAvailableTasks;
	}

	public City getCurrentCity() {
		return mCurrentCity;
	}
	
}
