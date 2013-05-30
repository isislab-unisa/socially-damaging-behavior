package sim.app.SociallyDamagingBehav;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class SociallyDamagingBehavior extends SimState
{
	private static final long serialVersionUID = 1;
	
	//logger
	public boolean logging = false;
	public FileOutputStream file;
	public PrintStream ps;
	
	/*SDB*/
	public static int numHumanBeing = 1000;
	public static double width = 200;
	public static double height = 200;
	public static int EPOCH = 100;

	public static int MODEL0_RANDOM_DAMAGING=0;
	public static int MODEL1_PROPORTIONAL_DAMAGING=1;
	public static int MODEL2_RANDOM_MOVEMENT=2;
	public static int MODEL3_AGGREGATION_MOVEMENT=3;
	public static int MODEL4_MEMORY=4;
	public static int MODEL = MODEL4_MEMORY;
	public static double neighborhood = 10;
	public static double MIN_AOI_AGGREGATION_MODEL3 = 5;
	public static double MAX_AOI_AGGREGATION_MODEL3 = 10;
	public static double RHO_MODEL4_MEMORY = 0.2;

	public static double DAMAGING_PAYOFF_PROB = 1.0;
	public static double DAMAGING_PAYOFF = 1.5;
	public static double SOCIAL_INFLUENCE = 0.010;
	public static int PERCENTAGE_PAYOFF_FITNESS=10;

	public static double PUNISHIMENT_PROB = 1.0;
	public static int PUNISHIMENT_STRICT = 1; 
	public static int PUNISHIMENT_FAIR = 2;
	public static int PUNISHIMENT_LAX = 3;
	public int PUNISHIMENT_SEVERITY = PUNISHIMENT_FAIR;

	public static double HONEST_PAYOFF = 1.0;
	public static double HONEST_PROB = 1.0;
	public static int PERCENT_HONEST = 50;
	
	public static final int HONEST_ACTION=1;
	public static final int DISHOSNEST_ACTION=2;
	/*SDB*/

	public Continuous2D human_being;
	public double cohesion = 1.0;
	public double avoidance = 1.0;
	public double randomness = 1.0;
	public double consistency = 1.0;
	public double momentum = 1.0;
	public double jump = 0.7;  // how far do we move in a timestep?
	public double totalFitness = 0;
	public double lastTotalFitness = 0;
	public Bag allHumans;
	public Bag lastAllHumans;
	public int honestAction = 0;
	public int numHonestAction = 0;
	public int numDishonestAction = 0;
	public int dishonestAction = 0;
	public int honest = 0;
	public int numHonest = 0;
	public int dishonest = 0;
	public int numDishonest = 0;
	

	/** Creates a SDB simulation with the given random number seed. */
	public SociallyDamagingBehavior(long seed)
	{
		super(0);
	}

	@Override
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

		//file logging
		if(logging)
			try {
				file = new FileOutputStream("Model="+MODEL+"_NumAgent="+numHumanBeing+"_Width="+width+"_Height="+height+".txt");
				ps = new PrintStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		// make a bunch of humans and schedule 'em.  

	    honest = (numHumanBeing*PERCENT_HONEST)/100;
	    dishonest = numHumanBeing - honest;
		
		//System.out.println("Honest="+hon+"     DisHon="+disHon);
		allHumans = new Bag();

		//Create Honest Agent
		for (int x=0;x<honest;x++) 
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/
			double dna=5+this.random.nextInt(4)+this.random.nextDouble(); //5<value<10

			Human hAgent = new Human(location,this,dna);
			/*SDB*/
			human_being.setObjectLocation(hAgent, location);
			hAgent.humans = human_being;
			hAgent.theHuman = this;

			allHumans.add(new EntryAgent<Double, Human>(0.0, hAgent));//////Model 2-3

			//schedule.scheduleRepeating(hAgent);
			schedule.scheduleOnce(hAgent);
		}

		//Create Dishonest Agent
		for(int x=0;x<dishonest;x++)
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/

			double dna=this.random.nextInt(4)+this.random.nextDouble(); //0<value<5

			Human dhAgent = new Human(location,this,dna);
			/*SDB*/
			human_being.setObjectLocation(dhAgent, location);
			dhAgent.humans = human_being;
			dhAgent.theHuman = this;

			allHumans.add(new EntryAgent<Double, Human>(0.0, dhAgent));//////Model 2-3

			schedule.scheduleOnce(dhAgent);
		}

		//////Model 2-3
		allHumans.sort(new Comparator<EntryAgent<Double, Human>>() {
			@Override
			public int compare(EntryAgent<Double, Human> o1, EntryAgent<Double, Human> o2) {
				if(o1.getFitSum()>o2.getFitSum()) return 1;
				else if(o1.getFitSum()<o2.getFitSum()) return -1;
				return 0;
			}
		});

		for(Object o : allHumans)
		{
			EntryAgent<Double, Human> ea = (EntryAgent)o;
			totalFitness+=ea.getH().fitness;
			ea.setFitSum(totalFitness);
		}

		lastAllHumans = allHumans;
		allHumans = new Bag();
		lastTotalFitness = totalFitness;
		totalFitness = 0;

