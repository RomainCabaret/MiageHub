package com.GlassFishJSF.beans;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.service.CoursService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ApplicationScoped
public class ProgressBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CoursDAO coursDAO;

    @Inject
    private CoursService coursService;


    private long startTime;
    private long endTime;

    @PostConstruct
    public void init() {
        updateTimestamps();
    }

    public void updateTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        this.startTime = now.withSecond(0).withNano(0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        this.endTime = now.withSecond(0).withNano(0).plusMinutes(1)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        System.out.println("🔄 Nouveaux Timestamps générés : " + startTime + " -> " + endTime);
    }

    public long getStartTime() {
        return startTime;

    }

    public long getEndTime() {
        return endTime;
    }


}