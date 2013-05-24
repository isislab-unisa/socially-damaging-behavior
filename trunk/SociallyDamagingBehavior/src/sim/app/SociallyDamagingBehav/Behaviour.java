package sim.app.SociallyDamagingBehav;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * An abstract class for honest and dishonest agents 
 *
 */
public abstract class Behaviour {

	
	public abstract void action(Human agent,SociallyDamagingBehavior state,Bag neigh);
	public abstract void calculateCEI(Human a, SociallyDamagingBehavior sdb, Bag n);
	public abstract void socialInfluence(Human agent,SimState state,Bag neigh);
	public abstract Double2D move(SimState state, Double2D loc, Bag neigh);
	public abstract Double2D consistency(Human agent,Bag b, Continuous2D humans);
	public abstract Double2D cohesion(Human agent,Bag b, Continuous2D humans);
	public abstract Double2D avoidance(Human agent,Bag b, Continuous2D humans);
	public abstract Double2D randomness(SociallyDamagingBehavior state);
}
