import java.util.ArrayList;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
	double r1;
	double r2;
	double r3;
	double tl1;
	double tl2;
	double tl3;
	double tl_dev1;
	double tl_dev2;
	double tl_dev3;
	ArrayList<Emploi> emplois;
	Random random;
	PoleEmploi poleEmploi;

	protected void setup() {
		// Initialisation message
		System.out.println("Etat "+getAID().getName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 14) {
			nbEmplois1 = (int) args[0];
			nbEmplois2 = (int) args[1];
			nbEmplois3 = (int) args[2];
			r1 = (double) args[3];
			r2 = (double) args[4];
			r3 = (double) args[5];
			tl1 = (double) args[6];
			tl2 = (double) args[7];
			tl3 = (double) args[8];
			tl_dev1 = (double) args[9];
			tl_dev2 = (double) args[10];
			tl_dev3 = (double) args[11];
			random = (Random) args[12];
			poleEmploi = (PoleEmploi) args[13];
			
			// Créer les emplois
			emplois = new ArrayList<Emploi>();
			for(int i=0; i<nbEmplois1; i++){
				emplois.add(new Emploi(r1,tl1,tl_dev1,random,this.getAID(),Individu.Qualification.OUVRIER));
			}
			for(int i=0; i<nbEmplois2; i++){
				emplois.add(new Emploi(r2,tl2,tl_dev2,random,this.getAID(),Individu.Qualification.TECHNICIEN));
			}
			for(int i=0; i<nbEmplois3; i++){
				emplois.add(new Emploi(r3,tl3,tl_dev3,random,this.getAID(),Individu.Qualification.CADRE));
			}
			
			// Publier les emplois dans poleEmploi pour la première fois
			// Les autres fois, il suffira de republier seulement quand un emploi est de novueau libre (démission d'un employé)
			for(Emploi e:emplois){
				if(!e.estPourvu()){
					// publier l'emploi dans PoleEmploi
					if(!poleEmploi.attente.contains(e))	poleEmploi.attente.add(e);
				}
			}
			
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
			
			// Add behaviour
			addBehaviour(new Demission());
		}
		else {
			// Make the agent terminate
			System.out.println("Etat is not correctly initialised.");
			doDelete();
		}
	}

	private class PublierEmplois extends OneShotBehaviour {
		
		Emploi emploi;
		
		PublierEmplois(Emploi e){
			emploi = e;
		}
		
		public void action() {
			// publier l'emploi dans PoleEmploi
			if(!poleEmploi.attente.contains(emploi))	poleEmploi.attente.add(emploi);
		}
	}
	
	private class Demission extends CyclicBehaviour {
		
		public void action() {
			// Gérer le protocole de démission
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null && msg.getContent().equals("demission")) {
				// Gérer la démission
				System.out.println(myAgent.getLocalName()+": demission reçue");
				
				Emploi e = null;
				boolean found = false;
				// Chercher l'emploi correspondant
				for(Emploi tmp : emplois){
					if(tmp.getEmploye()==msg.getSender()){
						found = true;
						e = tmp;
						break;
					}
				}
				
				if(found){
					// Répondre par une confirmation
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent("demission");
					myAgent.send(reply);

					// Supprimer l'emploi de la liste "pourvus" de poleEmploi
					poleEmploi.pourvus.remove(e);

					// Supprimer le champs "employe" de l'emploie
					e.setEmploye(null);

					// Republier l'emploi
					myAgent.addBehaviour(new PublierEmplois(e));
				}
				else{
					// Répondre par une erreur
					System.err.println("Erreur dans le protocole de démission!!");
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("demission");
					myAgent.send(reply);
				}
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
