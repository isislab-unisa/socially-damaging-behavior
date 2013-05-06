package dmason.sim.app.SociallyDamagingBehav;

import dmason.sim.engine.DistributedState;
import dmason.sim.field.continuous.DContinuous2D;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

public class Dishonest extends DBehaviour {

	@Override
	public void action(DHuman agent,SimState state,Bag neigh) {
		DSociallyDamagingBehavior dsdb=(DSociallyDamagingBehavior)state;
		
		int action = dsdb.chooseAction(agent.dna);
		if(action == 1)
		{
			agent.honestAction=true;
			if(dsdb.tryHonestAgentAction())
				agent.fitness+=dsdb.HONEST_PAYOFF;
		}
		else
		{
			agent.honestAction=false;
			if(dsdb.tryDisHonestAgentAction())
			{
				agent.fitness+=dsdb.DAMAGING_PAYOFF;
				if(neigh.size()!=0)
					((DHuman)neigh.get(state.random.nextInt(neigh.size()))).fitness-=dsdb.DAMAGING_PAYOFF;
				dsdb.legalPunishment(agent, neigh);
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
	public Double2D consistency(DHuman agent,Bag b, DContinuous2D humans)
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
		DHuman a=(DHuman)b.get(0);
		for(i=1;i<b.numObjs && a!=null;i++)
		{
			DHuman a2=(DHuman)b.get(i);
			if(a2!=null && a2.fitness > a.fitness)
				a=a2;
			else
				break;
		
		}
		if(a!=null)
		{
			double dx = humans.tdx(agent.loc.x,a.loc.x);
			double dy = humans.tdy(agent.loc.y,a.loc.y);
			Double2D m = ((DHuman)a).momentum();
			count++;
			x += m.x;
			y += m.y;
		}
		
		if (count > 0) { x /= count; y /= count; }
		return new Double2D(x,y);
	}

	public Double2D cohesion(DHuman agent,Bag b, DContinuous2D humans)
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
//				x += dx;
//				y += dy;
//			}
//		}
		count=1;
		DHuman a=(DHuman)b.get(0);
		for(i=1;i<b.numObjs && a!=null;i++)
		{
			DHuman a2=(DHuman)b.get(i);
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

	public Double2D avoidance(DHuman agent,Bag b, DContinuous2D humans)
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
//				double dx = humans.tdx(agent.loc.x,other.loc.x);
//				double dy = humans.tdy(agent.loc.y,other.loc.y);
//				double lensquared = dx*dx+dy*dy;
//				count++;
//				x += dx/(lensquared*lensquared + 1);
//				y += dy/(lensquared*lensquared + 1);
//			}
//		}
		count=1;
		DHuman a=(DHuman)b.get(0);
		for(i=1;i<b.numObjs && a!=null;i++)
		{
			DHuman a2=(DHuman)b.get(i);
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
	
	public Double2D randomness(MersenneTwisterFast r)
	{
		double x = r.nextDouble() * 2 - 1.0;
		double y = r.nextDouble() * 2 - 1.0;
		double l = Math.sqrt(x * x + y * y);
		return new Double2D(0.05*x/l,0.05*y/l);
	}

	@Override
	public void calculateCEI(DHuman a, DSociallyDamagingBehavior sdb, Bag n)	//Calcola l'actual social influence
	{
		Bag neigh = n;
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
	public Double2D move(DistributedState state, Double2D loc, Bag n) {
		
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
	public void socialInfluence(DHuman agent, DistributedState state, Bag neigh) {
		// TODO Auto-generated method stub
		
		int H_neigh=0;
		int DH_neigh=0;
		DHuman a = agent;
		
		if(neigh.size()>0)
		{
			for(Object o:neigh)
			{
				DHuman n_a=(DHuman)o;
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