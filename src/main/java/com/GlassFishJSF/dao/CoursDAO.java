package com.GlassFishJSF.dao;

import com.GlassFishJSF.model.Cours;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Cours> findByPeriod(LocalDate start, LocalDate end) {
        return em.createQuery(
                        "SELECT c FROM Cours c WHERE c.date >= :start AND c.date <= :end ORDER BY c.date, c.timestampDebut",
                        Cours.class)
                .setParameter("start", java.sql.Date.valueOf(start))
                .setParameter("end", java.sql.Date.valueOf(end))
                .getResultList();
    }

    public List<Cours> findByDate(LocalDate date) {
        return findByPeriod(date, date);
    }

    public Map<String, Cours> findLastAndNextExam() {
        Map<String, Cours> context = new HashMap<>();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        List<Cours> pastExams = em.createQuery(
                        "SELECT c FROM Cours c WHERE c.typeCours = 'EXAMEN' AND c.timestampDebut < :now " +
                                "ORDER BY c.timestampDebut DESC", Cours.class)
                .setParameter("now", now)
                .setMaxResults(1)
                .getResultList();

        List<Cours> futureExams = em.createQuery(
                        "SELECT c FROM Cours c WHERE c.typeCours = 'EXAMEN' AND c.timestampDebut >= :now " +
                                "ORDER BY c.timestampDebut ASC", Cours.class)
                .setParameter("now", now)
                .setMaxResults(1)
                .getResultList();

        if (!pastExams.isEmpty()) context.put("last", pastExams.get(0));
        if (!futureExams.isEmpty()) context.put("next", futureExams.get(0));

        return context;
    }

    public Map<String, Cours> findFirstAndLastExam() {
        Map<String, Cours> boundaries = new HashMap<>();

        List<Cours> first = em.createQuery(
                        "SELECT c FROM Cours c WHERE c.typeCours = 'EXAMEN' ORDER BY c.timestampFin ASC", Cours.class)
                .setMaxResults(1)
                .getResultList();

        // On récupère le tout dernier examen (le plus lointain)
        List<Cours> last = em.createQuery(
                        "SELECT c FROM Cours c WHERE c.typeCours = 'EXAMEN' ORDER BY c.timestampDebut DESC", Cours.class)
                .setMaxResults(1)
                .getResultList();

        if (!first.isEmpty()) boundaries.put("first", first.get(0));
        if (!last.isEmpty()) boundaries.put("last", last.get(0));

        return boundaries;
    }
}