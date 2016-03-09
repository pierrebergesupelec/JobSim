import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {


	public static void main(String[] args) throws StaleProxyException, FileNotFoundException {
		// Parametres
		
		int pasHorloge = 200;
		
		double proportion_ouvriers = 0.4;
		double proportion_techniciens = 0.3;
		double proportion_cadres = 0.3;
		int nb_initial = 320;
		int nb_ouvriers = (int) (nb_initial*proportion_ouvriers);
		int nb_techniciens = (int) (nb_initial*proportion_techniciens);
		int nb_cadres = (int) (nb_initial*proportion_cadres);
		int[] nbInitial = {nb_ouvriers,nb_techniciens,nb_cadres};
		
		double epsilon = 0;
		int nbEmplois1 = (int) (nb_initial*(proportion_ouvriers-epsilon));
		int nbEmplois2 = (int) (nb_initial*(proportion_techniciens-epsilon));
		int nbEmplois3 = (int) (nb_initial*(proportion_cadres-epsilon));
		
		double r_1 = 1000;
		double r_2 = 2000;
		double r_3 = 3000;
		double e_tl1 = 10;
		double e_tl2 = 10;
		double e_tl3 = 10;
		double e_tl_dev1 = 2;
		double e_tl_dev2 = 2;
		double e_tl_dev3 = 2;
		
		int x = 3;
		int y = 3;
		double z = 0.02;
		
		double tl_mean = 8;			// temps libre attendu assez faible -> situation stable
		double tl_std_dev = 0;			// idem
		double[] rm_mean = new double[]{1000, 2000, 3000};
		double[] rm_std_dev = new double[]{200, 200, 200};
		
		int nb_arrivants_moyen = 8;
		double nb_arrivants_std_dev = 0;
		
		// Paramètres "entreprises"
		int nbEntreprises = 1;
		double a = 1.2;
		double b = 0.8;
		double c = 1;
		double d = 0.7;
		int m = 5;
		double prod1 = 1100;
		double prod2 = 2200;
		double prod3 = 3300;
		double dmd_dev = 100;
		double rapport_cdd_init = 0.3;
		int seuil_ouvriers = 10;
		int seuil_techniciens = 10;
		double alpha = ((prod1+prod2+prod3)*nbEntreprises)/3.0;
		
		PrintWriter sortie = new PrintWriter("jobsim_rm_z" + z + ".txt");
		
		int seed = 10; 
		Random random = new Random(seed);
		
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		Profile pMain = new ProfileImpl("localhost",8888,null);
		// Les 2 lignes suivantes nous ont posé beaucoup de problème... ------------
		// La taille des search du DF étant limité à 100 sinon
		String property_dx_maxresult = "10000";
		pMain.setParameter("jade_domain_df_maxresult", property_dx_maxresult); 
		// -------------------------------------------------------------------------
		AgentContainer mc = rt.createMainContainer(pMain);

		// Classe qui s'occupe des statistiques sur les individus
		AgentController statistiques = mc.createNewAgent("statistiques", "Statistiques", new Object[]{sortie});
		statistiques.start();
		
		AgentController poleEmploi = mc.createNewAgent("poleEmploi", "PoleEmploi", new Object[0]);
		poleEmploi.start();
		
		/*
		Object[] paramEtat = new Object[]{nbEmplois1 ,nbEmplois2, nbEmplois3,r_1,r_2,r_3,e_tl1,e_tl2,e_tl3,e_tl_dev1,e_tl_dev2,e_tl_dev3};
		AgentController etat = mc.createNewAgent("etat", "Etat", paramEtat);
		etat.start();
		*/
		int compteur = 0;
		for(int degreQualif=0; degreQualif<nbInitial.length; degreQualif++){
			for(int i=0; i<nbInitial[degreQualif]; i++){
				Individu.Qualification qualif = Individu.Qualification.values()[degreQualif];
				//System.out.println(qualif);
				double rm = random.nextGaussian()*rm_std_dev[degreQualif]+rm_mean[degreQualif];
				double tl = random.nextGaussian()*tl_std_dev+tl_mean;
				Object[] paramIndividu = new Object[]{qualif, rm, tl, x, y, z, Math.random()*43.0};
				AgentController individu = mc.createNewAgent("individu "+compteur, "Individu", paramIndividu);
				compteur++;
				individu.start();
			}
		}
		
		Object[] paramHorloge = new Object[]{pasHorloge};
		AgentController horloge = mc.createNewAgent("horloge", "Horloge", paramHorloge);
		horloge.start();
		
		Object[] paramGenerateur = {nb_arrivants_moyen, nb_arrivants_std_dev, nb_initial,
									proportion_ouvriers, proportion_techniciens, proportion_cadres,
									rm_mean, rm_std_dev, tl_mean, tl_std_dev,
									x, y, z, mc};
		AgentController generateur = mc.createNewAgent("generateurIndividu", "GenerateurIndividu", paramGenerateur);
		generateur.start();
		
		for(int i=0; i<nbEntreprises; i++){
			Object[] paramE = new Object[]{10,10,10,r_1,r_2,r_3,e_tl1,e_tl2,e_tl3,e_tl_dev1,e_tl_dev2,e_tl_dev3,alpha,dmd_dev,rapport_cdd_init,
					seuil_ouvriers,seuil_techniciens,prod1,prod2,prod3,a,b,c,d,m};
			AgentController e = mc.createNewAgent("entreprise "+i, "Entreprise", paramE);
			e.start();
		}
	}

}
