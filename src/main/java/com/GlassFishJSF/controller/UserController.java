package com.GlassFishJSF.controller;

import com.GlassFishJSF.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.Serializable;
import java.util.List;


@Named("userController")
@RequestScoped
public class UserController implements Serializable {
    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    private List<User> users;

    public List<User> getUsers() {
        if (users == null) {
            loadUsers();
        }
        return users;
    }

    private void loadUsers() {
        try {
            users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace(); // à remplacer par du log pro
        }
    }

    public boolean isUsersEmpty() {
        return getUsers().isEmpty();
    }
}