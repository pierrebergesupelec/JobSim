import java.util.ArrayList;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


public class Entreprise extends Agent {
	
	int nbEmplois1;//contrairement à Etat, ce paramètre n'est pas fixe tout au long du processus.
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
	
	//Dans notre modèle, la moyenne de la demande est proportionnelle au nombre d'individus.
	double dmd;
	//Le coefficient de proportionnalité entre dmd et nombre d'individus est noté alpha
	double alpha;
	//On fixe seulement l'écart-type au départ.
	double dmd_dev;
	double rapport_cdd_init;//proportion initiale de CDD
	
	//contraintes
	int seuil_ouvriers;
	int seuil_techniciens;
	
	protected void setup() {
		// Initialisation message
		System.out.println("Entreprise "+getLocalName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 17) {
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
			alpha = (double) args[12];
			dmd_dev = (double) args[13];
			rapport_cdd_init = (double) args[14];
			seuil_ouvriers = (int) args[15];
			seuil_techniciens = (int) args[16];
			
			// CrÃ©er les emplois
			/*emplois = new ArrayList<Emploi>();
			for(int i=0; i<nbEmplois1; i++){
				emplois.add(new Emploi(r1,tl1,tl_dev1,this.getAID(),Individu.Qualification.OUVRIER,Integer.MAX_VALUE));
			}
			for(int i=0; i<nbEmplois2; i++){
				emplois.add(new Emploi(r2,tl2,tl_dev2,this.getAID(),Individu.Qualification.TECHNICIEN,Integer.MAX_VALUE));
			}
			for(int i=0; i<nbEmplois3; i++){
				emplois.add(new Emploi(r3,tl3,tl_dev3,this.getAID(),Individu.Qualification.CADRE,Integer.MAX_VALUE));
			}*///TODO
			
			// Register "etat" service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("entreprise");
			sd.setName("");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// Add behaviour
			//TODO

		}
		else {
			// Make the agent terminate
			System.out.println("Entreprise is not correctly initialised.");
			doDelete();
		}
	}

}
