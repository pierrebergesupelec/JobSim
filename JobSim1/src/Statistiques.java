import java.io.PrintWriter;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.CyclicBehaviour;


public class Statistiques extends Agent{

	private static ArrayList<Individu> individus;
	private static int monthsSinceStart = 0;
	PrintWriter sortie;
	int save_data_end = 12*40;
	int compteur = 0;
	
	protected void setup() {
		
		individus = new ArrayList<Individu>();
		monthsSinceStart = 0;
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			sortie = (PrintWriter) args[0];
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
		
		// Add behaviours
		addBehaviour(new stats());
	}
	
	public static ArrayList<Individu> getIndividus() {
		return individus;
	}

	public static void addIndividu(Individu i) {
		Statistiques.individus.add(i);
	}
	
	public static void removeIndividu(Individu i) {
		Statistiques.individus.remove(i);
	}

	private class stats extends CyclicBehaviour{
		
		public void action() {
			// clock msg
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null && msg.getContent().equals("clock")) {
				monthsSinceStart ++;
				// Taux de chomage
				int nbChomeurs = 0;
				double rm_moyen = 0;
				int emploisPubliques = 0;
				int emploisPrives = 0;
				for(Individu i : individus){
					if(i.emploi == null){
						nbChomeurs++;
					}
					else{
						if(i.emploi.getEmployeur().getLocalName().equals("etat")){
							emploisPubliques++;
						}
						else	emploisPrives++;
					}
					rm_moyen += i.rm;
				}
				rm_moyen = rm_moyen / individus.size();
				System.out.println("Nombre d'inscrits : " + individus.size());
				System.out.println("Nombre de chômeurs : " + nbChomeurs);
				double taux = (nbChomeurs*100.0)/individus.size();
				System.out.println("Taux de chomage : " + taux + " %");
				System.out.println("Revenu moyen exigé : " + rm_moyen + " ");
				// Stats sur l'Etat
				System.out.println("Embauches Etat : " + emploisPubliques);
				System.out.println("Embauches privées : " + emploisPrives);
				// ----------------
				int rm_int = (int) (rm_moyen*100);
				double rm_print = rm_int/100.0;
				if (compteur < save_data_end) sortie.print(rm_print + " ");
				if (compteur == save_data_end) sortie.close();
				compteur++;
			}
			else {
				block();
			}
		}
	}
	
	protected void takeDown() {
		// Deregister
		try
		{
			DFService.deregister(this);
		} catch (FIPAException fe) {
			System.err.println("Can't deregister "+getLocalName()+"!");
		}
		// Affichage
		System.err.println(getLocalName()+" terminating.");
	}
	
}
