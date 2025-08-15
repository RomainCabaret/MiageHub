package com.GlassFishJSF;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("monBean")
@RequestScoped
public class MonBean {

    private String nom;
    private String message;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMessage() { return message; }

    public String saluer() {
        this.message = "Salut " + nom + " ! Bienvenue dans JSF 😄";
        return null;
    }
    @PostConstruct
    public void init() {
        System.out.println("✅ Bean MonBean initialisé !");
    }
}
