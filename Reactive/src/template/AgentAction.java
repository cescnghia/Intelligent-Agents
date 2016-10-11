package template;

import logist.topology.Topology.City;

public class AgentAction{
	/*
	 * MOVE : moving to a neighbor city for finding some task
	 * PICKUP : moving to a city for pickup some task
	 * 
	 */
	public enum Action {MOVE, PICKUP}
	
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
	
	public City getDestination() {return this.destinationCity;}
	public Action getAction() {return this.action;}
	
	public void setDestinantion(City that) {this.destinationCity = that;}
	public void setAction(Action that) {this.action = that;}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
	
		result = prime * result
				+ ((destinationCity == null) ? 0 : destinationCity.hashCode());
		result = prime * result + (isPickup() ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentAction other = (AgentAction) obj;

		if (destinationCity == null) {
			if (other.destinationCity != null)
				return false;
		} else if (!destinationCity.equals(other.destinationCity))
			return false;
		if (isPickup() != other.isPickup())
			return false;
		return true;
	}
	
	
	
}
