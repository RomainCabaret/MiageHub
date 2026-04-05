package com.GlassFishJSF.service;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.dto.ProgressDTO;
import com.GlassFishJSF.model.Cours;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ProgressService {

    @Inject
    private CoursDAO coursDAO;

    public ProgressDTO calculateMinuteProgress() {
        LocalDateTime now = LocalDateTime.now();
        long start = toMs(now.withSecond(0).withNano(0));
        long end = toMs(now.withSecond(0).withNano(0).plusMinutes(1));
        int minuteIndex = now.getMinute() + 1;
        return new ProgressDTO(start, end, "Minute n°" + minuteIndex, true);
    }
    public ProgressDTO calculateHourProgress() {
        LocalDateTime now = LocalDateTime.now();
        long start = toMs(now.withMinute(0).withSecond(0).withNano(0));
        long end = toMs(now.withMinute(0).withSecond(0).withNano(0).plusHours(1));
        return new ProgressDTO(start, end, "Heure n°" + now.getHour(), true);
    }

    public ProgressDTO calculateMonthProgress() {
        LocalDateTime now = LocalDateTime.now();
        long start = toMs(now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        long end = toMs(now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0).plusMonths(1));

        // Récupération du mois en français (ex: "avril")
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        // Première lettre en majuscule : "Avril"
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

        return new ProgressDTO(start, end, "Mois " + monthName, true);
    }

    public ProgressDTO calculateYearProgress() {
        LocalDateTime now = LocalDateTime.now();
        long start = toMs(now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        long end = toMs(now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0).plusYears(1));
        return new ProgressDTO(start, end, "Année " + now.getYear(), true);
    }

    public ProgressDTO calculateCenturyProgress() {
        LocalDateTime startCentury = LocalDateTime.of(2001, 1, 1, 0, 0);
        LocalDateTime endCentury = LocalDateTime.of(2101, 1, 1, 0, 0);
        return new ProgressDTO(toMs(startCentury), toMs(endCentury), "21ème Siècle", true);
    }



    public ProgressDTO calculateExamInterval() {
        Map<String, Cours> exams = coursDAO.findLastAndNextExam();
        if (!exams.containsKey("next")) return new ProgressDTO(0, 0, "", false);

        Cours next = exams.get("next");
        long end = next.getTimestampDebut().getTime();
        long start = exams.containsKey("last")
                ? exams.get("last").getTimestampDebut().getTime()
                : toMs(LocalDateTime.now().withHour(0).withMinute(0));

        return new ProgressDTO(start, end, "Prochain examen: " + next.getMatiere(), true);
    }

    public ProgressDTO calculateGlobalSession() {
        Map<String, Cours> boundaries = coursDAO.findFirstAndLastExam();
        if (!boundaries.containsKey("first") || !boundaries.containsKey("last")) {
            return new ProgressDTO(0, 0, "", false);
        }

        Cours first = boundaries.get("first");
        Cours last = boundaries.get("last");
        long start = first.getTimestampFin().getTime();
        long end = last.getTimestampDebut().getTime();

        return new ProgressDTO(start, end, "Dernier examen: " + last.getMatiere(), end > start);
    }



    private long toMs(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.of("Europe/Paris"))
                .toInstant()
                .toEpochMilli();
    }
}