package sim.app.mason.SociallyDamagingBehav;

import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

public final class Behaviour {
	
	public static final Agent createAgent(Double2D location,SimState state,float dna){
		return (dna < 5)?new Honest(location, state, dna)
			:new Dishonest(location, state, dna);
	}
}
