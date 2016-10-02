import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author
 * Stephane Cayssials (272048)
 * Anh Nghia Khau (223613) 
 */

public class RabbitsGrassSimulationSpace {
	
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	// Supposed that in this project, size of grassSpace and agentSpace are the same
	private int xSize;
	private int ySize;
	private static final int INIT_NUM_GRASS = 30;
	
	/**
	 * Create a space for "agent" and a space for "grass".
	 * Distribute randomly some grass into space with a number INIT_NUM_GRASS of grass 
	 *  
	 * @param xSize: size of x-axis
	 * @param ySize: size of y-axis
	 */
	public RabbitsGrassSimulationSpace(int x, int y){
		if (x < 0 || y < 0)
			throw new IllegalArgumentException("Size of space can not be negative");
		
		this.xSize = x;
		this.ySize = y;
		grassSpace = new Object2DGrid(xSize, ySize);
		agentSpace = new Object2DGrid(xSize, ySize);
		for(int i = 0; i < xSize; i++){
			for(int j = 0; j < ySize; j++){
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
		spreadGrass(INIT_NUM_GRASS);
	}
	
	/**
	 * Distribute randomly grass into space 
	 * 
	 * @param numGrass: amount of grass to distribute
	 */
	public void spreadGrass(int numGrass){
		for(int i = 0; i < numGrass; i++){		
			//Choose coordinates to put grass into cell
			int x = (int)(Math.random()*(this.xSize));
		    int y = (int)(Math.random()*(this.ySize));
		    
		    //Update amount of grass 
		    grassSpace.putObjectAt(x, y, new Integer(getQuantityGrass(x, y) + 1));
		}
	}
	
	/**
	 * Get quantity of grass at a given point in space
	 * 
	 * @param x: coordinate of x-axis
	 * @param y: coordinate of y-axis
	 * @return quantity of grass at this point
	 */
	public int getQuantityGrass(int x, int y){
		if (!checkCoordinate(x, y))
			throw new IllegalArgumentException("Coordinate is not valide");
		int quantity = 0;
		if (grassSpace.getObjectAt(x,y)!= null){
			quantity = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
		}
		return quantity;
	}
	
	/**
	 * Get quantity at this point AND remove quantity of grass at this point
	 * 
	 * @param x: coordinate of x-axis
	 * @param y: coordinate of y-axis
	 * @return: quantity of grass at this point 
	 */
	public int getAndEatGrass(int x, int y){
		if (!checkCoordinate(x, y))
			throw new IllegalArgumentException("Coordinate is not valide");
		
		int quantity = getQuantityGrass(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return quantity;
	}
	
	public Object2DGrid getCurrentGrassSpace(){
		return this.grassSpace;
	}
	
	public Object2DGrid getCurrentAgentSpace(){
		return this.agentSpace;
	}
	
	/**
	 * Moving agent from old point to new point, coordinate is already checked on method move()
	 * 
	 * 
	 * @param oldX: old coordinate of x-axis
	 * @param oldY: old coordinate of y-axis
	 * @param newX: new coordinate of x-axis
	 * @param newY: new coordinate of y-axis
	 */
	public void moveAgent(int oldX, int oldY, int newX, int newY){ 
			RabbitsGrassSimulationAgent agentToMove = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(oldX, oldY);
			removeAgentAt(oldX, oldY);
			agentToMove.setPosition(newX, newY);
			agentSpace.putObjectAt(newX, newY, agentToMove);
	}

	/**
	 * Method for checking if the cell is occupied
	 * 
	 * @param x: coordinate of x-axis
	 * @param y: coordinate of y-axis
	 * @return true if the cell is occupied
	 */
	
	public boolean isCellOccupied(int x, int y){
		if (!checkCoordinate(x, y))
			throw new IllegalArgumentException("Coordinate is not valide");
		
		boolean retVal = false;
		if (agentSpace.getObjectAt(x, y) != null){
			retVal = true;
		}
		return retVal;
	}
	
	/**
	 * Try to add an agent into space randomly with a limit number of time
	 * 
	 * @param agent: agent that we have to add
	 * @return true if success
	 */
	public boolean addAgentIntoSpace(RabbitsGrassSimulationAgent agent){
		boolean retVal = false;
		int count = 0;
		// try to add with a countLimit times
		int countLimit = 10*this.xSize*this.ySize;
		
		while((! retVal) && (count < countLimit)){
		      int x = (int)(Math.random()*(this.xSize));
		      int y = (int)(Math.random()*(this.ySize));
		      if (! isCellOccupied(x,y)){
		        agentSpace.putObjectAt(x,y,agent);
		        agent.setPosition(x, y);
		        agent.setRabbitsGrassSimulationSpace(this);
		        retVal = true;
		      }
		      count++;
		    }
		return retVal;
	}
	
	/**
	 * Try to remove an agent from the space
	 * 
	 * @param x: coordinate of x-axis
	 * @param y: coordinate of y-axis
	 */
	public void removeAgentAt(int x, int y){
		if (!checkCoordinate(x, y))
			throw new IllegalArgumentException("Coordinate is not valide");
		
		agentSpace.putObjectAt(x, y, null);
	}
	
	/**
	 * Get total amount of grass, have to go throw each cell
	 * 
	 * @return amount of grass
	 */
	public int getTotalGrass(){
		int total = 0;
		for (int i = 0; i < grassSpace.getSizeX(); i++){
			for ( int j = 0; j < grassSpace.getSizeY(); j++){
				total += getQuantityGrass(i, j);
			}
		}
		return total;
	}
	
	/**
	 * Check coordinate of one point in space
	 * 
	 * @param x: coordinate of x-axis
	 * @param y: coordinate of y-axis
	 * @return true if that coordinate is on the space 
	 */
	public boolean checkCoordinate(int x, int y){
		return ((x >= 0 && x < this.xSize ) && (y >= 0 && y < this.ySize));
	}
	
	public int getXSize() { return this.xSize;}
	public int getYSize() { return this.ySize;}
}
