package com.GlassFishJSF.dao;

import com.GlassFishJSF.model.Cours;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CoursDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    public List<Cours> findAll() {
        return em.createQuery("SELECT c FROM Cours c", Cours.class).getResultList();
    }

    // SUPPRESSION de @Transactional - la transaction est gérée par le service
    public void save(Cours cours) {
        try {
            System.out.println("➡️ Tentative de sauvegarde du cours : " + cours);
            em.persist(cours);
            System.out.println("✅ Cours sauvegardé avec succès.");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde du cours : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // SUPPRESSION de @Transactional - la transaction est gérée par le service
    public void deleteAll() {
        try {
            int deletedCount = em.createQuery("DELETE FROM Cours").executeUpdate();
            System.out.println("🗑️ Suppression de tous les cours réussie. Nombre de cours supprimés : " + deletedCount);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression de tous les cours : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}