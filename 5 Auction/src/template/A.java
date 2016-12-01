package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import template.Task_.Action;
import logist.task.Task;
 /**
  * 
  *  @author Anh Nghia Khau and Stephane Cayssials
  *
  *	Represent a plan for our problem. 
  * We took the name "A" for the compatibility with the one in the slide for Stochastic Local Search Algorithm
  *
  */
public class A {
	
	/*A plan is just a hash map of vehicle and list of task */
	/*
	 Ex : Vehicle 1 have to pick up and deliver task A, B
	      Vehicle 2 have to pick up and deliver task C, E
	      Vehicle 3 have to pick up and deliver task D
	 */

	private HashMap<Vehicle_, LinkedList<Task_>> mVehicleTasks;
	private List<Vehicle_> mVehicles = new ArrayList<Vehicle_>();
	
	
	/*----------CONSTRUCTOR-----------*/
	
	public A(HashMap<Vehicle_, LinkedList<Task_>> map){
		this.mVehicleTasks = map;
		this.mVehicles.addAll(map.keySet());
	}
	
	public A(A that){
		this.mVehicles = that.mVehicles;
		this.mVehicleTasks = new HashMap<Vehicle_, LinkedList<Task_>>(that.mVehicleTasks);
	}
	
	public A(ArrayList<Vehicle_> vehicles){
		for (Vehicle_ v : vehicles)
			mVehicleTasks.put(v, new LinkedList<Task_>());
		this.mVehicles = vehicles;
	}
	
	/*----------GETTER-----------*/
	
	public List<Vehicle_> getVehicles() { return this.mVehicles ; }
	
	public HashMap<Vehicle_, LinkedList<Task_>> getMap() { return this.mVehicleTasks; }
	
	public LinkedList<Task_> getTasksOfVehicle(Vehicle_ v){ return this.mVehicleTasks.get(v); }
	
	
	/*------ALL TRANSFORMATIONS METHODS------*/
	
	public void updateTask(Task task){
		for(Vehicle_ v: this.mVehicles){
			System.out.println(v);
			LinkedList<Task_> tasks_ = this.mVehicleTasks.get(v);
			for (int i = 0 ; i < tasks_.size(); i++){
				if(tasks_.get(i).getTask().id == task.id){
					tasks_.set(i, new Task_(task, tasks_.get(i).getAction()));
//					this.mVehicleTasks.remove(v);
					this.mVehicleTasks.put(v, tasks_);
				}
			}
			mVehicleTasks.size();
		}
	}
	
	// Add a task to the head
	public void addTaskForVehicle(Task_ a, Vehicle_ v){
		LinkedList<Task_> tasks = new LinkedList<Task_>(mVehicleTasks.get(v));
		tasks.addFirst(a);
		this.mVehicleTasks.put(v, tasks);
	}
	
	// Remove a specific task 
	public void removeTaskFromVehicle(Task_ a, Vehicle_ v){
		LinkedList<Task_> tasks = new LinkedList<Task_>(mVehicleTasks.get(v));
		tasks.remove(a);
		this.mVehicleTasks.put(v, tasks);
	}
	
	// Change order of 2 tasks
	public void changeOrderOfTwoTasks(int idx1, int idx2, Vehicle_ v){
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
		
		for (Vehicle_ v : mVehicleTasks.keySet()){
			
			if (mVehicleTasks.get(v).isEmpty()) //no task for this vehicle => continue
				continue;
			
			// Cost for going to pickup a task at the beginning
			cost += (v.getHomeCity().distanceTo(mVehicleTasks.get(v).getFirst().getTask().pickupCity))*v.getVehicle().costPerKm();
			LinkedList<Task_> tasks = this.mVehicleTasks.get(v);
			
			for (int i = 0 ; i < tasks.size() - 1 ; i++ ){
				Task_ task = tasks.get(i);
				Task_ nextTask = tasks.get(i+1);
				
				if (task.getAction() == Action.PICKUP && nextTask.getAction() == Action.PICKUP){
					cost += task.getTask().pickupCity.distanceTo(nextTask.getTask().pickupCity)*v.getVehicle().costPerKm();
				} else if (task.getAction() == Action.DELIVERY && nextTask.getAction() == Action.PICKUP){
					cost += task.getTask().deliveryCity.distanceTo(nextTask.getTask().pickupCity)*v.getVehicle().costPerKm();
				} else if (task.getAction() == Action.PICKUP && nextTask.getAction() == Action.DELIVERY){
					cost += task.getTask().pickupCity.distanceTo(nextTask.getTask().deliveryCity)*v.getVehicle().costPerKm();
				} else {
					cost += task.getTask().deliveryCity.distanceTo(nextTask.getTask().deliveryCity)*v.getVehicle().costPerKm();
				}
			}
		}	
		return cost;
	}
}
