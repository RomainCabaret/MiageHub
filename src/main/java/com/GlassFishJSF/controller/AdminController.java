package com.GlassFishJSF.controller;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.service.HyperplanningService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

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
            List<Cours> nouveauxCours = hyperplanningService.scrapAllWeeks();

            if (nouveauxCours == null || nouveauxCours.isEmpty()) {
                message = "[SCRAPING] échoué, anciens cours conservés";
                return;
            }

            coursDAO.deleteAll();

            for (Cours cours : nouveauxCours) {
                coursDAO.save(cours);
            }

            message = "[SCRAPING] terminé et planning mis à jour (" + nouveauxCours.size() + " cours)";

        } catch (Exception e) {
            System.err.println("[SCRAPING] Erreur lors du scraping : " + e.getMessage());
            message = "[SCRAPING] Erreur lors du scraping";
        }
    }


    public String getMessage() {
        return message;
    }
}
