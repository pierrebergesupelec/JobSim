import java.util.ArrayList;

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
				
				//on peut fixer le fait que, par nature, PoleEmploi envoie l'offre à tous les travailleurs de même qualification
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				//TODO : aller chercher la liste des travailleurs
				Agent[] travailleurs = new Agent[0];//là, on envoie rien
				for (int i = 0; i < travailleurs.length; ++i) {
					Individu a = (Individu) travailleurs[i];
					if (a.qualif.equals(qualif)) msg.addReceiver(travailleurs[i].getAID());
				}
				//Envoi des informations relatives à l'emploi
				msg.setContent(revenu);
				myAgent.send(msg);
				
			}
		}
		
	}

}
