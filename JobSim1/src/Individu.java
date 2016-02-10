import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
	int moisSansTl;//nombre de mois que l'individu passe sans le temps libre souhait�
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
				System.out.println(myAgent.getLocalName()+": clock reçu"); //TODO à enlever
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
		private int step = 0;//en tout 2 �tapes : demande et confirmation
		private boolean terminate = false;//processus de d�cision termin�
		
		public void action() {
			switch(step){
			case 0:
				// Envoie du message
				ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
				req.addReceiver(emploi.getEmployeur().getAID());
				req.setContent("demission");
				myAgent.send(req);
				step = 1;
			case 1:
				// Reception de la réponse
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.FAILURE));
				ACLMessage msg = myAgent.receive(mt);
				if(msg.getPerformative()==ACLMessage.CONFIRM){
					// Terminer ce behaviour
					terminate = true;
					// Supprime l'emploi de la mémoire de l'individu
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
			// A chaque pas d'horloge, incrémenter  moisSansEmploi
			if (msg_clock != null && msg_clock.getContent().equals("clock")) {
				moisSansEmploi ++;
				System.out.println(myAgent.getLocalName()+": clock reçu"); //TODO à enlever
				/*MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// TODO protocole pour l'acceptation ou non d'un emploi
					System.out.println(myAgent.getLocalName()+": proposition d'emploi reçu");
					// Obtenir l'emploi et passer en behaviour -> avecEmploi
					// Renvoyer accept proposal
					// moisSansEmploi = 0;
					// Etc TODO
				}*/
				
				//pas vraiment d'accord avec �a: le gusse va � poleEmploi pour chercher du taf, pas l'inverse
				//proposition d'une autre structure
				addBehaviour(new recevoirOffres());
				
				//r�ception message PoleEmploi
				//TODO � compl�ter
				ArrayList<Emploi> listeEmplois = new ArrayList<Emploi>();//TODO, � mettre la liste re�ue par PoleEmploi
				
				//choix offre
				addBehaviour(new choixEmploi(listeEmplois));
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

class recevoirOffres extends Behaviour{
	
	//TODO � compl�ter

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean done() {
		return true;
	}
	
}

class choixEmploi extends Behaviour{
	
	ArrayList<Emploi> liste;
	
	choixEmploi(ArrayList<Emploi> liste){
		this.liste = liste;
	}

	@Override
	public void action() {
		// TODO m�thode de choix
		//je propose un truc de type glouton, d�s qu'il trouve un truc satisfaisant il prend
		//il envoie sa candidature
	}

	@Override
	public boolean done() {
		return true;
	}
	
}
