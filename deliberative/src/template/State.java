package template;

import java.util.List;

import logist.plan.Action;
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
	private City mCurrentCity;
	private int mWeight;
	private int mFreeWeight;
	
	public State(Vehicle mVehicule, TaskSet mAvailableTasks, City currentCity) {	
		this.mVehicle = mVehicule;
		this.mAvailableTasks = mAvailableTasks;
		this.mCurrentCity = currentCity;
		
		this.mWeight = 0;
		for (Task task: mVehicle.getCurrentTasks()) {
			this.mWeight += task.weight;
		}
		this.mFreeWeight = this.mVehicle.capacity() - this.mWeight;	
	}
	
	public boolean isFinal() {
		// if there is no available task on the map and the vehicle do not carry task
		// this means that the state is final
		if (this.mAvailableTasks.isEmpty() && this.mVehicle.getCurrentTasks().isEmpty()) {
			return true;
		}
		return false;
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
