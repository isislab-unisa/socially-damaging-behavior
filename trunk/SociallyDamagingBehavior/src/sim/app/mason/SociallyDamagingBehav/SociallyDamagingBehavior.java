
package sim.app.mason.SociallyDamagingBehav;
import java.awt.Color;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class SociallyDamagingBehavior extends SimState
{
	private static final long serialVersionUID = 1;
	/*SDB*/
	public static double DAMAGING_PAYOFF_PROB=0.5;
	public static double DAMAGING_PAYOFF=2.5;

	public static double PUNISHIMENT_PROB=0.5;
	public static Object PUNISHIMENT_STRICT=new Object();
	public static Object PUNISHIMENT_FAIR=new Object();
	public static Object PUNISHIMENT_LAX=new Object();
	public Object PUNISHIMENT_SEVERITY=PUNISHIMENT_STRICT;

	public static double HONEST_PAYOFF=5;
	public static double HONEST_PROB=0.5;
	
	public static int EPOCH=500;
	/*SDB*/

	public Continuous2D flockers;
	public double width = 150;
	public double height = 150;
	public int numFlockers = 200;
	public double cohesion = 1.0;
	public double avoidance = 1.0;
	public double randomness = 1.0;
	public double consistency = 1.0;
	public double momentum = 1.0;
	public double deadFlockerProbability = 0.0;
	public double neighborhood = 40;
	public double jump = 0.7;  // how far do we move in a timestep?

	public double getCohesion() { return cohesion; }
	public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
	public double getAvoidance() { return avoidance; }
	public void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
	public double getRandomness() { return randomness; }
	public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
	public double getConsistency() { return consistency; }
	public void setConsistency(double val) { if (val >= 0.0) consistency = val; }
	public double getMomentum() { return momentum; }
	public void setMomentum(double val) { if (val >= 0.0) momentum = val; }
	public int getNumFlockers() { return numFlockers; }
	public void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }
	public double getNeighborhood() { return neighborhood; }
	public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
	public double getDeadFlockerProbability() { return deadFlockerProbability; }
	public void setDeadFlockerProbability(double val) { if (val >= 0.0 && val <= 1.0) deadFlockerProbability = val; }

	public Double2D[] getLocations()
	{
		if (flockers == null) return new Double2D[0];
		Bag b = flockers.getAllObjects();
		if (b==null) return new Double2D[0];
		Double2D[] locs = new Double2D[b.numObjs];
		for(int i =0; i < b.numObjs; i++)
			locs[i] = flockers.getObjectLocation(b.objs[i]);
		return locs;
	}

	public Double2D[] getInvertedLocations()
	{
		if (flockers == null) return new Double2D[0];
		Bag b = flockers.getAllObjects();
		if (b==null) return new Double2D[0];
		Double2D[] locs = new Double2D[b.numObjs];
		for(int i =0; i < b.numObjs; i++)
		{
			locs[i] = flockers.getObjectLocation(b.objs[i]);
			locs[i] = new Double2D(locs[i].y, locs[i].x);
		}
		return locs;
	}

	/** Creates a Flockers simulation with the given random number seed. */
	public SociallyDamagingBehavior(long seed)
	{
		super(seed);
	}

	public void start()
	{
		super.start();
		
		this.schedule.scheduleRepeating(new NewGenAgent());

		// set up the flockers field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		flockers = new Continuous2D(neighborhood/1.5,width,height);

		// make a bunch of flockers and schedule 'em.  A few will be dead
		for(int x=0;x<numFlockers;x++)
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/
			float dna=this.random.nextInt(10)+this.random.nextFloat();
			//Agent flocker =dna<5?new Honest(location,this,dna):new Dishonest(location,this,dna);
			
			Agent flocker = new Agent(location,this,dna);
			/*SDB*/
			flockers.setObjectLocation(flocker, location);
			flocker.flockers = flockers;
			flocker.theFlock = this;
			schedule.scheduleRepeating(flocker);
		}
	}

	/*SDB*/
	public  boolean askHonestAgentAction(float dna)
	{
		return this.random.nextFloat()+this.random.nextInt(10)<dna?
				true:
					false;

	}
	public  boolean tryHonestAgentAction()
	{
		return this.random.nextFloat()<HONEST_PROB?
				true:
					false;

	}
	public  boolean tryDisHonestAgentAction()
	{
		return this.random.nextFloat()<1-HONEST_PROB?
				true:
					false;

	}
	public void legalPunishment(Agent a,Bag neigh)
	{
		
		double prob_pun=this.PUNISHIMENT_PROB;
		if(neigh.size()>0)
		{
			int H_neigh=0;
			int DH_neigh=0;
			for(Object o:neigh)
			{
				Agent n_a=(Agent)o;
				if(n_a.behavior instanceof Honest) H_neigh++;
				else DH_neigh++;
				
			}
			int tot=H_neigh+DH_neigh;
			double perc_H=(H_neigh*100)/tot;
			double perc_DH=(DH_neigh*100)/tot;
			double p_perc_h=perc_H/100;
			double p_perc_dh=perc_DH/100;
			if(H_neigh>DH_neigh) prob_pun=this.PUNISHIMENT_PROB+p_perc_h;
			else if(H_neigh<DH_neigh) prob_pun=this.PUNISHIMENT_PROB-p_perc_dh;
			
		}
		float random_pun=this.random.nextFloat();
		if(random_pun < prob_pun)
		{
			
			if(PUNISHIMENT_SEVERITY.equals(PUNISHIMENT_FAIR))
			{
				a.fitness-=DAMAGING_PAYOFF;
			}else
				if(PUNISHIMENT_SEVERITY.equals(PUNISHIMENT_STRICT))
				{
					a.fitness-=DAMAGING_PAYOFF*2;
				}else
				{
					a.fitness-=DAMAGING_PAYOFF/2;
				}
				
		}

	}
	/*SDB*/
	public static void main(String[] args)
	{
		doLoop(SociallyDamagingBehavior.class, args);
		System.exit(0);
	}    
}
