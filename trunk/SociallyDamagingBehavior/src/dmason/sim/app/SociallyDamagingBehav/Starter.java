package dmason.sim.app.SociallyDamagingBehav;

import java.util.ArrayList;

import sim.display.Console;
import dmason.batch.data.GeneralParam;
import dmason.sim.field.grid.DSparseGrid2DFactory;


public class Starter {
	
	public static void main(String[] args) 
	{	
		int rows = 2;
		int columns = 2;
		int MAX_DISTANCE=10;
		int NUM_AGENTS=30;
		int WIDTH=200;
		int HEIGHT=200;
		//int MODE=DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE;
		int MODE=DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		
		if(MODE==DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
		{
			ArrayList<Console> dsdb=new ArrayList<Console>();
			for (int j = 0; j < columns; j++) 
			{		
				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, MAX_DISTANCE, 1,columns,NUM_AGENTS, MODE); 
				genParam.setI(0);
				genParam.setJ(j);
				genParam.setIp("127.0.0.1");
				genParam.setPort("61616");
				DSociallyDamagingBehaviorWithUI t=new DSociallyDamagingBehaviorWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,0,j,MODE}
				Console c=(Console)t.createController();
	
				c.pressPause();
				//dants.add(c);
			}
			for(Console cc:dsdb) cc.pressPause();
		}
		
		if(MODE==DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE)
		{
			ArrayList<Console> dsdb=new ArrayList<Console>();
			for (int i = 0; i < rows; i++) 
			{
				for (int j = 0; j < columns; j++) 
				{
					GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, MAX_DISTANCE, rows,columns,NUM_AGENTS, MODE); 
					genParam.setI(i);
					genParam.setJ(j);
					genParam.setIp("127.0.0.1");
					genParam.setPort("61616");
					DSociallyDamagingBehaviorWithUI t=new DSociallyDamagingBehaviorWithUI(genParam); //new Object[]{"127.0.0.1","61616",MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEGHT,i,j,MODE}
				    Console c=(Console)t.createController();
				    c.pressPause();
				    //dsdb.add(c);
				}
			}
			
		}

	}
}