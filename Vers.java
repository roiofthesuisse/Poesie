package main;

import static org.junit.Assert.assertEquals;

public class Vers {
    private static final String VOYELLES = "aeiuoàéèêëôöîïâäùüû";

    public static void main(String[] args) {
        assertEquals(2, compterPieds("Le sol"));
        assertEquals(3, compterPieds("La patate"));
        assertEquals(8, compterPieds("La patate est tombée par terre"));
        assertEquals(8, compterPieds("La patate est tombée au sol"));
        assertEquals(9, compterPieds("C'est la patate de Charlemagne"));
        assertEquals(9, compterPieds("Charlemagne fut un roi sans peur"));
        assertEquals(11, compterPieds("Patate, patate ! Quel est ton secret ?"));
        assertEquals(5, compterPieds("Les patates chaudes"));
    }

    private static int compterPieds(String vers) {
        // nettoyer le vers (ponctuation, majuscules...)
        vers = nettoyerLeVers(vers);

        int pieds = 0;
        String[] mots = vers.split(" ");
        boolean motPrecedentFinitParE = false;
        for (String mot : mots) {
            // 'e' final du mot précedent
            pieds = nePasCompterLeEFinalDuMotPrecedent(pieds, motPrecedentFinitParE, mot);
            // pieds du mot
            boolean estVoyelle = false;
            for (int i = 0; i < mot.length(); i++) {
                char c = mot.charAt(i);
                if (estVoyelle(c)) {
                    // voyelle
                    if (!estVoyelle) {
                        pieds++;
                    }
                    estVoyelle = true;
                } else {
                    //consonne
                    estVoyelle = false;
                }
            }
            // quelle est la dernière lettre de ce mot ?
            char derniereLettreDuMot = mot.charAt(mot.length() - 1);
            if (derniereLettreDuMot == 'e') {
                char avantDerniereLettreDuMot = mot.charAt(mot.length() - 2);
                motPrecedentFinitParE = !estVoyelle(avantDerniereLettreDuMot);
            } else {
                motPrecedentFinitParE = false;
            }
        }
        // 'e' final du vers
        pieds = nePasCompterLeEFinalDuMotPrecedent(pieds, motPrecedentFinitParE, "a");

        System.out.println(pieds + "\t" + vers);
        return pieds;
    }

    private static String nettoyerLeVers(String vers) {
        vers = vers.toLowerCase();
        vers = vers.replace("'", "").replace("!", " ").replace(")", " ").replace("(", " ").replace("?", " ").replace(
                ".", " ").replace(";", " ").replace(",", " ").replace("-", " ").replace("~", " ");
        while (vers.contains("  ")) {
            vers = vers.replace("  ", " ");
        }
        vers = vers.trim();
        if (vers.endsWith("es")) {
            vers = vers.substring(0, vers.length() - 1);
        }
        return vers;
    }

    private static boolean estVoyelle(char c) {
        return VOYELLES.indexOf(c) >= 0;
    }

    private static final int nePasCompterLeEFinalDuMotPrecedent(int pieds, boolean motPrecedentFinitParE,
            String motActuel) {
        char premiereLettre = motActuel.charAt(0);
        if (motPrecedentFinitParE && estVoyelle(premiereLettre)) {
            motPrecedentFinitParE = false;
            return pieds - 1;
        }
        return pieds;
    }
}
