package sim.app.mason.SociallyDamagingBehav;

import java.util.Iterator;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class Dishonest extends Behaviour{

	

	@Override
	public void action(Agent agent,SimState state,Bag neigh) {
		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
		
		int action = sdb.chooseAction(agent.dna);
		if(action == 1)
		{
			agent.honestAction=true;
			if(sdb.tryHonestAgentAction())
				agent.fitness+=sdb.HONEST_PAYOFF;
		}
		else
		{
			agent.honestAction=false;
			if(sdb.tryDisHonestAgentAction())
			{
				agent.fitness+=sdb.DAMAGING_PAYOFF;
				if(neigh.size()!=0)
					((Agent)neigh.get(state.random.nextInt(neigh.size()))).fitness-=sdb.DAMAGING_PAYOFF;
				sdb.legalPunishment(agent, neigh);
			}
		}
//		if((!sdb.chooseAction(agent.dna)) && sdb.tryDisHonestAgentAction())
//		{
//			agent.fitness+=sdb.DAMAGING_PAYOFF;
//			if(neigh.size()!=0)
//			((Agent)neigh.get(state.random.nextInt(neigh.size()))).fitness-=sdb.DAMAGING_PAYOFF;
//			sdb.legalPunishment(agent, neigh);
//		}
		
	}
	public Double2D consistency(Agent agent,Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);

		double x = 0; 
		double y= 0;
		int i =0;
		int count = 0;
//		for(i=0;i<b.numObjs;i++)
//		{
//			Agent other = (Agent)(b.objs[i]);
//			if (!other.dead && other instanceof Dishonest)
//			{
//				double dx = flockers.tdx(loc.x,other.loc.x);
//				double dy = flockers.tdy(loc.y,other.loc.y);
//				Double2D m = ((Agent)b.objs[i]).momentum();
//				count++;
//				x += m.x;
//				y += m.y;
//			}
//		}
		count=1;
		Agent a=(Agent)b.get(0);
		for(i=1;i<b.numObjs && a!=null;i++)
		{
			Agent a2=(Agent)b.get(i);
			if(a2!=null && a2.fitness > a.fitness)
				a=a2;
			else
				break;
		
		}
		if(a!=null)
		{
			double dx = flockers.tdx(agent.loc.x,a.loc.x);
			double dy = flockers.tdy(agent.loc.y,a.loc.y);
			Double2D m = ((Agent)a).momentum();
			count++;
			x += m.x;
			y += m.y;
		}
		
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(x,y);
	}

	public Double2D cohesion(Agent agent,Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);

		double x = 0; 
		double y= 0;        

		int count = 0;
		int i =0;
//		for(i=0;i<b.numObjs;i++)
//		{
//			Agent other = (Agent)(b.objs[i]);
//			if (!other.dead && other instanceof Dishonest)
//			{
//				double dx = flockers.tdx(loc.x,other.loc.x);
//				double dy = flockers.tdy(loc.y,other.loc.y);
//				count++;
//				x += dx;
//				y += dy;
//			}
//		}
		count=1;
		Agent a=(Agent)b.get(0);
		for(i=1;i<b.numObjs && a!=null;i++)
		{
			Agent a2=(Agent)b.get(i);
			if(a2!=null && a2.fitness > a.fitness)
				a=a2;
			else
				break;
			
		}
		if(a!=null)
		{
			double dx = flockers.tdx(agent.loc.x,a.loc.x);
			double dy = flockers.tdy(agent.loc.y,a.loc.y);
			count++;
			x += dx;
			y += dy;
		}
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(-x/10,-y/10);
	}

	public Double2D avoidance(Agent agent,Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);
		double x = 0;
		double y = 0;

		int i=0;
		int count = 0;

