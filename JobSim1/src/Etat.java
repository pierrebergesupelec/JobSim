import java.util.Random;

import jade.core.Agent;

public class Etat extends Agent{

	int nbEmplois1;
	int nbEmplois2;
	int nbEmplois3;
	Random random;

	protected void setup() {
		// Initialisation message
		System.out.println("Etat "+getAID().getName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 4) {
			nbEmplois1 = (int) args[0];
			nbEmplois2 = (int) args[1];
			nbEmplois3 = (int) args[2];
			random = (Random) args[3];
		}
		else {
			// Make the agent terminate
			System.out.println("Etat is not correctly initialised.");
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println("Etat "+getAID().getName()+" terminating.");
	}
}
