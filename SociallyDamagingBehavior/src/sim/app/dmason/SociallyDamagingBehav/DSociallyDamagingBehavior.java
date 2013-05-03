package sim.app.dmason.SociallyDamagingBehav; 

import java.awt.Color;
import java.util.List;
import dmason.batch.data.EntryParam;
import dmason.batch.data.GeneralParam;
import dmason.sim.app.DFlockers.DFlocker;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.sim.field.continuous.DContinuous2DFactory;
import dmason.util.exception.DMasonException;
import sim.app.mason.SociallyDamagingBehav.Agent;
import sim.app.mason.SociallyDamagingBehav.Honest;
import sim.app.mason.SociallyDamagingBehav.NewGenAgent;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class DSociallyDamagingBehavior extends DistributedState<Double2D>
    {
    private static final long serialVersionUID = 1;

    public DContinuous2D human_being;
    
	/*SDB*/
	public int numHumanBeing = 200;
	public double width = 150;
	public double height = 150;
	public static int EPOCH = 10000;
	
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
	/*SDB*/
    
	private static boolean isToroidal=true;
    public static String topicPrefix = "";
	
    public double cohesion = 1.0;
    public double avoidance = 1.0;
    public double randomness = 1.0;
    public double consistency = 1.0;
    public double momentum = 1.0;
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
    public int getNumFlockers() { return numHumanBeing; }
    public void setNumFlockers(int val) { if (val >= 1) numHumanBeing = val; }
    public double getWidth() { return width; }
    public void setWidth(double val) { if (val > 0) width = val; }
    public double getHeight() { return height; }
    public void setHeight(double val) { if (val > 0) height = val; }
    public double getNeighborhood() { return neighborhood; }
    public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
        
    public double gridWidth ;
    public double gridHeight ;   
    public int MODE;
    
    public DSociallyDamagingBehavior(GeneralParam params)
    {    	
    	super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
    			params.getJ(),params.getIp(),params.getPort(),params.getMode(),
    			isToroidal,new DistributedMultiSchedule<Double2D>(),topicPrefix);
    	ip = params.getIp();
    	port = params.getPort();
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    }
    
    public DSociallyDamagingBehavior(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
    {    	
    	super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
    			params.getJ(),params.getIp(),params.getPort(),params.getMode(),
    			isToroidal,new DistributedMultiSchedule<Double2D>(), prefix);
    	ip = params.getIp();
    	port = params.getPort();
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    	topicPrefix = prefix; 
    	
    	//System.out.println(simParams.size());
    	for (EntryParam<String, Object> entryParam : simParams) {
    		
    		try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
    	for (EntryParam<String, Object> entryParam : simParams) {
    		
    		try {
				System.out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    }
    
    public DSociallyDamagingBehavior()
    {
    	super();
    }
    
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
    
    public void start()
    {
		super.start();
		
		this.schedule.scheduleRepeating(new NewGenAgent());

		// set up the human field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		// make a bunch of humans and schedule 'em.  
		
		int hon = (numHumanBeing*PERCENT_HONEST)/100;
		int disHon = numHumanBeing - hon;    	

    	try 
    	{
    		human_being = DContinuous2DFactory.createDContinuous2D(neighborhood/1.5,gridWidth, gridHeight,this,
    				super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"human_being", topicPrefix);
    		init_connection();
    	} catch (DMasonException e) { e.printStackTrace(); }

		//Create Honest Agent
    	for (int x=0;x<hon;x++) 
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/
			double dna=5+this.random.nextInt(4)+this.random.nextDouble(); //5<value<10
			
			DHuman dhAgent = new DHuman(location,this,dna);
			/*SDB*/
			human_being.setObjectLocation(dhAgent, location);
			dhAgent.humans = human_being;
			dhAgent.theHuman = this;
			schedule.scheduleOnce(dhAgent);
		}
    	
		//Create Dishonest Agent
		for(int x=0;x<disHon;x++)
		{
			Double2D location = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
			/*SDB*/
			
			double dna=this.random.nextInt(4)+this.random.nextDouble(); //0<value<5
			
			DHuman dhAgent = new DHuman(location,this,dna);
			/*SDB*/
			human_being.setObjectLocation(dhAgent, location);
			dhAgent.humans = human_being;
			dhAgent.theHuman = this;
			schedule.scheduleRepeating(dhAgent);
		}

    	try {
			getTrigger().publishToTriggerTopic("Simulation cell "+human_being.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void main(String[] args)
        {
        doLoop(DSociallyDamagingBehavior.class, args);
        System.exit(0);
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
	
	public void legalPunishment(DHuman a,Bag neigh)
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
	
	@Override
	public DistributedField<Double2D> getField() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void addToField(RemoteAgent<Double2D> rm, Double2D loc) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean setPortrayalForObject(Object o) {
		// TODO Auto-generated method stub
		return false;
	}    
}
