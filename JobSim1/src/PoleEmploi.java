import java.io.IOException;
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
import jade.lang.acl.UnreadableException;


public class PoleEmploi extends Agent {
	
	ArrayList<Emploi> pourvus;
	ArrayList<Emploi> attente;
	
	protected void setup() {
		
		// Initialisation message
		System.out.println("Pole Emploi "+getAID().getName()+" is ready.");

		// Initialisation liste d'emplois
		pourvus = new ArrayList<Emploi>();
		attente = new ArrayList<Emploi>();
		
		// Register "etat" service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("poleemploi");
		sd.setName("");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new maj());
		
	}
	
	protected void takeDown() {
		System.out.println("Pole Emploi "+getAID().getName()+" terminating.");
	}
	
	//PoleEmploi s'occupe de donner les emplois en attente é des travailleurs
	/* TODO delete
	private class donnerEmploi extends CyclicBehaviour{

		int step = 0;
		int nb_tentatives = 0;//nombre de fois oé on essaie de donner un emploi
		
		@Override
		public void action() {
			
			int index = 0;
			MessageTemplate mt = null; // Template pour réception des messages

			//on agit é condition qu'il y ait des emplois en attente
			while (attente.size() > 0 && nb_tentatives < 100){
				switch (step){
				case 0:
					//Emploi é traiter
					Emploi e = attente.get(index);
					
					//Message envoyé
					ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

					//liste de destinataires : tous les travailleurs avec la bonne qualification
					AID[] travailleurs = new AID[0];
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType(e.getQualif().name());
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
					
					//choix d'un destinataire au pif
					int i = (int) (Math.random()*travailleurs.length);
					enCours = travailleurs[i];
					msg.addReceiver(travailleurs[i]);
					System.out.println("Pole Emploi envoie une proposition d'emploi é " + travailleurs[i].getName());
					
					//Envoi des informations relatives é l'emploi
					try {
						msg.setContentObject(e);// PJ
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
							 nb_tentatives = 0;
						 }
						 if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
							 step = 0;
							 nb_tentatives++;
						 }
						 
					 } else {
						 block();
					 }
				}
			}
		}
	}
	*/
	
	private class maj extends CyclicBehaviour{

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive();
			 if (msg != null) {
				 // msg de démission
				 if (msg.getPerformative() == ACLMessage.INFORM) {
					 try {
						attente.add((Emploi)msg.getContentObject());
						pourvus.remove((Emploi)msg.getContentObject());
					 } catch (UnreadableException e) {
						 e.printStackTrace();
					 }
				 }
				 // msg de nouvel emploi
				 else if(msg.getPerformative() == ACLMessage.PROPOSE){
					 Emploi e;
					 try {
						 e = (Emploi)msg.getContentObject();
						 System.out.println("maj: "+e);
						 if(!pourvus.contains(e) && !attente.contains(e)){
							 attente.add(e);
							 addBehaviour(new donnerEmploi(e));
						 }
					 } catch (UnreadableException e1) {
						 e1.printStackTrace();
					 }
				 }

			 } else {
				 block();
			 }
		}
	}
	
	//PoleEmploi s'occupe de donne l'emplois en attente é un travailleur
	private class donnerEmploi extends Behaviour{

		private boolean terminate = false;
		int step = 0;
		Emploi e = null;	//Emploi é traiter
		
		public donnerEmploi(Emploi emploi){
			e = emploi;
		}
		
		@Override
		public void action() {
			
			MessageTemplate mt = null; // Template pour réception des messages

			switch (step){
			case 0:
				//Message envoyé
				ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

				//liste de destinataires : tous les travailleurs avec la bonne qualification
				AID[] travailleurs = new AID[0];
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(e.getQualif().name());
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
				
				if(travailleurs.length > 0){
					//choix d'un destinataire au pif
					int i = (int) (Math.random()*travailleurs.length);
					msg.addReceiver(travailleurs[i]);
					System.out.println("Pole Emploi envoie une proposition d'emploi à " + travailleurs[i].getName());

					//Envoi des informations relatives é l'emploi
					try {
						msg.setContentObject(e);// PJ
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					myAgent.send(msg);
					step = 1;
				}
				break;
			case 1:
				mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));
				ACLMessage reply = myAgent.receive(mt);
				try {
					if (reply != null && reply.getContentObject()==e) {
						// réponse du travailleur
						if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
							e.setEmploye(reply.getSender());
							pourvus.add(e);
							attente.remove(e);
							step = 2;
							terminate = true;
						}
						if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
							step = 0;
						}
					} else {
						block();
					}
				} catch (UnreadableException e1) {
					e1.printStackTrace();
				}
			}
		}

		@Override
		public boolean done() {
			return terminate;
		}
	}

}
