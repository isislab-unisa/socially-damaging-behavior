package dmason.sim.app.SociallyDamagingBehav;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import javax.swing.*;
import dmason.batch.data.GeneralParam;
import java.awt.*;
import sim.portrayal.simple.*;
import sim.portrayal.SimplePortrayal2D;

public class DSociallyDamagingBehaviorWithUI extends GUIState
{
    public Display2D display;
    public JFrame displayFrame;
    public static String name;
    
    public static String topicPrefix = "";

    @Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

    ContinuousPortrayal2D DSDBPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    
 
    public DSociallyDamagingBehaviorWithUI(GeneralParam args) 
    { 
    	super(new DSociallyDamagingBehavior(args));
    
    	name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
    }
  
    public static String getName() { return "Peer: <"+name+">"; }

    @Override
	public void start()
    {
        super.start();
        setupPortrayals();
    }

    @Override
	public void load(SimState state)
    {
        super.load(state);
        setupPortrayals();
    }
        
    public void setupPortrayals()
    {
        DSociallyDamagingBehavior dsdb = (DSociallyDamagingBehavior)state;

        DSDBPortrayal.setField(dsdb.human_being);
        // uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
/*       trailsPortrayal.setField(flock.flockers);  */
        
        // make the flockers random colors and four times their normal size (prettier)
        for(int x=0;x<dsdb.human_being.allObjects.numObjs;x++)
        {
        SimplePortrayal2D basic =       new TrailedPortrayal2D(
            this,
            new OrientedPortrayal2D(
                new SimplePortrayal2D(), 0, 4.0,
                (dsdb.human_being.allObjects.objs[x] instanceof Honest)?
                		(Color.green): (Color.red)
				,
                OrientedPortrayal2D.SHAPE_COMPASS),
            trailsPortrayal, 100);

        // note that the basic portrayal includes the TrailedPortrayal.  We'll add that to BOTH 
        // trails so it's sure to be selected even when moving.  The issue here is that MovablePortrayal2D
        // bypasses the selection mechanism, but then sends selection to just its own child portrayal.
        // but we need selection sent to both simple portrayals in in both field portrayals, even after
        // moving.  So we do this by simply having the TrailedPortrayal wrapped in both field portrayals.
        // It's okay because the TrailedPortrayal will only draw itself in the trailsPortrayal, which
        // we passed into its constructor.
                    
        DSDBPortrayal.setPortrayalForObject(dsdb.human_being.allObjects.objs[x], 
            new AdjustablePortrayal2D(new MovablePortrayal2D(basic)));
        trailsPortrayal.setPortrayalForObject(dsdb.human_being.allObjects.objs[x], basic );
        }
        dsdb.human_being.attachPortrayal(DSDBPortrayal);
        
        // update the size of the display appropriately.
        double w = dsdb.human_being.getWidth();
        double h = dsdb.human_being.getHeight();
        if (w == h)
            { display.insideDisplay.width = display.insideDisplay.height = 750; }
        else if (w > h)
            { display.insideDisplay.width = 750; display.insideDisplay.height = 750 * (h/w); }
        else if (w < h)
            { display.insideDisplay.height = 750; display.insideDisplay.width = 750 * (w/h); }
            
        // reschedule the displayer
        display.reset();
                
        // redraw the display
        display.repaint();
    }

    @Override
	public void init(Controller c)
    {
        super.init(c);

        // make the displayer
        display = new Display2D(750,750,this,1);
        display.setBackdrop(Color.black);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Socially Damaging Behaviors");
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
        /* display.attach( trailsPortrayal, "Trails" ); */
                
        display.attach( DSDBPortrayal, "Behold the Human!" );
    }
        
    @Override
	public void quit()
    {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }
    public static Class<?> getSimClass(){return DSociallyDamagingBehavior.class;}
}
