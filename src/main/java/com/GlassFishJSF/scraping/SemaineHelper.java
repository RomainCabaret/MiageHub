package com.GlassFishJSF.scraping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SemaineHelper {

    private static final LocalDate PREMIERE_SEMAINE = LocalDate.of(2025, 9, 1); // 1er sept 2025
    private static final String SWAP_WEEK_ID = "GInterface.Instances[1].Instances[3]_j_";

    /**
     * Renvoie l'ID du bouton semaine à partir d'une date donnée.
     * Exemple : si date = 8 sept 2024 => retourne "GInterface.Instances[1].Instances[3]_j_2"
     */
    public static String getIdSemaineFromDate(LocalDate date) {
        long semaineIndex = ChronoUnit.WEEKS.between(PREMIERE_SEMAINE, date) + 1;
        if (semaineIndex < 1 || semaineIndex > 52) {
            throw new IllegalArgumentException("La date n'est pas dans la plage de l'année scolaire");
        }
        return SWAP_WEEK_ID + semaineIndex;
    }
}
