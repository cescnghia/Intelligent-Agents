package template;

public class TupleActionValue {
	
	private AgentAction agentAction;
	private double value;
	
	
	public TupleActionValue(AgentAction action, double v){
		this.agentAction = action;
		this.value = v;
	}
	
	public AgentAction getAgentAction() { return this.agentAction; }
	public double getValue() {return this.value; }
	
	public void setAgentAction(AgentAction action) {this.agentAction = action;}
	public void setValue(double v) {this.value = v;}
}
