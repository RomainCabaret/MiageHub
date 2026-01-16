package com.GlassFishJSF.beans;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.utils.DateUtils;
import com.GlassFishJSF.utils.FeriesUtils;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;


import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;
@Named
@SessionScoped
public class CoursBean implements Serializable {

    @Inject
    private CoursDAO coursDAO;

    private List<Cours> allCours;
    private LocalDate semaineCourante;

    // Nouvelles propriétés pour le week picker
    private String weekPickerStart;
    private String weekPickerEnd;

    private static final DateTimeFormatter FORMAT_FR =
            DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH);

    @PostConstruct
    public void init() {
        allCours = coursDAO.findAll();

        LocalDate today = LocalDate.now();
        DayOfWeek day = today.getDayOfWeek();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            semaineCourante = today.with(DayOfWeek.MONDAY).plusWeeks(1);
        } else {
            semaineCourante = today.with(DayOfWeek.MONDAY);
        }
    }

    public void semainePrecedente() {
        semaineCourante = semaineCourante.minusWeeks(1);
    }

    public void semaineSuivante() {
        semaineCourante = semaineCourante.plusWeeks(1);
    }

    /**
     * Nouvelle méthode pour appliquer la sélection du week picker - CORRIGÉE
     */
    public void appliquerSelectionSemaine() {
        System.out.println("semaineCourante : " + semaineCourante);
        System.out.println(weekPickerStart.isEmpty());

        if (weekPickerStart != null && !weekPickerStart.isEmpty()) {
            try {
                LocalDate dateSelectionnee = LocalDate.parse(weekPickerStart);

                if (dateSelectionnee.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    semaineCourante = dateSelectionnee.plusDays(1); // Lundi suivant
                } else {
                    semaineCourante = dateSelectionnee.with(DayOfWeek.MONDAY);
                }

                System.out.println("📅 Date reçue du week picker: " + weekPickerStart);
                System.out.println("📅 Semaine courante définie: " + semaineCourante);
                System.out.println("📅 Jour de la semaine: " + semaineCourante.getDayOfWeek());

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Semaine sélectionnée",
                                "Semaine du " + getPeriodeSemaine()));
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la sélection de semaine: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                                "Impossible de sélectionner cette semaine"));
            }
        } else {
            System.out.println("semaineCourante : IS NULL");
        }
    }

    public String getPeriodeSemaine() {
        LocalDate fin = semaineCourante.plusDays(4);
        return semaineCourante.format(FORMAT_FR) + " – " + fin.format(FORMAT_FR);
    }

    /** Cours groupés par jour pour la semaine affichée */
    public Map<DayOfWeek, List<Cours>> getCoursParJour() {
        List<Cours> allCours = coursDAO.findAll();

        LocalDate lundi = semaineCourante;
        LocalDate vendredi = lundi.plusDays(4);

        Map<Long, Cours> uniqueCours = allCours.stream()
                .filter(c -> {
                    LocalDate dateCours = c.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return !dateCours.isBefore(lundi) && !dateCours.isAfter(vendredi);
                })
                .collect(Collectors.toMap(
                        Cours::getId,      // clé unique = id
                        c -> c,
                        (existing, replacement) -> existing // en cas de doublon, garder le premier
                ));

        // Créer la map avec les cours groupés par jour
        Map<DayOfWeek, List<Cours>> coursParJourTemp = uniqueCours.values().stream()
                .sorted(Comparator.comparing(Cours::getDate).thenComparing(Cours::getTimestampDebut))
                .collect(Collectors.groupingBy(
                        c -> c.getDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Créer une nouvelle map dans l'ordre correct (Lundi -> Vendredi)
        Map<DayOfWeek, List<Cours>> coursParJour = new LinkedHashMap<>();

        for (DayOfWeek day : new DayOfWeek[]{
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        }) {
            // Ajouter le jour s'il a des cours OU s'il est férié
            if (coursParJourTemp.containsKey(day)) {
                coursParJour.put(day, coursParJourTemp.get(day));
            } else if (estJourFerie(day)) {
                coursParJour.put(day, new ArrayList<>());
            }
        }

        return coursParJour;
    }


    /** Liste plate pour la version desktop */
    public List<Cours> getCoursSemaine() {
        return getCoursParJour().values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public LocalDate getSemaineCourante() {
        return semaineCourante;
    }

    public void setSemaineCourante(LocalDate semaineCourante) {
        this.semaineCourante = semaineCourante;
    }

    // Getters et setters pour le week picker
    public String getWeekPickerStart() {
        return weekPickerStart;
    }

    public void setWeekPickerStart(String weekPickerStart) {
        this.weekPickerStart = weekPickerStart;
    }

    public String getWeekPickerEnd() {
        return weekPickerEnd;
    }

    public void setWeekPickerEnd(String weekPickerEnd) {
        this.weekPickerEnd = weekPickerEnd;
    }

    public String getJourAvecDate(String dayName) {
        try {
            DayOfWeek day = DayOfWeek.valueOf(dayName);
            return getJourAvecDate(day);
        } catch (IllegalArgumentException e) {
            return dayName;
        }
    }
    public String getJourAvecDate(java.time.DayOfWeek day) {
        LocalDate dateJour = semaineCourante.with(day);

        DateTimeFormatter formatJourDate = DateTimeFormatter.ofPattern("EEE d MMMM", Locale.FRENCH);

        return dateJour.format(formatJourDate);
    }


    public int gridColumn(Date date) {
        return DateUtils.getGridColumn(date);
    }

    public int gridRow(Timestamp start) {
        return DateUtils.getGridRow(start);
    }

    public int rowSpan(Timestamp start, Timestamp end) {
        return DateUtils.getRowSpan(start, end);
    }

    private static final Map<String, String> COURS_COLORS = Map.ofEntries(
            // Matières techniques - Nuances de bleu et violet
            Map.entry("Bases de données avancées", "event-bdd-avancees"),
            Map.entry("Conception orientée objet de logiciel", "event-conception-objet"),
            Map.entry("Méthodes formelles pour le génie logiciel", "event-methodes-formelles"),
            Map.entry("Systèmes et applications répartis", "event-systemes-repartis"),
            Map.entry("Cryptographie et sécurité", "event-cryptographie"),
            Map.entry("Technologies logicielles", "event-tech-logicielles"),
            Map.entry("Ingénierie des composants", "event-ingenierie-composants"),
            Map.entry("Implémentation du projet", "event-implementation"),

            // Matières de gestion et droit - Nuances de vert et bleu-vert
            Map.entry("Gestion financière", "event-gestion-financiere"),
            Map.entry("Droit numérique", "event-droit-numerique"),
            Map.entry("Simulation de Gestion d'Entreprise", "event-simulation-gestion"),

            // Projets et recherche - Nuances d'orange et rouge-orange
            Map.entry("TER-Projets", "event-ter-projets"),
            Map.entry("Recherche opérationnelle", "event-recherche-operationnelle"),
            Map.entry("Analyse de données", "event-analyse-donnees"),

            // Matières scientifiques - Nuances de violet et magenta
            Map.entry("Statistiques", "event-statistiques"),

            // Langues et communication - Nuances de teal et cyan
            Map.entry("Anglais 1", "event-anglais-1"),
            Map.entry("Anglais 1 bis", "event-anglais-1-bis"),

            // Administratif - Couleurs neutres mais distinctes
            Map.entry("PRESENCE UNIVERSITAIRE", "event-presence"),
            Map.entry("RENTREE", "event-rentree")
    );

    public String getCssClassForCours(String matiere, String cours) {
        return COURS_COLORS.getOrDefault(matiere, "event-default") +
                (cours.equals("EXAMEN") ? " schedule__event--exam" : "");
    }

    public boolean isTodayOrOtherWeek(DayOfWeek day) {
        LocalDate today = LocalDate.now();
        LocalDate mondayThisWeek = today.with(DayOfWeek.MONDAY);

        if (semaineCourante == null) {
            return true;
        }

        if (semaineCourante.equals(mondayThisWeek)) {
            return day.equals(today.getDayOfWeek());
        }

        return true;
    }

    public boolean estJourFerie(DayOfWeek day) {
        LocalDate dateJour = semaineCourante.with(day);
        return FeriesUtils.estJourFerie(dateJour);
    }

    public String getNomJourFerie(DayOfWeek day) {
        LocalDate dateJour = semaineCourante.with(day);
        return FeriesUtils.getNomJourFerie(dateJour);
    }

    public String redirectionIntoDriver() {
        return "drive?faces-redirect=true";
    }


}
