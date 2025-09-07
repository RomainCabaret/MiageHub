package com.GlassFishJSF.scheduler;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.service.CoursService;
import com.GlassFishJSF.service.HyperplanningService;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.service.CoursService;
import com.GlassFishJSF.service.HyperplanningService;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.util.List;

@Singleton
@Startup
public class ScrapingScheduler {

    @Inject
    private HyperplanningService hyperplanningService;

    @Inject
    private CoursService coursService;

    @Schedule(minute = "*/10", hour = "*", persistent = false)
    public void runScraping() {
        try {
            System.out.println("[SCRAPING] Lancement auto du scraping...");

            List<Cours> nouveauxCours = hyperplanningService.scrapAllWeeks();

            if (nouveauxCours != null && !nouveauxCours.isEmpty()) {
                // Transactionnel, commit garanti par CoursService
                coursService.replaceAllCours(nouveauxCours);
                System.out.println("[SCRAPING] Terminé (" + nouveauxCours.size() + " cours enregistrés)");
            } else {
                System.err.println("[SCRAPING] Aucun cours récupéré → anciens cours conservés");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[SCRAPING] Erreur scraping : " + e.getMessage());
        }
    }
}