
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
	
	public Qualification qualif;
	double rm;
	double tl;
	int moisSansTl;//nombre de mois que l'individu passe sans le temps libre souhaitï¿½
	int moisSansEmploi;//nombre de mois sans emploi
	int x;
	int y;
	double z;
	
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
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add behaviours
			addBehaviour(new sansEmploi());
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
				// VÃ©rifier que l'individu a un emploi
				if(emploi!=null){
					// Obtenir le temps libre de ce mois pour cet emploi 
					double tl_reel = emploi.tlRealisation();
					// Mettre Ã  jour le nombre de mois avec un temps libre < tl
					if(tl_reel<tl){
						moisSansTl ++;
					}
					else{
						moisSansTl = 0;
					}
					// VÃ©rifier la condition sur le tl et quitter Ã©ventuellement
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
		private int step = 0;//en tout 2 ï¿½tapes : demande et confirmation
		private boolean terminate = false;//processus de dï¿½cision terminï¿½
		
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
				// Reception de la rÃ©ponse
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.FAILURE));
				ACLMessage msg = myAgent.receive(mt);
				if(msg.getPerformative()==ACLMessage.CONFIRM){
					// Terminer ce behaviour
					terminate = true;
					// Supprime l'emploi de la mÃ©moire de l'individu
					emploi = null;
					// Ce mettre dans le behaviour sansEmploi
					addBehaviour(new sansEmploi());
				}
				else{
					step = 0;
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
			MessageTemplate mt_clock = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg_clock = myAgent.receive(mt_clock);
			// A chaque pas d'horloge, incrÃ©menter  moisSansEmploi
			if (msg_clock != null && msg_clock.getContent().equals("clock")) {
				moisSansEmploi ++;
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					//protocole pour l'acceptation ou non d'un emploi
					System.out.println(myAgent.getLocalName()+": proposition d'emploi recu");
					
					//réponse à l'offre reçu
					
					//l'individu possède-t-il la bonne qualification ?
					boolean goodQualif = msg.getContent().startsWith(qualif.name());
					//extraction du revenu
					msg.getContent().replaceAll("\\D+","");//enleve les lettres
					int revenu = Integer.parseInt(msg.getContent());//extrait int
					//conditions pour un réponse positive
					if (emploi==null && goodQualif && revenu > rm){
						ACLMessage answer = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						answer.addReceiver(msg.getSender());
						myAgent.send(answer);
						moisSansEmploi = 0;
						//TODO définir nouvel emploi, plus de PJ ?
					} else {
						ACLMessage answer = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						answer.addReceiver(msg.getSender());
						myAgent.send(answer);
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

	protected void takeDown() {
		System.out.println(getAID().getName()+" terminating.");
	}
}
