package com.GlassFishJSF.utils;

import java.sql.Timestamp;
import java.util.Date;

public class DateUtils {

    static public int getGridColumn(Date dateCours) {
        // Lundi = 2, Mardi = 3, ...
        java.time.DayOfWeek day = dateCours.toInstant().atZone(java.time.ZoneId.systemDefault()).getDayOfWeek();

        System.out.println("day gridCol " + day);
        return day.getValue() + 1; // car colonne 1 = heures
    }

    static public int getGridRow(Timestamp start) {
        // 08:00 = row 1, +1 par tranche de 30 min
        int hour = start.toLocalDateTime().getHour();

        int minute = start.toLocalDateTime().getMinute();
        System.out.println("minutes row " + minute);

        return (hour - 8) * 2 + (minute >= 30 ? 2 : 1);
    }

    static public int getRowSpan(Timestamp start, Timestamp end) {
        long minutes = java.time.Duration.between(start.toLocalDateTime(), end.toLocalDateTime()).toMinutes();
        System.out.println("minutes span" + minutes);
        return (int) (minutes / 30);
    }

}
