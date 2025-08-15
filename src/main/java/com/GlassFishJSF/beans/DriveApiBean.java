package com.GlassFishJSF.beans;

import com.GlassFishJSF.dao.DriveDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;

@Named
@RequestScoped
public class DriveApiBean implements Serializable {

    @Inject
    private DriveDAO driveDAO;

    public void getFilesJson() throws IOException {
        HttpServletResponse response =
                (HttpServletResponse) FacesContext.getCurrentInstance()
                        .getExternalContext().getResponse();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (String fileName : driveDAO.getFiles()) {
            arr.add(Json.createObjectBuilder()
                    .add("id", fileName.hashCode()) // ID simple
                    .add("name", fileName)
                    .add("type", fileName.contains(".") ? "file" : "folder")
            );
        }
        response.getWriter().write(arr.build().toString());
        FacesContext.getCurrentInstance().responseComplete();
    }
}