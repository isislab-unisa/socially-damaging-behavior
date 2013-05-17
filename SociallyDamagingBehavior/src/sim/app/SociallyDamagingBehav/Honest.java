package sim.app.SociallyDamagingBehav;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * The class of honest agent 
 *  
 *  
 **/
public class Honest extends Behaviour{

	@Override
	public void action(Human agent,SociallyDamagingBehavior state,Bag neigh) 
	{		
		if(state.getMODEL()==state.MODEL0_RANDOM_DAMAGING)
			actionModel_0(agent, state, neigh);
		else
			if(state.getMODEL()==state.MODEL1_PROPORTIONAL_DAMAGING)
				actionModel_1(agent, state, neigh);
			else
				if(state.getMODEL()==state.MODEL2_RANDOM_MOVEMENT)
					actionModel_2_3(agent, state, neigh);
				else
					if(state.getMODEL()==state.MODEL3_AGGREGATION_MOVEMENT)
						actionModel_2_3(agent, state, neigh);
					else
						if(state.getMODEL()==state.MODEL4_MEMORY)
							actionModel_4(agent, state, neigh);		
		//		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
		//		int action = sdb.chooseAction(agent.dna);
		//		if(action == 1)
		//		{
		//			agent.honestAction=true;
		//			if(sdb.tryHonestAgentAction())
		//				agent.fitness+=SociallyDamagingBehavior.HONEST_PAYOFF;
		//			
		//		}
		//		else
		//		{
		//			agent.honestAction=false;
		//			if(sdb.tryDisHonestAgentAction())
		//			{
		//				agent.fitness+=SociallyDamagingBehavior.DAMAGING_PAYOFF;
		//				if(neigh.size()!=0)
		//					((Human)neigh.get(state.random.nextInt(neigh.size()))).fitness-=SociallyDamagingBehavior.DAMAGING_PAYOFF;
		//			}
		//		}
	}

	/**
	 * This is the action of agent with damaging on another random agent
	 */
	public void actionModel_0(Human agent, SociallyDamagingBehavior sdb, Bag neigh){
		Bag allAgents = sdb.human_being.getAllObjects();
		
		int action = sdb.random.nextInt(10)+sdb.random.nextDouble()<agent.dna?1:2;
				//sdb.random.nextDouble()*10>agent.dna?1:2; 
				//sdb.chooseAction(agent.dna);
		
		if(action == 1)
		{
			agent.honestAction=true;
			if(sdb.tryHonestAgentAction())
				agent.fitness+=SociallyDamagingBehavior.HONEST_PAYOFF;
		}
		else
		{
			agent.honestAction=false;
			if(sdb.tryDisHonestAgentAction())
			{
				agent.fitness+=SociallyDamagingBehavior.DAMAGING_PAYOFF;
				if(allAgents.size()!=0)
					((Human)allAgents.get(sdb.random.nextInt(allAgents.size()))).fitness-=SociallyDamagingBehavior.DAMAGING_PAYOFF;
				sdb.legalPunishment(agent, neigh);
			}
		}
	}


	public void actionModel_1(Human agent, SociallyDamagingBehavior sdb, Bag neigh){

		int action = sdb.chooseAction(agent.dna);
		if(action == 1)
		{
			agent.honestAction=true;
			if(sdb.tryHonestAgentAction())
				agent.fitness+=SociallyDamagingBehavior.HONEST_PAYOFF;
		}
		else
		{
			agent.honestAction=false;
			if(sdb.tryDisHonestAgentAction())
			{
				double percF=(agent.fitness*sdb.getPERCENTAGE_PAYOFF_FITNESS())/100;

				double var = 0;

				if(sdb.lastTotalFitness >= 1)
				{
					var = (sdb.random.nextInt((int) sdb.lastTotalFitness)+1) + sdb.random.nextDouble();
				}

				Human damaged;
				for (int i = 1; i < sdb.lastAllHumans.size(); i++) {
					if(((EntryAgent<Double,Human>)(sdb.lastAllHumans.get(i))).getFitSum()>var)
					{
						EntryAgent<Double, Human> ea = (EntryAgent<Double, Human>)sdb.lastAllHumans.get(i-1);
						damaged = ea.getH();

						agent.fitness=+percF;

						damaged.fitness-=percF;
						sdb.legalPunishment(agent, neigh);
						break;
					}
				}
			}
		}
	}

