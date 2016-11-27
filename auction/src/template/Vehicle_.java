package template;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

/*
 * This class is a similar with Vehicle class, but we can set the homeCity for the vehicle
 * */


public class Vehicle_ {
	
	private Vehicle mVehicle;
	private City mHomeCity;
	
	public Vehicle_(Vehicle v, City home){
		this.mVehicle = v;
		this.mHomeCity = home;
	}
	public void setVehicle(Vehicle that) {this.mVehicle = that;}
	
	public void setHomeCity(City that){ this.mHomeCity = that;}
	
	public City getHomeCity() { return this.mHomeCity;}
	
	public Vehicle getVehicle() { return this.mVehicle;}

}
