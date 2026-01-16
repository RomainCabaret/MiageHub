package com.GlassFishJSF.utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class FeriesUtils {

    // Calcul de Pâques (algorithme de Meeus)
    private static LocalDate calculerPaques(int annee) {
        int a = annee % 19;
        int b = annee / 100;
        int c = annee % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mois = (h + l - 7 * m + 114) / 31;
        int jour = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(annee, mois, jour);
    }

    public static Map<LocalDate, String> getJoursFeries(int annee) {
        Map<LocalDate, String> feries = new HashMap<>();

        // Jours fériés fixes
        feries.put(LocalDate.of(annee, Month.JANUARY, 1), "Jour de l'an");
        feries.put(LocalDate.of(annee, Month.MAY, 1), "Fête du Travail");
        feries.put(LocalDate.of(annee, Month.MAY, 8), "Victoire 1945");
        feries.put(LocalDate.of(annee, Month.JULY, 14), "Fête Nationale");
        feries.put(LocalDate.of(annee, Month.AUGUST, 15), "Assomption");
        feries.put(LocalDate.of(annee, Month.NOVEMBER, 1), "Toussaint");
        feries.put(LocalDate.of(annee, Month.NOVEMBER, 11), "Armistice 1918");
        feries.put(LocalDate.of(annee, Month.DECEMBER, 25), "Noël");

        // Jours fériés mobiles (basés sur Pâques)
        LocalDate paques = calculerPaques(annee);
        feries.put(paques.plusDays(1), "Lundi de Pâques");
        feries.put(paques.plusDays(39), "Ascension");
        feries.put(paques.plusDays(50), "Lundi de Pentecôte");

        return feries;
    }

    public static boolean estJourFerie(LocalDate date) {
        Map<LocalDate, String> feries = getJoursFeries(date.getYear());
        return feries.containsKey(date);
    }

    public static String getNomJourFerie(LocalDate date) {
        Map<LocalDate, String> feries = getJoursFeries(date.getYear());
        return feries.get(date);
    }
}