	/**
	 * This is the action of agent with damaging on a proportional fitness
	 * with random movement and sigma
	 */
	public void actionModel_2_3(Human agent, SociallyDamagingBehavior sdb, Bag neigh){

		int action = sdb.chooseAction(agent.dna);
		if(action == 1)
		{
			agent.honestAction=true;
			if(sdb.tryHonestAgentAction())
				agent.fitness+=SociallyDamagingBehavior.HONEST_PAYOFF;
		}
		else
		{
			agent.honestAction=false;
			if(!neigh.isEmpty())
				if(sdb.tryDisHonestAgentAction())
				{
					double percF=(agent.fitness*sdb.getPERCENTAGE_PAYOFF_FITNESS())/100;

					double var = 0;

					if(agent.neighFitness >= 1)
					{
						var = (sdb.random.nextInt((int) agent.neighFitness)+1) + sdb.random.nextDouble();
					}

					Human damaged;
					for (int i = 1; i < agent.entryNeigh.size(); i++) {
						if(((EntryAgent<Double,Human>)(agent.entryNeigh.get(i))).getFitSum()>var)
						{
							EntryAgent<Double, Human> ea = (EntryAgent<Double, Human>)agent.entryNeigh.get(i-1);
							damaged = ea.getH();

							agent.fitness=+percF;

							damaged.fitness-=percF;
							sdb.legalPunishment(agent, neigh);
							break;
						}
					}
				}
		}
	}

	/**
	 * This is the action of agent with damaging on a proportional fitness
	 * with random movement and sigma
	 */
	public void actionModel_4(Human agent, SociallyDamagingBehavior sdb, Bag neigh){

		double dna;
		if(agent.punprob!=0.0)
			dna = (agent.dna - (((2*agent.punprob)-1)*sdb.RHO_MODEL4_MEMORY));
		else
			dna = agent.dna;

		int action = sdb.chooseAction(dna);
		agent.isActionDishonest = false;
		agent.isPunished = false;

		if(action == 1)
		{
			agent.honestAction=true;
			if(sdb.tryHonestAgentAction())
			{
				agent.fitness+=SociallyDamagingBehavior.HONEST_PAYOFF;
			}
		}
		else
		{
			agent.honestAction=false;
			if(!neigh.isEmpty())
				if(sdb.tryDisHonestAgentAction())
				{
					double percF=(agent.fitness*sdb.getPERCENTAGE_PAYOFF_FITNESS())/100;

					double var = 0;

					if(agent.neighFitness >= 1)
					{
						var = (sdb.random.nextInt((int) agent.neighFitness)+1) + sdb.random.nextDouble();
					}

					Human damaged;
					for (int i = 1; i < agent.entryNeigh.size(); i++) {
						if(((EntryAgent<Double,Human>)(agent.entryNeigh.get(i))).getFitSum()>var)
						{
							EntryAgent<Double, Human> ea = (EntryAgent<Double, Human>)agent.entryNeigh.get(i-1);
							damaged = ea.getH();

							agent.fitness=+percF;
							agent.numNeighDamager++;
							agent.isActionDishonest = true;

							damaged.fitness-=percF;

							if(sdb.legalPunishment(agent, neigh))
							{
								agent.numNeighPunished++;
								agent.isPunished = true;
							}

							break;
						}
					}
				}
		}
	}

