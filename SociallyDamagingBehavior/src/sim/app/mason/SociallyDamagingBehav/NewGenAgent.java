package sim.app.mason.SociallyDamagingBehav;

import java.awt.Color;
import java.security.AllPermission;
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
		
		
			Bag all=flock.human_being.allObjects;
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
						double dna=ra.dna+state.random.nextDouble();
						if(dna > 10) dna=10;
						else ra.dna=dna;
					}else
					{
						double dna=ra.dna-state.random.nextDouble();
						if(dna < 0) dna=0;
						else ra.dna=dna;
					}
				
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
