package sim.app.SociallyDamagingBehav;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.*;

/**
 *  Agent 
 *
 */
public class Human extends OvalPortrayal2D implements Steppable//, sim.portrayal.Orientable2D 
{
	private static final long serialVersionUID = 1;

	public Double2D loc = new Double2D(0,0);
	public Double2D lastd = new Double2D(0,0);
	public Continuous2D humans;
	public SociallyDamagingBehavior theHuman;
	public Color behav_color;
	public Behaviour behavior;
	public double dx;
	public double dy;
	
	/*SDB*/
	public ArrayDeque<PastData> agentPast;
	public double fitness;
	public double dna;
	public double ce = 0.0;
	public double cei = 0.0;	
	public double tpi = 0.0;
	public boolean honestAction;
	
	//Model 2-3
	public double neighFitness;
	//Model 2-3
	
	//Model 4-5
	public double min_aoi_aggregation = 0;
	public double max_aoi_aggregation = 0;
	public static double sigma1 = 2.0;
	public static double sigma2 = 3.0;
	public double numNeighPunished = 0;
	public double numNeighDamager = 0;
	public boolean isPunished;
	public boolean isActionDishonest;
	public double punprob=0;
	//Model 4-5
	/*SDB*/

	public Human() {}
	
	public Human(Double2D location, SimState state, double dna) { 
		//super(new SimplePortrayal2D(), 0, 4.0,Color.GREEN,OrientedPortrayal2D.SHAPE_COMPASS);
		
		theHuman = (SociallyDamagingBehavior)state;
		humans = theHuman.human_being;
		agentPast = new ArrayDeque<PastData>();
		loc = location;
		fitness=state.random.nextInt(100);

		this.dna=dna;
		behavior=(dna>5)?new Honest():new Dishonest();
		behav_color=(dna>5)?Color.GREEN:Color.RED;
		honestAction = false;
		isPunished = false;
		isActionDishonest = false;
		
		if((theHuman.getMODEL()==theHuman.MODEL4_AGGREGATION_MOVEMENT) || 
				(theHuman.getMODEL()==theHuman.MODEL5_MEMORY))
		{
			min_aoi_aggregation = theHuman.MIN_AOI_AGGREGATION_MODEL3;
			max_aoi_aggregation = theHuman.MAX_AOI_AGGREGATION_MODEL3;
		}
	}

