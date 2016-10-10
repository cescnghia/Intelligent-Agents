package template;

import logist.topology.Topology.City;

public class AgentAction{
	/*
	 * MOVE : moving to a city for finding some task OR delivering a task
	 * PICKUP : moving to a city for pickup some task
	 * 
	 */
	public enum Action {MOVE, PICKUP, DELIVER}
	
	private City destinationCity;
	private Action action;
	
	public AgentAction(City destination, Action action){
		this.destinationCity = destination;
		this.action = action;
	}
	
	public boolean isMove(){
		return (this.action == Action.MOVE);
	}
	
	public boolean isPickup(){
		return (this.action == Action.PICKUP);
	}
	
	public boolean isDeliver(){
		return (this.action == Action.DELIVER) ;
	}
	
	public City getDestination() {return this.destinationCity;}
	public Action getAction() {return this.action;}
	
	public void setDestinantion(City that) {this.destinationCity = that;}
	public void setAction(Action that) {this.action = that;}
	
	
	
	
	
}
