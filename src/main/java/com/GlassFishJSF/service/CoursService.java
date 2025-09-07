package com.GlassFishJSF.service;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Stateless
public class CoursService {

    @Inject
    private CoursDAO coursDAO;

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void replaceAllCours(List<Cours> nouveauxCours) {
        try {
            System.out.println("🔄 Début du remplacement de tous les cours...");

            // 1. Supprimer tous les cours existants
            coursDAO.deleteAll();

            // 2. Forcer la suppression avant d'insérer
            em.flush();

            System.out.println("📝 Insertion de " + nouveauxCours.size() + " nouveaux cours...");

            // 3. Insérer les nouveaux cours
            for (int i = 0; i < nouveauxCours.size(); i++) {
                Cours cours = nouveauxCours.get(i);
                coursDAO.save(cours);

                // Flush périodique pour éviter les problèmes de mémoire
                if (i % 50 == 0) {
                    em.flush();
                    em.clear();
                }
            }

            // 4. Flush final pour s'assurer que tout est persisté
            em.flush();

            System.out.println("✅ Remplacement terminé avec succès !");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du remplacement des cours : " + e.getMessage());
            e.printStackTrace();
            throw e; // Cela provoquera un rollback de la transaction
        }
    }
}