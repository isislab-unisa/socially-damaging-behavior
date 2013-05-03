package sim.app.mason.SociallyDamagingBehav;

import java.awt.Color;

import java.util.Comparator;

import ec.util.MersenneTwisterFast;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;


public class NewGenAgent implements Steppable{

	@Override
	public void step(SimState state) {
		final SociallyDamagingBehavior sdbState = (SociallyDamagingBehavior)state;
		if(state.schedule.getSteps()!=0 && state.schedule.getSteps()%sdbState.EPOCH==0)
		{
			Bag all=sdbState.human_being.allObjects;
			all.sort(new Comparator<Agent>() {
	
				@Override
				public int compare(Agent o1, Agent o2) {
					if(o1.fitness<o2.fitness) return 1;
					else if(o1.fitness>o2.fitness) return -1;
					return 0;
				}
			});
			int tot=all.size();
			
			boolean disp=false;
			int riprodurre=(50*tot)/100;
			
			if(tot%2==0)
				disp=false;
			else
				disp=true;
			
			for (int i =0; i < riprodurre ; i++) {
				
				Agent a1 = (Agent)all.get(i);
				Agent a2 = (Agent)all.get(i+riprodurre);
				
				if(i<riprodurre)
				{	
					double dna1 = a1.dna+NewGenAgent.delta(state);
					double dna2 = a1.dna+NewGenAgent.delta(state);
					
					if(dna1 > 10) dna1=10;
						else 
							if(dna1 < 0) dna1=0;
					if(dna2 > 10) dna2=10;
						else 
							if(dna2 < 0) dna2=0;
				
					Double2D loc1 = a1.loc;
					Double2D loc2 = a2.loc;
					
					if(i==riprodurre-1 && disp)
					{
						Agent a3 = (Agent)all.get(tot-1);
						double dna3 = a1.dna+NewGenAgent.delta(state);
						
						if(dna3 > 10) dna3=10;
							else 
								if(dna3 < 0) dna3=0;
						
						Double2D loc3 = a3.loc;
						a3 = new Agent(loc3, sdbState, dna3);
						a3.behavior=(a3.dna>5)?new Honest():new Dishonest();
						a3.behav_color=(a3.dna>5)?Color.GREEN:Color.RED;
					}
					
					a1 = new Agent(loc1, sdbState, dna1);
					a1.behavior=(a1.dna>5)?new Honest():new Dishonest();
					a1.behav_color=(a1.dna>5)?Color.GREEN:Color.RED;
					
					a2 = new Agent(loc2, sdbState, dna2);
					a2.behavior=(a2.dna>5)?new Honest():new Dishonest();
					a2.behav_color=(a2.dna>5)?Color.GREEN:Color.RED;
				}				
				
			}	
		}
		
	}
	
	private boolean createNewAgent(Agent ra, SociallyDamagingBehavior sdbState){
		
		Double2D location = ra.loc;
		double dna=sdbState.random.nextInt(9)+sdbState.random.nextDouble(); //0<value<5
		
		Agent agent = new Agent(location, sdbState, dna);
		
		ra.behavior=(ra.dna>5)?new Honest():new Dishonest();
		ra.behav_color=(ra.dna>5)?Color.GREEN:Color.RED;
		
		//human_being.setObjectLocation(dhAgent, location);
		return true;
	}
	
	/**
	 * Return a random double value in range [-0.05,0.05]
	 *
	 * @return a double value 
	 */
	private static double delta(SimState state){
		
		
		double value=state.random.nextDouble()/2;
		double delta=value/10;
		boolean probability=state.random.nextBoolean();
		
		if(probability)
			return delta;
		else 
			return delta*-1;
		
	}

}
