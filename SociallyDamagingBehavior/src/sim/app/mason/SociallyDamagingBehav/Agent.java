
package sim.app.mason.SociallyDamagingBehav;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.*;
import ec.util.*;

public class Agent extends OvalPortrayal2D implements Steppable//, sim.portrayal.Orientable2D 
{
	private static final long serialVersionUID = 1;

	public Double2D loc = new Double2D(0,0);
	public Double2D lastd = new Double2D(0,0);
	public Continuous2D flockers;
	public SociallyDamagingBehavior theFlock;
	public boolean dead = false;
	public static Color behav_color;
	public Behaviour behavior;

	/*SDB*/
	public double fitness;
	public float dna;
	
	
	/*SDB*/
 

	public Agent(Double2D location,SimState state,float dna) { 
//		super(new SimplePortrayal2D(), 0, 4.0,behav_color,OrientedPortrayal2D.SHAPE_COMPASS);

		loc = location;
		fitness=state.random.nextInt(100);
		this.dna=dna;
		behavior=(dna<5)?new Honest():new Dishonest();
		behav_color=(dna<5)?Color.GREEN:Color.RED;
	}

	  public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
       {
     
	       // this code was stolen from OvalPortrayal2D
		   graphics.setColor(behav_color);
	       int x = (int)(info.draw.x - info.draw.width / 2.0);
	       int y = (int)(info.draw.y - info.draw.height / 2.0);
	       int width = (int)(info.draw.width);
	       int height = (int)(info.draw.height);
	       graphics.fillOval(x,y,width, height);

       }
	   
	public Bag getNeighbors()
	{
		return flockers.getNeighborsExactlyWithinDistance(loc, theFlock.neighborhood, true);
	}

	public double getOrientation() { return orientation2D(); }
	public boolean isDead() { return dead; }
	public void setDead(boolean val) { dead = val; }

	public void setOrientation2D(double val)
	{
		lastd = new Double2D(Math.cos(val),Math.sin(val));
	}

	public double orientation2D()
	{
		if (lastd.x == 0 && lastd.y == 0) return 0;
		return Math.atan2(lastd.y, lastd.x);
	}

	public Double2D momentum()
	{
		return lastd;
	}
	
	
	public Double2D randomness(MersenneTwisterFast r)
	{
		double x = r.nextDouble() * 2 - 1.0;
		double y = r.nextDouble() * 2 - 1.0;
		double l = Math.sqrt(x * x + y * y);
		return new Double2D(0.05*x/l,0.05*y/l);
	}

	public void step(SimState state)
	{      
		SociallyDamagingBehavior sdb = (SociallyDamagingBehavior)state;

		if (dead) return;
		if (state.schedule.getTime()==0 || (state.schedule.getTime()%sdb.EPOCH)!=0)
		{
			final SociallyDamagingBehavior flock = (SociallyDamagingBehavior)state;
			loc = flock.flockers.getObjectLocation(this);
	
		
	
			Bag b = getNeighbors();
	//
			Double2D avoid = behavior.avoidance(this,b,flock.flockers);
			Double2D cohe = behavior.cohesion(this,b,flock.flockers);
			Double2D rand = randomness(flock.random);
			Double2D cons = behavior.consistency(this,b,flock.flockers);
			Double2D mome = momentum();
	
			double dx = flock.cohesion * cohe.x + flock.avoidance * avoid.x + flock.consistency* cons.x + flock.randomness * rand.x + flock.momentum * mome.x;
			double dy = flock.cohesion * cohe.y + flock.avoidance * avoid.y + flock.consistency* cons.y + flock.randomness * rand.y + flock.momentum * mome.y;
	
			// renormalize to the given step size
			double dis = Math.sqrt(dx*dx+dy*dy);
			if (dis>0)
			{
				dx = dx / dis * flock.jump;
				dy = dy / dis * flock.jump;
			}
			
			behavior.action(this,state, b);
		//	loc=move(state, loc);
			lastd = new Double2D(dx,dy);
			loc = new Double2D(flock.flockers.stx(loc.x + dx), flock.flockers.sty(loc.y + dy));
			flock.flockers.setObjectLocation(this, loc);
		}else dead=true;
	}
	class Direction extends ArrayList<Agent> implements Comparable
	{
		double dx;
		double dy;
		public Direction(double ddx,double ddy)
		{
			dx=ddx;
			dy=ddy;
		}
		@Override
		public int compareTo(Object o) {
			if(((Direction)o).size() < this.size()) return -1;
			else if(((Direction)o).size() > this.size()) return 1;
			return 0;
		}
	}
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	public float getDna() {
		return dna;
	}
	public void setDna(float dna) {
		this.dna = dna;
	}


}
