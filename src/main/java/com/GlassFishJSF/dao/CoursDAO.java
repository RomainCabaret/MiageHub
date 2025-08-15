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

    @Transactional
    public void save(Cours cours) {
        try {
            System.out.println("➡️ Tentative de sauvegarde du cours : " + cours);
            em.persist(cours);
            System.out.println("✅ Cours sauvegardé avec succès.");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde du cours : " + e.getMessage());
            e.printStackTrace();

            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("Cause: " + cause);
                cause = cause.getCause();
            }

            throw e; // si tu souhaites remonter l'exception
        }
    }

    @Transactional
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
//
//    public Optional<Cours> findByUniqueFields(Cours cours) {
//        List<Cours> results = em.createQuery("""
//                SELECT c FROM Cours c
//                WHERE c.jour = :jour AND c.horaire = :horaire AND c.matiere = :matiere AND c.salle = :salle
//            """, Cours.class)
//                .setParameter("jour", cours.getJour())
//                .setParameter("horaire", cours.getHoraire())
//                .setParameter("matiere", cours.getMatiere())
//                .setParameter("salle", cours.getSalle())
//                .getResultList();
//
//        return results.stream().findFirst();
//    }
//
//    public List<Cours> getCoursForDate(LocalDate date) {
//        return em.createQuery("SELECT c FROM Cours c WHERE c.jour = :date", Cours.class)
//                .setParameter("date", date)
//                .getResultList();
//    }


}