//		//////End Model 2-3
//		//test
//		long start = System.currentTimeMillis();
//		ps.println("Start="+(start/1000));
//		ps.flush();
		
	}

	/**
	 * Choose kind of action. if random value < dna -->honest action(1), 
	 * if  dna<random value < 10 -  ->dishonest action(2)
	 * @param dna
	 * @return 1 if honest, 2 if dishonest
	 */
	public int chooseAction(double dna)
	{
		return this.random.nextInt(10)+this.random.nextDouble()<dna?this.HONEST_ACTION:this.DISHOSNEST_ACTION;
	}

	/**
	 * 
	 * try honest action
	 */
	public boolean tryHonestAgentAction()
	{
		boolean isHonest=((this.random.nextDouble()<HONEST_PROB));
		if(isHonest)
			this.numHonestAction++;
		return isHonest;
	}
	
	/**
	 *  try dishonest action 
	 */
	public boolean tryDisHonestAgentAction()
	{		
		boolean isDishonest=((this.random.nextDouble()<HONEST_PROB));
		if(isDishonest)
			this.numDishonestAction++;
		return isDishonest;
	}

	/**
	 * 
	 * @param a     
	 * @param neigh 
	 * 
	 * @return true   if agent is punished
	 * @return false  if agent is not punished
	 */
	public boolean legalPunishment(Human a,Bag neigh){

		double prob_pun=SociallyDamagingBehavior.PUNISHIMENT_PROB;
		// neighborhood influence punishment only for model 2 3 4
		if(SociallyDamagingBehavior.getMODEL()==SociallyDamagingBehavior.MODEL2_RANDOM_MOVEMENT ||
				SociallyDamagingBehavior.getMODEL()==SociallyDamagingBehavior.MODEL3_AGGREGATION_MOVEMENT ||
				SociallyDamagingBehavior.getMODEL()==SociallyDamagingBehavior.MODEL4_MEMORY )

		{
			if(neigh.size()>0)
			{
				int H_neigh=0;
				int DH_neigh=0;
				for(Object o:neigh)
				{
					Human n_a=(Human)o;
					if(n_a.behavior instanceof Honest) H_neigh++;
					else DH_neigh++;

				}
				int tot=H_neigh+DH_neigh;
				double perc_H=(H_neigh*100)/tot;
				double perc_DH=(DH_neigh*100)/tot;
				double p_perc_h=perc_H/100;
				double p_perc_dh=perc_DH/100;
				if(H_neigh>DH_neigh) prob_pun=SociallyDamagingBehavior.PUNISHIMENT_PROB+p_perc_h;
				else if(H_neigh<DH_neigh) prob_pun=(SociallyDamagingBehavior.PUNISHIMENT_PROB-p_perc_dh);

			}
		}	


		double random_pun=this.random.nextDouble();
		if(random_pun < prob_pun)
		{
			if(PUNISHIMENT_SEVERITY==PUNISHIMENT_FAIR)
			{
				a.fitness-=DAMAGING_PAYOFF;
			}else
				if(PUNISHIMENT_SEVERITY==PUNISHIMENT_STRICT)
				{
					a.fitness-=DAMAGING_PAYOFF*2;
				}else
				{
					a.fitness-=DAMAGING_PAYOFF/2;
				}
			return true;
		}
		else
			return false;
	}

	/*SDB*/
	public static void main(String[] args)
	{
//		if(args.length!=5)
//			System.out.println("java -jar nomefile.jar <Model 0-4> <width> <height> <numAgents> <AOI>");
//		else
//		{
//			MODEL = Integer.parseInt(args[0]);
//			width = Double.parseDouble(args[1]);
//			height = Double.parseDouble(args[2]);
//			numHumanBeing = Integer.parseInt(args[3]);
//			neighborhood = Double.parseDouble(args[4]);
//			MAX_AOI_AGGREGATION_MODEL3 = Double.parseDouble(args[4]);
			doLoop(SociallyDamagingBehavior.class, args);
			System.exit(0);
//		}
	}

	public int getNumHumanBeing() {return numHumanBeing;}
	public void setNumHumanBeing(int numHumanBeing) {this.numHumanBeing = numHumanBeing;}
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }

	public double getNeighborhood() { return neighborhood; }
	public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
	public static double getMIN_AOI_AGGREGATION_MODEL3() {
		return MIN_AOI_AGGREGATION_MODEL3;
	}
	public static void setMIN_AOI_AGGREGATION_MODEL3(double mIN_AOI_AGGREGATION_MODEL3) {
		if((mIN_AOI_AGGREGATION_MODEL3 > 0) && 
				(mIN_AOI_AGGREGATION_MODEL3<=MAX_AOI_AGGREGATION_MODEL3))
			MIN_AOI_AGGREGATION_MODEL3 = mIN_AOI_AGGREGATION_MODEL3;
	}
	public static double getMAX_AOI_AGGREGATION_MODEL3() {
		return MAX_AOI_AGGREGATION_MODEL3;
	}
	public static void setMAX_AOI_AGGREGATION_MODEL3(double mAX_AOI_AGGREGATION_MODEL3) {
		if((mAX_AOI_AGGREGATION_MODEL3 >= MIN_AOI_AGGREGATION_MODEL3) &&
				(mAX_AOI_AGGREGATION_MODEL3 <= neighborhood))
			MAX_AOI_AGGREGATION_MODEL3 = mAX_AOI_AGGREGATION_MODEL3;
	}
	public static double getDAMAGING_PAYOFF_PROB() {return DAMAGING_PAYOFF_PROB;}
	public static void setDAMAGING_PAYOFF_PROB(double dAMAGING_PAYOFF_PROB) {DAMAGING_PAYOFF_PROB = dAMAGING_PAYOFF_PROB;}
	public static double getDAMAGING_PAYOFF() {return DAMAGING_PAYOFF;}
	public static void setDAMAGING_PAYOFF(double dAMAGING_PAYOFF) {DAMAGING_PAYOFF = dAMAGING_PAYOFF;}
	public static double getPUNISHIMENT_PROB() {return PUNISHIMENT_PROB;}
	public static void setPUNISHIMENT_PROB(double pUNISHIMENT_PROB) {PUNISHIMENT_PROB = pUNISHIMENT_PROB;}
	public static double getHONEST_PAYOFF() {return HONEST_PAYOFF;}
	public static void setHONEST_PAYOFF(double hONEST_PAYOFF) {HONEST_PAYOFF = hONEST_PAYOFF;}
	public static double getHONEST_PROB() {return HONEST_PROB;}
	public static void setHONEST_PROB(double hONEST_PROB) {HONEST_PROB = hONEST_PROB;}
	public static int getPERCENT_HONEST() {return PERCENT_HONEST;}
	public static void setPERCENT_HONEST(int pERCENT_HONEST) {PERCENT_HONEST = pERCENT_HONEST;}
	public static int getEPOCH() {return EPOCH;}
	public static void setEPOCH(int ePOCH) {EPOCH = ePOCH;}
	public static int getMODEL() {return MODEL;}
	public static void setMODEL(int model) {MODEL = model;}
	public static double getSOCIAL_INFLUENCE() {return SOCIAL_INFLUENCE;}
	public static void setSOCIAL_INFLUENCE(double sOCIAL_INFLUENCE) {SOCIAL_INFLUENCE = sOCIAL_INFLUENCE;}
	public int getPUNISHIMENT_SEVERITY() {return PUNISHIMENT_SEVERITY;}
	public void setPUNISHIMENT_SEVERITY(int pUNISHIMENT_SEVERITY) {PUNISHIMENT_SEVERITY = pUNISHIMENT_SEVERITY;}
	public static int getPERCENTAGE_PAYOFF_FITNESS() {return PERCENTAGE_PAYOFF_FITNESS;}
	public static void setPERCENTAGE_PAYOFF_FITNESS(int pERCENTAGE_PAYOFF_FITNESS) {PERCENTAGE_PAYOFF_FITNESS = pERCENTAGE_PAYOFF_FITNESS;}
	public double getCohesion() {return cohesion;}
	public void setCohesion(double cohesion) {this.cohesion = cohesion;}
	public int getHonest() {return honest;}
	public int getDishonest() {return dishonest;}
	public int getHonestAction() {return honestAction;}
	public int getDishonestAction() {return dishonestAction;}
	public boolean isLogging() {return logging;}
	public void setLogging(boolean logging) {this.logging = logging;}
}