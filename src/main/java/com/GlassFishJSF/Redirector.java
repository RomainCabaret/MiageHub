package com.GlassFishJSF;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.IOException;

@Named("redirector")
@RequestScoped
public class Redirector {

    public void redirigerAccueil() {
        try {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .redirect("pages/home.xhtml"); // ou dashboard.xhtml
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}