package sim.app.mason.SociallyDamagingBehav;

import java.util.Comparator;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;

public class NewGenAgent implements Steppable{

	@Override
	public void step(SimState state) {
		final SociallyDamagingBehavior flock = (SociallyDamagingBehavior)state;
		if(state.schedule.getSteps()!=0 && state.schedule.getSteps()%flock.EPOCH==0)
		{
			System.out.println("NUOVA EPOCA!"+state.schedule.getSteps());
		
			Bag all=flock.flockers.allObjects;
			all.sort(new Comparator<Agent>() {
	
				@Override
				public int compare(Agent o1, Agent o2) {
					if(o1.fitness<o2.fitness) return -1;
					else if(o1.fitness>o2.fitness) return 1;
					return 0;
				}
			});
			int tot=all.size();
			int riprodurre=(25*tot)/100;
			
			for (int i = 0; i < all.size(); i++) {
				
				Agent ra=(Agent)all.get(i);
				ra.dead=false;
				if(i<riprodurre)
				{
					
					if(state.random.nextBoolean())
					{
						float dna=ra.dna+state.random.nextFloat();
						if(dna > 10) dna=10;
						else ra.dna=dna;
					}else
					{
						float dna=ra.dna-state.random.nextFloat();
						if(dna < 0) dna=0;
						else ra.dna=dna;
					}
					
					ra.fitness=state.random.nextInt(100);
					
				}else{
					float dna=state.random.nextInt(10)+state.random.nextFloat();
					ra.fitness=state.random.nextInt(100);
					ra.dna=dna;
					//ra=new Agent(ra.loc, state, dna);
				}
				
				
			}
		}
		
	}

}
