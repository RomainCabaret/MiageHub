package com.GlassFishJSF.service;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.dto.IziaTaskDTO;
import com.GlassFishJSF.dto.ProgressDTO;
import com.GlassFishJSF.model.Cours;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

@ApplicationScoped
public class ProgressService {

    @Inject
    private CoursDAO coursDAO;

    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");
    private static final ZoneId KATHMANDU_ZONE = ZoneId.of("Asia/Kathmandu");

    // Méthode de base : on récupère toujours l'instant présent à Paris
    private ZonedDateTime nowParis() {
        return ZonedDateTime.now(PARIS_ZONE);
    }

    private long toMs(ZonedDateTime zdt) {
        return zdt.toInstant().toEpochMilli();
    }

    // ------------------- TIME BAR -------------------

    public ProgressDTO calculateMinuteProgress() {
        ZonedDateTime now = nowParis();
        ZonedDateTime start = now.withSecond(0).withNano(0);
        ZonedDateTime end = start.plusMinutes(1);

        return new ProgressDTO(toMs(start), toMs(end), "Minute", true);
    }

    public ProgressDTO calculateHourProgress() {
        ZonedDateTime now = nowParis();
        // Correction : on part de nowParis() directement
        ZonedDateTime start = now.withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime end = start.plusHours(1);

        return new ProgressDTO(toMs(start), toMs(end), "Heure", true);
    }

    public ProgressDTO calculateMonthProgress() {
        ZonedDateTime now = nowParis();
        // Correction : on utilise start.plusMonths(1) pour la fin
        ZonedDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime end = start.plusMonths(1);

        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

        return new ProgressDTO(toMs(start), toMs(end), "Mois " + monthName, true);
    }

    public ProgressDTO calculateYearProgress() {
        ZonedDateTime now = nowParis();
        ZonedDateTime start = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime end = start.plusYears(1);

        boolean isActive = now.isAfter(start) && now.isBefore(end);

        return new ProgressDTO(toMs(start), toMs(end), "Année " + now.getYear(), isActive);
    }

    public ProgressDTO calculateCenturyProgress() {
        ZonedDateTime now = nowParis();
        ZonedDateTime startCentury = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, PARIS_ZONE);
        ZonedDateTime endCentury = ZonedDateTime.of(2101, 1, 1, 0, 0, 0, 0, PARIS_ZONE);

        boolean isActive = now.isAfter(startCentury) && now.isBefore(endCentury);

