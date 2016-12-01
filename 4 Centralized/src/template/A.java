package template;

import java.util.HashMap;
import java.util.LinkedList;

import template.Task_.Action;
import logist.simulation.Vehicle;
 /**
  * 
  *  @author Anh Nghia Khau and Stephane Cayssials
  *
  *	Represent a plan for our problem. 
  * We took the name "A" for the compatibility with the one in the slide for Stochastic Local Search Algorithm
  *
  */
public class A {

	private HashMap<Vehicle, LinkedList<Task_>> mVehicleTasks;
	
	public A(HashMap<Vehicle, LinkedList<Task_>> map){
		this.mVehicleTasks = map;
	}
	
	public A(A that){
		this.mVehicleTasks = new HashMap<Vehicle, LinkedList<Task_>>(that.mVehicleTasks);
	}
	
	public HashMap<Vehicle, LinkedList<Task_>> getMap(){
		return this.mVehicleTasks;
	}
	
	public LinkedList<Task_> getTasksOfVehicle(Vehicle v){
		return this.mVehicleTasks.get(v);
	}
	
	/*------ALL TRANSFORMATIONS METHODS------*/
	
	// Add a task to the head
	public void addTaskForVehicle(Task_ a, Vehicle v){
		LinkedList<Task_> tasks = new LinkedList<Task_>(mVehicleTasks.get(v));
		tasks.addFirst(a);
		this.mVehicleTasks.put(v, tasks);
	}
	
	// Remove a specific task 
	public void removeTaskFromVehicle(Task_ a, Vehicle v){
		LinkedList<Task_> tasks = new LinkedList<Task_>(mVehicleTasks.get(v));
		tasks.remove(a);
		this.mVehicleTasks.put(v, tasks);
	}
	
	// Change order of 2 tasks
	public void changeOrderOfTwoTasks(int idx1, int idx2, Vehicle v){
		LinkedList<Task_> tasks = mVehicleTasks.get(v);

		Task_ task1 = tasks.get(idx1);
		Task_ task2 = tasks.get(idx2);
		
		tasks.set(idx1, task2);
		tasks.set(idx2, task1);
		this.mVehicleTasks.put(v, tasks);
	}
	
	/*--------COMPUTE COST OF THIS PLAN---------*/
	
	public double cost(){
		double cost = 0.0;
		
		for (Vehicle v : mVehicleTasks.keySet()){
			
			if (mVehicleTasks.get(v).isEmpty()) //no task for this vehicle => continue
				continue;
			
			// Cost for going to pickup a task at the beginning
			cost += (v.homeCity().distanceTo(mVehicleTasks.get(v).getFirst().getTask().pickupCity))*v.costPerKm();
			LinkedList<Task_> tasks = this.mVehicleTasks.get(v);
			
			for (int i = 0 ; i < tasks.size() - 1 ; i++ ){
				Task_ task = tasks.get(i);
				Task_ nextTask = tasks.get(i+1);
				
				if (task.getAction() == Action.PICKUP && nextTask.getAction() == Action.PICKUP){
					cost += task.getTask().pickupCity.distanceTo(nextTask.getTask().pickupCity)*v.costPerKm();
				} else if (task.getAction() == Action.DELIVERY && nextTask.getAction() == Action.PICKUP){
					cost += task.getTask().deliveryCity.distanceTo(nextTask.getTask().pickupCity)*v.costPerKm();
				} else if (task.getAction() == Action.PICKUP && nextTask.getAction() == Action.DELIVERY){
					cost += task.getTask().pickupCity.distanceTo(nextTask.getTask().deliveryCity)*v.costPerKm();
				} else {
					cost += task.getTask().deliveryCity.distanceTo(nextTask.getTask().deliveryCity)*v.costPerKm();
				}
			}
		}	
		return cost;
	}
}
