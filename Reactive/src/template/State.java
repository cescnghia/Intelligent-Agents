package template;

import logist.topology.Topology.City;

public class State {

	private City currentCity;
	private City destinationCity;

	public State(City currentCity, City destinationCity){
		this.currentCity = currentCity;
		this.destinationCity = destinationCity;
	}
	
	public boolean hasTask(){
		return (this.destinationCity != null);
	}
	
	public City getCurrentCity() {return this.currentCity;}
	public City getDestinationCity() {return this.destinationCity;}
	
	public void setCurrentCity(City that) {this.currentCity = that;}
	public void setDestinationCity(City that) {this.destinationCity = that;}
}
