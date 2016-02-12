import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


public class PoleEmploi extends Agent {
	
	ArrayList<Emploi> pourvus;
	ArrayList<Emploi> attente;
	
	protected void setup() {
		// Initialisation message
		System.out.println("Pole Emploi "+getAID().getName()+" is ready.");

		// Initialisation liste d'emplois
		ArrayList<Emploi> pourvus = new ArrayList<Emploi>();
		ArrayList<Emploi> attente = new ArrayList<Emploi>();
		
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
		
		addBehaviour(new donnerEmploi());
		
	}
	
	protected void takeDown() {
		System.out.println("Pole Emploi "+getAID().getName()+" terminating.");
	}
	
	
	//PoleEmploi s'occupe de donner les emplois en attente à des travailleurs
	private class donnerEmploi extends CyclicBehaviour{

		@Override
		public void action() {
			//on agit à condition qu'il y ait des emplois en attente
			if (attente.size() > 0){
				//Infos relatives à l'emploi en tête de liste
				Emploi e = attente.get(0);
				Individu.Qualification qualif = e.getQualif();
				String revenu = "" + e.getRevenu();
				
				//Message envoyé
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);

				//liste de destinataires : tous les travailleurs
				AID[] travailleurs = new AID[0];
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("worker");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					travailleurs = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						travailleurs[i] = result[i].getName();
					}
				}
				catch (FIPAException fe) {
				fe.printStackTrace();
				}
				
				//ajout des destinataires à l'envoi du message
				for (int i = 0; i < travailleurs.length; ++i) {
					msg.addReceiver(travailleurs[i]);// est-ce que ça marche, si on met l'AID et pas l'agent ??
				}
				
				//Envoi des informations relatives à l'emploi
				msg.setContent(revenu);
				myAgent.send(msg);
				
			}
		}
		
	}

}