        return new ProgressDTO(toMs(startCentury), toMs(endCentury), "21ème Siècle", isActive);
    }

    // ------------------- EXAMEN BAR -------------------


    public ProgressDTO calculateExamInterval() {
        Map<String, Cours> exams = coursDAO.findLastAndNextExam();
        if (!exams.containsKey("next")) return new ProgressDTO(0, 0, "", false);

        Cours next = exams.get("next");
        long end = next.getTimestampDebut().getTime();

        long start = exams.containsKey("last")
                ? exams.get("last").getTimestampDebut().getTime()
                : toMs(nowParis().withHour(0).withMinute(0).withSecond(0).withNano(0));

        return new ProgressDTO(start, end, "Prochain examen: " + next.getMatiere(), true);
    }

    public ProgressDTO calculateGlobalSession() {
        Map<String, Cours> boundaries = coursDAO.findFirstAndLastExam();
        if (!boundaries.containsKey("first") || !boundaries.containsKey("last")) {
            return new ProgressDTO(0, 0, "", false);
        }

        long start = boundaries.get("first").getTimestampFin().getTime();
        long end = boundaries.get("last").getTimestampDebut().getTime();

        return new ProgressDTO(start, end, "Dernier examen: " + boundaries.get("last").getMatiere(), end > start);
    }

    // ------------------- MASTER BAR -------------------


    public ProgressDTO calculateEndingMasterProgress() {
        ZonedDateTime now = nowParis();
        ZonedDateTime startMaster = ZonedDateTime.of(2025, 9, 8, 8, 30, 0, 0, PARIS_ZONE);
        ZonedDateTime endMaster = ZonedDateTime.of(2027, 4, 17, 16, 15, 0, 0, PARIS_ZONE);

        boolean isActive = now.isAfter(startMaster) && now.isBefore(endMaster);

        return new ProgressDTO(toMs(startMaster), toMs(endMaster), "Fin du master", isActive);
    }

    public ProgressDTO calculateNextITNight() {
        ZonedDateTime now = nowParis();
        ZonedDateTime start = ZonedDateTime.of(2025, 12, 5, 8, 30, 0, 0, PARIS_ZONE);
        ZonedDateTime end = ZonedDateTime.of(2026, 12, 3, 16, 15, 0, 0, PARIS_ZONE);

        boolean isActive = now.isAfter(start) && now.isBefore(end);

        return new ProgressDTO(toMs(start), toMs(end), "Prochaine nuit de l'info", isActive);
    }

    public ProgressDTO calculateNextBigEvent() {
        ZonedDateTime now = nowParis();
        ZonedDateTime start = ZonedDateTime.of(2026, 3, 4, 8, 30, 0, 0, PARIS_ZONE);
        ZonedDateTime end = ZonedDateTime.of(2026, 4, 25, 8, 30, 0, 0, PARIS_ZONE);

        boolean isActive = now.isAfter(start) && now.isBefore(end);

        return new ProgressDTO(toMs(start), toMs(end), "Debut des Olympiades", isActive);
    }



    public ProgressDTO calculateNextSolarEclipse() {
        ZonedDateTime now = nowParis();

        ZonedDateTime start = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, KATHMANDU_ZONE);
        ZonedDateTime end = ZonedDateTime.of(2027, 8, 2, 16, 27, 31, 0, KATHMANDU_ZONE);

        boolean isActive = now.isBefore(end);

        return new ProgressDTO(
                toMs(start),
                toMs(end),
                "Prochaine éclipse solaire à Katmandou",
                isActive
        );
    }

    public ProgressDTO calculateNextCompanyProgress() {
        // TODO A REFAIRE
        ZonedDateTime now = nowParis();
        List<Cours> upcoming = coursDAO.findAllUpcomingCourses(now);

        if (upcoming.isEmpty()) return new ProgressDTO(0, 0, "", false);

        if (upcoming.get(0).getTimestampDebut().getTime() > toMs(now.plusDays(3))) {
            return new ProgressDTO(0, 0, "", false);
        }

        ZonedDateTime endOfSchoolBlock = null;
        long cinqJoursEnMs = 5L * 24 * 60 * 60 * 1000;

        for (int i = 0; i < upcoming.size() - 1; i++) {
            long finCoursActuel = upcoming.get(i).getTimestampFin().getTime();
            long debutCoursSuivant = upcoming.get(i + 1).getTimestampDebut().getTime();

            if (debutCoursSuivant - finCoursActuel > cinqJoursEnMs) {
                endOfSchoolBlock = ZonedDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(finCoursActuel), PARIS_ZONE);
                break;
            }
        }

        if (endOfSchoolBlock == null) {
            endOfSchoolBlock = ZonedDateTime.ofInstant(
                    upcoming.get(upcoming.size()-1).getTimestampFin().toInstant(), PARIS_ZONE);
        }

        long start = upcoming.get(0).getTimestampDebut().getTime();

        return new ProgressDTO(
                start,
                toMs(endOfSchoolBlock),
                "Retour en entreprise",
                true
        );
    }

    // ----------------- IZIA -----------------

    public ProgressDTO calculateNextIziaFormProgress() {
        ZonedDateTime now = nowParis();

        List<IziaTaskDTO> tasks = new ArrayList<>();

        tasks.add(new IziaTaskDTO("Formulaire Izia : Thématique",
                ZonedDateTime.of(2026, 2, 1, 0, 0, 0, 0, PARIS_ZONE),
                ZonedDateTime.of(2026, 4, 30, 23, 59, 59, 0, PARIS_ZONE)));

        tasks.add(new IziaTaskDTO("Formulaire Izia : Suivi Alternance #1",
                ZonedDateTime.of(2026, 2, 1, 0, 0, 0, 0, PARIS_ZONE),
                ZonedDateTime.of(2026, 5, 1, 23, 59, 59, 0, PARIS_ZONE)));

        tasks.add(new IziaTaskDTO("Formulaire Izia : Visite en entreprise",
                ZonedDateTime.of(2026, 2, 1, 0, 0, 0, 0, PARIS_ZONE),
                ZonedDateTime.of(2026, 5, 31, 23, 59, 59, 0, PARIS_ZONE)));

        tasks.add(new IziaTaskDTO("Formulaire Izia : Bibliographie & Synthèse",
                ZonedDateTime.of(2026, 4, 1, 0, 0, 0, 0, PARIS_ZONE),
                ZonedDateTime.of(2026, 8, 19, 23, 59, 59, 0, PARIS_ZONE)));

        for (IziaTaskDTO task : tasks) {
            if (task.getEnd().isAfter(now)) {
                boolean isStarted = now.isAfter(task.getStart());

                return new ProgressDTO(
                        toMs(task.getStart()),
                        toMs(task.getEnd()),
                        task.getLabel(),
                        isStarted
                );
            }
        }

        return new ProgressDTO(0, 0, "Aucun formulaire Izia en attente", false);
    }
    // -----------------

}