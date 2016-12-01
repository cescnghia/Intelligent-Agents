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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime* result + (!hasTask() ? 0 : this.destinationCity.hashCode()) ;
		result = prime* result + currentCity.hashCode();
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
		State other = (State) obj;
		if (hasTask() != other.hasTask())
			return false;
		if (currentCity == null){
			if(other.currentCity != null){
				return false;
			}
		}
		else if (!currentCity.equals(other.currentCity))
			return false;
		if (destinationCity == null){
			if(other.destinationCity != null){
				return false;
			}
		}
		else if (!destinationCity.equals(other.destinationCity))
			return false;
		return true;
	}
}
