package sim.app.SociallyDamagingBehav;
import java.util.Comparator;
import dmason.util.Util;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * 
 * Genetic algorithm
 *
 */
public class NewGenAgent implements Steppable{

	private static final long serialVersionUID = 1L;

	@Override
	public void step(SimState state) {
		
		final SociallyDamagingBehavior sdbState = (SociallyDamagingBehavior)state;
		
		if(state.schedule.getSteps()!=0 && state.schedule.getSteps()%SociallyDamagingBehavior.EPOCH==0)
		{
			
			sdbState.honest=0;
			sdbState.dishonest=0;
			
			Bag all=(Bag)Util.clone(sdbState.human_being.allObjects);
			sdbState.totalFitness = 0;
			all.sort(new Comparator<Human>() {

				@Override
				public int compare(Human o1, Human o2) {
					if(o1.fitness<o2.fitness) return 1;
					else if(o1.fitness>o2.fitness) return -1;
					return 0;
				}
			});
			int tot=all.size();
			
			Bag nuovi = new Bag();

			boolean disp=false;
			int riprodurre=(50*tot)/100;

			if(tot%2==0)
				disp=false;
			else
				disp=true;
			
			for (int i =0; i < riprodurre ; i++) {

				Human a1 = (Human)all.get(i);
				Human a2 = (Human)all.get(i+riprodurre);
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
					Human a3 = (Human)all.get(tot-1);
					double dna3 = a1.dna+NewGenAgent.delta(state);

					if(dna3 > 10) dna3=10;
					else 
						if(dna3 < 0) dna3=0;

					Double2D loc3 = a3.loc;
					a3 = new Human(loc3, sdbState, dna3);
				
					all.set(i, a3);
				}
				
				Human n1 = new Human(loc1, sdbState, dna1);
				nuovi.add(n1);
				Human n2 = new Human(loc2, sdbState, dna2);
				nuovi.add(n2);
			}	
			sdbState.human_being.clear();
			
			for(Object o : nuovi)
			{
				Human h = (Human)o;
				
				if(h.behavior instanceof Honest)
					sdbState.honest++;else sdbState.dishonest++;
				
				
				sdbState.human_being.setObjectLocation(h, h.loc);
				sdbState.schedule.scheduleOnce(h);
			}
		}
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
