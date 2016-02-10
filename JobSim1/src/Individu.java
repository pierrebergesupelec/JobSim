import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Individu extends Agent{
	
	public enum Qualification{
		OUVRIER,
		TECHNICIEN,
		CADRE
	}
	
	Qualification qualif;
	double rm;
	double tl;
	int x;
	int y;
	double z;
	
	boolean employed = false;
	Emploi emploi;
	
	protected void setup() {
		// Initialisation message
		System.out.println(getAID().getName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 6) {
			qualif = (Qualification) args[0];
			rm = (double) args[1];
			tl = (double) args[2];
			x = (int) args[3];
			y = (int) args[4];
			z = (double) args[5];
			
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
			addBehaviour(new avecEmploi());
			addBehaviour(new sansEmploi());
		}
		else {
			// Make the agent terminate
			System.out.println(getAID().getName()+" is not correctly initialised.");
			doDelete();
		}
	}
	
	private class avecEmploi extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null && msg.getContent().equals("clock")) {
				// TODO verifier la condition sur le tl et quitter éventuellement
				System.out.println(myAgent.getLocalName()+": clock reçu");
			}
			else {
				block();
			}
		}
	}
	
	private class sansEmploi extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// TODO protocole pour l'acceptation ou non d'un emploi
				System.out.println(myAgent.getLocalName()+": proposition d'emploi reçu");
			}
			else {
				block();
			}
		}
	}

	protected void takeDown() {
		System.out.println(getAID().getName()+" terminating.");
	}
}
