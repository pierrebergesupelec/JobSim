
import java.io.IOException;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class Individu extends Agent{
	
	public enum Qualification{
		OUVRIER,
		TECHNICIEN,
		CADRE
	}
	
	public Qualification qualif;
	double rm;
	double tl;
	int moisSansTl;//nombre de mois que l'individu passe sans le temps libre souhait�
	int moisSansEmploi;//nombre de mois sans emploi
	int x;
	int y;
	double z;
	
	Emploi emploi = null;
	
	protected void setup() {
		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 6) {
			qualif = (Qualification) args[0];
			rm = (double) args[1];
			tl = (double) args[2];
			x = (int) args[3];
			y = (int) args[4];
			z = (double) args[5];
			
			moisSansTl = 0;
			moisSansEmploi = 0;
			
			// Register "clock" service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("clock");
			sd.setName("");
			dfd.addServices(sd);
			// Register "worker" service
			ServiceDescription sd2 = new ServiceDescription();
			sd2.setType("worker");
			sd2.setName("");
			dfd.addServices(sd2);
			// Register qualification service
			ServiceDescription sd3 = new ServiceDescription();
			sd3.setType(qualif.name());
			sd3.setName("");
			dfd.addServices(sd3);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add behaviours
			addBehaviour(new sansEmploi());
			
			// Initialisation message
			System.out.println(getAID().getName()+" is ready. "+qualif.name());
		}
		else {
			// Make the agent terminate
			System.out.println(getAID().getName()+" is not correctly initialised.");
			doDelete();
		}
	}
	
	private class avecEmploi extends Behaviour {
		
		private boolean terminate = false;
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null && msg.getContent().equals("clock")) {
				// Vérifier que l'individu a un emploi
				if(emploi!=null){
					System.out.println(myAgent.getAID().getName() + " Un mois de travail en plus");
					// Obtenir le temps libre de ce mois pour cet emploi 
					double tl_reel = emploi.tlRealisation();
					// Mettre à jour le nombre de mois avec un temps libre < tl
					if(tl_reel<tl){
						moisSansTl ++;
					}
					else{
						moisSansTl = 0;
					}
					// Vérifier la condition sur le tl et quitter éventuellement
					if(moisSansTl>=x){
						// Ajouter le behaviour demissionner
						addBehaviour(new Demissionner());
						// Terminer ce behaviour
						terminate = true;
					}
				}
			}
			else {
				block();
			}
		}
		
		public boolean done() {
			return terminate;
		}
	}
	
	private class Demissionner extends Behaviour {
		private int step = 0;//en tout 2 etapes : demande et confirmation
		private boolean terminate = false;//processus de d�cision termin�
		
		public void action() {
			switch(step){
			case 0:
				// Envoie du message
				ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
				req.addReceiver(emploi.getEmployeur());
				req.setContent("demission");
				myAgent.send(req);
				step = 1;
			case 1:
				// Reception de la réponse
				MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.FAILURE));
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null){
					if(msg.getPerformative()==ACLMessage.CONFIRM){
						// Terminer ce behaviour
						terminate = true;
						// Supprime l'emploi de la m�moire de l'individu
						emploi = null;
						// Se mettre dans le behaviour sansEmploi
						addBehaviour(new sansEmploi());
					}
					else{
						step = 0;
					}
				}
				else{
					block();
				}
			}
		}

		public boolean done() {
			return terminate;
		}
	}
	
	private class sansEmploi extends Behaviour {
		private boolean terminate = false;
		
		public void action() {
			// Clock msg
			MessageTemplate mt_clock = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg_clock = myAgent.receive(mt_clock);
			// Proposition d'emploi msg
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			ACLMessage msg = myAgent.receive(mt);
			// A chaque pas d'horloge, incr�menter  moisSansEmploi
			if (msg_clock != null && msg_clock.getContent().equals("clock")) {
				moisSansEmploi ++;
			} else
				try {
					if (msg != null && msg.getContentObject() instanceof Emploi) {
						//protocole pour l'acceptation ou non d'un emploi
						System.out.println(myAgent.getLocalName()+": proposition d'emploi recu");

						//r�ponse � l'offre re�u
						if (emploi == null){//condition 1: inactif
							Emploi e;
							try {
								e = (Emploi) msg.getContentObject();
								boolean goodQualif = e.getQualif() == qualif;
								System.out.println(e.getQualif()+" "+qualif+" "+e.getRevenu()+" "+rm);
								if (goodQualif && e.getRevenu()>=rm){
									System.out.println(myAgent.getLocalName()+": emploi accept�");
									//envoi r�ponse
									ACLMessage answer = msg.createReply();
									answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
									answer.setContentObject(e);
									myAgent.send(answer);
									moisSansEmploi = 0;
									emploi = e;
									// Ajouter le behaviour avecEmploi
									addBehaviour(new avecEmploi());
									// Terminer ce behaviour
									terminate = true;
								}
								else {
									System.out.println(myAgent.getLocalName()+": emploi refus�");
									ACLMessage answer = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
									answer.addReceiver(msg.getSender());
									myAgent.send(answer);
								}
							} catch (UnreadableException | IOException e1) {
								e1.printStackTrace();
							}

						} else {
							System.out.println(myAgent.getLocalName()+": emploi refus�");
							ACLMessage answer = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
							answer.addReceiver(msg.getSender());
							myAgent.send(answer);
						}

					}
					else {
						block();
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
		}
		
		public boolean done() {
			return terminate;
		}
	}

	protected void takeDown() {
		System.out.println(getAID().getName()+" terminating.");
	}
}
