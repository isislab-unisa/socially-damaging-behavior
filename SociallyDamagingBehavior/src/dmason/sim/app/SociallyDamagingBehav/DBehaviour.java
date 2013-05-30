package dmason.sim.app.SociallyDamagingBehav;

import java.io.Serializable;
import dmason.sim.field.continuous.DContinuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public abstract class DBehaviour implements Serializable{
	
	public abstract void action(DHuman agent, DSociallyDamagingBehavior state, Bag neigh, Bag entryNeigh);
	public abstract void calculateCEI(DHuman a, DSociallyDamagingBehavior sdb, Bag n);
	public abstract void socialInfluence(DHuman agent, Bag neigh);
	public abstract Double2D move(DSociallyDamagingBehavior state, Double2D loc, Bag neigh);
	public abstract Double2D consistency(DHuman agent, Bag b, DContinuous2D humans);
	public abstract Double2D cohesion(DHuman agent, Bag b, DContinuous2D humans);
	public abstract Double2D avoidance(DHuman agent, Bag b, DContinuous2D humans);
	public abstract Double2D randomness(DSociallyDamagingBehavior state);
}