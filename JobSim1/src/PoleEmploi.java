import java.util.ArrayList;
import java.util.Random;

import jade.core.Agent;


public class PoleEmploi extends Agent {
	
	ArrayList<Emploi> pourvus;
	ArrayList<Emploi> attente;
	
	protected void setup() {
		// Initialisation message
		System.out.println("Pole Emploi "+getAID().getName()+" is ready.");

		// Initialisation liste d'emplois
		ArrayList<Emploi> pourvus = new ArrayList<Emploi>();
		ArrayList<Emploi> attente = new ArrayList<Emploi>();
		
	}
	
	protected void takeDown() {
		System.out.println("Pole Emploi "+getAID().getName()+" terminating.");
	}

}
