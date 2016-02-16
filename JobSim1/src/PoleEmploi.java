import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class PoleEmploi extends Agent {
	
	ArrayList<Emploi> pourvus;
	ArrayList<Emploi> attente;
	
	AID enCours; //mémorise le nom de l'individu avec qui on discute pour proposition d'emploi
	int step = 0;
	
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
	private class donnerEmploi extends Behaviour{

		@Override
		public void action() {
			int index = 0;
			MessageTemplate mt = null; // Template pour réception des messages
			
			//on agit à condition qu'il y ait des emplois en attente
			while (index < attente.size()){
				switch (step){
				case 0:
					//Infos relatives à l'emploi en tête de liste
					Emploi e = attente.get(index);
					Individu.Qualification qualif = e.getQualif();
					String revenu = "" + e.getRevenu();
					
					//Message envoyé
					ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

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
					
					//choix d'un destinataire 
					//TODO ce serait mieux si on faisait un présélection des agents (chomeurs + qualification correcte): est-ce possible ?
					int i = (int) (Math.random()*travailleurs.length);
					enCours = travailleurs[i];
					msg.addReceiver(travailleurs[i]);
					System.out.println("Pole Emploi envoie une proposition d'emploi à " + travailleurs[i].getName());
					
					//Envoi des informations relatives à l'emploi
					msg.setContent(qualif.name() + " " + revenu);//qualification et revenu : OUVRIER 1200
					myAgent.send(msg);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("jobOffer"),
							 MessageTemplate.MatchInReplyTo(msg.getReplyWith()));

					step = 1;
					break;
				case 1:
					ACLMessage reply = myAgent.receive(mt);
					 if (reply != null) {
						 //réponse du travailleur
						 if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
							 Emploi copy = new Emploi(attente.get(index));
							 copy.setEmploye(reply.getSender());
							 pourvus.add(copy);
							 attente.remove(0);
							 step = 2;
						 } else {
							 step = 0;
						 }
						 
					 } else {
						 block();
					 }

				}
				
				
				
				
			}
		}

		@Override
		public boolean done() {
			System.out.println("Emploi accepté");
			return (step == 2);
		}
		
	}

}
