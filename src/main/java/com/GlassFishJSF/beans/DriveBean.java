package com.GlassFishJSF.beans;

import com.GlassFishJSF.dao.DriveDAO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class DriveBean implements Serializable {

    @Inject
    private DriveDAO driveDAO;

    public List<String> getItems() {
        return driveDAO.getFiles();
    }


    public void deleteFile(String filename) {
        driveDAO.delete(filename);
    }


}
