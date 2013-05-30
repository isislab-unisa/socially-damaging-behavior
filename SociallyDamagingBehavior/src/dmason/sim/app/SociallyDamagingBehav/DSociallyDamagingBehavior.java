package dmason.sim.app.SociallyDamagingBehav; 

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import dmason.annotation.batch;
import dmason.batch.data.EntryParam;
import dmason.batch.data.GeneralParam;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.Entry;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.sim.field.continuous.DContinuous2DFactory;
import dmason.sim.field.continuous.DContinuous2DXY;
import dmason.util.exception.DMasonException;
import sim.engine.*;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.*;

public class DSociallyDamagingBehavior extends DistributedState<Double2D>
{
    private static final long serialVersionUID = 1;
	public DContinuous2D human_being;
	private static boolean isToroidal=true;
	//logger
	public boolean logging = false;
	public FileOutputStream file;
	public PrintStream ps;
	
	@batch(
    		domain = "100-300",
        	suggestedValue = "250"
	)
	public double width = 150;
	@batch
	public double height = 150;
    @batch
	public int numHumanBeing=50;
    @batch
    public double cohesion = 1.0;
    @batch
    public double avoidance = 1.0;
    @batch
    public double randomness = 1.0;
    @batch
    public double consistency = 1.0;
    @batch
    public double momentum = 1.0;
    @batch
    public double neighborhood = 10;
    
    public double jump = 0.7;  // how far do we move in a timestep?
	
	/*SDB*/
    public  int EPOCH = 100;
	public int ACTUAL_EPOCH=0;
	
	public static int MODEL0_RANDOM_DAMAGING=0;
	public static int MODEL1_PROPORTIONAL_DAMAGING=1;
	public static int MODEL2_RANDOM_MOVEMENT=2;
	public static int MODEL3_AGGREGATION_MOVEMENT=3;
	public static int MODEL4_MEMORY=4;
	public int MODEL=MODEL4_MEMORY;
	public int MIN_AOI_AGGREGATION_MODEL3=5;
	public int MAX_AOI_AGGREGATION_MODEL3=10;
	public double RHO_MODEL4_MEMORY = 0.2;

	public double DAMAGING_PAYOFF_PROB = 1.0;
	public double DAMAGING_PAYOFF = 1.5;
	public double SOCIAL_INFLUENCE = 0.010;
	public int PERCENTAGE_PAYOFF_FITNESS=10;

	public double PUNISHIMENT_PROB = 1.0;
	public static int PUNISHIMENT_STRICT = 1; 
	public static int PUNISHIMENT_FAIR = 2;
	public static int PUNISHIMENT_LAX = 3;
	public int PUNISHIMENT_SEVERITY = PUNISHIMENT_FAIR;

	public double HONEST_PAYOFF = 1.0;
	public double HONEST_PROB = 1.0;
	public int PERCENT_HONEST = 50;
	
	public final int HONEST_ACTION=1;
	public final int DISHOSNEST_ACTION=2;
	/*SDB*/
	
	Logger log;
	
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
        
    public double gridWidth ;
    public double gridHeight ;   
    public int MODE;
    
    public static String topicPrefix = "";
    
    //-----------------------------------------------
    //-----------------------------------------------
    //-----------------------------------------------
    Bag localReinitializeTest = null; Bag globalReinitializeTest= null;
    ArrayList<Entry<Double2D>> lastSended;
    public ArrayList<Entry<Double2D>> getReinitializeTest(){
    	return  (lastSended=((human_being!=null)?((DContinuous2DXY)human_being).getAllVisibleAgent():null));
    } //� obbligatorio ai fini del corretto funzionamento
  
    public void setReinitialize(Bag value){ localReinitializeTest = value;} //� obbligatorio ai fini del corretto funzionamento
    public boolean globalReinitializeTest() { return true;}
    public ArrayList<Entry<Double2D>> getGlobalReinitializeTest(){  
    	return (((human_being!=null)?(lastSended=((DContinuous2DXY)human_being).getAllVisibleAgent()):null));
    }
   
