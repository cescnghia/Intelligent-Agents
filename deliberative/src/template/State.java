package template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State implements Comparable<State> {
	
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
	private Plan mPlan;
	private City mInitialCity;
	public boolean isFinal;
	
	public State(Vehicle vehicule, TaskSet availableTasks, City currentCity) {
		this.mPlan = new Plan(currentCity);
		this.mInitialCity = currentCity;
		
		
		this.mVehicle = vehicule;
		this.mAvailableTasks = availableTasks;
		this.mCurrentCity = currentCity;
		
		this.mWeight = 0;
		this.mCarriedTasks = this.mVehicle.getCurrentTasks();
		for (Task task: this.mCarriedTasks) {
			this.mWeight += task.weight;
		}
		this.mFreeWeight = this.mVehicle.capacity() - this.mWeight;	
		this.isFinal = this.isFinal();
	}
	
	public Plan getPlan() {
		return mPlan;
	}

	public State(Vehicle vehicule, TaskSet availableTasks, City currentCity, int freeWeight, TaskSet carriedTasks, Plan plan, City initialCity) {
		this.mPlan = plan;
		this.mInitialCity = initialCity;
		
		
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
		this.isFinal = this.isFinal();
	}
	
	public boolean isFinal() {
		// if there is no available task on the map and the vehicle do not carry task
		// this means that the state is final		
		if (this.mAvailableTasks.size() == 0 && this.mCarriedTasks.size() == 0) {
			return true;
		}
		return false;
	}
	
	public List<State> succ() throws InterruptedException {
			
		List<State> stateList = new ArrayList<State>();
		// fill the list with all the next state for each action type
		// for PICKUP in a city or for DELIVERY in a city
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

			Plan newPlan = new Plan(this.mInitialCity);

			Iterator<Action> planIterator = this.mPlan.iterator();
			while (planIterator.hasNext()) {
				Action previousPlanAction = planIterator.next();
				newPlan.append(previousPlanAction);
			}

			for (City city : this.mCurrentCity.pathTo(taskAvailable.pickupCity))
				newPlan.appendMove(city);

			newPlan.appendPickup(taskAvailable);
			// set the new city
			City newCity = taskAvailable.pickupCity;
			// set the new availableTasks Set by removing the picked one
			TaskSet newAvailableTasks = this.mAvailableTasks.clone();
			newAvailableTasks.remove(taskAvailable);
			// set the new carriedTask Set
			TaskSet newCarriedTasks = this.mCarriedTasks.clone();
			newCarriedTasks.add(taskAvailable);
			// add the new nextState to the nextState List
			stateList.add(new State(this.mVehicle, newAvailableTasks, newCity, newFreeWeight, newCarriedTasks, newPlan, this.mInitialCity));
		}
		// We check if one of more of all carriedTask can be Delivered
		for (Task taskDelivrable : this.mCarriedTasks) {
			// calculate the new freeWeight of the vehicle that has pickup the new task
			int newFreeWeight = this.mFreeWeight - taskDelivrable.weight;
			Plan newPlan = new Plan(this.mInitialCity);

			Iterator<Action> planIterator = this.mPlan.iterator();
			while (planIterator.hasNext()) {
				Action previousPlanAction = planIterator.next();
				newPlan.append(previousPlanAction);
			}

			for (City city : this.mCurrentCity.pathTo(taskDelivrable.deliveryCity))
				newPlan.appendMove(city);

			newPlan.appendDelivery(taskDelivrable);
			// set the new city
			City newCity = taskDelivrable.deliveryCity;
			// set the new availableTasks which is the same as previously
			TaskSet newAvailableTasks = this.mAvailableTasks.clone();
			// set the new carriedTask Set
			TaskSet newCarriedTasks = this.mCarriedTasks.clone();
			newCarriedTasks.remove(taskDelivrable);
			// add the new nextState to the nextState List
			stateList.add(new State(this.mVehicle, newAvailableTasks, newCity, newFreeWeight, newCarriedTasks, newPlan, this.mInitialCity));
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (mAvailableTasks == null) {
			if (other.mAvailableTasks != null)
				return false;
		} else if (!mAvailableTasks.equals(other.mAvailableTasks))
			return false;
		if (mCarriedTasks == null) {
			if (other.mCarriedTasks != null)
				return false;
		} else if (!mCarriedTasks.equals(other.mCarriedTasks))
			return false;
		if (mCurrentCity == null) {
			if (other.mCurrentCity != null)
				return false;
		} else if (!mCurrentCity.equals(other.mCurrentCity))
			return false;
		if (mWeight != other.mWeight)
			return false;
		if (mFreeWeight != other.mFreeWeight)
			return false;
		return true;
	}

	@Override
	public int compareTo(State o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
