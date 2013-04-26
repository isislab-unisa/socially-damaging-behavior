package sim.app.mason.SociallyDamagingBehav;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public abstract class Behaviour {

	public abstract void action(Agent agent,SimState state,Bag neigh);
//	public abstract Double2D move(Agent agent,SimState state,Double2D loc);
	public abstract Double2D consistency(Agent agent,Bag b, Continuous2D flockers);
	public abstract Double2D cohesion(Agent agent,Bag b, Continuous2D flockers);
	public abstract Double2D avoidance(Agent agent,Bag b, Continuous2D flockers);
}
