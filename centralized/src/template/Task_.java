package template;

import logist.task.Task;

/**
 * 
 * @author Anh Nghia Khau (223613)
 * 
 * A task of a vehicle, one vehicle can have multiple Task (a linked list of Task)
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
	
	public Task_ getInverse(){
		return new Task_(this.mTask, (this.mAction == Action.PICKUP) ? Action.DELIVERY : Action.PICKUP);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mTask == null) ? 0 : mTask.hashCode());
		result = prime * result + ((mAction == Action.PICKUP) ? 1231 : 1237);
		return result;
	}
	@Override
	public String toString() {
		String a = (mAction == Action.PICKUP) ? " pickup " : " delivery " ;
		return "Action "+a+"task: "+mTask ;
	}
}
