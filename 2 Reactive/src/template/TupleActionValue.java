package template;
/**
 * Class that having a tuple for storing (BestAction, BestValue) 
 * @author Cescnghia
 *
 */
public class TupleActionValue {
	
	private AgentAction agentAction;
	private double value;
	
	
	public TupleActionValue(AgentAction action, double v){
		this.agentAction = action;
		this.value = v;
	}
	
	public AgentAction getBestAction() { return this.agentAction; }
	public double getBestValue() {return this.value; }
	
	public void setBestAction(AgentAction action) {this.agentAction = action;}
	public void setBestValue(double v) {this.value = v;}
}
