package com.GlassFishJSF.beans;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
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

    private static final DateTimeFormatter FORMAT_FR =
            DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH);

    @PostConstruct
    public void init() {
        allCours = coursDAO.findAll();
        semaineCourante = LocalDate.now().with(DayOfWeek.MONDAY);
    }

    public void semainePrecedente() {
        semaineCourante = semaineCourante.minusWeeks(1);
    }

    public void semaineSuivante() {
        semaineCourante = semaineCourante.plusWeeks(1);
    }

    public String getPeriodeSemaine() {
        LocalDate fin = semaineCourante.plusDays(4);
        return semaineCourante.format(FORMAT_FR) + " – " + fin.format(FORMAT_FR);
    }

    /** Cours groupés par jour pour la semaine affichée */
    public Map<DayOfWeek, List<Cours>> getCoursParJour() {
        LocalDate lundi = semaineCourante;
        LocalDate vendredi = lundi.plusDays(4);

        return allCours.stream()
                .filter(c -> {
                    LocalDate dateCours = c.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return !dateCours.isBefore(lundi) && !dateCours.isAfter(vendredi);
                })
                .sorted(Comparator.comparing(Cours::getDate).thenComparing(Cours::getTimestampDebut))
                .collect(Collectors.groupingBy(
                        c -> c.getDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
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

    public String getJourFrancais(java.time.DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Lundi";
            case TUESDAY: return "Mardi";
            case WEDNESDAY: return "Mercredi";
            case THURSDAY: return "Jeudi";
            case FRIDAY: return "Vendredi";
            case SATURDAY: return "Samedi";
            case SUNDAY: return "Dimanche";
            default: return day.toString();
        }
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
            Map.entry("Projet Personnel d'Etudes et d'Insertion", "event-projet-personnel"),
            Map.entry("Systèmes d'exploitation", "event-systemes-exploitation"),
            Map.entry("Expression écrite orale", "event-expression"),
            Map.entry("Management des systèmes d'information", "event-management"),
            Map.entry("Technologies objet avancées", "event-objet-avancees"),
            Map.entry("Système comptable", "event-comptable"),
            Map.entry("Projet", "event-projet"),
            Map.entry("Activité", "event-activite"),
            Map.entry("PRESENCE UNIVERSITAIRE", "event-presence"),
            Map.entry("Modélisation objet", "event-modelisation"),
            Map.entry("Technologie Javascript", "event-javascript")
    );

    public String getCssClassForCours(String matiere, String cours) {
        return COURS_COLORS.getOrDefault(matiere, "event-default") + (cours.equals("EXAMEN") ? " schedule__event--exam" : "");
    }

    public String redirectionIntoDriver() {
        return "drive?faces-redirect=true";
    }

}