	@Override
	public Double2D consistency(Human agent,Bag b, Continuous2D humans)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);

		double x = 0; 
		double y= 0;
		int i =0;
		int count = 0;
		double lambda = 0;
		double dist = 0;
		double peso = 0;

		for(i=0;i<b.numObjs;i++)
			lambda += (1 - (Math.abs(agent.dna-((Human)b.get(i)).dna)/10.0));

		for(i=0;i<b.numObjs;i++)
		{
			Human h = (Human)(b.objs[i]);
			dist = Math.abs(agent.dna - h.dna)/10;

			peso = ((1 - dist)/lambda)*b.numObjs;

			agent.dx = humans.tdx(agent.loc.x,h.loc.x);
			agent.dy = humans.tdy(agent.loc.y,h.loc.y);
			Double2D m = ((Human)b.objs[i]).momentum();
			count++;
			
			x += m.x*peso;
			y += m.y*peso;

		}	

		if (count > 0) { x /= count; y /= count; }
		return new Double2D(x,y);
	}

	@Override
	public Double2D cohesion(Human agent,Bag b, Continuous2D humans)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);

		double x = 0; 
		double y= 0;        
		int count = 0;
		int i =0;
		double lambda = 0;
		double dist = 0;
		double peso = 0;

		for(i=0;i<b.numObjs;i++)
			lambda += (1 - (Math.abs(agent.dna-((Human)b.get(i)).dna)/10.0));

		for(i=0;i<b.numObjs;i++)
		{
			Human h = (Human)(b.objs[i]);
			dist = Math.abs(agent.dna - h.dna)/10;

			peso = ((1 - dist)/lambda)*b.numObjs;
			double dx = humans.tdx(agent.loc.x, h.loc.x);
			double dy = humans.tdy(agent.loc.y,h.loc.y);
			count++;
			x += dx * peso;
			y += dy * peso;
		}

		if (count > 0) { x /= count; y /= count; }
		return new Double2D(-x/10,-y/10);
	}

	@Override
	public Double2D avoidance(Human agent,Bag b, Continuous2D humans)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);
		double x = 0;
		double y = 0;
		int i=0;
		int count = 0;
		double lambda = 0;
		double dist = 0;
		double peso = 0;

		for(i=0;i<b.numObjs;i++)
			lambda += (1 - (Math.abs(agent.dna-((Human)b.get(i)).dna)/10.0));

		for(i=0;i<b.numObjs;i++)
		{
			Human h = (Human)(b.objs[i]);

			if(h != agent)
			{
				dist = Math.abs(agent.dna - h.dna)/10;
				peso = ((1 - dist)/lambda)*b.numObjs;
				double dx = humans.tdx(agent.loc.x, h.loc.x);
				double dy = humans.tdy(agent.loc.y,h.loc.y);
				double lensquared = dx*dx+dy*dy;
				count++;
				x += (dx/(lensquared*lensquared + 1));
				y += (dy/(lensquared*lensquared + 1));
			}
		}

		if (count > 0) { x /= count; y /= count; }
		return new Double2D(400*x,400*y);     
	}
	
	@Override
	public Double2D randomness(SociallyDamagingBehavior state)
	{
		double x = state.random.nextDouble() * 2 - 1.0;
		double y = state.random.nextDouble() * 2 - 1.0;
		return new Double2D(x,y);
	}
	
	@Override
	public void calculateCEI(Human a, SociallyDamagingBehavior sdb, Bag n)	//Calcola l'actual social influence
	{
		Bag neigh = n;
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

			if(H_neigh != DH_neigh)
			{
				if(H_neigh > DH_neigh)
					a.ce = (SociallyDamagingBehavior.SOCIAL_INFLUENCE * H_neigh);
				if(DH_neigh < H_neigh)
					a.ce = (SociallyDamagingBehavior.SOCIAL_INFLUENCE * DH_neigh);
			}
			else
				a.ce = 0;
		}
		double t1 = a.dna;
		double t2 = (10 - a.dna);
		if(t1 > 72)
			a.tpi = t1;
		if(t2 > t1)
			a.tpi = t2;
		a.cei = ((a.tpi/100)*a.ce);	
	} 

	public Double2D momentum(Double2D lastd)
	{
		return lastd;
	}

	@Override
	public Double2D move(SimState state, Double2D loc, Bag n) {

		//		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
		//
		//		Bag neigh = n;
		//		double dx = 0.0;
		//		double dy = 0.0;
		//		double y_max = sdb.height;
		//		double x_max = sdb.width;
		//		double ip = 0.0;
		//		double sin = 0.0;
		//		double raggio = sdb.neighborhood;
		//		int fx = 0;
		//		int fy = 0;
		//		int q0 = 0;
		//		int q1 = 0;
		//		int q2 = 0;
		//		int q3 = 0;
		//		int q4 = 0;
		//		int q5 = 0;
		//		int q6 = 0;
		//		int q7 = 0;
		//		
		//		//Distanze
		//		for(Object o:neigh)
		//		{
		//			Agent n_a=(Agent)o;
		//			dx = Math.abs(loc.x-n_a.loc.x)>raggio?x_max-Math.abs(loc.x-n_a.loc.x):Math.abs(loc.x-n_a.loc.x);
		//			dy = Math.abs(loc.y-n_a.loc.y)>raggio?y_max-Math.abs(loc.y-n_a.loc.y):Math.abs(loc.y-n_a.loc.y);
		//			
		//			//seno
		//			ip = Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2));
		//			sin = dy/ip;
		//			
		//			//Quadranti
		//			if(Math.abs(loc.x-n_a.loc.x)>raggio)
		//				fx = (loc.x-dx)<0?1:0;
		//			else
		//				fx = (n_a.loc.x<=loc.x)?1:0;
		//			
		//			if(Math.abs(loc.y-n_a.loc.y)>raggio)
		//				fy = (loc.y-dy)<0?1:0;
		//			else
		//				fy = (n_a.loc.y<=loc.y)?1:0;
		//			
		//			//Quadri
		//			if(fx==0 && fy==1)
		//				if((sin>=0) && (sin<Math.sqrt(2)/2))
		//					q0++;
		//				else
		//					q1++;
		//			
		//			if(fx==1 && fy==1)
		//				if((sin>=0) && (sin<Math.sqrt(2)/2))
		//					q3++;
		//				else
		//					q2++;
		//			
		//			if(fx==1 && fy==0)
		//				if((sin>=0) && (sin<Math.sqrt(2)/2))
		//					q4++;
		//				else
		//					q5++;
		//			
		//			if(fx==0 && fy==0)
		//				if((sin>=0) && (sin<Math.sqrt(2)/2))
		//					q7++;
		//				else
		//					q6++;
		//		}
		//		
		//		System.out.println("q0="+q0+"\nq1="+q1+"\nq2="+q2+"\nq3="+q3+"\nq4="+q4+"\nq5="+q5+"\nq6="+q6+"\nq7="+q7);
		//		
		return null;
	}


	@Override
	public void socialInfluence(Human agent, SimState state, Bag neigh) {
		// TODO Auto-generated method stub

		int H_neigh=0;
		int DH_neigh=0;
		Human a = agent;

		if(neigh.size()>0)
		{
			for(Object o:neigh)
			{
				Human n_a=(Human)o;
				if(n_a.behavior instanceof Honest) H_neigh++;
				else DH_neigh++;

			}
		}

		if(H_neigh>=DH_neigh) //if nethonest
		{
			if(a.honestAction)
			{
				double newDna = a.dna + (a.ce - a.cei);
				if(newDna > 10)
					newDna = 10;
				a.dna = newDna; 
			}
			else
			{
				double newDna = a.dna + (a.ce - a.cei);
				if(newDna > 10)
					newDna = 10;
				a.dna = newDna; 
			}
		}
		else	//if netdishonest
		{
			if(a.honestAction)
			{
				double newDna = a.dna - (a.ce - a.cei);
				if(newDna > 10)
					newDna = 10;
				a.dna = newDna; 
			}
			else
			{
				double newDna = a.dna - (a.ce - a.cei);
				if(newDna > 10)
					newDna = 10;
				a.dna = newDna; 
			}
		}
	}

}