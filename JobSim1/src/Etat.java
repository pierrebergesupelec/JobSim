import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import jade.core.AID;
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

	protected void setup() {
		// Initialisation message
		System.out.println("Etat "+getAID().getName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 13) {
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
			
			// Register "etat" service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("etat");
			sd.setName("");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add behaviour
			addBehaviour(new PublierEmplois());
			addBehaviour(new Demission());
		}
		else {
			// Make the agent terminate
			System.out.println("Etat is not correctly initialised.");
			doDelete();
		}
	}

	private class PublierEmplois extends OneShotBehaviour{

		@Override
		public void action() {
			for(Emploi e : emplois){
				System.out.println("Etat"+e);
				// Message
				ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

				// poleEmploi est le destinataire
				AID poleEmploi = new AID();
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("poleemploi");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					poleEmploi = result[0].getName();
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
				msg.addReceiver(poleEmploi);

				// gestion des emplois
				try {
					msg.setContentObject(e);// PJ
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// envoi
				myAgent.send(msg);
			}
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
					System.out.println(tmp.getID()+" / "+Integer.parseInt(msg.getContent()));
					if(tmp.getID()==Integer.parseInt(msg.getContent())){
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
					
					// Supprimer le champs "employe" de l'emploie
					e.setEmploye(null);
					
					ACLMessage newJob = new ACLMessage(ACLMessage.INFORM);
					// poleEmploi est le destinataire
					AID poleEmploi = new AID();
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("poleemploi");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template);
						poleEmploi = result[0].getName();
					}
					catch (FIPAException fe) {
					fe.printStackTrace();
					}
					newJob.addReceiver(poleEmploi);
					//gestion des emplois
					try {
						newJob.setContentObject(e);// PJ
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					//envoi
					myAgent.send(newJob);
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
