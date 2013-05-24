package sim.app.SociallyDamagingBehav;

public class EntryAgent <D, H>{

	private D fitSum;
	private H h;
	
	public D getFitSum() {
		return fitSum;
	}
	

	public void setFitSum(D fitSum) {
		this.fitSum = fitSum;
	}

	public H getH() {
		return h;
	}

	public void setH(H h) {
		this.h = h;
	}

	public EntryAgent(D fitsum, H h) {

		this.fitSum = fitsum;
		this.h = h;
	}
}