	@Override
	public void step(SimState state)
	{      		
		if (state.schedule.getSteps()==0 || state.schedule.getSteps()%SociallyDamagingBehavior.EPOCH!=0)
		{
			final SociallyDamagingBehavior sdbState = (SociallyDamagingBehavior)state;
			loc = sdbState.human_being.getObjectLocation(this);

			behavior=(dna>5)?new Honest():new Dishonest();
			behav_color=(dna>5)?Color.GREEN:Color.RED;

			Bag b;
			Bag entryNeigh = new Bag();

			if(sdbState.getMODEL()==sdbState.MODEL0_RANDOM_DAMAGING)
			{
				b = getNeighbors();
				dx = 0;
				dy = 0;
			}
			else
				if(sdbState.getMODEL()==sdbState.MODEL1_PROPORTIONAL_DAMAGING_ALLAGENTS)
				{
					b  = getNeighbors();
					dx = 0;
					dy = 0;
					
					if(sdbState.allHumans.size()<sdbState.numHumanBeing){
						double tot = sdbState.totalFitness+this.fitness;
						sdbState.allHumans.add(new EntryAgent<Double, Human>(tot, this));
						sdbState.totalFitness = tot;
					}
					else
					{
						double tot = sdbState.totalFitness+this.fitness;
						sdbState.allHumans.add(new EntryAgent<Double, Human>(tot, this));
						sdbState.allHumans.sort(new Comparator<EntryAgent<Double, Human>>() {
							@Override
							public int compare(EntryAgent<Double, Human> o1, EntryAgent<Double, Human> o2) {
								if(o1.getFitSum()>o2.getFitSum()) return 1;
								else if(o1.getFitSum()<o2.getFitSum()) return -1;
								return 0;
							}
						});
						sdbState.totalFitness = tot;
						sdbState.lastAllHumans = sdbState.allHumans;
						sdbState.lastTotalFitness = sdbState.totalFitness;
						sdbState.allHumans = new Bag();
						sdbState.totalFitness = 0;
					}

					if(state.schedule.getSteps()!=0)
					{
						if(sdbState.allHumans.size()<sdbState.numHumanBeing){
							double tot = sdbState.totalFitness+this.fitness;
							sdbState.allHumans.add(new EntryAgent<Double, Human>(tot, this));
							sdbState.totalFitness = tot;
						}
						else
						{
							double tot = sdbState.totalFitness+this.fitness;
							sdbState.allHumans.add(new EntryAgent<Double, Human>(tot, this));
							sdbState.allHumans.sort(new Comparator<EntryAgent<Double, Human>>() {
								@Override
								public int compare(EntryAgent<Double, Human> o1, EntryAgent<Double, Human> o2) {
									if(o1.getFitSum()>o2.getFitSum()) return 1;
									else if(o1.getFitSum()<o2.getFitSum()) return -1;
									return 0;
								}
							});
							sdbState.totalFitness = tot;
							sdbState.lastAllHumans = sdbState.allHumans;
							sdbState.lastTotalFitness = sdbState.totalFitness;
							sdbState.allHumans = new Bag();
							sdbState.totalFitness = 0;
						}
					}
				}
				else
					if(sdbState.getMODEL()==sdbState.MODEL2_PROPORTIONAL_DAMAGING_NEIGH)
					{
						b  = getNeighbors();
						dx = 0;
						dy = 0;
						
						b.sort(new Comparator<Human>() {
							@Override
							public int compare(Human o1, Human o2) {
								if(o1.fitness>o2.fitness) return 1;
								else if(o1.fitness<o2.fitness) return -1;
								return 0;
							}
						});
						
						System.out.println("fit "+fitness);
						
						neighFitness = 0;

						for (Object o : b) {
							Human h = (Human)o;
							neighFitness += h.fitness;
							entryNeigh.add(new EntryAgent<Double, Human>(neighFitness, h));
						}
					}
					else
						if(sdbState.getMODEL()==sdbState.MODEL3_RANDOM_MOVEMENT)
						{
							b = getNeighbors();
							Double2D rand = behavior.randomness(sdbState);
							Double2D mome = momentum();
	
							dx = rand.x + sdbState.momentum * mome.x;
							dy = rand.y + sdbState.momentum * mome.y;
	
							// renormalize to the given step size
							double dis = Math.sqrt(dx*dx+dy*dy);
							if (dis>0)
							{
								dx = dx / dis * sdbState.jump;
								dy = dy / dis * sdbState.jump;
							}
	
							b.sort(new Comparator<Human>() {
								@Override
								public int compare(Human o1, Human o2) {
									if(o1.fitness>o2.fitness) return 1;
									else if(o1.fitness<o2.fitness) return -1;
									return 0;
								}
							});
	
							neighFitness = 0;
	
							for (Object o : b) {
								Human h = (Human)o;
								neighFitness += h.fitness;
								entryNeigh.add(new EntryAgent<Double, Human>(neighFitness, h));
							}
						}
						else
							if(sdbState.getMODEL()==sdbState.MODEL4_AGGREGATION_MOVEMENT)
							{
								b = getAggregatedNeighbors();
								
								double valoreMedio = 0;
								double valoreMedio2 = 0;
								double varianza = 0;
	
								for(Object o : b)
								{
									Human h = (Human)o;
									
									valoreMedio += h.dna * (1/(double)b.size());
								
									valoreMedio2 += (h.dna*h.dna) * (1/(double)b.size());
								}
							    
								
								varianza = (valoreMedio2) - ((valoreMedio)*(valoreMedio));
								double deviazione=Math.sqrt(varianza);
								
								if((deviazione > sigma2) && (max_aoi_aggregation < theHuman.neighborhood))
									max_aoi_aggregation += 1.0;
								else
									if((deviazione < sigma1) && (max_aoi_aggregation>min_aoi_aggregation))
										max_aoi_aggregation -= 1.0;									
								
								b.sort(new Comparator<Human>() {
									@Override
									public int compare(Human o1, Human o2) {
										if(o1.fitness>o2.fitness) return 1;
										else if(o1.fitness<o2.fitness) return -1;
										return 0;
									}
								});
	
								neighFitness = 0;
	
								for (Object o : b) {
									Human h = (Human)o;
									neighFitness += h.fitness;
									entryNeigh.add(new EntryAgent<Double, Human>(neighFitness, h));
								}
								
								Double2D avoid = behavior.avoidance(this,b,sdbState.human_being);
								Double2D cohe = behavior.cohesion(this,b,sdbState.human_being);
								Double2D cons = behavior.consistency(this,b,sdbState.human_being);
								Double2D mome = momentum();
								
								dx = sdbState.cohesion * cohe.x + sdbState.avoidance * avoid.x + sdbState.consistency* cons.x + sdbState.momentum * mome.x;
								dy = sdbState.cohesion * cohe.y + sdbState.avoidance * avoid.y + sdbState.consistency* cons.y + sdbState.momentum * mome.y;
								
								// renormalize to the given step size
								double dis = Math.sqrt(dx*dx+dy*dy);
								if (dis>0)
								{
									dx = dx / dis * sdbState.jump;
									dy = dy / dis * sdbState.jump;
								}
							}
							else
							{
								if(agentPast.size()>9)
								{
									agentPast.removeFirst();
									agentPast.add(new PastData(numNeighPunished, numNeighDamager, dna));
								}
								else
								{
									agentPast.add(new PastData(numNeighPunished, numNeighDamager, dna));
								}
								
								b = getAggregatedNeighbors();
								numNeighPunished = 0;
								numNeighDamager = 0;
								double valoreMedio = 0;
								double valoreMedio2 = 0;
								double varianza = 0;
	
								for(Object o : b)
								{
									Human h = (Human)o;
									
									if(h.isPunished)
										numNeighPunished++;
									
									if(h.isActionDishonest)
										numNeighDamager++;
									
									valoreMedio += h.dna * (1/(double)b.size());
									valoreMedio2 += (h.dna*h.dna) * (1/(double)b.size());
								}
							    
								for(PastData pd : agentPast)
								{
									numNeighPunished += pd.numNeighPunished;
									numNeighDamager += pd.numNeighDamager;
								}
								
								if(numNeighDamager!=0.0)
									punprob = (numNeighPunished/numNeighDamager);
								else
									punprob = 0.0;
	
								varianza = (valoreMedio2) - ((valoreMedio)*(valoreMedio));
								double deviazione=Math.sqrt(varianza);
								
								if((deviazione > sigma2) && (max_aoi_aggregation < theHuman.neighborhood))
									max_aoi_aggregation += 1.0;
								else
									if((deviazione < sigma1) && (max_aoi_aggregation>min_aoi_aggregation))
										max_aoi_aggregation -= 1.0;									
								
								b.sort(new Comparator<Human>() {
									@Override
									public int compare(Human o1, Human o2) {
										if(o1.fitness>o2.fitness) return 1;
										else if(o1.fitness<o2.fitness) return -1;
										return 0;
									}
								});
	
								neighFitness = 0;
	
								for (Object o : b) {
									Human h = (Human)o;
									neighFitness += h.fitness;
									entryNeigh.add(new EntryAgent<Double, Human>(neighFitness, h));
								}
								
								Double2D avoid = behavior.avoidance(this,b,sdbState.human_being);
								Double2D cohe = behavior.cohesion(this,b,sdbState.human_being);
								Double2D cons = behavior.consistency(this,b,sdbState.human_being);
								Double2D mome = momentum();
								
								dx = sdbState.cohesion * cohe.x + sdbState.avoidance * avoid.x + sdbState.consistency* cons.x + sdbState.momentum * mome.x;
								dy = sdbState.cohesion * cohe.y + sdbState.avoidance * avoid.y + sdbState.consistency* cons.y + sdbState.momentum * mome.y;
								
								// renormalize to the given step size
								double dis = Math.sqrt(dx*dx+dy*dy);
								if (dis>0)
								{
									dx = dx / dis * sdbState.jump;
									dy = dy / dis * sdbState.jump;
								}
							}

			behavior.action(this, sdbState, b, entryNeigh);

			//Social Influence
			behavior.calculateCEI(this, sdbState, b);
			behavior.socialInfluence(this, sdbState, b);
			
			dataLogger(sdbState);
			
			lastd = new Double2D(dx,dy);
			loc = new Double2D(sdbState.human_being.stx(loc.x + dx), sdbState.human_being.sty(loc.y + dy));
			sdbState.human_being.setObjectLocation(this, loc);
			sdbState.schedule.scheduleOnce(this);
		}
	}
	
