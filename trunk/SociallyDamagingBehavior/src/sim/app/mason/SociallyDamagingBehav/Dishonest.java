package sim.app.mason.SociallyDamagingBehav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class Dishonest extends Agent{

	public Dishonest(Double2D location,SimState state,float dna) {
		super(location,state,dna);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void action(SimState state,Bag neigh) {
		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
		if(!sdb.askHonestAgentAction(dna) && sdb.tryDisHonestAgentAction())
		{
			this.fitness+=sdb.DAMAGING_PAYOFF;
			if(neigh.size()!=0)
			((Agent)neigh.get(state.random.nextInt(neigh.size()))).fitness-=sdb.DAMAGING_PAYOFF;
			sdb.legalPunishment(this, neigh);
		}
		
	}
	public Double2D consistency(Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);

		double x = 0; 
		double y= 0;
		int i =0;
		int count = 0;
		for(i=0;i<b.numObjs;i++)
		{
			Agent other = (Agent)(b.objs[i]);
			if (!other.dead && other instanceof Dishonest)
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				Double2D m = ((Agent)b.objs[i]).momentum();
				count++;
				x += m.x;
				y += m.y;
			}
		}
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(x,y);
	}

	public Double2D cohesion(Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);

		double x = 0; 
		double y= 0;        

		int count = 0;
		int i =0;
		for(i=0;i<b.numObjs;i++)
		{
			Agent other = (Agent)(b.objs[i]);
			if (!other.dead && other instanceof Dishonest)
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				count++;
				x += dx;
				y += dy;
			}
		}
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(-x/10,-y/10);
	}

	public Double2D avoidance(Bag b, Continuous2D flockers)
	{
		if (b==null || b.numObjs == 0) return new Double2D(0,0);
		double x = 0;
		double y = 0;

		int i=0;
		int count = 0;

		for(i=0;i<b.numObjs;i++)
		{
			Agent other = (Agent)(b.objs[i]);
			if (other != this && other instanceof Dishonest )
			{
				double dx = flockers.tdx(loc.x,other.loc.x);
				double dy = flockers.tdy(loc.y,other.loc.y);
				double lensquared = dx*dx+dy*dy;
				count++;
				x += dx/(lensquared*lensquared + 1);
				y += dy/(lensquared*lensquared + 1);
			}
		}
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(400*x,400*y);      
	}

	@Override
	public Double2D move(SimState state,Double2D loc) {
		SociallyDamagingBehavior sdb=(SociallyDamagingBehavior)state;
		Bag neigh=super.getNeighbors();
		
		
		Direction Q0=new Direction(sdb.jump, sdb.jump);
		Direction Q1=new Direction(sdb.jump, sdb.jump);
		Direction Q2=new Direction(sdb.jump, sdb.jump);
		Direction Q3=new Direction(sdb.jump, sdb.jump);
		Direction Q4=new Direction(sdb.jump, sdb.jump);
		Direction Q5=new Direction(sdb.jump, sdb.jump);
		Direction Q6=new Direction(sdb.jump, sdb.jump);;
		Direction Q7=new Direction(sdb.jump, sdb.jump);
		
	  
		ArrayList<Direction> all_dir=new ArrayList<Direction>();
		all_dir.add(Q0);
		all_dir.add(Q1);
		all_dir.add(Q2);
		all_dir.add(Q3);
		all_dir.add(Q4);
		all_dir.add(Q5);
		all_dir.add(Q6);
		all_dir.add(Q7);
	
		
		for(Object o:neigh)
		{
			Agent n_a=(Agent)o;
			
			if(n_a instanceof Honest && (n_a.loc.y -  loc.y!=0) && (n_a.loc.x -  loc.x)!=0) 
			{
				double m=(n_a.loc.y -  loc.y)/(n_a.loc.x -  loc.x);
//				System.out.println(m);
//				double teta=Math.toRadians(m);
//				System.out.println(teta);
//				if(teta>0 && teta <= 45) Q0.add((Honest)n_a);
//				else if(teta>0 && teta <= 90)  Q1.add((Honest)n_a);
//				else if(teta>0 && teta <= 135) Q2.add((Honest)n_a);
//				else if(teta>0 && teta <= 180)  Q3.add((Honest)n_a);
//				else if(teta>0 && teta <= 225)  Q4.add((Honest)n_a);
//				else if(teta>0 && teta <= 270)  Q5.add((Honest)n_a);
//				else if(teta>0 && teta <= 315)  Q6.add((Honest)n_a);
//				else if(teta>0 && teta <= 360)  Q7.add((Honest)n_a);
				
				if(n_a.loc.x > loc.x && n_a.loc.y > loc.y)
				{
					double ipo=Math.sqrt(Math.pow((n_a.loc.x-loc.x),2)
							+ Math.pow((n_a.loc.y-loc.y),2) );
					double sin=ipo/(n_a.loc.x-loc.x);
					if(sin>0 && sin<=Math.sqrt(2)/2)
					
						 Q0.add((Honest)n_a);
					else
						 Q1.add((Honest)n_a);
					
				}else
					if(n_a.loc.x < loc.x && n_a.loc.y > loc.y)
					{
						double ipo=Math.sqrt(Math.pow(((-1*n_a.loc.x)-(-1*loc.x)),2) 
								+ Math.pow((n_a.loc.y-loc.y),2) );
						double sin=ipo/((-1*n_a.loc.x)-(-1*loc.x));
						if(sin>0 && sin<=Math.sqrt(2)/2)
						
							 Q2.add((Honest)n_a);
						else
							 Q3.add((Honest)n_a);
						
					}else
						if(n_a.loc.x < loc.x && n_a.loc.y < loc.y)
						{
							double ipo=Math.sqrt(Math.pow(((-1*n_a.loc.x)-(-1*loc.x)),2) 
									+ Math.pow(((-1*n_a.loc.y)-(-1*loc.y)),2) );
							double sin=ipo/((-1*n_a.loc.x)-(-1*loc.x));
							if(sin>0 && sin<=Math.sqrt(2)/2)
							
								 Q4.add((Honest)n_a);
							else
								 Q5.add((Honest)n_a);
							
			
						}else
							if(n_a.loc.x > loc.x && n_a.loc.y < loc.y)
							{
								double ipo=Math.sqrt(Math.pow(((n_a.loc.x)-(loc.x)),2) 
										+ Math.pow(((-1*n_a.loc.y)-(-1*loc.y)),2) );
								double sin=ipo/((n_a.loc.x)-(loc.x));
								if(sin>0 && sin<=Math.sqrt(2)/2)
								
									 Q6.add((Honest)n_a);
								else
									 Q7.add((Honest)n_a);
							}
							
				
			}

		}
		
		Collections.sort(all_dir);
		
		int min=all_dir.get(0).size();
		if(min!=0)
		{
			Agent aa=all_dir.get(0).get(state.random.nextInt(all_dir.get(0).size()));
			return aa.loc;
		}
		else
		{
			//System.out.println(all_dir.get(0).dx+" "+all_dir.get(0).dy);
			return new Double2D(loc.x+all_dir.get(0).dx,
					loc.y+all_dir.get(0).dy);
		}
		
	}

}
