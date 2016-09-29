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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.activation.DataSource;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
	
		private static final int NUMAGENTS = 35; //the initial number of rabbits
		private static final int BIRTHTHRESHOLD = 15; //energy level at which the rabbits reproduce
		private static final int GRASSGROWRATE = 30; //the rate at which the grass grows
		private static final int INIT_GRASSENERGY = 10; 
		private static final int WORLDXSIZE = 20;
		private static final int WORLDYSIZE = 20;
		private static final int MAX_ENERGY_OF_GRASS = 500;

	
		private Schedule schedule;
		private int numAgents = NUMAGENTS;
		private int birthThreshold = BIRTHTHRESHOLD;
		private int grassGrowRate = GRASSGROWRATE;
		private int initGrassEnergy = INIT_GRASSENERGY;
		private int worldXSize = WORLDXSIZE;
		private int worldYSize = WORLDYSIZE;

		private RabbitsGrassSimulationSpace rgsSpace;
		private DisplaySurface displaySurf;
		private ArrayList<RabbitsGrassSimulationAgent> agentList;
		private OpenSequenceGraph graph;
		

		public static void main(String[] args) {
			SimInit init = new SimInit();
		    RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		    init.loadModel(model, "", false);	
		}
		
		
		public void setup() { // "2 curved arrows is pressed"
			this.rgsSpace = null;
			this.agentList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
			if (this.displaySurf != null){ //to remove the old stuff
				displaySurf.dispose();
			}
			
			if (this.graph != null){ //to remove the old stuff
				graph.dispose();
			}
			
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
			graph = new OpenSequenceGraph("Flot", this);
			
			registerDisplaySurface("Rabbits Grass Simulation windows", displaySurf);
			registerMediaProducer("Flot", graph);
		}

		public void begin() {// "Initialize" button
			buildModel();
			buildSchedule();
			buildDisplay();	
			displaySurf.display();
			graph.display();	
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

		public void buildSchedule(){
			
			// action of agent, grass
			class RabbitsGrassSimulationMove extends BasicAction {
				@Override
				public void execute() {
					// no agent
					if (0 == agentList.size())
						stop();
					
					// spread some grass
					rgsSpace.spreadGrass(grassGrowRate);
					// ????
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
					displaySurf.updateDisplay();
				}
			}
			schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationMove());
			
	        class Update extends BasicAction {
	            public void execute() {
	                graph.step();
	            }
	        }
	        schedule.scheduleActionAtInterval(10, new Update());
		}

		public void buildDisplay(){
			ColorMap map = new ColorMap();
			
			//map color for grass
			for(int i = 0; i < MAX_ENERGY_OF_GRASS ; i++){
				map.mapColor(i, Color.green);
			}
			
			//map color for background
			map.mapColor(0, Color.black);
			
			//interconnect 2 into 1
			Value2DDisplay displayGrass = new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
			Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
			displayAgents.setObjectList(agentList);
			
			displaySurf.addDisplayableProbeable(displayGrass, "Grass");
			displaySurf.addDisplayableProbeable(displayAgents, "Agents");
			
			graph.addSequence("Agent Count", new AgentCount());
			graph.addSequence("Grass Count", new GrassCount());
		}
		
		private boolean addNewAgent(){
			RabbitsGrassSimulationAgent agent = new RabbitsGrassSimulationAgent(-1,-1,this.initGrassEnergy);
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

		public Schedule getSchedule() {
			return schedule;
		}
		
		public int getNumAgents(){
			return this.numAgents;
		}
		
		public void setNumAgents(int num){
			this.numAgents = num;
		}
		
		public int getBirthThreshold(){
			return this.birthThreshold;
		}
		
		public void setBirthThreshold(int num){
			this.birthThreshold = num;
		}
		
		public int getGrassGrowRate(){
			return this.grassGrowRate;
		}
		
		public void setGrassGrowRate(int num){
			this.grassGrowRate = num;
		}
		
		public int getWorldXSize(){
		    return this.worldXSize;
		}

		public void setWorldXSize(int wxs){
		    this.worldXSize = wxs;
		}

		public int getWorldYSize(){
		    return this.worldYSize;	
		}

		public void setWorldYSize(int wys){
		    this.worldYSize = wys;
		}	
		
		
		class AgentCount implements DataSource, Sequence {

			public Object execute(){
				return new Double(getSValue());
			}
			
			@Override
			public double getSValue() {
				return agentList.size();
			}

			@Override
			public String getContentType() { return null; }
			@Override
			public InputStream getInputStream() throws IOException { return null; }
			@Override
			public String getName() { return null;}
			@Override
			public OutputStream getOutputStream() throws IOException { return null; }
			
		}
		
		class GrassCount implements DataSource, Sequence {
	        public Object execute() {
	            return new Double(getSValue());
	        }
	        @Override
	        public double getSValue() {
	            return (double)rgsSpace.getTotalGrass();
	        }
			@Override
			public String getContentType() { return null;}
			@Override
			public InputStream getInputStream() throws IOException { return null; }
			@Override
			public String getName() { return null; }
			@Override
			public OutputStream getOutputStream() throws IOException { return null;}
	    }
}
