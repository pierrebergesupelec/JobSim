import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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


public class Entreprise extends Agent {
	
	public static int[] dureeCDD = new int[]{3,6,9,12,24};
	
	int nbEmplois1_i;	// Nombre d'emplois initial
	int nbEmplois2_i;
	int nbEmplois3_i;
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
	
	//Dans notre mod�le, la moyenne de la demande est proportionnelle au nombre d'individus.
	double dmd;
	// Production
	double prod;
	//Le coefficient de proportionnalit� entre dmd et nombre d'individus est not� alpha
	double alpha;
	//On fixe seulement l'�cart-type au d�part.
	double dmd_dev;
	double rapport_cdd_init;//proportion initiale de CDD
	
	//Paramètres prolongation CDD en CDI
	double a;
	double b;
	
	//Paramètres création d'emplois
	double c;
	double d;
	int m;
	
	//productivit� de chaque qualification
	double prod1;
	double prod2;
	double prod3;
	
	//contraintes
	int seuil_ouvriers;
	int seuil_techniciens;
	
	// Générateur
	private Random random;
	
	//choix d'une dur�e de CDD
	public int choix_duree_CDD(){
		int index = (int) (Math.random()*dureeCDD.length);
		return dureeCDD[index];
	}
	
	protected void setup() {
		// Initialisation message
		System.out.println("Entreprise "+getLocalName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 25) {
			nbEmplois1_i = (int) args[0];
			nbEmplois2_i = (int) args[1];
			nbEmplois3_i = (int) args[2];
			r1 = (double) args[3];
			r2 = (double) args[4];
			r3 = (double) args[5];
			tl1 = (double) args[6];
			tl2 = (double) args[7];
			tl3 = (double) args[8];
			tl_dev1 = (double) args[9];
			tl_dev2 = (double) args[10];
			tl_dev3 = (double) args[11];
			alpha = (double) args[12];
			dmd_dev = (double) args[13];
			rapport_cdd_init = (double) args[14];
			seuil_ouvriers = (int) args[15];
			seuil_techniciens = (int) args[16];
			prod1 = (double) args[17];
			prod2 = (double) args[18];
			prod3 = (double) args[19];
			a = (double) args[20];
			b = (double) args[21];
			c = (double) args[22];
			d = (double) args[23];
			m = (int) args[24];
			
			// Initialiser générateur
			random = new Random();
			
			// Créer les emplois
			emplois = new ArrayList<Emploi>();
			// poleEmploi est le destinataire ----
			AID poleEmploi = new AID();
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("poleemploi");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(this, template);
				poleEmploi = result[0].getName();
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			//------------------------------------
			int nb_cdd1 = (int) (rapport_cdd_init*nbEmplois1_i);
			int nb_cdi1 = nbEmplois1_i - nb_cdd1;
			for(int i=0; i<nb_cdd1; i++){
				Emploi e = new Emploi(r1,tl1,tl_dev1,this.getAID(),Individu.Qualification.OUVRIER,choix_duree_CDD());
				emplois.add(e);
				addBehaviour(new Publier1Emploi(e,poleEmploi));
			}
			for(int i=0; i<nb_cdi1; i++){
				Emploi e = new Emploi(r1,tl1,tl_dev1,this.getAID(),Individu.Qualification.OUVRIER,Integer.MAX_VALUE);
				emplois.add(e);
				addBehaviour(new Publier1Emploi(e,poleEmploi));
			}
			
			int nb_cdd2 = (int) (rapport_cdd_init*nbEmplois2_i);
			int nb_cdi2 = nbEmplois2_i - nb_cdd2;
			for(int i=0; i<nb_cdd2; i++){
				Emploi e = new Emploi(r2,tl2,tl_dev2,this.getAID(),Individu.Qualification.TECHNICIEN,choix_duree_CDD());
				emplois.add(e);
				addBehaviour(new Publier1Emploi(e,poleEmploi));
			}
			for(int i=0; i<nb_cdi2; i++){
				Emploi e = new Emploi(r2,tl2,tl_dev2,this.getAID(),Individu.Qualification.TECHNICIEN,Integer.MAX_VALUE);
				emplois.add(e);
				addBehaviour(new Publier1Emploi(e,poleEmploi));
			}
			
			int nb_cdd3 = (int) (rapport_cdd_init*nbEmplois3_i);
			int nb_cdi3 = nbEmplois3_i - nb_cdd3;
			for(int i=0; i<nb_cdd3; i++){
				Emploi e = new Emploi(r3,tl3,tl_dev3,this.getAID(),Individu.Qualification.CADRE,choix_duree_CDD());
				emplois.add(e);
				addBehaviour(new Publier1Emploi(e,poleEmploi));
			}
			for(int i=0; i<nb_cdi3; i++){
				Emploi e = new Emploi(r3,tl3,tl_dev3,this.getAID(),Individu.Qualification.CADRE,Integer.MAX_VALUE);
				emplois.add(e);
				addBehaviour(new Publier1Emploi(e,poleEmploi));
			}
			
			// Register "entreprise" service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd2 = new ServiceDescription();
			sd2.setType("clock");
			sd2.setName(getName());
			dfd.addServices(sd2);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add behaviour
			addBehaviour(new Comptabilite());
			addBehaviour(new Demission());
			addBehaviour(new ProlongationCDD());

		}
		else {
			// Make the agent terminate
			System.out.println("Entreprise is not correctly initialised.");
			doDelete();
		}
	}
	
