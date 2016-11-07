package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import template.Task_.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class PickupDeliveryProblem {

	private List<Vehicle> mVehicles;
	private TaskSet mTasks;
	private A mBestA;
	// Total cost of this PickupDeliveryProblem, have to minimize !!!!!
	private double mCost;
	
	private static final double P = 0.4;
	private static final int MAX_ITER = 3000;
	
	public PickupDeliveryProblem(List<Vehicle> vehicles, TaskSet tasks){
		this.mVehicles = new ArrayList<Vehicle>(vehicles);
		this.mTasks = tasks;
		this.mCost = Integer.MAX_VALUE;
		this.mBestA = null;
	}
	
	public A getBestA(){
		return this.mBestA;
	}
	
	public double getCost(){
		return this.mCost;
	}
	
	// Give all the task for the biggest vehicle
	public A SelectInitialSolution(){
		
		Vehicle vehicle = null;
		LinkedList<Task_> tasks = new LinkedList<Task_>();
		HashMap<Vehicle, LinkedList<Task_>> map = new HashMap<Vehicle, LinkedList<Task_>>();
		
		int biggestCapacity = Integer.MIN_VALUE;
		for (Vehicle v : this.mVehicles){
			if (v.capacity() > biggestCapacity){
				biggestCapacity = v.capacity();
				vehicle = v;
			}
		}
		
		for (Task t : this.mTasks){
			tasks.add(new Task_(t, Action.PICKUP));
			tasks.add(new Task_(t, Action.DELIVERY));
		}
		
		map.put(vehicle, tasks);
		
		return new A(map);
	}
	
	public A StochasticLocalSearch(){
		
		A plan = SelectInitialSolution();
		int iter = MAX_ITER;
		
		for (int i = 0; i < iter ; i++){
			A oldPlan = plan;
			ArrayList<A> plans = ChooseNeighbors(oldPlan);
			plan = LocalChoice(oldPlan, plans);
		}
		
		return plan;
		
	}

	private A LocalChoice(A old, ArrayList<A> setOfPlan) {
		A choice = old;
		
		double minCost = Double.MIN_VALUE;
		A minCostPlan = null;
		for (A a : setOfPlan){
			double cost = a.cost();
			if (cost < minCost) {
				minCost = cost;
				minCostPlan = a;
			}
		}
		
		Random rd = new Random();
		double d = rd.nextDouble();
		
		if (d < P){
			choice = minCostPlan;
			if (minCost < this.mCost) {
				this.mBestA = minCostPlan;
				this.mCost = minCost;
			}
		} else {
			choice = old;
		}
		return choice;
	}

	private ArrayList<A> ChooseNeighbors(A old) {
		ArrayList<A> plans = new ArrayList<A>();
		Random rd = new Random();
		
		Vehicle vehicle = null;
		while (true){ // Find a vehicle that has a task
			vehicle = this.mVehicles.get(rd.nextInt(this.mVehicles.size()));
			if (! old.getTasksOfVehicle(vehicle).isEmpty())
				break;
		}
		
		//Applying the changing vehicle operator
		
		for (Vehicle v : this.mVehicles){
			if (v != vehicle && (old.getTasksOfVehicle(vehicle).get(0).getTask().weight < v.capacity())){
				A newA = ChangingVehicle(old, vehicle, v);
				if (checkConstraint(newA))
					plans.add(newA);
			}
		}
		
		//Applying the changing task order operator
		
		int numOfTask = old.getTasksOfVehicle(vehicle).size();
		if (numOfTask >= 2){
			for (int i = 1 ; i <= numOfTask-1 ; i++ ){
				for (int j = i+1; j < numOfTask ; j++){
					A newA = ChangingTaskOrder(old, vehicle, i, j);
					if (checkConstraint(newA))
						plans.add(newA);
				}
			}
		}
		
		return plans;
	}
	
	// Move 1st task of v1 to 1st task of v2
	private A ChangingVehicle(A a, Vehicle v1, Vehicle v2){
		A newA = new A(a);
		
		Task_ taskToMove = newA.getTasksOfVehicle(v1).getFirst();
		
		// Have to remove 2 actions (pickup and delivery) for this task in v1...
		newA.removeTaskFromVehicle(taskToMove, v1);
		newA.removeTaskFromVehicle(taskToMove.getInverse(), v1);
		
		// and add to the head of v2, add action delivery first...
		newA.addTaskForVehicle(taskToMove.getInverse(), v2);
		newA.addTaskForVehicle(taskToMove, v2);
		
		return newA;	
	}
	
	
	private A ChangingTaskOrder(A a, Vehicle v, int idx1, int idx2){
		
		A newA = new A(a);
		
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
		
		// Otherwise fell free to swap
		newA.changeOrderOfTwoTasks(idx1, idx2, v);
		
		return newA;
	}
	
	private boolean checkConstraint(A a){
		
		for (Map.Entry<Vehicle, LinkedList<Task_>> entry : a.getA().entrySet()){
			Vehicle vehicle = entry.getKey();
			LinkedList<Task_> tasks = entry.getValue();
			HashMap<Task, Integer> map = new HashMap<Task, Integer>();
			int weight = 0;
			
			if (tasks != null){
				for (int i = 0 ; i < tasks.size(); i++){
					Task task = tasks.get(i).getTask();
					
					//check condition : one task can not be taken by two vehicles
					if (! map.containsKey(task)){
						map.put(task, 0);
					} else {
						int temp = map.get(task) + 1;
						if (temp > 2) 
							return false;
						else
							map.put(task, temp);
					}
					
					//check condition : capacity of a vehicle
					if (tasks.get(i).getAction() == Action.PICKUP){
						weight += task.weight;
					}
					
				}
			}
			if (weight > vehicle.capacity())
				return false;		
		}
		return true;
	}
}
