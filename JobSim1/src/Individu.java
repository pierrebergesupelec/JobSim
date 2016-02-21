
import java.io.IOException;
import java.util.Iterator;
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
	int moisSansTl;//nombre de mois que l'individu passe sans le temps libre souhaité
	int moisSansEmploi;//nombre de mois sans emploi
	int offresRefusees;//nombre d'offre refuées
	int x;
	int y;
	double z;
	
	double annees_cotisation = 0.0;
	
	public static double depart_retraite = 43.0;
	
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
			offresRefusees = 0;
			
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
			System.out.println(getLocalName()+" is ready. "+qualif.name());
		}
		else {
			// Make the agent terminate
			System.out.println(getLocalName()+" is not correctly initialised.");
			doDelete();
		}
	}
	
	private class avecEmploi extends Behaviour {
		
		private boolean terminate = false;
		
		public void action() {
			// clock msg
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			// Proposition d'emploi msg -> renvoyer un refu
			MessageTemplate mt_e = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			ACLMessage msg_e = myAgent.receive(mt_e);
			// Conditions
			boolean condition1 = msg != null && msg.getContent().equals("clock");
			boolean condition2 = msg_e != null;
			if (condition1) {
				//+1 mois de cotisation pour la retraite
				annees_cotisation += (1.0/12.0);
				// Vérifier que l'individu a un emploi
				if(emploi!=null){
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
						System.out.println(myAgent.getLocalName() + " démissionne de l'emploi "+emploi);
						// Terminer ce behaviour
						terminate = true;
					}
					System.out.println(myAgent.getLocalName() + " Un mois de travail en plus: tlreel="+tl_reel+", tlindiv="+tl+", moisSansTl="+moisSansTl);
				}
			}
			if(condition2){
				ACLMessage answer = msg_e.createReply();
				answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
				try {
					answer.setConversationId(Integer.toString(((Emploi)msg_e.getContentObject()).getID()));
					answer.setContentObject(msg_e.getContentObject());
					System.out.println(myAgent.getLocalName()+": "+(Emploi)msg_e.getContentObject()+" refusé, car déjà employé.");
				} catch (Exception e) {
					e.printStackTrace();
				}
				myAgent.send(answer);
			}
			if(!condition1 && !condition2) {
				block();
			}
		}
		
		public boolean done() {
			return terminate;
		}
	}
	
	private class Demissionner extends Behaviour {
		private int step = 0;//en tout 2 etapes : demande et confirmation
		private boolean terminate = false;//processus de décision terminé
		
		public void action() {
			switch(step){
			case 0:
				// Envoie du message
				ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
				req.addReceiver(emploi.getEmployeur());
				req.setContent(Integer.toString(emploi.getID()));
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
						// Supprime l'emploi de la mémoire de l'individu
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
			// A chaque pas d'horloge, incrémenter  moisSansEmploi
			if (msg_clock != null && msg_clock.getContent().equals("clock")) {
				moisSansEmploi ++;
			}
			try {
				if (msg != null && msg.getContentObject() instanceof Emploi) {
					//protocole pour l'acceptation ou non d'un emploi
					//réponse à l'offre reçu
					if (emploi == null){//condition 1: inactif
						Emploi e;
						try {
							e = (Emploi) msg.getContentObject();
							boolean goodQualif = e.getQualif() == qualif;
							System.out.println(myAgent.getLocalName()+": proposition d'emploi recu "+e.getQualif()+" "+qualif+" "+e.getRevenu()+" "+rm);
							if (goodQualif && e.getRevenu()>=rm){
								System.out.println(myAgent.getLocalName()+": "+e+" accepté");
								//envoi réponse
								ACLMessage answer = msg.createReply();			
								answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								answer.setConversationId(Integer.toString(e.getID()));
								answer.setContentObject(e);
								myAgent.send(answer);
								moisSansEmploi = 0;
								emploi = e;
								emploi.setEmploye(getAID());
								// Ajouter le behaviour avecEmploi
								addBehaviour(new avecEmploi());
								// Terminer ce behaviour
								terminate = true;
								// RAZ offresRefusees
								offresRefusees = 0;
							}
							else {
								System.out.println(myAgent.getLocalName()+": "+e+" refusé");
								ACLMessage answer = msg.createReply();
								answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
								answer.setConversationId(Integer.toString(e.getID()));
								answer.setContentObject(e);
								myAgent.send(answer);
								// Incrémenter offresRefusees
								offresRefusees ++;
								// Décroitre le revenu attendu à chaque fois que offreRefusees devient multiple de y (sauf 0)
								if( offresRefusees != 0 && (offresRefusees % y) == 0){
									System.out.println(myAgent.getLocalName()+": décroit son revenu attendu de "+rm+" à "+rm*(1-z)+". ("+offresRefusees+" offres refusées.)");
									rm = rm*(1-z);
								}
							}
						} catch (UnreadableException | IOException e1) {
							e1.printStackTrace();
						}

					} else {
						System.out.println(myAgent.getLocalName()+": "+(Emploi)msg.getContentObject()+" refusé");
						ACLMessage answer = msg.createReply();
						answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
						try {
							answer.setConversationId(Integer.toString(((Emploi)msg.getContentObject()).getID()));
							answer.setContentObject(msg.getContentObject());
						} catch (Exception e) {
							e.printStackTrace();
						}
						myAgent.send(answer);
						// Incrémenter offresRefusees
						offresRefusees ++;
						// Décroitre le revenu attendu à chaque fois que offreRefusees devient multiple de y (sauf 0)
						if( offresRefusees != 0 && (offresRefusees % y) == 0){
							System.out.println(myAgent.getLocalName()+": décroit son revenu attendu de "+rm+" à "+rm*(1-z)+". ("+offresRefusees+" offres refusées.)");
							rm = rm*(1-z);
						}
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
		System.out.println(getLocalName()+" terminating.");
	}
}
