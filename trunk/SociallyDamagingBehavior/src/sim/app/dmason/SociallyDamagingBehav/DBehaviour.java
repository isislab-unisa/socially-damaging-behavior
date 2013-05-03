package sim.app.dmason.SociallyDamagingBehav;

import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.continuous.DContinuous2D;

public abstract class DBehaviour {

	public abstract void action(DHuman agent, SimState state, Bag neigh);
	public abstract void calculateCEI(DHuman a, DSociallyDamagingBehavior sdb, Bag n);
	public abstract void socialInfluence(DHuman agent, DistributedState state,Bag neigh);
	public abstract Double2D move(DistributedState state, Double2D loc, Bag neigh);
	public abstract Double2D consistency(DHuman agent, Bag b, DContinuous2D humans);
	public abstract Double2D cohesion(DHuman agent, Bag b, DContinuous2D humans);
	public abstract Double2D avoidance(DHuman agent, Bag b, DContinuous2D humans);
}
