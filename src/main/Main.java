package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
	private final static String CORPUS_BRUT = "ressources/corpus.txt";
	private final static String CORPUS_NETTOYE = "ressources/corpus_clean.txt";
	private final static int TAILLE_LIMITE_DE_RECHERCHE = 30;
	private static final boolean DETERMINISTE = false;
	private static final Random RANDOM = DETERMINISTE ? null : new Random();
	
	private static final String debut = "C'est à l'orée du bois que";
	private static final int metre = 8;
	private static final int tailleStrophe = 4;
	private static final int combienDeStrophes = 3;
	private static final int nombreDeVers = combienDeStrophes*tailleStrophe;
	private static final List<String> motsInterditsEnFinDeVers = motsInterditsEnFinDeVers();
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		// formater le corpus pour le rendre exploitable
		nettoyerCorpus();
		
		List<String> poeme = new ArrayList<>();
		String versActuel = debut;
		int piedsActuel = Vers.compterPieds(versActuel);
		while (poeme.size() < nombreDeVers) {
			
			// on remplit le vers
			boolean impossible = false;
			while (piedsActuel < metre && !impossible) {
				String quoiChercher = "";
				for(String v : poeme) {
					quoiChercher += v + " ";
				}
				quoiChercher += versActuel;
				if (quoiChercher.length() > TAILLE_LIMITE_DE_RECHERCHE) {
					quoiChercher = quoiChercher.substring(quoiChercher.length() - TAILLE_LIMITE_DE_RECHERCHE);
				}
				if (quoiChercher.contains(" ")) {
					quoiChercher = quoiChercher.split(" ", 2)[1];
				}
				
				List<String> possibilites = chercher(quoiChercher);
				//possibilites.forEach(System.out::println);
				
				// retirer les possibilités qui feraient déborder le vers
				int nombreDePossiblilites = possibilites.size();
				for (int i = 0; i<nombreDePossiblilites; i++) {
					String p = possibilites.get(i);
					int piedsPossibilite = Vers.compterPieds(p);
					if (piedsActuel + piedsPossibilite > metre) {
						// trop long ! cette possiblité ferait déborder le vers
						possibilites.remove(i);
						nombreDePossiblilites--;
						i--;
					}
				}
				
				// retirer les mots qui ne peuvent pas clore un vers proprement
				for (int i = 0; i<nombreDePossiblilites; i++) {
					String p = possibilites.get(i);
					int piedsPossibilite = Vers.compterPieds(p);
					if (piedsActuel + piedsPossibilite == metre) {
						// ce mot va clore le vers...
						if (motsInterditsEnFinDeVers.contains(p)) {
							possibilites.remove(i);
							nombreDePossiblilites--;
							i--;
						}
					}
				}
				
				// choisir une des possibilités
				if (!possibilites.isEmpty()) {
					int numeroPossibilite = random(possibilites.size());
					String choisie = possibilites.get(numeroPossibilite);
					versActuel += " "+choisie;
					piedsActuel += Vers.compterPieds(choisie);
				} else {
					System.err.println("Corpus trop petit pour compléter le vers ! '"+versActuel+"'");
					impossible = true;
				}
				//System.out.println(versActuel);
			}
			// le vers est rempli
			
			poeme.add(versActuel);
			versActuel = "";
			piedsActuel = 0;
		}
		
		System.out.println("-----------------------------");
		for(int i=0; i<poeme.size(); i++) {
			String vers = poeme.get(i);
			System.out.println(vers);
			if (i % tailleStrophe == tailleStrophe-1) {
				System.out.println();
			}
		}
	}
	
	private final static void nettoyerCorpus() throws IOException{
		// supprimer ancien corpus
		File corpusNettoye = new File(CORPUS_NETTOYE);
		if (corpusNettoye.exists()) {
			corpusNettoye.delete();
		}
		
		// écrire dans le fichier du corpus nettoyé
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(CORPUS_NETTOYE, true), "UTF-8"));
		String ligne;
		BufferedReader br = new BufferedReader(
				   new InputStreamReader(
		                      new FileInputStream(CORPUS_BRUT), "UTF8"));
	    while ((ligne = br.readLine()) != null) {
	    	// bas de casse
	    	ligne = ligne.toLowerCase()
	    			// pas de caractère spéciaux
	    			.replaceAll("['’-]", "_")
	    			.replaceAll("[0-9\",:;!\\?\\.\\]\\[\\)\\(—]", " ");
	    	// retirer les espaces inutiles
	    	while (ligne.contains("  ")) {
	    		ligne = ligne.replaceAll("  ", " ");
	    	}
	    	ligne = ligne.trim();
	    	if (!"".equals(ligne)) { // ne pas mettre les lignes vides
	    		writer.append(ligne+"\n");
	    	}
	    }
	    br.close();
		writer.close();
	}
	
	private final static List<String> chercher(String expression) throws FileNotFoundException, IOException {
		List<String> possibilites = new ArrayList<>();
		if ("".equals(expression)) {
			System.err.println("Expression vide à rechercher !");
			possibilites.add("et");
			return possibilites;
		}
		
		String[] mots = expression.split(" ");
		
		int nombreDeMotsAChercher = 4;
		String recherche = "";
		while (nombreDeMotsAChercher > 0) {
		
			// on essaye de trouver plusieurs mots
			recherche = "";
			for (int i=0; i<nombreDeMotsAChercher; i++) {
				if (mots.length-1 >= i) {
					recherche = mots[mots.length-1-i] + " " + recherche;
				}
			}
			recherche = recherche.trim();
			recherche = " "+recherche+" ";
			//System.out.println("On cherche '"+recherche+"' dans le corpus...");
			
			String lignePrecedente = "";
			String ligne;
			BufferedReader br = new BufferedReader(
					   new InputStreamReader(
			                      new FileInputStream(CORPUS_NETTOYE), "UTF8"));
		    while ((ligne = br.readLine()) != null) {
		    	String ligneTotale = " " + lignePrecedente + " " + ligne + " ";
		    	if (ligneTotale.contains(recherche)){
		    		int pos = ligneTotale.indexOf(recherche) + recherche.length();
		    		String motSuivant = ligneTotale.substring(pos).trim();
		    		motSuivant = motSuivant.split(" ")[0];
		    		if (!"".equals(motSuivant)) {
		    			possibilites.add(motSuivant);
		    		}
		    	}
		    	lignePrecedente = ligne;
		    }
		    br.close();
		    
		    nombreDeMotsAChercher--;
		}
		
		if (possibilites.isEmpty()) {
			System.err.println("Impossible de trouver '"+recherche+"' dans le corpus !");
		}
		return possibilites;
	}
	
	private static final int random(int max){
		if(DETERMINISTE){
			return (int) (1234567891239941l % max);
		}else{
			return RANDOM.nextInt(max);
		}
	}
	
	private static final List<String> motsInterditsEnFinDeVers() {
		List<String> motsInterdits = new ArrayList<>();
		motsInterdits.add("a");
		motsInterdits.add("à");
		motsInterdits.add("au");
		motsInterdits.add("aux");
		motsInterdits.add("le");
		motsInterdits.add("la");
		motsInterdits.add("les");
		motsInterdits.add("en");
		motsInterdits.add("par");
		motsInterdits.add("dans");
		motsInterdits.add("pour");
		motsInterdits.add("que");
		motsInterdits.add("puisque");
		motsInterdits.add("parce");
		motsInterdits.add("ce");
		motsInterdits.add("ces");
		motsInterdits.add("cette");
		motsInterdits.add("cet");
		motsInterdits.add("un");
		motsInterdits.add("des");
		motsInterdits.add("une");
		motsInterdits.add("nos");
		motsInterdits.add("vos");
		motsInterdits.add("notre");
		motsInterdits.add("votre");
		motsInterdits.add("leur");
		motsInterdits.add("leurs");
		motsInterdits.add("ma");
		motsInterdits.add("mon");
		motsInterdits.add("mes");
		motsInterdits.add("ta");
		motsInterdits.add("ton");
		motsInterdits.add("tes");
		motsInterdits.add("son");
		motsInterdits.add("sa");
		motsInterdits.add("ses");
		motsInterdits.add("et");
		motsInterdits.add("mais");
		motsInterdits.add("sur");
		motsInterdits.add("sous");
		motsInterdits.add("car");
		motsInterdits.add("tu");
		motsInterdits.add("je");
		motsInterdits.add("il");
		motsInterdits.add("on");
		motsInterdits.add("l_on");
		motsInterdits.add("ils");
		motsInterdits.add("nous");
		motsInterdits.add("vous");
		motsInterdits.add("est");
		motsInterdits.add("es");
		motsInterdits.add("si");
		motsInterdits.add("ni");
		motsInterdits.add("dont");
		motsInterdits.add("qui");
		motsInterdits.add("qu_on");
		motsInterdits.add("qu_un");
		motsInterdits.add("qu_en");
		motsInterdits.add("qu_à");
		motsInterdits.add("qu_il");
		motsInterdits.add("qu_elle");
		motsInterdits.add("quelle");
		motsInterdits.add("quel");
		motsInterdits.add("quels");
		motsInterdits.add("qu");
		motsInterdits.add("où");
		motsInterdits.add("ou");
		motsInterdits.add("tout");
		motsInterdits.add("quelque");
		motsInterdits.add("de");
		motsInterdits.add("du");
		motsInterdits.add("ô");
		motsInterdits.add("j_ai");
		motsInterdits.add("j_en");
		motsInterdits.add("ai");
		motsInterdits.add("n_a");
		motsInterdits.add("n_ai");
		motsInterdits.add("ont");
		motsInterdits.add("n_en");
		motsInterdits.add("comme");
		motsInterdits.add("quant");
		motsInterdits.add("entre");
		return motsInterdits;
	}
	
}
