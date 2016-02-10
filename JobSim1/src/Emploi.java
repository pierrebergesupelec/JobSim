import java.util.Random;

import jade.core.Agent;

public class Emploi {
	
	private double revenu;
	private double tl_reel;
	private double tl_std_dev;
	private Random random;
	
	private Agent employeur;
	private Agent employe;
	private Individu.Qualification qualif;
	
	public Emploi(double r, double tl, double tl_dev, Random rand, Agent empl, Individu.Qualification q){
		revenu = r;
		tl_reel = tl;
		tl_std_dev = tl_dev;
		random = rand;
		employeur = empl;
		qualif = q;
	}
	
	public double getRevenu(){
		return revenu;
	}
	
	public double tlRealisation(){
		return random.nextGaussian()*tl_std_dev+tl_reel;
	}

	public Agent getEmployeur() {
		return employeur;
	}

	public Individu.Qualification getQualif() {
		return qualif;
	}

	public Agent getEmploye() {
		return employe;
	}

	public void setEmploye(Agent employe) {
		this.employe = employe;
	}
	
	public boolean estPourvu(){
		return getEmploye()!=null;
	}

}
