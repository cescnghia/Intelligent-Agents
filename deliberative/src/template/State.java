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
	private int mFreeWeight;
	private int mWeight;
	private Plan mPlan;
	private City mInitialCity;
	public boolean isFinal;
	public double cost;
	public double heuristicCost;
	
	public State(Vehicle vehicule, TaskSet availableTasks, City currentCity) {
		this.mPlan = new Plan(currentCity);
		this.mInitialCity = currentCity;
		this.cost = 0;

		this.mVehicle = vehicule;
		this.mAvailableTasks = availableTasks;
		this.mCurrentCity = currentCity;
		
		this.mCarriedTasks = this.mVehicle.getCurrentTasks(); 
		
		this.mWeight = 0;
		
		for (Task task: this.mCarriedTasks) {
			this.mWeight += task.weight;
		}
		this.mFreeWeight = this.mVehicle.capacity() - this.mWeight;	

		this.isFinal = availableTasks.isEmpty() && mCarriedTasks.isEmpty();
		
		double heuristicTmp = 0;
		Task heuristicTask = null;
		boolean isPickupTask = false;
		int heuristicFreeWeight = this.mFreeWeight;
		City heuristicCityTmp = currentCity;
		TaskSet heuristicAvailableTasks = availableTasks.clone();
		TaskSet heuristicCarriedTasks = availableTasks.clone();
		int totalTaskNb = heuristicCarriedTasks.size() + heuristicAvailableTasks.size();
		Plan heuristicPlan = new Plan(currentCity);
		while(heuristicAvailableTasks.size() != 0 || heuristicCarriedTasks.size() != 0){
//			System.out.println("-----------"+heuristicAvailableTasks+heuristicCarriedTasks);
			heuristicTmp = 0;
			for (Task ta : heuristicAvailableTasks) {
				if(heuristicFreeWeight > ta.weight){
					double tmpCost = (heuristicCityTmp.distanceTo(ta.pickupCity) + ta.pathLength())*mVehicle.costPerKm();
					if(heuristicTmp <= tmpCost) {
						heuristicTmp = tmpCost;
						heuristicTask = ta;
						isPickupTask = true;
						heuristicCityTmp = ta.deliveryCity;
					}
				}
			}
			for(Task tc: heuristicCarriedTasks){
				double tmpCost = heuristicCityTmp.distanceTo(tc.deliveryCity)*mVehicle.costPerKm();
				if(heuristicTmp <= tmpCost) {
					heuristicTmp = tmpCost;
					heuristicTask = tc;
					isPickupTask = false;
					heuristicCityTmp = tc.deliveryCity;
				}
			}
			if(isPickupTask) {
				heuristicPlan.appendPickup(heuristicTask);
				heuristicAvailableTasks.remove(heuristicTask);
			} else {
				heuristicFreeWeight = heuristicFreeWeight + heuristicTask.weight;
				heuristicCarriedTasks.remove(heuristicTask);
			}
			heuristicPlan.appendDelivery(heuristicTask);
			
		}
		this.heuristicCost = heuristicPlan.totalDistance()*mVehicle.costPerKm()/totalTaskNb;
	}
	
	public Plan getPlan() {
		return mPlan;
	}

	public State(	Vehicle vehicule, 
					TaskSet availableTasks, 
					City currentCity, 
					int freeWeight, 
					TaskSet carriedTasks, 
					Plan plan, 
					City initialCity,
					double cost) {
		this.mPlan = plan;
		this.mInitialCity = initialCity;
		this.cost = cost;
		
		this.mVehicle = vehicule;
		this.mAvailableTasks = availableTasks;
		this.mCurrentCity = currentCity;
		this.mWeight = 0;
		this.mCarriedTasks = carriedTasks;
		for (Task task: this.mCarriedTasks) {
			this.mWeight += task.weight;
		}
		this.mFreeWeight = freeWeight;
		this.isFinal = (mAvailableTasks.isEmpty() && mCarriedTasks.isEmpty());
		
		double heuristicTmp = 0;
		Task heuristicTask = null;
		boolean isPickupTask = false;
		int heuristicFreeWeight = this.mFreeWeight;
		City heuristicCityTmp = currentCity;
		TaskSet heuristicAvailableTasks = availableTasks.clone();
		TaskSet heuristicCarriedTasks = availableTasks.clone();
		int totalTaskNb = heuristicCarriedTasks.size() + heuristicAvailableTasks.size();
		Plan heuristicPlan = new Plan(currentCity);
		while(heuristicAvailableTasks.size() != 0 || heuristicCarriedTasks.size() != 0){
//			System.out.println("-----------"+heuristicAvailableTasks+heuristicCarriedTasks);
			heuristicTmp = 0;
			for (Task ta : heuristicAvailableTasks) {
				if(heuristicFreeWeight > ta.weight){
					double tmpCost = (heuristicCityTmp.distanceTo(ta.pickupCity) + ta.pathLength())*mVehicle.costPerKm();
					if(heuristicTmp <= tmpCost) {
						heuristicTmp = tmpCost;
						heuristicTask = ta;
						isPickupTask = true;
						heuristicCityTmp = ta.deliveryCity;
					}
				}
			}
			for(Task tc: heuristicCarriedTasks){
				double tmpCost = heuristicCityTmp.distanceTo(tc.deliveryCity)*mVehicle.costPerKm();
				if(heuristicTmp <= tmpCost) {
					heuristicTmp = tmpCost;
					heuristicTask = tc;
					isPickupTask = false;
					heuristicCityTmp = tc.deliveryCity;
				}
			}
			if(isPickupTask) {
				heuristicPlan.appendPickup(heuristicTask);
				heuristicAvailableTasks.remove(heuristicTask);
			} else {
				heuristicFreeWeight = heuristicFreeWeight + heuristicTask.weight;
				heuristicCarriedTasks.remove(heuristicTask);
			}
			heuristicPlan.appendDelivery(heuristicTask);
		}
		this.heuristicCost = heuristicPlan.totalDistance()*mVehicle.costPerKm()/totalTaskNb;

	}
	
	public boolean isFinal() {
		// if there is no available task on the map and the vehicle do not carry task
		// this means that the state is final		
		return (this.mAvailableTasks.isEmpty() && this.mCarriedTasks.isEmpty());
	}
	
	public List<State> succ() {
		List<State> stateList = new ArrayList<State>();
		// fill the list with all the next state for each action type
		// for PICKUP in a city or for DELIVERY in a city
		// We check if one of more of all available task can be a next
		// pickup state

		
		
		for (Task taskAvailable : mAvailableTasks) {
			
			// we check if the vehicle has enough weight for the task
			if (this.mFreeWeight < taskAvailable.weight) {
				// continue to checking the others tasks because this task is too heavy for the vehicle
				continue;
			}
			// calculate the new freeWeight of the vehicle that has pickup the new task
			
			int newFreeWeight = this.mFreeWeight - taskAvailable.weight;

			Plan newPlan = new Plan(this.mInitialCity);

			// add all old plan to new plan
			Iterator<Action> planIterator = this.mPlan.iterator();
			while (planIterator.hasNext()) {
				Action previousPlanAction = planIterator.next();
				newPlan.append(previousPlanAction);
			}
		
			for (City city : this.mCurrentCity.pathTo(taskAvailable.pickupCity))
				newPlan.appendMove(city);

			newPlan.appendPickup(taskAvailable);
			// set the cost
			double newCost = this.mCurrentCity.distanceTo(taskAvailable.pickupCity) * this.mVehicle.costPerKm();
			newCost += this.cost;
			// set the new city
			City newCity = taskAvailable.pickupCity;
			// set the new availableTasks Set by removing the picked one
			TaskSet newAvailableTasks = this.mAvailableTasks.clone();
			newAvailableTasks.remove(taskAvailable);
			// set the new carriedTask Set
			TaskSet newCarriedTasks = this.mCarriedTasks.clone();
			newCarriedTasks.add(taskAvailable);
			// add the new nextState to the nextState List
			stateList.add(new State(this.mVehicle, newAvailableTasks, newCity, newFreeWeight, newCarriedTasks, newPlan, this.mInitialCity, newCost));
		}
		// We check if one of more of all carriedTask can be Delivered
		for (Task taskDelivrable : this.mCarriedTasks) {
			
			
			// calculate the new freeWeight of the vehicle that has pickup the new task

			int newFreeWeight = this.mFreeWeight + taskDelivrable.weight;
			Plan newPlan = new Plan(this.mInitialCity);

			Iterator<Action> planIterator = this.mPlan.iterator();
			while (planIterator.hasNext()) {
				Action previousPlanAction = planIterator.next();
				newPlan.append(previousPlanAction);
			}

			for (City city : this.mCurrentCity.pathTo(taskDelivrable.deliveryCity))
				newPlan.appendMove(city);

			newPlan.appendDelivery(taskDelivrable);
			// set the cost
			double newCost = mCurrentCity.distanceTo(taskDelivrable.deliveryCity) * this.mVehicle.costPerKm();
			newCost += this.cost;
			// set the new city
			City newCity = taskDelivrable.deliveryCity;
			// set the new availableTasks which is the same as previously
			TaskSet newAvailableTasks = this.mAvailableTasks.clone();
			// set the new carriedTask Set
			TaskSet newCarriedTasks = this.mCarriedTasks.clone();
			newCarriedTasks.remove(taskDelivrable);
			// add the new nextState to the nextState List
			stateList.add(new State(this.mVehicle, newAvailableTasks, newCity, newFreeWeight, newCarriedTasks, newPlan, this.mInitialCity, newCost));
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
	
	// compare 2 states base on function f(n) = g(n) + h(n) 
	// where g(n) = cost of this agent so far and h(n) = heuristic cost
	@Override
	public int compareTo(State o) {
		if((this.cost + this.heuristicCost) < (o.cost + o.heuristicCost)) {
			return -1;
		} 
	    else if((o.cost + o.heuristicCost) < (this.cost + this.heuristicCost)) {
	    	return 1;
	    }
	    return 0;
	}

	@Override
	public String toString() {
		return "State [currentCity=" + mCurrentCity + ", cost=" + cost + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.mFreeWeight;

		result = prime * result + (isFinal() ? 1231 : 1237);
		result = prime * result
				+ ((this.mCurrentCity == null) ? 0 : mCurrentCity.hashCode());
		result = prime * result
				+ ((this.mInitialCity == null) ? 0 : mCurrentCity.hashCode());
		return result;
	}

}
