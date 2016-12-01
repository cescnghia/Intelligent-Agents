import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.util.SimUtilities;

import java.awt.Color;
import java.util.ArrayList;


/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 * Stephane Cayssials (272048)
 * Anh Nghia Khau (223613)
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
	
		private static final int NUMAGENTS = 35; //the initial number of rabbits
		private static final int BIRTHTHRESHOLD = 15; //energy level at which the rabbits reproduce
		private static final int GRASSGROWRATE = 30; //the rate at which the grass grows
		private static final int INIT_ENERGY_AGENT = 10; 
		private static final int WORLDXSIZE = 20;
		private static final int WORLDYSIZE = 20;
		private static final int MAX_ENERGY_OF_GRASS = 1000;

	
		private Schedule schedule;
		private int numAgents = NUMAGENTS;
		private int birthThreshold = BIRTHTHRESHOLD;
		private int grassGrowRate = GRASSGROWRATE;
		private int initEnergyAgent = INIT_ENERGY_AGENT;
		private int worldXSize = WORLDXSIZE;
		private int worldYSize = WORLDYSIZE;

		private RabbitsGrassSimulationSpace rgsSpace;
		private DisplaySurface displaySurf;
		private ArrayList<RabbitsGrassSimulationAgent> agentList;
		private OpenSequenceGraph agentGraph;
		private OpenSequenceGraph grassGraph;
		

		public static void main(String[] args) {
			SimInit init = new SimInit();
		    RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		    init.loadModel(model, "", false);	
		}
		
		
		public void setup() { // button 2 curve arrow
			this.rgsSpace = null;
			this.agentList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
			if (this.displaySurf != null){ //remove the old stuff
				displaySurf.dispose();
			}
			
			if (this.agentGraph != null){ //remove the old stuff
				agentGraph.dispose();
			}
			
			if (this.grassGraph != null){
				grassGraph.dispose();
			}
			
			//slider
			
			RangePropertyDescriptor sliderSizeX = new RangePropertyDescriptor("WorldXSize", 0, 100, 20);
			descriptors.put("WorldXSize", sliderSizeX);
			RangePropertyDescriptor sliderSizeY = new RangePropertyDescriptor("WorldYSize", 0, 100, 20);
			descriptors.put("WorldYSize", sliderSizeY);
			RangePropertyDescriptor agents = new RangePropertyDescriptor("NumAgents", 0, 500, 50);
			descriptors.put("NumAgents", agents);
			RangePropertyDescriptor grassRate = new RangePropertyDescriptor("GrassGrowRate", 0, 100, 15);
			descriptors.put("GrassGrowRate", grassRate);
			RangePropertyDescriptor birthThresh = new RangePropertyDescriptor("BirthThreshold", 0, 30, 6);
			descriptors.put("BirthThreshold", birthThresh);
			
			displaySurf = new DisplaySurface(this, "Rabbits Grass Simulation windows");
			agentGraph = new OpenSequenceGraph("AgentFlot", this);
			grassGraph = new OpenSequenceGraph("GrassFlot", this);
			
			registerDisplaySurface("Rabbits Grass Simulation windows", displaySurf);
			registerMediaProducer("AgentFlot", agentGraph);
			registerMediaProducer("GrassFlot", grassGraph );
		}

		public void begin() {// "Initialize" button
			buildModel();
			buildSchedule();
			buildDisplay();	
			displaySurf.display();
			agentGraph.display();	
			grassGraph.display();
		}
		
		public void buildModel(){
			this.rgsSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
			
			//build agent
			for(int i = 0; i < numAgents; i++){
				if (! addNewAgent()) {
					System.out.println("Cannot add some agents into space, you have to reduce number of agent !");
					stop();
				}
			}
		}

		/**
		 * Action of agent. For each simulation step, first we spread some grass and then for each agent:
		 * check if he has enough energy, if yes he can move else he has to die.
		 */
		public void buildSchedule(){
			// inner class for action of agent
			class RabbitsGrassSimulationMove extends BasicAction {
				@Override
				public void execute() {
					// no agent
					if (0 == agentList.size())
						stop();
					
					// spread some grass
					rgsSpace.spreadGrass(grassGrowRate);

					SimUtilities.shuffle(agentList);					
					for(int i = 0; i < agentList.size(); i++){
						RabbitsGrassSimulationAgent agent = agentList.get(i);
						
						if (agent.getEnergy() <= 0){// have to die
							rgsSpace.removeAgentAt(agent.getX(), agent.getY());
							agentList.remove(i);
						} else {
							agent.move();
							if (agent.getEnergy() >= birthThreshold){
								RabbitsGrassSimulationAgent child =	agent.reproduce();
								if (child != null)
									agentList.add(child);
							}
						}
					}
					// update new position of all agent and energy of grass 
					displaySurf.updateDisplay();
				}
			}
			schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationMove());
			
			//another inner class for graph
	        class Update extends BasicAction {
	            public void execute() {
	                agentGraph.step();
	                grassGraph.step();
	            }
	        }
	        schedule.scheduleActionAtInterval(10, new Update());
		}

		/**
		 * Choose color for displaying grass.
		 * We suppose that all grass have the same color
		 */
		public void buildDisplay(){
			ColorMap map = new ColorMap();
			
			//map color for grass (grass that has energy >= 1)
			for(int i = 1; i <= MAX_ENERGY_OF_GRASS ; i++){
				map.mapColor(i, Color.green);
			}
			
			//map color for background ("grass" that has energy = 0)
			map.mapColor(0, Color.black);
			
			Value2DDisplay displayGrass = new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
			Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
			displayAgents.setObjectList(agentList);
			
			displaySurf.addDisplayableProbeable(displayGrass, "Grass");
			displaySurf.addDisplayableProbeable(displayAgents, "Agents");
			
			agentGraph.addSequence("Agent Count", new AgentCount());
			grassGraph.addSequence("Grass Count", new GrassCount());
			
		}
		
		/**
		 * create an new agent with an initGrassEnergy and try to add into the space
		 * @return true if success
		 */
		private boolean addNewAgent(){
			RabbitsGrassSimulationAgent agent = new RabbitsGrassSimulationAgent(-1,-1,this.initEnergyAgent);
			this.agentList.add(agent); // Add to list
			return this.rgsSpace.addAgentIntoSpace(agent);	
		}
		

		public String[] getInitParam() {
			String[] initParams = {"NumAgents", "BirthThreshold", "GrassGrowRate", "WorldXSize", "WorldYSize"};
			return initParams;
		}
		
		
		public String getName() { // Name of the simulation model
			return "Rabbit Grass Simulation...";
		}

		public Schedule getSchedule() { return schedule; }
		
		public int getNumAgents(){ return this.numAgents; }
		
		public void setNumAgents(int num){ this.numAgents = num;}
		
		public int getBirthThreshold(){ return this.birthThreshold; }
		
		public void setBirthThreshold(int num){ this.birthThreshold = num; }
		
		public int getGrassGrowRate(){ return this.grassGrowRate; }
		
		public void setGrassGrowRate(int num){ this.grassGrowRate = num; }
		
		public int getWorldXSize(){  return this.worldXSize; }

		public void setWorldXSize(int wxs){ this.worldXSize = wxs; }

		public int getWorldYSize(){ return this.worldYSize;	}

		public void setWorldYSize(int wys){ this.worldYSize = wys; }	
		
		/**
		 * 
		 * Inner class implement interface Sequence for the plotting
		 * Method getSValue() return number of agent and this number will be plotted into graph
		 *
		 */
		class AgentCount implements Sequence {

			public Object execute(){
				return new Double(getSValue());
			}
	
			@Override
			public double getSValue() {
				return agentList.size();
			}
		}
		/**
		 * Inner class implement interface Sequence for the plotting
		 * Method getSValue() return number of total grass presented in the model
		 * This number will be plotted into graph
		 */
		class GrassCount implements Sequence {
			
			public Object execute() {
	            return new Double(getSValue());
	        }
			
	        @Override
	        public double getSValue() {
	            return (double)rgsSpace.getTotalGrass();
	        }
	    }
}
