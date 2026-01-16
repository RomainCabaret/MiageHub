package com.GlassFishJSF.beans;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named
@RequestScoped
public class ProgressBean implements Serializable {

    @Inject
    private CoursDAO coursDAO;

    private LocalDateTime now;
    private List<Cours> coursAujourdhui;

    @PostConstruct
    public void init() {
        now = LocalDateTime.now();
        loadCoursAujourdhui();
    }

    private void loadCoursAujourdhui() {
        LocalDate today = LocalDate.now();
        List<Cours> allCours = coursDAO.findAll();

        coursAujourdhui = allCours.stream()
                .filter(c -> {
                    LocalDate dateCours = c.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return dateCours.equals(today);
                })
                .sorted(Comparator.comparing(Cours::getTimestampDebut))
                .collect(Collectors.toList());
    }

    // ===== GETTERS ESSENTIELS =====
    public LocalDateTime getNow() {
        return now;
    }

    public List<Cours> getCoursAujourdhui() {
        return coursAujourdhui;
    }

    // ===== Cours actuel =====
    public Cours getCoursActuel() {
        for (Cours cours : coursAujourdhui) {
            LocalDateTime debut = cours.getTimestampDebut().toLocalDateTime();
            LocalDateTime fin = cours.getTimestampFin().toLocalDateTime();
            if (now.isAfter(debut) && now.isBefore(fin)) {
                return cours;
            }
        }
        return null;
    }

    public int getProgressCoursActuel() {
        Cours cours = getCoursActuel();
        if (cours == null) return 0;

        LocalDateTime debut = cours.getTimestampDebut().toLocalDateTime();
        LocalDateTime fin = cours.getTimestampFin().toLocalDateTime();

        long total = java.time.Duration.between(debut, fin).toMinutes();
        long elapsed = java.time.Duration.between(debut, now).toMinutes();

        return (int) Math.min(100, (elapsed * 100) / total);
    }

    public String getTempsRestantCoursActuel() {
        Cours cours = getCoursActuel();
        if (cours == null) return "Pas de cours en ce moment";

        LocalDateTime fin = cours.getTimestampFin().toLocalDateTime();
        long minutes = java.time.Duration.between(now, fin).toMinutes();

        if (minutes > 60) {
            long heures = minutes / 60;
            long mins = minutes % 60;
            return heures + "h" + (mins > 0 ? mins + "min" : "");
        }
        return minutes + " min";
    }

    // ===== Pause =====
    public boolean isEnPause() {
        LocalTime nowTime = now.toLocalTime();

        // Chercher s'il y a une pause entre deux cours
        for (int i = 0; i < coursAujourdhui.size() - 1; i++) {
            LocalTime finCours = coursAujourdhui.get(i).getTimestampFin().toLocalDateTime().toLocalTime();
            LocalTime debutProchain = coursAujourdhui.get(i + 1).getTimestampDebut().toLocalDateTime().toLocalTime();

            if (nowTime.isAfter(finCours) && nowTime.isBefore(debutProchain)) {
                return true;
            }
        }
        return false;
    }

    public int getProgressPause() {
        if (!isEnPause()) return 0;

        LocalTime nowTime = now.toLocalTime();

        for (int i = 0; i < coursAujourdhui.size() - 1; i++) {
            LocalTime finCours = coursAujourdhui.get(i).getTimestampFin().toLocalDateTime().toLocalTime();
            LocalTime debutProchain = coursAujourdhui.get(i + 1).getTimestampDebut().toLocalDateTime().toLocalTime();

            if (nowTime.isAfter(finCours) && nowTime.isBefore(debutProchain)) {
                long total = java.time.Duration.between(finCours, debutProchain).toMinutes();
                long elapsed = java.time.Duration.between(finCours, nowTime).toMinutes();
                return (int) Math.min(100, (elapsed * 100) / total);
            }
        }
        return 0;
    }

    public String getTempsRestantPause() {
        if (!isEnPause()) return "Pas en pause";

        LocalTime nowTime = now.toLocalTime();

        for (int i = 0; i < coursAujourdhui.size() - 1; i++) {
            LocalTime finCours = coursAujourdhui.get(i).getTimestampFin().toLocalDateTime().toLocalTime();
            LocalTime debutProchain = coursAujourdhui.get(i + 1).getTimestampDebut().toLocalDateTime().toLocalTime();

            if (nowTime.isAfter(finCours) && nowTime.isBefore(debutProchain)) {
                long minutes = java.time.Duration.between(nowTime, debutProchain).toMinutes();
                return minutes + " min";
            }
        }
        return "Pas en pause";
    }