	//Behaviour qui s'occupe chaque mois de calculer la production, la demande
	private class Comptabilite extends CyclicBehaviour{

		@Override
		public void action() {
			// clock msg
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			// Calculer la production/demande chaque mois
			if(msg != null && msg.getContent().equals("clock")){ 
				// Calculer demande
				dmd = alpha*Statistiques.getIndividus().size()+dmd_dev*random.nextGaussian();
				System.out.println("Demande : "+dmd); //TODO
				// Calcul production
				int nbEmplois1 = 0;
				int nbEmplois2 = 0;
				int nbEmplois3 = 0;
				for(Emploi e:emplois){
					if(e.getQualif().equals(Individu.Qualification.CADRE))		nbEmplois3++;
					if(e.getQualif().equals(Individu.Qualification.TECHNICIEN))	nbEmplois2++;
					if(e.getQualif().equals(Individu.Qualification.OUVRIER))	nbEmplois1++;
				}
				prod = prod1*nbEmplois1+prod2*nbEmplois2+prod3*nbEmplois3;
				System.out.println("Production : "+prod); //TODO
				// Lancer la création d'emplois
				addBehaviour(new CreerEmplois(nbEmplois1,nbEmplois2,nbEmplois3));
			}
			else{
				block();
			}
		}
		
	}
	
