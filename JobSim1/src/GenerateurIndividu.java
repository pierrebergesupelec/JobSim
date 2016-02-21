import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


public class GenerateurIndividu extends Agent {
	
	Random random;
	int nb_arrivants_moyen;
	double nb_arrivants_std_dev;
	
	protected void setup() {
		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 3) {
			random = (Random) args[0];
			nb_arrivants_moyen = (int) args[1];
			nb_arrivants_std_dev = (double) args[2];
			
			// Register "clock" service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("clock");
			sd.setName("");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add behaviours
			addBehaviour(new generer());
		}
		else {
			// Make the agent terminate
			System.out.println(getLocalName()+" is not correctly initialised.");
			doDelete();
		}
	}
	
	
	private class generer extends Behaviour{

		@Override
		public void action() {
			int nb_arrivants = (int) (random.nextGaussian()*nb_arrivants_std_dev + nb_arrivants_moyen);
			
			//TODO création de nouveaux agents ?
		}

		@Override
		public boolean done() {
			return true;
		}
		
	}
	

}