    public void setGlobalReinitializeTest(Object value){
    	
    	((DistributedMultiSchedule)this.schedule).clear();
    	DHuman a = (DHuman)value;
    	int var = 0;
    	ArrayList<RemoteAgent<Double2D>> figli=new ArrayList<RemoteAgent<Double2D>>();
    	
    	for (Entry<Double2D> human : lastSended) {
    		DHuman f = (DHuman)human.r;
    		if(f.getFitness() > a.fitness || 
    				(f.getFitness() == a.fitness && f.getId().compareTo(a.getId())<=0))
    		{
    			double dna1 = f.dna+delta(this);
				double dna2 = f.dna+delta(this);
				var+=2;
				if(dna1 > 10) dna1=10;
					else 
						if(dna1 < 0) dna1=0;
				if(dna2 > 10) dna2=10;
					else 
						if(dna2 < 0) dna2=0;
				
				f.setDna(dna1);
				DHuman f2 = new DHuman(this, f.getPos());
				f2.setDna(dna2);

				figli.add(f);
				figli.add(f2);
    		}
		}
    	human_being.clear();
		((DContinuous2DXY)human_being).resetAddAll(figli);
    	
    }
    public long getReinitializeTestValueOf(){return EPOCH;}
    
    public DHuman reduceReinitializeTest(Object[] shard) {
    	
    	ArrayList<DHuman> reinit = new ArrayList<DHuman>();
    	ArrayList<Entry<Double2D>> obj = null;
    	int HONEST=0;
    	int DISHONEST=0;
    	
    	int HONEST_ACTION=0;
    	int DISHONEST_ACTION=0;
    	
    	for (int i = 0; i < shard.length; i++) 
    	{
    		obj = (ArrayList<Entry<Double2D>>)shard[i];
    		//System.out.println("TAGLIA OBJ="+obj.size());
     		for(Entry<Double2D> f: obj){
     			
     			DHuman ff = (DHuman) f.r;
     			if( ff.behavior instanceof Honest) HONEST++;
     			else DISHONEST++;
     			
     			if(ff.honestAction) HONEST_ACTION++;
     			else DISHONEST_ACTION ++;
     			
     			reinit.add(ff);
     		}
    	}
     	Collections.sort(reinit, new Comparator<DHuman>() {
			@Override
			public int compare(DHuman d0, DHuman d1) {
				// TODO Auto-generated method stub
				if(d0.fitness<d1.fitness) return 1;
				else if(d0.fitness>d1.fitness) return -1;
				else return d0.getId().compareTo(d1.getId());
			}
		});
     	
     	int mediana=(50*reinit.size())/100;
     	//System.out.println("REINIT_TAGLIA="+reinit.size() +" MEDIANA "+mediana);
     	
     	if(log==null)
    	{
     		log = Logger.getLogger("DSDBLog");
    		try {
    			GregorianCalendar gc = new GregorianCalendar();
    			log.addHandler(new FileHandler("DistributedSociallyDamagingBehavior_"+(gc.get(Calendar.MONTH)+1)+
    					"-"+(gc.get(Calendar.DAY_OF_MONTH)+1)+"-"+(gc.get(Calendar.YEAR))+
    					"_"+gc.get(Calendar.HOUR_OF_DAY)+":"+gc.get(Calendar.MINUTE)+":"+gc.get(Calendar.SECOND)+".log"));
    			
    		} catch (SecurityException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		log.info(
    					"SIMULATION PARAMETERS: \n"+
    					"CELL ID: "+human_being.cellType+" | "+" FIELD[WIDTH:"+gridWidth+",HEIGHT:"+gridHeight+"] | "+
    					"AOI: "+MAX_DISTANCE+" | #AGENTS:"+numHumanBeing+" | EPOCH="+EPOCH+" | \n"+
    					"MODEL PARAMETERS: \n"+
    					"SOCIAL_INFLUENCE:"+SOCIAL_INFLUENCE+" | \n"+
    					"DAM-PAY-PROB:"+DAMAGING_PAYOFF_PROB+" | \n"+
    					"DAM_PAY:"+DAMAGING_PAYOFF+" | \n"+
    					"PUNISHIMENT_PROB:"+PUNISHIMENT_PROB+" | \n"+
    					"PUNISHIMENT_SEVERITY:"+PUNISHIMENT_SEVERITY+" | \n"+
    					"HONEST_PAYOFF:"+HONEST_PAYOFF+" | \n"+
    					"HONEST_PROB:"+HONEST_PROB+" | \n"+
    					"PERCENT_HONEST:"+PERCENT_HONEST
    				);
    		
    	}else{
    		
    		double TOT=HONEST+DISHONEST;
    		double percHonest=(HONEST*100)/TOT;
    		double percDisHonest=(DISHONEST*100)/TOT;
    		
    		double TOT_ACTION=HONEST_ACTION+DISHONEST_ACTION;
    		double percHonestAction=(HONEST*100)/TOT_ACTION;
    		double percDisHonestAction=(DISHONEST*100)/TOT_ACTION;
    		
    		log.info(
    					"#EPOCH:"+ACTUAL_EPOCH+" | #AGENT:"+TOT+" | HONEST:"+percHonest+"% | DISHONEST:"+percDisHonest+"% |" +
    					" HONEST_ACTION:"+percHonestAction+"% | "+"DISHONEST_ACTION:"+percDisHonestAction +"% | MEDIAN:"+reinit.get(mediana-1)
    					+"\n"
    				);
    	}
     	ACTUAL_EPOCH++;
     	return reinit.get(mediana-1);
    } 
    //-----------------------------------------------
    //-----------------------------------------------
    //-----------------------------------------------
    
    public DSociallyDamagingBehavior(GeneralParam params)
    {    	
    	super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
    			params.getJ(),params.getIp(),params.getPort(),params.getMode(),
    			isToroidal,new DistributedMultiSchedule<Double2D>(),topicPrefix);
    	numHumanBeing = params.getNumAgents();
    	ip = params.getIp();
    	port = params.getPort();
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
		numDishonestAction=0;
		numHonestAction=0;


    	
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
    
    public DSociallyDamagingBehavior() {super();}
    
    @Override
	public void start()
    {
		super.start();
		
		// set up the human field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		// make a bunch of humans and schedule 'em.  

		try 
    	{
			human_being = DContinuous2DFactory.createDContinuous2D(neighborhood/1.5,gridWidth, gridHeight,this,
    				super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"humans_being", topicPrefix);
    		init_connection();
    	} catch (DMasonException e) { e.printStackTrace(); }
		
		//file logging
		if(logging)
			try {
				file = new FileOutputStream(this.TYPE+"Model="+MODEL+"_NumAgent="+numHumanBeing+"_Width="+gridWidth+"_Height="+gridHeight+".txt");
				ps = new PrintStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		honest = (numHumanBeing*PERCENT_HONEST)/100;
		dishonest = numHumanBeing - honest;
			
		 //System.out.println("Honest="+hon+"     DisHon="+disHon);
		allHumans = new Bag();
		
		DHuman hAgent = new DHuman(this, new Double2D(0,0));
		//Create Honest Agent
    	for (int x=0;x<honest;x++) 
		{
    		hAgent.setPos(human_being.setAvailableRandomLocation(hAgent));
			/*SDB*/
			double dna=5+this.random.nextInt(4)+this.random.nextDouble(); //5<value<10
			hAgent.setDna(dna);

			/*SDB*/
			if(human_being.setObjectLocation(hAgent, new Double2D(hAgent.pos.getX(), hAgent.pos.getY())))
			{	
				schedule.scheduleOnce(hAgent);
				hAgent = new DHuman(this, new Double2D(0,0));
			}
		}
    	
    	DHuman dhAgent = new DHuman(this, new Double2D(0,0));
		//Create Dishonest Agent
		for(int x=0;x<dishonest;x++)
		{
			dhAgent.setPos(human_being.setAvailableRandomLocation(dhAgent));

			/*SDB*/
			double dna=this.random.nextInt(4)+this.random.nextDouble(); //0<value<5
			dhAgent.setDna(dna);

			/*SDB*/
			if(human_being.setObjectLocation(dhAgent, new Double2D(dhAgent.pos.getX(), dhAgent.pos.getY())))
			{	
				schedule.scheduleOnce(dhAgent);
				dhAgent = new DHuman(this, new Double2D(0,0));
			}
		}
		
		//////Model 2-3
		allHumans.sort(new Comparator<EntryAgent<Double, DHuman>>() {
			@Override
			public int compare(EntryAgent<Double, DHuman> o1, EntryAgent<Double, DHuman> o2) {
				if(o1.getFitSum()>o2.getFitSum()) return 1;
				else if(o1.getFitSum()<o2.getFitSum()) return -1;
				return 0;
			}
		});

		for(Object o : allHumans)
		{
			EntryAgent<Double, DHuman> ea = (EntryAgent)o;
			totalFitness+=ea.getH().fitness;
			ea.setFitSum(totalFitness);
		}

		lastAllHumans = allHumans;
		allHumans = new Bag();
		lastTotalFitness = totalFitness;
		totalFitness = 0;
		//////End Model 2-3
		
    	try {
			getTrigger().publishToTriggerTopic("Simulation cell "+human_being.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("SIMULATION PARAMETERS: \n"+
		"CELL ID: "+human_being.cellType+" | "+" FIELD[WIDTH:"+gridWidth+",HEIGHT:"+gridHeight+"] | "+
		"AOI: "+MAX_DISTANCE+" | #AGENTS:"+numHumanBeing+" | EPOCH="+EPOCH+" | \n"+
		"MODEL PARAMETERS: \n"+
		"SOCIAL_INFLUENCE:"+SOCIAL_INFLUENCE+" | \n"+
		"DAM-PAY-PROB:"+DAMAGING_PAYOFF_PROB+" | \n"+
		"DAM_PAY:"+DAMAGING_PAYOFF+" | \n"+
		"PUNISHIMENT_PROB:"+PUNISHIMENT_PROB+" | \n"+
		"PUNISHIMENT_SEVERITY:"+PUNISHIMENT_SEVERITY+" | \n"+
		"HONEST_PAYOFF:"+HONEST_PAYOFF+" | \n"+
		"HONEST_PROB:"+HONEST_PROB+" | \n"+
		"PERCENT_HONEST:"+PERCENT_HONEST);
    }

    public static void main(String[] args)
        {
        doLoop(DSociallyDamagingBehavior.class, args);
        System.exit(0);
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
		if(isDishonest){
			if((this.numHonestAction+this.numDishonestAction)==this.numHumanBeing){
				this.numDishonestAction=1;
				this.numHonestAction=0;
			}
			else
				this.numDishonestAction++;
		}
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
	public boolean legalPunishment(DHuman a,Bag neigh){

		double prob_pun=PUNISHIMENT_PROB;
		// neighborhood influence punishment only for model 2 3 4
		if(getMODEL()==DSociallyDamagingBehavior.MODEL2_RANDOM_MOVEMENT ||
				getMODEL()==DSociallyDamagingBehavior.MODEL3_AGGREGATION_MOVEMENT ||
				getMODEL()==DSociallyDamagingBehavior.MODEL4_MEMORY )

		{
			if(neigh.size()>0)
			{
				int H_neigh=0;
				int DH_neigh=0;
				for(Object o:neigh)
				{
					DHuman n_a=(DHuman)o;
					if(n_a.behavior instanceof Honest) H_neigh++;
					else DH_neigh++;

				}
				int tot=H_neigh+DH_neigh;
				double perc_H=(H_neigh*100)/tot;
				double perc_DH=(DH_neigh*100)/tot;
				double p_perc_h=perc_H/100;
				double p_perc_dh=perc_DH/100;
				if(H_neigh>DH_neigh) prob_pun=PUNISHIMENT_PROB+p_perc_h;
				else if(H_neigh<DH_neigh) prob_pun=(PUNISHIMENT_PROB-p_perc_dh);

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
	
	@Override
	public DistributedField<Double2D> getField() {
		// TODO Auto-generated method stub
		return human_being;
	}
	@Override
	public void addToField(RemoteAgent<Double2D> rm, Double2D loc) {
    	human_being.setObjectLocation(rm,loc);
        setPortrayalForObject(rm);
		
	}
	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return this;
	}
	@Override
	public boolean setPortrayalForObject(Object o) {
    	if(human_being.p!=null)
    	{
    		DHuman f=(DHuman)o;
    		SimplePortrayal2D pp = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
    				f.getBehav_Color(),
    				OrientedPortrayal2D.SHAPE_COMPASS)));
    		human_being.p.setPortrayalForObject(o, pp);
    		return true;
    	}
    	return false;
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
    
	private static double delta(SimState state){
		
		
		double value=state.random.nextDouble()/2;
		double delta=value/10;
		boolean probability=state.random.nextBoolean();
		
		if(probability)
			return delta;
		else 
			return delta*-1;
		
	}
	public int getEPOCH() {return EPOCH;}
	public void setEPOCH(int ePOCH) {EPOCH = ePOCH;}
	public Object putEPOCH(String value){return Integer.parseInt(value);}
	public double getDAMAGING_PAYOFF_PROB() {return DAMAGING_PAYOFF_PROB;}
	public void setDAMAGING_PAYOFF_PROB(double dAMAGING_PAYOFF_PROB) {DAMAGING_PAYOFF_PROB = dAMAGING_PAYOFF_PROB;}
	public Object putDAMAGING_PAYOFF_PROB(String value) throws Exception
	{
		double p = Double.parseDouble(value);
		if(p<0 || p>1)
			throw new Exception("Wrong probability value.");
		return p;
	}
	public double getDAMAGING_PAYOFF() {return DAMAGING_PAYOFF;}
	public void setDAMAGING_PAYOFF(double dAMAGING_PAYOFF) {DAMAGING_PAYOFF = dAMAGING_PAYOFF;}
	public Object putDAMAGING_PAYOFF(String value){return Double.parseDouble(value);}
	public double getSOCIAL_INFLUENCE() {return SOCIAL_INFLUENCE;}
	public void setSOCIAL_INFLUENCE(double sOCIAL_INFLUENCE) {SOCIAL_INFLUENCE = sOCIAL_INFLUENCE;}
	public Object putSOCIAL_INFLUENCE(String value) {return Double.parseDouble(value);}
	public double getPUNISHIMENT_PROB() {return PUNISHIMENT_PROB;}
	public void setPUNISHIMENT_PROB(double pUNISHIMENT_PROB) {PUNISHIMENT_PROB = pUNISHIMENT_PROB;}
	public Object putPUNISHIMENT_PROB(String value) throws Exception
	{
		double p = Double.parseDouble(value);
		if(p<0 || p>1)
			throw new Exception("Wrong probability value.");
		return p;
	}
	public int getPUNISHIMENT_SEVERITY() {return PUNISHIMENT_SEVERITY;}
	public void setPUNISHIMENT_SEVERITY(int pUNISHIMENT_SEVERITY) {PUNISHIMENT_SEVERITY = pUNISHIMENT_SEVERITY;}
	public Object putPUNISHIMENT_SEVERITY(String value) {return Integer.parseInt(value);}
	public double getHONEST_PAYOFF() {return HONEST_PAYOFF;}
	public void setHONEST_PAYOFF(double hONEST_PAYOFF) {HONEST_PAYOFF = hONEST_PAYOFF;}
	public Object putHONEST_PAYOFF(String value) {return Double.parseDouble(value);}
	public double getHONEST_PROB() {return HONEST_PROB;}
	public void setHONEST_PROB(double hONEST_PROB) {HONEST_PROB = hONEST_PROB;}
	public Object putHONEST_PROB(String value) throws Exception
	{
		double p = Double.parseDouble(value);
		if(p<0 || p>1)
			throw new Exception("Wrong probability value.");
		return Double.parseDouble(value);
	}
	public int getPERCENT_HONEST() {return PERCENT_HONEST;}
	public void setPERCENT_HONEST(int pERCENT_HONEST) {PERCENT_HONEST = pERCENT_HONEST;}
	public Object putPERCENT_HONEST(String value) throws Exception
	{
		int p = Integer.parseInt(value);
		if(p<0 || p>100)
			throw new Exception("Wrong Percent value.");
		return Integer.parseInt(value);
	}	
	public int getMIN_AOI_AGGREGATION_MODEL3() {return MIN_AOI_AGGREGATION_MODEL3;}
	public void setMIN_AOI_AGGREGATION_MODEL3(int mIN_AOI_AGGREGATION_MODEL3) {MIN_AOI_AGGREGATION_MODEL3 = mIN_AOI_AGGREGATION_MODEL3;}
	public Object putMIN_AOI_AGGREGATION_MODEL3(String value) 
	{	
		int val = Integer.parseInt(value);
		if(val>0)
			if(val<neighborhood)
				return val;
			else
				return neighborhood;
		else
			return 1;
			
	}
	public int getMAX_AOI_AGGREGATION_MODEL3() {return MAX_AOI_AGGREGATION_MODEL3;}
	public void setMAX_AOI_AGGREGATION_MODEL3(int mAX_AOI_AGGREGATION_MODEL3) {MAX_AOI_AGGREGATION_MODEL3 = mAX_AOI_AGGREGATION_MODEL3;}
	public Object putMAX_AOI_AGGREGATION_MODEL3(String value) {
		
		int val = Integer.parseInt(value);
		if(val<neighborhood)
			if(val>MIN_AOI_AGGREGATION_MODEL3)
				return val;
			else
				return MIN_AOI_AGGREGATION_MODEL3;
		else
			return neighborhood;
	}
	public int getMODEL() {return MODEL;}
	public void setMODEL(int model) {MODEL = model;}
	public Object putMODEL(String value) {return Integer.parseInt(value);}
	public int getPERCENTAGE_PAYOFF_FITNESS() {return PERCENTAGE_PAYOFF_FITNESS;}
	public void setPERCENTAGE_PAYOFF_FITNESS(int pERCENTAGE_PAYOFF_FITNESS) {PERCENTAGE_PAYOFF_FITNESS = pERCENTAGE_PAYOFF_FITNESS;}
	public Object putPERCENTAGE_PAYOFF_FITNESS(String value) {return Integer.parseInt(value);}
	public double getCohesion() { return cohesion; }
	public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
	public Object putCohesion(String value) { 
		double val = Double.parseDouble(value);
		if(val >= 0.0) 
			return (cohesion = val);
		else
			return cohesion;
	}
	public double getAvoidance() { return avoidance; }
	public void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
	public double getRandomness() { return randomness; }
	public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
	public double getConsistency() { return consistency; }
	public void setConsistency(double val) { if (val >= 0.0) consistency = val; }
	public double getMomentum() { return momentum; }
	public void setMomentum(double val) { if (val >= 0.0) momentum = val; }
	public int getNumHumans() { return numHumanBeing; }
	public void setNumHumans(int val) { if (val >= 1) numHumanBeing = val; }
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }
	public double getNeighborhood() { return neighborhood; }
	public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }	
	public boolean isLogging() {return logging;}
	public void setLogging(boolean logging) {this.logging = logging;}
	public Object putLogging(String value){return Boolean.parseBoolean(value);}
	
}