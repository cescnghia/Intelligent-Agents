package template;

import logist.task.Task;

/**
 * 
 * @author Anh Nghia Khau and Stephane Cayssials
 * 
 * A task of a vehicle, one vehicle can carry multiple Task (a linked list of Task in our assignment)
 * 
 * Task is the task, and Action is either PICKUP or DELIVERY
 */

public class Task_ {

	public enum Action {PICKUP, DELIVERY}
	
	private Task mTask;
	private Action mAction;
	
	public Task_(Task t, Action g){
		this.mTask = t;
		this.mAction = g;
	}
	
	public Task getTask(){
		return this.mTask;
	}
	
	public Action getAction(){
		return this.mAction;
	}
	
	// The goal of this method is find the opposite Action (serve to remove all of two actions to another vehicle)
	public Task_ getInverse(){
		return new Task_(this.mTask, (this.mAction == Action.PICKUP) ? Action.DELIVERY : Action.PICKUP);
	}
	// For removing
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mTask == null) ? 0 : mTask.hashCode());
		result = prime * result + ((mAction == Action.PICKUP) ? 12312331 : 1221113);
		return result;
	}
	// For removing 
	@Override
	public boolean equals(Object o){
		Task_ s = (Task_)o;
		return ((s.mAction==this.mAction)&&(s.mTask==this.mTask));
	}
	
	@Override
	public String toString() {
		String a = (mAction == Action.PICKUP) ? " pickup " : " delivery " ;
		return "Action "+a+" task: "+mTask ;
	}
}
