import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.util.leap.Serializable;

public class Emploi implements Serializable {
	
	// Permet de générer une id unique pour chaque Emploi à la création
	private static int idgenerator = 0;
	
	private double revenu;
	private double tl_reel;
	private double tl_std_dev;
	private Random random;
	
	private int ID;
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
		ID = idgenerator;
		idgenerator ++;
	}
	
	// Copie constructeur
	public Emploi(Emploi e){
		revenu = e.getRevenu();
		tl_reel = e.tl_reel;
		tl_std_dev = e.tl_std_dev;
		random = e.random;
		employeur = e.getEmployeur();
		qualif = e.getQualif();
	}
	
	// Surcharge de equals pour ne comparer que l'ID
	@Override
	public boolean equals(Object other){
	    if (other == null) 				return false;
	    if (!(other instanceof Emploi))	return false;
	    return ((Emploi)other).getID() == this.getID();
	}
	
	// Surcharge de toString pour afficher l'ID de l'emploi
	@Override 
	public String toString() {
	    return "Emploi n"+Integer.toString(getID());
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
	
	public int getID() {
		return ID;
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
