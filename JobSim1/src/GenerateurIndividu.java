import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;


public class GenerateurIndividu extends Agent {
	
	Random random;
	int nb_arrivants_moyen;
	double nb_arrivants_std_dev;
	int nb_initial;
	
	double proportion_ouvriers;
	double proportion_techniciens;
	double proportion_cadres;
	
	double[] rm_mean;
	double[] rm_std_dev;
	
	double tl_mean;
	double tl_std_dev;		
	
	int x;
	int y;
	double z;
	
	AgentContainer mc;
	
	int compteur = 0;
	int id_individus = 1;
	

	//statistiques
	public static int nb_inscrits = 0;
	public static int nb_chomeurs = 0;
	
	
	protected void setup() {
		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 15) {
			random = (Random) args[0];
			nb_arrivants_moyen = (int) args[1];
			nb_arrivants_std_dev = (double) args[2];
			nb_initial = (int) args[3];
			proportion_ouvriers = (double) args[4];
			proportion_techniciens = (double) args[5];
			proportion_cadres = (double) args[6];
			rm_mean = (double[]) args[7];
			rm_std_dev = (double[]) args[8];
			tl_mean = (double) args[9];
			tl_std_dev = (double) args[10];
			x = (int) args[11];
			y = (int) args[12];
			z = (double) args[13];
			mc = (AgentContainer) args[14];
			nb_chomeurs = nb_initial;
			nb_inscrits = nb_initial;
			
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
			addBehaviour(new generer());
			
			// Initialisation message
			System.out.println(getLocalName()+" is ready. ");
		}
		else {
			// Make the agent terminate
			System.out.println(getLocalName()+" is not correctly initialised.");
			doDelete();
		}
	}
	
	
	private class generer extends CyclicBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null && msg.getContent().equals("clock")) {
				compteur++;
				if ((compteur > 0) && (compteur%12 == 0)){
					int nb_arrivants = (int) (random.nextGaussian()*nb_arrivants_std_dev + nb_arrivants_moyen);
					
					int nb_ouvriers = (int) (nb_arrivants*proportion_ouvriers);
					int nb_techniciens = (int) (nb_arrivants*proportion_techniciens);
					int nb_cadres = (int) (nb_arrivants*proportion_cadres);
					
					int[] cardinal = {nb_ouvriers,nb_techniciens,nb_cadres};
					
					Individu.Qualification[] qualifications = {Individu.Qualification.OUVRIER, 
															Individu.Qualification.TECHNICIEN,
															Individu.Qualification.CADRE};
					
					for (int i = 0; i < 3; i++){
						for (int j = 0; j < cardinal[i]; j++){
							Individu.Qualification qualif = qualifications[i];
							double rm = random.nextGaussian()*rm_std_dev[i]+rm_mean[i];
							double tl = random.nextGaussian()*tl_std_dev+tl_mean;
							Object[] paramIndividu = new Object[]{qualif, rm, tl, x, y, z, 0.0};
							AgentController individu;
							try {
								individu = mc.createNewAgent("individu "+ (nb_initial+id_individus), "Individu", paramIndividu);
								individu.start();
								//System.out.println("Un nouvel individu sur le marché du travail !!");
								id_individus++;
								nb_chomeurs++;
								nb_inscrits++;
							} catch (StaleProxyException e) {
								e.printStackTrace();
							}
						}
					}
					compteur = 0;
				}
			}
			else{
				block();
			}
		}
		
	}
	

}
