package template;

import java.util.ArrayList;
import java.util.List;

import logist.task.Task;

public class Ennemy {

	private int mId;
	private int mMaxCapacity;
	private List<Long> mBids = new ArrayList<Long>();
	
	public Ennemy(int id) {
		this.mId = id;
	}
	
	public void addBid(Long bid, Task task) {
		this.mBids.add(bid);
		if(bid == null){
			updateMaxCapacity(task);
		}
	}
	
	public void updateMaxCapacity(Task task){
		this.mMaxCapacity = task.weight;
	}
	
}
