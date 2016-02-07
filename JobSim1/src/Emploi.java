import java.util.Random;

public class Emploi {
	
	private double revenu;
	private double tl_reel;
	private double tl_std_dev;
	private Random random;
	
	public Emploi(double r, double tl, double tl_dev, Random rand){
		revenu = r;
		tl_reel = tl;
		tl_std_dev = tl_dev;
		random = rand;
	}
	
	public double getRevenu(){
		return revenu;
	}
	
	public double tlRealisation(){
		return random.nextGaussian()*tl_std_dev+tl_reel;
	}
}
