import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Individu extends Agent{
	
	int qualif;
	double rm;
	double tl;
	int x;
	int y;
	int z;
	Emploi emploi;
	
	protected void setup() {
		// Initialisation message
		System.out.println(getAID().getName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 6) {
			qualif = (int) args[0];
			rm = (double) args[1];
			tl = (double) args[2];
			x = (int) args[3];
			y = (int) args[4];
			z = (int) args[5];
			
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
			// TODO
			//addBehaviour(new avecEmploi());
			//addBehaviour(new sansEmploi());
		}
		else {
			// Make the agent terminate
			System.out.println(getAID().getName()+" is not correctly initialised.");
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println(getAID().getName()+" terminating.");
	}
}