	class Direction extends ArrayList<Human> implements Comparable
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
	
	@Override
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
	
	public double orientation2D()
	{
		if (lastd.x == 0 && lastd.y == 0) return 0;
		return Math.atan2(lastd.y, lastd.x);
	}

	public void dataLogger(SociallyDamagingBehavior sdbState) {
		if((sdbState.numHonest+sdbState.numDishonest)<sdbState.numHumanBeing-1){
			if(behavior instanceof Honest)
			{
				sdbState.numHonest++;
				sdbState.total_honest_fitness += this.fitness;
			}
			else
			{
				sdbState.numDishonest++;
				sdbState.total_dishonest_fitness += this.fitness;
			}
		}
		else
			if((sdbState.numHonest+sdbState.numDishonest)==sdbState.numHumanBeing-1)
			{
				if(behavior instanceof Honest)
					sdbState.numHonest++;
				else
					sdbState.numDishonest++;
				if(sdbState.logging && sdbState.schedule.getSteps()<=sdbState.epochLimit)
				{
					sdbState.ps.println(sdbState.schedule.getSteps()+";"+sdbState.numHonest+";"+
						sdbState.numDishonest+";"+sdbState.numHonestAction+";"+sdbState.numDishonestAction+
						";"+sdbState.total_honest_fitness+";"+sdbState.total_dishonest_fitness);
					sdbState.ps.flush();
				}
				sdbState.honestAction = sdbState.numHonestAction;
				sdbState.total_honest_fitness = 0;
				sdbState.total_dishonest_fitness = 0;
				sdbState.numHonestAction = 0;
				sdbState.dishonestAction = sdbState.numDishonestAction;
				sdbState.numDishonestAction = 0;
				sdbState.honest = sdbState.numHonest;
				sdbState.numHonest = 0;
				sdbState.dishonest = sdbState.numDishonest;
				sdbState.numDishonest = 0;
			}
	}
	public double getFitness() {return fitness;}
	public void setFitness(double fitness) {this.fitness = fitness;}
	public double getDna() {return dna;}
	public void setDna(double dna) {this.dna = dna;}
	public Bag getNeighbors(){return humans.getObjectsExactlyWithinDistance(loc, theHuman.neighborhood, true);}
	public Bag getAggregatedNeighbors(){return humans.getObjectsExactlyWithinDistance(loc, max_aoi_aggregation, true);}
	public double getOrientation() {return orientation2D();}
	public void setOrientation2D(double val){lastd = new Double2D(Math.cos(val),Math.sin(val));}
	public Double2D momentum(){return lastd;}
}