    // ===== Fin de journée =====
    public int getProgressJournee() {
        if (coursAujourdhui.isEmpty()) return 100;

        LocalDateTime premierCours = coursAujourdhui.get(0).getTimestampDebut().toLocalDateTime();
        LocalDateTime dernierCours = coursAujourdhui.get(coursAujourdhui.size() - 1).getTimestampFin().toLocalDateTime();

        if (now.isBefore(premierCours)) return 0;
        if (now.isAfter(dernierCours)) return 100;

        long total = java.time.Duration.between(premierCours, dernierCours).toMinutes();
        long elapsed = java.time.Duration.between(premierCours, now).toMinutes();

        return (int) Math.min(100, (elapsed * 100) / total);
    }

    public String getTempsRestantJournee() {
        if (coursAujourdhui.isEmpty()) return "Pas de cours aujourd'hui";

        LocalDateTime dernierCours = coursAujourdhui.get(coursAujourdhui.size() - 1).getTimestampFin().toLocalDateTime();

        if (now.isAfter(dernierCours)) {
            return "Journée terminée ! 🎉";
        }

        long minutes = java.time.Duration.between(now, dernierCours).toMinutes();

        if (minutes > 60) {
            long heures = minutes / 60;
            long mins = minutes % 60;
            return heures + "h" + (mins > 0 ? mins + "min" : "");
        }
        return minutes + " min";
    }

    // ===== Bloc de cours =====
    public List<Cours> getBlocActuel() {
        if (coursAujourdhui.isEmpty()) return new ArrayList<>();

        Cours coursActuel = getCoursActuel();
        if (coursActuel == null) return new ArrayList<>();

        List<Cours> bloc = new ArrayList<>();
        String matiere = coursActuel.getMatiere();

        // Trouver tous les cours consécutifs de la même matière
        int indexActuel = coursAujourdhui.indexOf(coursActuel);

        // Remonter
        for (int i = indexActuel; i >= 0; i--) {
            if (!coursAujourdhui.get(i).getMatiere().equals(matiere)) break;
            if (i < indexActuel) {
                // Vérifier qu'il y a moins de 30 min d'écart
                long pauseMinutes = java.time.Duration.between(
                        coursAujourdhui.get(i).getTimestampFin().toLocalDateTime(),
                        coursAujourdhui.get(i + 1).getTimestampDebut().toLocalDateTime()
                ).toMinutes();
                if (pauseMinutes > 30) break;
            }
            bloc.add(0, coursAujourdhui.get(i));
        }

        // Descendre
        for (int i = indexActuel + 1; i < coursAujourdhui.size(); i++) {
            if (!coursAujourdhui.get(i).getMatiere().equals(matiere)) break;
            // Vérifier qu'il y a moins de 30 min d'écart
            long pauseMinutes = java.time.Duration.between(
                    coursAujourdhui.get(i - 1).getTimestampFin().toLocalDateTime(),
                    coursAujourdhui.get(i).getTimestampDebut().toLocalDateTime()
            ).toMinutes();
            if (pauseMinutes > 30) break;
            bloc.add(coursAujourdhui.get(i));
        }

        return bloc;
    }

    public int getProgressBloc() {
        List<Cours> bloc = getBlocActuel();
        if (bloc.isEmpty()) return 0;

        LocalDateTime debut = bloc.get(0).getTimestampDebut().toLocalDateTime();
        LocalDateTime fin = bloc.get(bloc.size() - 1).getTimestampFin().toLocalDateTime();

        long total = java.time.Duration.between(debut, fin).toMinutes();
        long elapsed = java.time.Duration.between(debut, now).toMinutes();

        return (int) Math.min(100, (elapsed * 100) / total);
    }

    public String getTempsRestantBloc() {
        List<Cours> bloc = getBlocActuel();
        if (bloc.isEmpty()) return "Pas de bloc en cours";

        LocalDateTime fin = bloc.get(bloc.size() - 1).getTimestampFin().toLocalDateTime();
        long minutes = java.time.Duration.between(now, fin).toMinutes();

        if (minutes > 60) {
            long heures = minutes / 60;
            long mins = minutes % 60;
            return heures + "h" + (mins > 0 ? mins + "min" : "");
        }
        return minutes + " min";
    }

    public String getNomBloc() {
        List<Cours> bloc = getBlocActuel();
        if (bloc.isEmpty()) return "";
        return bloc.get(0).getMatiere();
    }
}