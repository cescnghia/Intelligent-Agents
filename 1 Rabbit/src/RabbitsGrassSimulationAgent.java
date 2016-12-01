import java.awt.Color;
import java.util.ArrayList;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * Class that specify the agents

 * @author
 * Stephane Cayssials (272048)
 * Anh Nghia Khau (223613)
 */

public class RabbitsGrassSimulationAgent  implements Drawable {
	
	private int x;
	private int y;
	private int energy;
	private int id;
	private static int COUNTER = 0;
	RabbitsGrassSimulationSpace rgsSpace;
	
	/**
	 * Create an agent with a unique ID via a static variable COUNTER 
	 * 
	 * @param x: position of this agent on x-axis
	 * @param y: position of this agent on y-axis
	 * @param energy: number of energy
	 */
	
	public RabbitsGrassSimulationAgent(int x, int y, int energy){
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.id = COUNTER++;
	}
	
	/**
	 * Form of an agent  
	 */
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
	
	/**
	 * First we find all possibilities that agent can move and after choose direction randomly,
	 *  then move agent to new cell and he can earn some energy if he find some grass.
	 * Agent will not move if all four cell (NEWS) are occupied. 
	 * We supposed that for each move, agent loses one unit of energy.(If not, agent has never died).
	 */
	
	public void move(){
		int xSize = rgsSpace.getXSize();
		int ySize = rgsSpace.getYSize();

		// Check for all neighbor that agent can go
		// Attention with java: -13 modulo 15 = -13 !!!! 
		ArrayList<String> toGo = new ArrayList<String>();
		if (! rgsSpace.isCellOccupied((x+1)%xSize,y))
			toGo.add("East");
		if (! rgsSpace.isCellOccupied((x-1+xSize)%xSize,y))
			toGo.add("West");
		if (! rgsSpace.isCellOccupied(x,(y+1)%ySize))
			toGo.add("North");
		if (! rgsSpace.isCellOccupied(x,((y-1+ySize)%ySize)))
			toGo.add("South");
		
		int newX = x, newY = y;
		if (! toGo.isEmpty()){
			int i = (int)(Math.random()*toGo.size());
			String direction = toGo.get(i);
			
			if (direction.equals("East")){
				newX += 1; 
			} else if (direction.equals("West")){
				newX -= 1;
			} else if (direction.equals("North")){
				newY += 1;
			} else {
				newY -= 1;
			}
			// Torus
			newX = (newX + xSize) % xSize;
			newY = (newY + ySize) % ySize;
			
			rgsSpace.moveAgent(x, y, newX, newY);
			this.addEnergy(rgsSpace.getAndEatGrass(x, y));
			//suppose that each time agent moves, he loses energy
			this.energy -= 1;
		}
	}
	
	/**
	 * When an agent reproduces, he has to share a half of his energy for his child, then we create a new child and try to add it into rgsSpace.
	 * 
	 * @return a RabbitsGrassSimulationAgent if we can put the child into rgsSpace, null otherwise.
	 */
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
