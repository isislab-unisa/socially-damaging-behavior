package sim.app.dmason.SociallyDamagingBehav;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;

import dmason.sim.engine.DistributedState;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.util.Util;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Orientable2D;
import sim.util.*;
import ec.util.*;

public class DHuman extends RemoteHuman<Double2D> implements Steppable, Orientable2D
{
	private static final long serialVersionUID = 1;

	public Double2D loc = new Double2D(0,0);
	public Double2D lastd = new Double2D(0,0);
	public DContinuous2D humans;
	public DSociallyDamagingBehavior theHuman;
	public Color behav_color;
	public DBehaviour behavior;

	/*SDB*/
	ArrayDeque<DHuman> agentPast;
	public double fitness;
	public double dna;
	public double ce = 0.0;
	public double cei = 0.0;	
	public double tpi = 0.0;
	public boolean honestAction;
	/*SDB*/
 
	public DHuman(){}
	public DHuman(Double2D location, DistributedState state, double dna) { 
		//super(new SimplePortrayal2D(), 0, 4.0,Color.GREEN,OrientedPortrayal2D.SHAPE_COMPASS);

		agentPast = new ArrayDeque<DHuman>();
		loc = location;
		fitness=state.random.nextInt(100);
		this.dna=dna;
		behavior=(dna>5)?new Honest():new Dishonest();
		behav_color=(dna>5)?Color.GREEN:Color.RED;
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
		return humans.getObjectsExactlyWithinDistance(loc, theHuman.neighborhood, true);
	}

	public double getOrientation() { return orientation2D(); }

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
		DSociallyDamagingBehavior sdbState = (DSociallyDamagingBehavior)state;
		
		if(agentPast.size()>9)
		{
			agentPast.removeFirst();
			DHuman a = ((DHuman)(Util.clone(this)));
			agentPast.add(a);
		}
		else
			{
				DHuman a = ((DHuman)(Util.clone(this)));
				agentPast.add(a);
			}

		if (state.schedule.getSteps()==0 || state.schedule.getSteps()%sdbState.EPOCH!=0)
		{
			loc = sdbState.human_being.getObjectLocation(this);
	
			behavior=(dna>5)?new Honest():new Dishonest();
			behav_color=(dna>5)?Color.GREEN:Color.RED;
	
			Bag b = getNeighbors();

			Double2D avoid = behavior.avoidance(this,b,sdbState.human_being);
			Double2D cohe = behavior.cohesion(this,b,sdbState.human_being);
			Double2D rand = randomness(sdbState.random);
			Double2D cons = behavior.consistency(this,b,sdbState.human_being);
			Double2D mome = momentum();
	
			double dx = sdbState.cohesion * cohe.x + sdbState.avoidance * avoid.x + sdbState.consistency* cons.x + sdbState.randomness * rand.x + sdbState.momentum * mome.x;
			double dy = sdbState.cohesion * cohe.y + sdbState.avoidance * avoid.y + sdbState.consistency* cons.y + sdbState.randomness * rand.y + sdbState.momentum * mome.y;
	
			// renormalize to the given step size
			double dis = Math.sqrt(dx*dx+dy*dy);
			if (dis>0)
			{
				dx = dx / dis * sdbState.jump;
				dy = dy / dis * sdbState.jump;
			}
			
			behavior.action(this, state, b);
			behavior.calculateCEI(this, sdbState, b);
			
			//Social Influence
			behavior.socialInfluence(this, sdbState, b);
			
		//	loc=move(state, loc);
			lastd = new Double2D(dx,dy);
			loc = new Double2D(sdbState.human_being.stx(loc.x + dx), sdbState.human_being.sty(loc.y + dy));
			sdbState.human_being.setObjectLocation(this, loc);
		}
	}
	class Direction extends ArrayList<DHuman> implements Comparable
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
	public double getDna() {
		return dna;
	}
	public void setDna(double dna) {
		this.dna = dna;
	}
}
