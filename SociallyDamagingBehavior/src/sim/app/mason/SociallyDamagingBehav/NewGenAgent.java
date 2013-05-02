package sim.app.mason.SociallyDamagingBehav;

import java.awt.Color;

import java.util.Comparator;

import ec.util.MersenneTwisterFast;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;


public class NewGenAgent implements Steppable{

	
	
	/**
	 * Return a random double value in range [-0.05,0.05]
	 *
	 * @return a double value 
	 */
	private static double delta(){
		
		MersenneTwisterFast a=new MersenneTwisterFast();
		double value=a.nextDouble()/2;
		double delta=value/10;
		double probability=a.nextDouble();
		if(probability<0.5)
			return delta;
		else 
			return delta*-1;
		
		
	}

	
	
	
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
			int riprodurre=(50*tot)/100;
			for (int i =0; i < all.size() ; i++) {
				
				Agent ra=(Agent)all.get(i);
				
				if(i<riprodurre)
				{
					//System.out.println(i+" "+ra.fitness);
					if(state.random.nextBoolean())
					{
						//double dna=ra.dna+(state.random.nextDouble()/10);
						double dna=ra.dna+NewGenAgent.delta();
						
						if(dna > 10) dna=10;
						else ra.dna=dna;
					}
//					else
//					{
//						double dna=ra.dna-(state.random.nextDouble()/10);
//						if(dna < 0) dna=0;
//						else ra.dna=dna;
//					}
				
					
					
				}/*else{
					double dna=state.random.nextInt(9)+state.random.nextDouble();
					ra.dna=dna;
				}*/
				ra.dead=false;
				//ra.fitness=state.random.nextInt(100);
				
				ra.behavior=(ra.dna>5)?new Honest():new Dishonest();
				ra.behav_color=(ra.dna>5)?Color.GREEN:Color.RED;	
			}	
		}
		
	}

}
