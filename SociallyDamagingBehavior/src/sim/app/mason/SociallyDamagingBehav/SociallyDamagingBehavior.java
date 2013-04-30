
package sim.app.mason.SociallyDamagingBehav;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class SociallyDamagingBehavior extends SimState
{
	private static final long serialVersionUID = 1;
	/*SDB*/
	public static double DAMAGING_PAYOFF_PROB = 1.0;
	public static double DAMAGING_PAYOFF = 1.5;
	public static double SOCIAL_INFLUENCE = 0.010;

	public static double PUNISHIMENT_PROB = 1.0;
	public static Object PUNISHIMENT_STRICT = new Object();
	public static Object PUNISHIMENT_FAIR = new Object();
	public static Object PUNISHIMENT_LAX = new Object();
	public Object PUNISHIMENT_SEVERITY = PUNISHIMENT_FAIR;

	public static double HONEST_PAYOFF = 1.0;
	public static double HONEST_PROB = 1.0;
	public static int PERCENT_HONEST = 50;
	
	public static int EPOCH = 100;
	/*SDB*/

	public Continuous2D human_being;
	public double width = 150;
	public double height = 150;
	public int numHumanBeing = 1000;
	public double cohesion = 1.0;
	public double avoidance = 1.0;
	public double randomness = 1.0;
	public double consistency = 1.0;
	public double momentum = 1.0;
	//public double deadHumanProbability = 0.0;
	public double neighborhood = 10;
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
	public int getNumHuman() { return numHumanBeing; }
	public void setNumHuman(int val) { if (val >= 1) numHumanBeing = val; }
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }
	public double getNeighborhood() { return neighborhood; }
	public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }

	public Double2D[] getLocations()
	{
		if (human_being == null) return new Double2D[0];
		Bag b = human_being.getAllObjects();
		if (b==null) return new Double2D[0];
		Double2D[] locs = new Double2D[b.numObjs];
		for(int i =0; i < b.numObjs; i++)
			locs[i] = human_being.getObjectLocation(b.objs[i]);
		return locs;
	}

	public Double2D[] getInvertedLocations()
	{
		if (human_being == null) return new Double2D[0];
		Bag b = human_being.getAllObjects();
		if (b==null) return new Double2D[0];
		Double2D[] locs = new Double2D[b.numObjs];
		for(int i =0; i < b.numObjs; i++)
		{
			locs[i] = human_being.getObjectLocation(b.objs[i]);
			locs[i] = new Double2D(locs[i].y, locs[i].x);
		}
		return locs;
	}

	/** Creates a SDB simulation with the given random number seed. */
	public SociallyDamagingBehavior(long seed)
	{
		super(seed);
	}

	public void start()
	{
		super.start();
		
		this.schedule.scheduleRepeating(new NewGenAgent());

		// set up the human field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		human_being = new Continuous2D(neighborhood/1.5,width,height);

		// make a bunch of humans and schedule 'em.  
		
		int hon = (numHumanBeing*PERCENT_HONEST)/100;
		int disHon = numHumanBeing - hon;
		
		System.out.println("Honest="+hon+"     DisHon="+disHon);
		
		//Create Honest Agent
		for (int x=0;x<hon;x++) 
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/
			double dna=5+this.random.nextInt(4)+this.random.nextDouble(); //5<value<10
			
			Agent dhAgent = new Agent(location,this,dna);
			/*SDB*/
			human_being.setObjectLocation(dhAgent, location);
			dhAgent.humans = human_being;
			dhAgent.theHuman = this;
			schedule.scheduleRepeating(dhAgent);
		}
		
		//Create Dishonest Agent
		for(int x=0;x<disHon;x++)
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/
			
			double dna=this.random.nextInt(4)+this.random.nextDouble(); //0<value<5
			
			Agent dhAgent = new Agent(location,this,dna);
			/*SDB*/
			human_being.setObjectLocation(dhAgent, location);
			dhAgent.humans = human_being;
			dhAgent.theHuman = this;
			schedule.scheduleRepeating(dhAgent);
		}
	}
	
	/**
	 * Sceglie il tipo di azione da eseguire. Se il valore random Ž < del dna l'azione  onesta(1), 
	 * se il valore random  compreso tra il dna e 10 l'azione  disonesta(2)
	 * @param dna
	 * @return 1 se onesta, 2 se disonesta
	 */
	public int chooseAction(double dna)
	{
		return this.random.nextInt(10)+this.random.nextDouble()<dna?1:2;
	}
	
	public boolean tryHonestAgentAction()
	{
		return this.random.nextDouble()<HONEST_PROB?
				true:
					false;

	}
	public boolean tryDisHonestAgentAction()
	{
		return this.random.nextDouble()<DAMAGING_PAYOFF_PROB?
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
			else if(H_neigh<DH_neigh) prob_pun=(this.PUNISHIMENT_PROB-p_perc_dh);
			
		}
		double random_pun=this.random.nextDouble();
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
