package com.GlassFishJSF.service;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.dto.ProgressDTO;
import com.GlassFishJSF.model.Cours;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ProgressService {

    @Inject
    private CoursDAO coursDAO;

    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    // Méthode de base : on récupère toujours l'instant présent à Paris
    private ZonedDateTime nowParis() {
        return ZonedDateTime.now(PARIS_ZONE);
    }

    public ProgressDTO calculateMinuteProgress() {
        ZonedDateTime now = nowParis();
        ZonedDateTime start = now.withSecond(0).withNano(0);
        ZonedDateTime end = start.plusMinutes(1);

        return new ProgressDTO(toMs(start), toMs(end), "Minute n°" + (now.getMinute() + 1), true);
    }

    public ProgressDTO calculateHourProgress() {
        ZonedDateTime now = nowParis();
        // Correction : on part de nowParis() directement
        ZonedDateTime start = now.withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime end = start.plusHours(1);

        return new ProgressDTO(toMs(start), toMs(end), "Heure n°" + now.getHour(), true);
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

        return new ProgressDTO(toMs(start), toMs(end), "Année " + now.getYear(), true);
    }

    public ProgressDTO calculateCenturyProgress() {
        // Dates fixes pour le siècle
        ZonedDateTime startCentury = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, PARIS_ZONE);
        ZonedDateTime endCentury = ZonedDateTime.of(2101, 1, 1, 0, 0, 0, 0, PARIS_ZONE);

        return new ProgressDTO(toMs(startCentury), toMs(endCentury), "21ème Siècle", true);
    }

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

    private long toMs(ZonedDateTime zdt) {
        return zdt.toInstant().toEpochMilli();
    }
}