//		for(i=0;i<b.numObjs;i++)
//		{
//			Agent other = (Agent)(b.objs[i]);
//			if (other != agent && other.behavior instanceof Dishonest )
//			{
//				double dx = flockers.tdx(agent.loc.x,other.loc.x);
//				double dy = flockers.tdy(agent.loc.y,other.loc.y);
//				double lensquared = dx*dx+dy*dy;
//				count++;
//				x += dx/(lensquared*lensquared + 1);
//				y += dy/(lensquared*lensquared + 1);
//			}
//		}
		count=1;
		Agent a=(Agent)b.get(0);
		for(i=1;i<b.numObjs && a!=null;i++)
		{
			Agent a2=(Agent)b.get(i);
			if(a2!=null && a2.fitness > a.fitness)
				a=a2;
			else
				break;
			
		}
		if(a!=null)
		{
			double dx = flockers.tdx(agent.loc.x,a.loc.x);
			double dy = flockers.tdy(agent.loc.y,a.loc.y);
			double lensquared = dx*dx+dy*dy;
			count++;
			x += dx/(lensquared*lensquared + 1);
			y += dy/(lensquared*lensquared + 1);
		}
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(400*x,400*y);      
   
	}

	@Override
	public void calculateCEI(Agent a, SociallyDamagingBehavior sdb, Bag n)	//Calcola l'actual social influence
	{
		Bag neigh = n;
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

			if(H_neigh != DH_neigh)
			{
				if(H_neigh > DH_neigh)
					a.ce = (sdb.SOCIAL_INFLUENCE * H_neigh);
				if(DH_neigh < H_neigh)
					a.ce = (sdb.SOCIAL_INFLUENCE * DH_neigh);
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
	
	@Override
	public Double2D move(SimState state, Double2D loc, Bag neigh) 
	{
		return null;
//		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
//		Bag neigh=super.getNeighbors();
//				
//		Direction Q0=new Direction(sdb.jump, sdb.jump);
//		Direction Q1=new Direction(sdb.jump, sdb.jump);
//		Direction Q2=new Direction(sdb.jump, sdb.jump);
//		Direction Q3=new Direction(sdb.jump, sdb.jump);
//		Direction Q4=new Direction(sdb.jump, sdb.jump);
//		Direction Q5=new Direction(sdb.jump, sdb.jump);
//		Direction Q6=new Direction(sdb.jump, sdb.jump);;
//		Direction Q7=new Direction(sdb.jump, sdb.jump);
//		
//	  
//		ArrayList<Direction> all_dir=new ArrayList<Direction>();
//		all_dir.add(Q0);
//		all_dir.add(Q1);
//		all_dir.add(Q2);
//		all_dir.add(Q3);
//		all_dir.add(Q4);
//		all_dir.add(Q5);
//		all_dir.add(Q6);
//		all_dir.add(Q7);
//	
//		
//		for(Object o:neigh)
//		{
//			Agent n_a=(Agent)o;
//			
//			if(n_a instanceof Honest && (n_a.loc.y -  loc.y!=0) && (n_a.loc.x -  loc.x)!=0) 
//			{
//				double m=(n_a.loc.y -  loc.y)/(n_a.loc.x -  loc.x);
////				System.out.println(m);
////				double teta=Math.toRadians(m);
////				System.out.println(teta);
////				if(teta>0 && teta <= 45) Q0.add((Honest)n_a);
////				else if(teta>0 && teta <= 90)  Q1.add((Honest)n_a);
////				else if(teta>0 && teta <= 135) Q2.add((Honest)n_a);
////				else if(teta>0 && teta <= 180)  Q3.add((Honest)n_a);
////				else if(teta>0 && teta <= 225)  Q4.add((Honest)n_a);
////				else if(teta>0 && teta <= 270)  Q5.add((Honest)n_a);
////				else if(teta>0 && teta <= 315)  Q6.add((Honest)n_a);
////				else if(teta>0 && teta <= 360)  Q7.add((Honest)n_a);
//				
//				if(n_a.loc.x > loc.x && n_a.loc.y > loc.y)
//				{
//					double ipo=Math.sqrt(Math.pow((n_a.loc.x-loc.x),2)
//							+ Math.pow((n_a.loc.y-loc.y),2) );
//					double sin=ipo/(n_a.loc.x-loc.x);
//					if(sin>0 && sin<=Math.sqrt(2)/2)
//					
//						 Q0.add((Honest)n_a);
//					else
//						 Q1.add((Honest)n_a);
//					
//				}else
//					if(n_a.loc.x < loc.x && n_a.loc.y > loc.y)
//					{
//						double ipo=Math.sqrt(Math.pow(((-1*n_a.loc.x)-(-1*loc.x)),2) 
//								+ Math.pow((n_a.loc.y-loc.y),2) );
//						double sin=ipo/((-1*n_a.loc.x)-(-1*loc.x));
//						if(sin>0 && sin<=Math.sqrt(2)/2)
//						
//							 Q2.add((Honest)n_a);
//						else
//							 Q3.add((Honest)n_a);
//						
//					}else
//						if(n_a.loc.x < loc.x && n_a.loc.y < loc.y)
//						{
//							double ipo=Math.sqrt(Math.pow(((-1*n_a.loc.x)-(-1*loc.x)),2) 
//									+ Math.pow(((-1*n_a.loc.y)-(-1*loc.y)),2) );
//							double sin=ipo/((-1*n_a.loc.x)-(-1*loc.x));
//							if(sin>0 && sin<=Math.sqrt(2)/2)
//							
//								 Q4.add((Honest)n_a);
//							else
//								 Q5.add((Honest)n_a);
//							
//			
//						}else
//							if(n_a.loc.x > loc.x && n_a.loc.y < loc.y)
//							{
//								double ipo=Math.sqrt(Math.pow(((n_a.loc.x)-(loc.x)),2) 
//										+ Math.pow(((-1*n_a.loc.y)-(-1*loc.y)),2) );
//								double sin=ipo/((n_a.loc.x)-(loc.x));
//								if(sin>0 && sin<=Math.sqrt(2)/2)
//								
//									 Q6.add((Honest)n_a);
//								else
//									 Q7.add((Honest)n_a);
//							}
//							
//				
//			}
//
//		}
//		
//		Collections.sort(all_dir);
//		
//		int min=all_dir.get(0).size();
//		if(min!=0)
//		{
//			Agent aa=all_dir.get(0).get(state.random.nextInt(all_dir.get(0).size()));
//			return aa.loc;
//		}
//		else
//		{
//			//System.out.println(all_dir.get(0).dx+" "+all_dir.get(0).dy);
//			return new Double2D(loc.x+all_dir.get(0).dx,
//					loc.y+all_dir.get(0).dy);
//		}
//		
	}

	@Override
	public void socialInfluence(Agent agent, SimState state, Bag neigh) {
		// TODO Auto-generated method stub
		
		int H_neigh=0;
		int DH_neigh=0;
		Agent a = agent;
		
		if(neigh.size()>0)
		{
			for(Object o:neigh)
			{
				Agent n_a=(Agent)o;
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
