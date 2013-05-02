package sim.app.mason.SociallyDamagingBehav;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class Honest extends Behaviour{

	@Override
	public void action(Agent agent,SimState state,Bag neigh) 
	{
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
			}
		}
		
//		if(sdb.chooseAction(agent.dna) && sdb.tryHonestAgentAction())
//		{
//			agent.fitness+=sdb.HONEST_PAYOFF;
//		}
	}
	
	public Double2D consistency(Agent agent,Bag b, Continuous2D humans)
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
//				double dx = humans.tdx(loc.x,other.loc.x);
//				double dy = humans.tdy(loc.y,other.loc.y);
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
			double dx = humans.tdx(agent.loc.x,a.loc.x);
			double dy = humans.tdy(agent.loc.y,a.loc.y);
			Double2D m = ((Agent)a).momentum();
			count++;
			x += m.x;
			y += m.y;
		}
		
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(x,y);
	}

	public Double2D cohesion(Agent agent,Bag b, Continuous2D humans)
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
//				double dx = humans.tdx(loc.x,other.loc.x);
//				double dy = humans.tdy(loc.y,other.loc.y);
//				count++;
//				x -= dx;
//				y -= dy;
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
			double dx = humans.tdx(agent.loc.x,a.loc.x);
			double dy = humans.tdy(agent.loc.y,a.loc.y);
			count++;
			x += dx;
			y += dy;
		}
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(-x/10,-y/10);
	}

	public Double2D avoidance(Agent agent,Bag b, Continuous2D humans)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);
		double x = 0;
		double y = 0;

		int i=0;
		int count = 0;

//		for(i=0;i<b.numObjs;i++)
//		{
//			Agent other = (Agent)(b.objs[i]);
//			if (other != agent )
//			{
//				double dx = humans.tdx(agent.loc.x,other.loc.x);
//				double dy = humans.tdy(agent.loc.y,other.loc.y);
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
			double dx = humans.tdx(agent.loc.x,a.loc.x);
			double dy = humans.tdy(agent.loc.y,a.loc.y);
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
	public Double2D move(SimState state,Double2D loc, Bag n) {

		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;

		Bag neigh = n;
		double dx = 0.0;
		double dy = 0.0;
		double y_max = sdb.height;
		double x_max = sdb.width;
		double ip = 0.0;
		double sin = 0.0;
		double raggio = sdb.neighborhood;
		int fx = 0;
		int fy = 0;
		int q0 = 0;
		int q1 = 0;
		int q2 = 0;
		int q3 = 0;
		int q4 = 0;
		int q5 = 0;
		int q6 = 0;
		int q7 = 0;
		
		//Distanze
		for(Object o:neigh)
		{
			Agent n_a=(Agent)o;
			dx = Math.abs(loc.x-n_a.loc.x)>raggio?x_max-Math.abs(loc.x-n_a.loc.x):Math.abs(loc.x-n_a.loc.x);
			dy = Math.abs(loc.y-n_a.loc.y)>raggio?y_max-Math.abs(loc.y-n_a.loc.y):Math.abs(loc.y-n_a.loc.y);
			
			//seno
			ip = Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2));
			sin = dy/ip;
			
			//Quadranti
			if(Math.abs(loc.x-n_a.loc.x)>raggio)
				fx = (loc.x-dx)<0?1:0;
			else
				fx = (n_a.loc.x<=loc.x)?1:0;
			
			if(Math.abs(loc.y-n_a.loc.y)>raggio)
				fy = (loc.y-dy)<0?1:0;
			else
				fy = (n_a.loc.y<=loc.y)?1:0;
			
			//Quadri
			if(fx==0 && fy==1)
				if((sin>=0) && (sin<Math.sqrt(2)/2))
					q0++;
				else
					q1++;
			
			if(fx==1 && fy==1)
				if((sin>=0) && (sin<Math.sqrt(2)/2))
					q3++;
				else
					q2++;
			
			if(fx==1 && fy==0)
				if((sin>=0) && (sin<Math.sqrt(2)/2))
					q4++;
				else
					q5++;
			
			if(fx==0 && fy==0)
				if((sin>=0) && (sin<Math.sqrt(2)/2))
					q7++;
				else
					q6++;
		}
		
		System.out.println("q0="+q0+"\nq1="+q1+"\nq2="+q2+"\nq3="+q3+"\nq4="+q4+"\nq5="+q5+"\nq6="+q6+"\nq7="+q7);
		
		return null;
//		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
//		Bag neigh=agent.getNeighbors();
//		
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
//			if(n_a instanceof Dishonest && (n_a.loc.y -  loc.y!=0) && (n_a.loc.x -  loc.x)!=0) 
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
//						 Q0.add((Dishonest)n_a);
//					else
//						 Q1.add((Dishonest)n_a);
//					
//				}else
//					if(n_a.loc.x < loc.x && n_a.loc.y > loc.y)
//					{
//						double ipo=Math.sqrt(Math.pow(((-1*n_a.loc.x)-(-1*loc.x)),2) 
//								+ Math.pow((n_a.loc.y-loc.y),2) );
//						double sin=ipo/((-1*n_a.loc.x)-(-1*loc.x));
//						if(sin>0 && sin<=Math.sqrt(2)/2)
//						
//							 Q2.add((Dishonest)n_a);
//						else
//							 Q3.add((Dishonest)n_a);
//						
//					}else
//						if(n_a.loc.x < loc.x && n_a.loc.y < loc.y)
//						{
//							double ipo=Math.sqrt(Math.pow(((-1*n_a.loc.x)-(-1*loc.x)),2) 
//									+ Math.pow(((-1*n_a.loc.y)-(-1*loc.y)),2) );
//							double sin=ipo/((-1*n_a.loc.x)-(-1*loc.x));
//							if(sin>0 && sin<=Math.sqrt(2)/2)
//							
//								 Q4.add((Dishonest)n_a);
//							else
//								 Q5.add((Dishonest)n_a);
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
//									 Q6.add((Dishonest)n_a);
//								else
//									 Q7.add((Dishonest)n_a);
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
