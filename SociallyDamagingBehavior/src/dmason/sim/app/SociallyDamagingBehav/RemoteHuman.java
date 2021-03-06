package dmason.sim.app.SociallyDamagingBehav;

import java.io.Serializable;
import sim.portrayal.simple.OvalPortrayal2D;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;

public abstract class RemoteHuman<E> extends OvalPortrayal2D implements Serializable,RemoteAgent<E>{

		private static final long serialVersionUID = 1L;
		public E pos;     // Location of agents  
		public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 
		
		public RemoteHuman() {}
		
		  /**
	     * Constructor of Remote Agent
		 * @param state the Distributed State of simulation
		 */
	    public RemoteHuman(DistributedState<E> state){
				int i=state.nextId();
				this.id=state.getType().toString()+"-"+i;		
		}

	    //getters and setters
	    @Override
		public E getPos() { return pos; }
	    @Override
		public void setPos(E pos) { this.pos = pos; }
	    @Override
		public String getId() {return id;	}
	    @Override
		public void setId(String id) {this.id = id;}	
}