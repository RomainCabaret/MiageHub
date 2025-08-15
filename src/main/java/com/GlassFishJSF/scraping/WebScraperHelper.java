package com.GlassFishJSF.scraping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

public class WebScraperHelper {

    public static Map<String, List<Map<String, String>>> extractCoursParJour(WebDriver driver) throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script = """
                
                const coursParJour = {};
                
                document.querySelectorAll(".EmploiDuTemps_Element").forEach((element) => {
                  const title = element.querySelector(".cours-simple")?.getAttribute("title") || "Horaire inconnu";
                  const spans = element.querySelectorAll(".contenu");
                
                  const total = spans.length;
                  
                  const contenuSpans = Array.from(spans).map(s => s.innerText.trim()).join(" | ");
                
                  let type = "Non spécifié (type)";
                  let matiere = "Non spécifié (matière)";
                  let groupe = "Non spécifié (groupe)";
                  let nbEtudiants = "Non spécifié (effectif)";
                  let salles = [];
                 
                
                  if (total >= 5) {
                    type = spans[0]?.innerText.trim() || type;
                    matiere = spans[1]?.innerText.trim() || matiere;
                    groupe = spans[total - 2]?.innerText.trim() || groupe;
                    nbEtudiants = spans[total - 1]?.innerText.trim() || nbEtudiants;
                
                    for (let i = 2; i < total - 2; i++) {
                      const salle = spans[i]?.innerText.trim();
                      if (salle) salles.push(salle);
                    }
                
                  } else if (total === 3) {
                    type = spans[0]?.innerText.trim() || type;
                    groupe = spans[1]?.innerText.trim() || "Non spécifié (groupe)";
                    nbEtudiants = spans[2]?.innerText.trim() || "Non spécifié (effectif)";
                    matiere = groupe; // On copie dans matière si pas présente
                    salles = ["Non spécifié (salle)"];
                  } else if (total === 2) {
                    type = spans[0]?.innerText.trim() || type;
                    nbEtudiants = spans[1]?.innerText.trim() || nbEtudiants;
                    matiere = type;
                    groupe = type;
                    salles = ["Non spécifié (salle)"];
                  } else {
                    type = getTextSafe(0, "type");
                    matiere = getTextSafe(1, "matière");
                    groupe = getTextSafe(2, "groupe");
                    nbEtudiants = getTextSafe(3, "effectif");
                    salles = ["Non spécifié (salle)"];
                  }
                
                  const cours = {
                    horaire: title,
                    type,
                    matiere,
                    salle: salles.length > 0 ? salles.join(" | ") : "Non spécifié (salle)",
                    groupe,
                    nbEtudiants,
                    contenuSpans
                  };
                
                  const spanDate = element.querySelector(".sr-only");
                  const texteDate = spanDate?.innerText || "";
                  const regexDate = /Cours du (\\d{1,2}) (\\w+)/;
                  const match = texteDate.match(regexDate);
                  let cleJour = "Inconnu";
                
                  if (match) {
                    const jour = match[1].padStart(2, "0");
                    const mois = match[2];
                    cleJour = jour + " " + mois;
                  }
                
                  if (!coursParJour[cleJour]) {
                    coursParJour[cleJour] = [];
                  }
                
                  coursParJour[cleJour].push(cours);
                });
                
                return JSON.stringify(coursParJour, null, 2);
                
""";


        String resultJson = (String) js.executeScript(script);

        System.out.println("result JSON : "  + resultJson);

        // Utilisation de Jackson pour parser le JSON en Map Java
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(resultJson, Map.class);
    }
}
