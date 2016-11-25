package template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import template.Task_.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class PickupDeliveryProblem {

	/*
	 * In this class, we use the algorithm StochasticLocalSearch that we have seen in class
	 * to find a best plan
	 * 
	 * P and MAX_ITER are 2 parameters using for this algorithm
	 * */
	
	
	
	private List<Vehicle> mVehicles;
	private TaskSet mTasks;
	
	// For storing best plan and the best cost
	private A mBestA;
	private double mCost;
	
	// Parameters for the algorithm
	private static final double P = 0.4;
	private static final int MAX_ITER = 10000;
	
	
	/**Constructor  
	 * 
	 * @param vehicles: all vehicles
	 * @param tasks: all tasks
	 */
	public PickupDeliveryProblem(List<Vehicle> vehicles, TaskSet tasks){
		this.mVehicles = new ArrayList<Vehicle>(vehicles);
		this.mTasks = tasks;
		this.mCost = Double.MAX_VALUE;
		this.mBestA = null;
	}
	
	public PickupDeliveryProblem(List<Vehicle> vehicles){
		this.mVehicles = new ArrayList<Vehicle>(vehicles);
		this.mTasks = null;
		this.mCost = Double.MAX_VALUE;
		this.mBestA = null;
	}
	
	public A getBestA() { return this.mBestA; }
	
	public double getCost() { return this.mCost; }
	
	public List<Vehicle> getVehicle() {return this.mVehicles;}
	
	
	// Use to add a new task each time we win a task from the auction
	
	public PickupDeliveryProblem addNewTask(Task task){
		this.mTasks.add(task);
		return new PickupDeliveryProblem(this.mVehicles, this.mTasks);
	}
	
/*	
	// Give all the task to the biggest vehicle 
	//(have to change CAPACITY one of the vehicle to  = (# of task * 3)
	public A SelectInitialSolution_1(){
		
		Vehicle vehicle = null;
		LinkedList<Task_> tasks = new LinkedList<Task_>();
		HashMap<Vehicle, LinkedList<Task_>> map = new HashMap<Vehicle, LinkedList<Task_>>();
		
		// Find the biggest vehicle (depend on it's load)
		int biggestCapacity = Integer.MIN_VALUE;
		for (Vehicle v : this.mVehicles){
			if (v.capacity() > biggestCapacity){
				biggestCapacity = v.capacity();
				vehicle = v;
			}
		}
		
		// Assign all tasks 
		for (Task t : this.mTasks){
			if (t.weight > vehicle.capacity())
				throw new IllegalArgumentException("The problem is unsolvable because the task is too heavy");
			tasks.add(new Task_(t, Action.DELIVERY));
			tasks.add(new Task_(t, Action.PICKUP));
		}
		// Initial for these others vehicles
		for (Vehicle v : mVehicles)
			if (v != vehicle)
				map.put(v, new LinkedList<Task_>());
		
		map.put(vehicle, tasks);
		
		//return a "initial" plan
		return new A(map);
	}
*/	
	// Give all tasks to all vehicles randomly
	public A SelectInitialSolution(){
		
		if (mTasks.isEmpty())
			throw new IllegalArgumentException("There is no task for delivering");
			
		HashMap<Vehicle, LinkedList<Task_>> map = new HashMap<Vehicle, LinkedList<Task_>>();
		HashMap<Vehicle, Integer> load = new HashMap<Vehicle, Integer>();
		
		for (Vehicle v : mVehicles)
				map.put(v, new LinkedList<Task_>());
		
		for (Vehicle v : mVehicles)
			load.put(v, 0);
		
		Random rd = new Random();
		
		for (Task t : mTasks){
			Vehicle v = mVehicles.get(rd.nextInt(mVehicles.size()));
			while (load.get(v) + t.weight > v.capacity()){
				v = mVehicles.get(rd.nextInt(mVehicles.size()));
			}
			load.put(v, load.get(v)+t.weight);
			LinkedList<Task_> actualyList = map.get(v);
			actualyList.add(new Task_(t, Action.DELIVERY));
			actualyList.add(new Task_(t, Action.PICKUP));
			map.put(v, actualyList);
		}
		
		return new A(map);
	}
	
	// Apply Stochastic Local Search Algorithm
	public A StochasticLocalSearch(){
		
		A plan = SelectInitialSolution();
		int iter = MAX_ITER;
		
		for (int i = 0; i < iter ; i++){
			A oldPlan = new A(plan);
			ArrayList<A> plans = ChooseNeighbors(oldPlan);
			plan = LocalChoice(oldPlan, plans);
		}
	
		return plan;	
	}
	
	// Have to choice the best plan (min cost) AMONG the setOfPlan with probability P, or return the old plan with probability = (1-P)
	private A LocalChoice(A old, ArrayList<A> setOfPlan) {
		
		PriorityQueue<A> queue = new PriorityQueue<A>(new Comparator<A>() {
			@Override
			// min cost !!!!
			public int compare(A o1, A o2) {
				if(o1.cost() < o2.cost())
					return -1;
				else if (o2.cost()==o1.cost())
					return 0;
				else 
					return 1;
			}
		});
		queue.addAll(setOfPlan);
		
		A choice = old;
		
		Random rd = new Random();
		double d = rd.nextDouble();

		if ( (d < P) && (! queue.isEmpty()) ) {
			choice = queue.poll();
			// Update min cost if necessary....
			if (choice.cost() < this.mCost) {
				this.mBestA = choice;
				this.mCost = choice.cost();
			}
		}
		
		return choice;
	}
	
	
	//Choose 
	private ArrayList<A> ChooseNeighbors(A old) {
		ArrayList<A> plans = new ArrayList<A>();

		Random rd = new Random();
		Vehicle vehicle = null;
	
		while (true){ // Find a vehicle that has a task
			vehicle = this.mVehicles.get(rd.nextInt(this.mVehicles.size()));
			LinkedList<Task_> tasks = old.getTasksOfVehicle(vehicle);
			if ( (tasks != null) && (!tasks.isEmpty()))
				break;
		}
	
		
		//Applying the changing vehicle operator
		for (Vehicle v : this.mVehicles){
			//check if vehicle "v" can take the task of vehicle "vehicle" (weight compatible)
			if ( (v != vehicle) && (old.getTasksOfVehicle(vehicle).get(0).getTask().weight <= v.capacity())){
				A newA = ChangingVehicle(old, vehicle, v);
				if ( (newA != null) && checkConstraint(newA))
					plans.add(newA);
			}
		}
		
		//Applying the changing task order operator
		int numOfTask = old.getTasksOfVehicle(vehicle).size();
		if (numOfTask >= 2){
			for (int i = 0 ; i < numOfTask ; i++ ){
				for (int j = i+1; j < numOfTask ; j++){
					A newA = ChangingTaskOrder(old, vehicle, i, j);
					if ( (newA != null ) && checkConstraint(newA))
						plans.add(newA);
				}
			}
		}
		
		return plans;
	}
	
	// Move the 1st task of v1 and append to the head of v2
	private A ChangingVehicle(A a, Vehicle v1, Vehicle v2){
		A newA = new A(a);

		// There is no task on v1
		if (newA.getTasksOfVehicle(v1)==null || newA.getTasksOfVehicle(v1).isEmpty())
			return newA;

		Task_ taskToMove = newA.getTasksOfVehicle(v1).getFirst();

		if (! newA.getTasksOfVehicle(v1).contains(taskToMove.getInverse()))
			throw new IllegalArgumentException("2 actions of a task is not in the same vehicle");

		// Have to remove 2 actions (pickup and delivery) of the task "taskToMove" in v1...
		newA.removeTaskFromVehicle(taskToMove, v1);
		newA.removeTaskFromVehicle(taskToMove.getInverse(), v1);
		
		// and add to the head of v2, add action delivery first...
		if (taskToMove.getAction() == Action.PICKUP){
			newA.addTaskForVehicle(taskToMove.getInverse(), v2);
			newA.addTaskForVehicle(taskToMove, v2);
		} else {
			newA.addTaskForVehicle(taskToMove, v2);
			newA.addTaskForVehicle(taskToMove.getInverse(), v2);
		}
		return newA;	
	}
	
	
	private A ChangingTaskOrder(A a, Vehicle v, int idx1, int idx2){
		
		A newA = new A(a);
		
		// There is no task on v
		if (newA.getTasksOfVehicle(v)==null || newA.getTasksOfVehicle(v).isEmpty())
			return newA;
		
		LinkedList<Task_> tasks = newA.getTasksOfVehicle(v);
		
		Task_ task1 = newA.getTasksOfVehicle(v).get(idx1);
		Task_ task2 = newA.getTasksOfVehicle(v).get(idx2);
		
		if (task1.getTask() == task2.getTask())
			//throw new IllegalArgumentException("Can not delivery than pickup !!!!");
			return null;
		
		// 2 cases can raise a problem after swap, need to avoid
		if (task1.getAction() == Action.PICKUP && tasks.indexOf(task1.getInverse()) < idx2)
			return null;
		
		if (task2.getAction() == Action.DELIVERY && tasks.indexOf(task2.getInverse()) > idx1)
			return null;
		
		// Otherwise feel free to swap
		newA.changeOrderOfTwoTasks(idx1, idx2, v);
		
		return newA;
	}
	
	// Method to check constraint...
	private boolean checkConstraint(A a){
		
		for (Map.Entry<Vehicle, LinkedList<Task_>> entry : a.getMap().entrySet()){

			Vehicle vehicle = entry.getKey();
			LinkedList<Task_> tasks = entry.getValue();
			HashMap<Task, Integer> map = new HashMap<Task, Integer>();
			
			if ( (tasks != null) && (!tasks.isEmpty()) ){
				int weight = 0;
				
				for (int i = 0 ; i < tasks.size(); i++){
					Task_ task_ = tasks.get(i);
					Task task = task_.getTask();
					
					//check condition : one task can not be taken by two vehicles
					if (! map.containsKey(task)){
						map.put(task, 0);
					} else {
						int temp = map.get(task) + 1;
						if (temp > 2) //there is another vehicle that delivery/pickup this task
							return false;
						else
							map.put(task, temp);
					}
				
					//check condition : capacity of a vehicle
					if (task_.getAction() == Action.PICKUP){
						weight += task.weight;
					} 
				}

				if (weight > vehicle.capacity())
					return false;
			}
		}
		return true;
	}
}
