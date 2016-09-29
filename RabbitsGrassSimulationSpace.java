import uchicago.src.sim.space.Object2DTorus;


/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	
	private Object2DTorus grassSpace;
	private Object2DTorus agentSpace;
	private static final int INIT_NUM_GRASS = 30;
	
	// Constructor
	public RabbitsGrassSimulationSpace(int xSize, int ySize){
		grassSpace = new Object2DTorus(xSize, ySize);
		agentSpace = new Object2DTorus(xSize, ySize);
		for(int i = 0; i < xSize; i++){
			for(int j = 0; j < ySize; j++){
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
		spreadGrass(INIT_NUM_GRASS);
	}
	
	public void spreadGrass(int numGrass){
		for(int i = 0; i < numGrass; i++){		
			//Choose coordinates to put grass into cell
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
		    int y = (int)(Math.random()*(grassSpace.getSizeY()));
		    
		    //Update amount of grass 
		    grassSpace.putObjectAt(x, y, new Integer(getAmountGrass(x, y) + 1));
		}
	}
	
	//Check and return amount of grass at those coordinates  
	public int getAmountGrass(int x, int y){
		int amount = 0;
		if (grassSpace.getObjectAt(x,y)!= null){
			amount = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
		}
		return amount;
	}
	
	public int getAndEatGrass(int x, int y){
		int amount = (Integer) grassSpace.getObjectAt(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return amount;
	}
	
	public Object2DTorus getCurrentGrassSpace(){
		return this.grassSpace;
	}
	
	public Object2DTorus getCurrentAgentSpace(){
		return this.agentSpace;
	}
	
	// move agent to new position in space, already checked for new coordinates !!!
	public void moveAgent(int oldX, int oldY, int newX, int newY){ 
			RabbitsGrassSimulationAgent agentToMove = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(oldX, oldY);
			removeAgentAt(oldX, oldY);
			agentToMove.setPosition(newX, newY);
			agentSpace.putObjectAt(newX, newY, agentToMove);
	}
	
	public boolean isCellOccupied(int x, int y){
		boolean retVal = false;
		if (agentSpace.getObjectAt(x, y) != null){
			retVal = true;
		}
		return retVal;
	}
	
	public boolean addAgentIntoSpace(RabbitsGrassSimulationAgent agent){
		boolean retVal = false;
		int count = 0;
		// try to add with a countLimit times
		int countLimit = 10*agentSpace.getSizeX()*agentSpace.getSizeY();
		
		while((! retVal) && (count < countLimit)){
		      int x = (int)(Math.random()*(agentSpace.getSizeX()));
		      int y = (int)(Math.random()*(agentSpace.getSizeY()));
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
	
	public void removeAgentAt(int x, int y){
		agentSpace.putObjectAt(x, y, null);
	}
	
	public int getTotalGrass(){
		int total = 0;
		for (int i = 0; i < grassSpace.getSizeX(); i++){
			for ( int j = 0; j < grassSpace.getSizeY(); j++){
				total += getAmountGrass(i, j);
			}
		}
		return total;
	}
}
