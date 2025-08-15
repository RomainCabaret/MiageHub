package com.GlassFishJSF.controller;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.scraping.SemaineHelper;
import com.GlassFishJSF.scraping.WebScraperHelper;
import com.GlassFishJSF.service.HyperplanningService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@ViewScoped
public class PlanningController implements Serializable {

    private Map<String, List<Map<String, String>>> planning;
    private LocalDate date = LocalDate.now();

    @Inject
    private CoursDAO coursDAO;

//    @PostConstruct
//    public void init() {
//        loadPlanning();
//    }
////
//    public void loadPlanning() {
//        List<Cours> coursList = coursDAO.getCoursForDate(date);
//        planning = coursList.stream()
//                .collect(Collectors.groupingBy(
//                        Cours::getJour,
//                        LinkedHashMap::new,
//                        Collectors.mapping(cours -> Map.of(
//                                "horaire", cours.getHoraire(),
//                                "type", cours.getType(),
//                                "matiere", cours.getMatiere(),
//                                "salle", cours.getSalle(),
//                                "groupe", cours.getGroupe(),
//                                "nbEtudiants", cours.getNbEtudiants() + ""
//                        ), Collectors.toList())
//                ));
//    }

    public Date getDateAsUtilDate() {
        return Date.from(date.atStartOfDay(ZoneId.of("Europe/Paris")).toInstant());
    }

    public void setDateAsUtilDate(Date utilDate) {
        this.date = utilDate.toInstant().atZone(ZoneId.of("Europe/Paris")).toLocalDate();
    }

    public Map<String, List<Map<String, String>>> getPlanning() {
        return planning;
    }
}
