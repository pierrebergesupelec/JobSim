import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
			addBehaviour(new ProposerEmplois());
		}
		else {
			// Make the agent terminate
			System.out.println("Etat is not correctly initialised.");
			doDelete();
		}
	}

	private class ProposerEmplois extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null && msg.getContent().equals("clock")) {
				// TODO créer de novueaux emplois
				System.out.println(myAgent.getLocalName()+": clock reçu");
			}
			else {
				block();
			}
		}
	}
	
	protected void takeDown() {
		System.out.println("Etat "+getAID().getName()+" terminating.");
	}
}
