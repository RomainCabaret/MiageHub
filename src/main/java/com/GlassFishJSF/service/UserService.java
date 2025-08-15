package com.GlassFishJSF.service;


import com.GlassFishJSF.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Named
@ApplicationScoped
public class UserService {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }
}