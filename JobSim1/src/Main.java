import java.util.Random;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {


	public static void main(String[] args) throws StaleProxyException {
		// Parametres
		int nbInitial = 10;
		int nbEntrants = 5;
		int nbSortants = 5;
		int nbEmplois1 = 6;
		int nbEmplois2 = 4;
		int nbEmplois3 = 3;
		double r_1 = 1000;
		double r_2 = 2000;
		double r_3 = 3000;
		double e_tl1 = 10;
		double e_tl2 = 10;
		double e_tl3 = 10;
		double e_tl_dev1 = 5;
		double e_tl_dev2 = 5;
		double e_tl_dev3 = 5;
		int x = 3;
		int y = 10;
		double z = 0.10;
		double tl_mean = 10;
		double tl_std_dev = 5;
		double[] rm_mean = new double[]{1000, 2000, 3000};
		double[] rm_std_dev = new double[]{200, 200, 200};
		
		
		int seed = 0;
		Random random = new Random(seed);
		
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		Profile pMain = new ProfileImpl("localhost",8888,null);
		AgentContainer mc = rt.createMainContainer(pMain);

		AgentController poleEmploi = mc.createNewAgent("poleEmploi", "PoleEmploi", new Object[0]);
		poleEmploi.start();
		
		Object[] paramEtat = new Object[]{nbEmplois1 ,nbEmplois2, nbEmplois3,r_1,r_2,r_3,e_tl1,e_tl2,e_tl3,e_tl_dev1,e_tl_dev2,e_tl_dev3,random};
		AgentController etat = mc.createNewAgent("etat", "Etat", paramEtat);
		etat.start();
		
		for(int i=0; i<nbInitial; i++){
			int degreQualif = random.nextInt(3);
			Individu.Qualification qualif = Individu.Qualification.values()[degreQualif];
			System.out.println(qualif);
			double rm = random.nextGaussian()*rm_std_dev[degreQualif]+rm_mean[degreQualif];
			double tl = random.nextGaussian()*tl_std_dev+tl_mean;
			Object[] paramIndividu = new Object[]{qualif, rm, tl, x, y, z};
			AgentController individu = mc.createNewAgent("individu "+i, "Individu", paramIndividu);
			individu.start();
		}
		
		Object[] paramHorloge = new Object[]{1000};
		AgentController horloge = mc.createNewAgent("horloge", "Horloge", paramHorloge);
		horloge.start();
	}

}
