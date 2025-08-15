package com.GlassFishJSF.controller;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.service.HyperplanningService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@RequestScoped
public class AdminController implements Serializable {

    @Inject
    private CoursDAO coursDAO;

    @Inject
    private HyperplanningService hyperplanningService;

    private String message;

    public void lancerScraping() {
        try {
            coursDAO.deleteAll();
            hyperplanningService.scrapAllWeeks();
            message = "Scraping terminé et planning mis à jour ✅";
        } catch (Exception e) {
            message = "Erreur lors du scraping ❌";
        }
    }

    public String getMessage() {
        return message;
    }
}
