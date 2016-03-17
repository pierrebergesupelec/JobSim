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
		
		// Lorsque le nombre d'entreprises est Ã©levÃ©, il peut y avoir surcharge des messages transmis
		// Il faut donc augmenter le pasHorloge pour donner le temps aux agents de traiter ces messages
		int pasHorloge = 1000;
		
		//proportion de chaque catégorie
		double proportion_ouvriers = 0.4;
		double proportion_techniciens = 0.3;
		double proportion_cadres = 0.3;
		
		//nombre d'employés
		int nb_initial = 320;
		int nb_ouvriers = (int) (nb_initial*proportion_ouvriers);
		int nb_techniciens = (int) (nb_initial*proportion_techniciens);
		int nb_cadres = (int) (nb_initial*proportion_cadres);
		int[] nbInitial = {nb_ouvriers,nb_techniciens,nb_cadres};
		
		//proportion d'emplois publics sur la masse totale d'emplois
		double proportionEmploiPublique = 0.5; // (nb emplois publiques) / (population initiable)
		
		//nombre d'emplois publics
		int nbEmplois1 = (int) (nb_initial*(proportion_ouvriers)*proportionEmploiPublique);//nombre de fonctionnaires ouvriers
		int nbEmplois2 = (int) (nb_initial*(proportion_techniciens)*proportionEmploiPublique);//nombre de fonctionnaires techniciens
		int nbEmplois3 = (int) (nb_initial*(proportion_cadres)*proportionEmploiPublique);//nombre de fonctionnaires cadres
		
		//revenus des employés dans le public par catégorie
		double r_1 = 1200;// salaire un peu plus Ã©levÃ© que la moyenne demandÃ©e initialement 
		double r_2 = 2300;
		double r_3 = 3500;
		//temps libre moyen des fonctionnaires selon leur catégorie
		double e_tl1 = 10;
		double e_tl2 = 10;
		double e_tl3 = 10;
		//écart type du temps libre des fonctionnaires selon leur catégorie
		double e_tl_dev1 = 2;
		double e_tl_dev2 = 2;
		double e_tl_dev3 = 2;
		
		//paramètres x, y, z
		int x = 3;
		int y = 3;
		double z = 0.02;
		
		//exigences des salariés
		double tl_mean = 8;	// temps libre attendu assez faible -> situation stable
		double tl_std_dev = 0;
		double[] rm_mean = new double[]{1000, 2000, 3000};//moyenne des salaires attendus
		double[] rm_std_dev = new double[]{50, 100, 200};//écart type des salaires attendus
		
		//arrivants chaque année
		int nb_arrivants_moyen = 8;//nombre d'arrivants moyen chaque année
		double nb_arrivants_std_dev = 0;//écart-type du nombre d'arrivants chaque année
		
		// ParamÃ¨tres "entreprises"
		int nbEntreprises = 3;
		double a = 1.2;
		double b = 0.8;
		double c = 1;
		double d = 0.7;
		int m = 5;
		//productivité de chaque catégorie en entreprise
		double prod1 = 1100;
		double prod2 = 2200;
		double prod3 = 3300;
		//écart-type de la demande
		double dmd_dev = 100;
		//proportion de cdd au départ
		double rapport_cdd_init = 0.3;
		//seuils
		int seuil_ouvriers = 10;//nombre d'ouvriers pour avoir au moins 1 technicien
		int seuil_techniciens = 10;//nombre de techniciens pour avoir au moins 1 cadre
		double alpha = (prod1+prod2+prod3)/30.0;
		
		//pour stocker des résultats
		PrintWriter sortie = new PrintWriter("jobsim_rm_z" + z + ".txt");
		
		int seed = 10; 
		Random random = new Random(seed);
		
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		Profile pMain = new ProfileImpl("localhost",8888,null);
		// Les 2 lignes suivantes nous ont posÃ© beaucoup de problÃ¨me... ------------
		// La taille des search du DF Ã©tant limitÃ© Ã  100 sinon
		String property_dx_maxresult = "10000";
		pMain.setParameter("jade_domain_df_maxresult", property_dx_maxresult); 
		// -------------------------------------------------------------------------
		AgentContainer mc = rt.createMainContainer(pMain);

		// Classe qui s'occupe des statistiques sur les individus
		AgentController statistiques = mc.createNewAgent("statistiques", "Statistiques", new Object[]{sortie});
		statistiques.start();
		
		AgentController poleEmploi = mc.createNewAgent("poleEmploi", "PoleEmploi", new Object[0]);
		poleEmploi.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} 
		
		Object[] paramEtat = new Object[]{nbEmplois1 ,nbEmplois2, nbEmplois3,r_1,r_2,r_3,e_tl1,e_tl2,e_tl3,e_tl_dev1,e_tl_dev2,e_tl_dev3};
		// Ne pas changer le nom de l'Etat "etat". Car le module de statistiques utilise cela.
		AgentController etat = mc.createNewAgent("etat", "Etat", paramEtat);
		etat.start();
		
		for(int i=0; i<nbEntreprises; i++){
			Object[] paramE = new Object[]{10,10,10,r_1,r_2,r_3,e_tl1,e_tl2,e_tl3,e_tl_dev1,e_tl_dev2,e_tl_dev3,alpha,dmd_dev,rapport_cdd_init,
					seuil_ouvriers,seuil_techniciens,prod1,prod2,prod3,a,b,c,d,m};
			AgentController e = mc.createNewAgent("entreprise "+i, "Entreprise", paramE);
			e.start();
		}
		
		int compteur = 0;
		for(int degreQualif=0; degreQualif<nbInitial.length; degreQualif++){
			for(int i=0; i<nbInitial[degreQualif]; i++){
				Individu.Qualification qualif = Individu.Qualification.values()[degreQualif];
				double rm = random.nextGaussian()*rm_std_dev[degreQualif]+rm_mean[degreQualif];
				double tl = random.nextGaussian()*tl_std_dev+tl_mean;
				Object[] paramIndividu = new Object[]{qualif, rm, tl, x, y, z, Math.random()*43.0};
				AgentController individu = mc.createNewAgent("individu "+compteur, "Individu", paramIndividu);
				compteur++;
				individu.start();
			}
		}
		
		Object[] paramGenerateur = {nb_arrivants_moyen, nb_arrivants_std_dev, nb_initial,
									proportion_ouvriers, proportion_techniciens, proportion_cadres,
									rm_mean, rm_std_dev, tl_mean, tl_std_dev,
									x, y, z, mc};
		AgentController generateur = mc.createNewAgent("generateurIndividu", "GenerateurIndividu", paramGenerateur);
		generateur.start();
		
		Object[] paramHorloge = new Object[]{pasHorloge};
		AgentController horloge = mc.createNewAgent("horloge", "Horloge", paramHorloge);
		horloge.start();
	}

}
