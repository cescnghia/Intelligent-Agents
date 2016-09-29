import java.awt.Color;
import java.util.ArrayList;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * Class that specify the agents

 * @author
 */

public class RabbitsGrassSimulationAgent  implements Drawable {
	
	private int x;
	private int y;
	private int energy;
	private int id;
	private static int COUNTER = 0;
	RabbitsGrassSimulationSpace rgsSpace;
	
	public RabbitsGrassSimulationAgent(int x, int y, int energy){
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.id = COUNTER++;
	}

	public void draw(SimGraphics g) {
		g.drawCircle(Color.white);	
	}

	public int getX() {	return this.x; }
	public void setX(int newX) { this.x = newX; }

	public int getY() { return this.y; }
	public void setY(int newY) { this.y = newY; }
	
	public int getEnergy() { return this.energy; }
	public void addEnergy(int amount) { this.energy += amount ;}
	
	public int getID() { return this.id; }

	public void setPosition(int newX, int newY){
		this.x = newX;
		this.y = newY;
	}
	
	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace space){
		this.rgsSpace = space;
	}
	

	public void move(){
		
		ArrayList<String> toGo = new ArrayList<String>();
		if (! rgsSpace.isCellOccupied(x+1,y))
			toGo.add("East");
		if (! rgsSpace.isCellOccupied(x-1,y))
			toGo.add("West");
		if (! rgsSpace.isCellOccupied(x,y+1))
			toGo.add("North");
		if (! rgsSpace.isCellOccupied(x,y-1))
			toGo.add("South");
		
		int newX = x, newY = y;
		if (! toGo.isEmpty()){
			int i = (int)(Math.random()*toGo.size());
			
			if (toGo.get(i).equals("East")){
				newX += 1; 
			} else if (toGo.get(i).equals("West")){
				newX -= 1;
			} else if (toGo.get(i).equals("North")){
				newY += 1;
			} else {
				newY -= 1;
			}
			newX = (newX +(rgsSpace.getCurrentAgentSpace().getSizeX())) % rgsSpace.getCurrentAgentSpace().getSizeX() ;
			newY = (newY +(rgsSpace.getCurrentAgentSpace().getSizeY())) % rgsSpace.getCurrentAgentSpace().getSizeY() ;
			
			rgsSpace.moveAgent(x, y, newX, newY);
			this.addEnergy(rgsSpace.getAndEatGrass(x, y));
			//suppose that each time agent moves, he loses energy
			this.energy -= 1;
		}
	}
	
	public RabbitsGrassSimulationAgent reproduce(){
			int sharedEnergy = this.energy/2;
			this.energy -= sharedEnergy;
			RabbitsGrassSimulationAgent child = new RabbitsGrassSimulationAgent(-1, -1, sharedEnergy);
			child.rgsSpace = this.rgsSpace;
			if (rgsSpace.addAgentIntoSpace(child))
				return child;
			else 
				return null;
			
	}

}
