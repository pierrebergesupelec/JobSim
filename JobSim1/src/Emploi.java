import java.util.Random;

import jade.core.AID;
import jade.core.Agent;

public class Emploi {
	
	private double revenu;
	private double tl_reel;
	private double tl_std_dev;
	private Random random;
	
	private AID employeur;
	private AID employe;
	private Individu.Qualification qualif;
	
	public Emploi(double r, double tl, double tl_dev, Random rand, AID empl, Individu.Qualification q){
		revenu = r;
		tl_reel = tl;
		tl_std_dev = tl_dev;
		random = rand;
		employeur = empl;
		qualif = q;
	}
	
	//copie constructeur
	public Emploi(Emploi e){
		revenu = e.getRevenu();
		tl_reel = e.tl_reel;
		tl_std_dev = e.tl_std_dev;
		random = e.random;
		employeur = e.getEmployeur();
		qualif = e.getQualif();
	}
	
	public double getRevenu(){
		return revenu;
	}
	
	public double tlRealisation(){
		return random.nextGaussian()*tl_std_dev+tl_reel;
	}

	public AID getEmployeur() {
		return employeur;
	}

	public Individu.Qualification getQualif() {
		return qualif;
	}

	public AID getEmploye() {
		return employe;
	}

	public void setEmploye(AID employe) {
		this.employe = employe;
	}
	
	public boolean estPourvu(){
		return getEmploye()!=null;
	}
	
	public double getTl(){
		return tl_reel;
	}
	
	public double getTlDev(){
		return tl_std_dev;
	}

}
