import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Horloge extends Agent{

	private DFAgentDescription[] allAgents;
	private int pasHorloge;
	
	protected void setup(){
		// Initialisation message
		System.out.println(getLocalName()+" is ready.");

		// Get the parameters
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			pasHorloge = (int) args[0];
			// Pas de l'horloge = pas du TickerBehaviour
			addBehaviour(new TickerBehaviour(this, pasHorloge) {
				protected void onTick() {
					System.out.println("1 month has passed");
					
					// TODO
					System.out.println("Nombre d'inscrits* " + GenerateurIndividu.nb_inscrits);
					System.out.println("Nombre de chômeurs* " + GenerateurIndividu.nb_chomeurs);
					double taux = (GenerateurIndividu.nb_chomeurs*100.0)/GenerateurIndividu.nb_inscrits;
					System.out.println("taux de chomage* :" + taux);
					
					// Maj de la liste de tous les agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("clock");
					template.addServices(sd);
					try {
						allAgents = DFService.search(myAgent, template); 
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Envoie du message
					ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
					for (int i = 0; i < allAgents.length; ++i) {
						cfp.addReceiver(allAgents[i].getName());
					} 
					cfp.setContent("clock");
					cfp.setConversationId("clock");
					myAgent.send(cfp);
				}
			} );
		}
		else {
			// Make the agent terminate
			System.out.println(getLocalName()+" is not correctly initialised.");
			doDelete();
		}

	}
	
}
