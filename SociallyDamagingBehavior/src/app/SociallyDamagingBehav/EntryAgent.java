package sim.app.SociallyDamagingBehav;

public class EntryAgent <D, H>{

	private double fitSum;
	private Human h;
	
	public double getFitSum() {
		return fitSum;
	}

	public void setFitSum(double fitSum) {
		this.fitSum = fitSum;
	}

	public Human getH() {
		return h;
	}

	public void setH(Human h) {
		this.h = h;
	}

	public EntryAgent(double fitsum, Human h) {

		this.fitSum = fitsum;
		this.h = h;
	}
}
