import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
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

	protected void setup() {
		// Initialisation message
		System.out.println("Etat "+getLocalName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 12) {
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
			
			// Créer les emplois
			emplois = new ArrayList<Emploi>();
			for(int i=0; i<nbEmplois1; i++){
				emplois.add(new Emploi(r1,tl1,tl_dev1,this.getAID(),Individu.Qualification.OUVRIER,Integer.MAX_VALUE));
			}
			for(int i=0; i<nbEmplois2; i++){
				emplois.add(new Emploi(r2,tl2,tl_dev2,this.getAID(),Individu.Qualification.TECHNICIEN,Integer.MAX_VALUE));
			}
			for(int i=0; i<nbEmplois3; i++){
				emplois.add(new Emploi(r3,tl3,tl_dev3,this.getAID(),Individu.Qualification.CADRE,Integer.MAX_VALUE));
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
				//System.out.println("Etat"+e);
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
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchConversationId("demission"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// Gérer la démission
				//System.out.println(myAgent.getLocalName()+": demission de "+msg.getSender().getLocalName()+" reçue");
				
				Emploi e = null;
				boolean found = false;
				// Chercher l'emploi correspondant
				for(Emploi tmp : emplois){
					if(tmp.getID()==Integer.parseInt(msg.getContent())){
						found = true;
						e = tmp;
						break;
					}
				}
				
				if(found){
					// Répondre à l'individu par une confirmation
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setConversationId("demission");
					reply.setContent("demission");
					myAgent.send(reply);
					
					// Supprimer le champs "employe" de l'emploie
					e.setEmploye(null);
					
					// Informer PoleEmploi de la démission
					addBehaviour(new DemissionInformerPoleEmploi(e));
				}
				else{
					// Répondre par une erreur
					System.err.println("Erreur dans le protocole de démission (Etat) !!");
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setConversationId("demission");
					reply.setContent("demission");
					myAgent.send(reply);
				}
			}
			else {
				block();
			}
		}
	}

	private class DemissionInformerPoleEmploi extends Behaviour {

		private boolean terminate = false;
		
		private Emploi e;
		private AID poleEmploi;
		private int step = 0;
		
		public DemissionInformerPoleEmploi(Emploi emploi){
			e = emploi;
			step = 0;
		}
		
		@Override
		public void action() {
			switch(step){
			case 0:
				
				poleEmploi = new AID();
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
				
				// Informer PoleEmploi de la suppression de cet emploi (<-> démission)
				ACLMessage oldJob = new ACLMessage(ACLMessage.INFORM);
				oldJob.addReceiver(poleEmploi);
				//gestion des emplois
				try {
					oldJob.setContentObject(e);// PJ
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//envoi
				myAgent.send(oldJob);
				
				step = 1;
				
			case 1:
				// Attente de la confirmation
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null){
					// Informer PoleEmploi de la remise sur le marché de cet emploi
					ACLMessage newJob = new ACLMessage(ACLMessage.PROPOSE);
					newJob.addReceiver(poleEmploi);
					//gestion des emplois
					try {
						newJob.setContentObject(e);// PJ
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					//envoi
					myAgent.send(newJob);
					terminate = true;
				}
				else{
					block();
				}
			}
		}

		@Override
		public boolean done() {
			return terminate;
		}
		
	}
	
	protected void takeDown() {
		System.out.println("Etat "+getLocalName()+" terminating.");
	}
}