	//Behaviour qui s'occupe des demandes de prolongation
	private class ProlongationCDD extends CyclicBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchConversationId("prolongation"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
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
					// 3 cas :
					// Cas 1 : refuser
					if(prod>a*dmd){
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setConversationId("prolongation");
						reply.setContent("prolongation");
						myAgent.send(reply);
						// Informer PoleEmploi de la fin de l'emploi e
						addBehaviour(new DemissionInformerPoleEmploi(e));
						// Mettre à jour la liste "emplois"
						emplois.remove(e);
					}
					// Cas 2 : prolonger en CDI
					else if(prod<b*dmd){
						Emploi newJob = new Emploi(e);
						newJob.setDuree(Integer.MAX_VALUE);
						
						// Répondre à l'individu par une affirmation en envoyant l'emploi newJob
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						try {
							reply.setContentObject(newJob);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						reply.setConversationId("prolongation");
						myAgent.send(reply);
						
						// Mettre à jour la liste "emplois"
						emplois.remove(e);
						emplois.add(newJob);
					}
					// Cas 2 : prolonger en CDD
					else{
						Emploi newJob = new Emploi(e);
						newJob.setDuree(choix_duree_CDD());
						
						// Répondre à l'individu par une affirmation en envoyant l'emploi newJob
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						try {
							reply.setContentObject(newJob);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						reply.setConversationId("prolongation");
						myAgent.send(reply);
						
						// Mettre à jour la liste "emplois"
						emplois.remove(e);
						emplois.add(newJob);
					}
				}
				else{
					// Répondre par un refus (cas d'erreur: l'emploi n'est pas trouvé)
					System.err.println("Erreur dans le protocole de prolongationCDD : "+myAgent.getLocalName()+" !!");
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setConversationId("prolongation");
					reply.setContent("prolongation");
					myAgent.send(reply);
					// Informer PoleEmploi de la fin de l'emploi e
					addBehaviour(new DemissionInformerPoleEmploi(e));
					// Mettre à jour la liste "emplois"
					emplois.remove(e);
				}
			}
			else{
				block();
			}
		}
		
	}
	
	// Behaviour qui s'occupe de créer de nouveaux emplois
	private class CreerEmplois extends OneShotBehaviour{
		
		int nbEmplois1;
		int nbEmplois2;
		int nbEmplois3;
		
		public CreerEmplois(int n1, int n2, int n3){
			nbEmplois1 = n1;
			nbEmplois2 = n2;
			nbEmplois3 = n3;
		}
		
		@Override
		public void action() {
			
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
			
			// Générer CDI
			if(prod<d*dmd){
				for(int i=0; i<m; i++){
					ArrayList<Individu.Qualification> list = new ArrayList<Individu.Qualification>();
					list.add(Individu.Qualification.OUVRIER);
					if(nbEmplois1>seuil_ouvriers)		list.add(Individu.Qualification.TECHNICIEN);
					if(nbEmplois2>seuil_techniciens)	list.add(Individu.Qualification.CADRE);
					int index = (int)(Math.random()*list.size());
					Individu.Qualification q = list.get(index);
					
					// valeur réelle de r aléatoire entre 0.5*ri et 1.5*ri
					double r;
					if(q.equals(Individu.Qualification.CADRE)) 				r = Math.random()*r3+r3*0.5; 
					else if(q.equals(Individu.Qualification.TECHNICIEN)) 	r = Math.random()*r2+r2*0.5; 
					else 											 		r = Math.random()*r1+r1*0.5; 
					
					Emploi e = new Emploi(r,tl1,tl_dev1,myAgent.getAID(),q,Integer.MAX_VALUE);
					// Envoyer la proposition à PoleEmploi
					addBehaviour(new Publier1Emploi(e,poleEmploi));
					// Mettre à jour emplois
					emplois.add(e);
				}
			}
			
			// Générer CDD
			if(prod<c*dmd){
				for(int i=0; i<m; i++){
					ArrayList<Individu.Qualification> list = new ArrayList<Individu.Qualification>();
					list.add(Individu.Qualification.OUVRIER);
					if(nbEmplois1>seuil_ouvriers)		list.add(Individu.Qualification.TECHNICIEN);
					if(nbEmplois2>seuil_techniciens)	list.add(Individu.Qualification.CADRE);
					int index = (int)(Math.random()*list.size());
					Individu.Qualification q = list.get(index);
					
					// valeur réelle de r aléatoire entre 0.5*ri et 1.5*ri
					double r;
					if(q.equals(Individu.Qualification.CADRE)) 				r = Math.random()*r3+r3*0.5; 
					else if(q.equals(Individu.Qualification.TECHNICIEN)) 	r = Math.random()*r2+r2*0.5; 
					else 											 		r = Math.random()*r1+r1*0.5; 
					
					Emploi e = new Emploi(r,tl1,tl_dev1,myAgent.getAID(),q,choix_duree_CDD());
					// Envoyer la proposition à PoleEmploi
					addBehaviour(new Publier1Emploi(e,poleEmploi));
					// Mettre à jour emplois
					emplois.add(e);
				}
			}
		}
		
	}
	
	// Behaviour qui publie 1 emploi chez PoleEmploi
	private class Publier1Emploi extends OneShotBehaviour{

		Emploi e;
		AID poleEmploi;
		
		public Publier1Emploi(Emploi emploi, AID pe){
			e = emploi;
			poleEmploi = pe;
		}
		
		@Override
		public void action() {
			// Message
			ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

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
	
	// Behaviour qui s'occupe de gérer les démissions
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
					
					// Supprimer l'emploi de la liste "emplois"
					emplois.remove(e);
					
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

}
