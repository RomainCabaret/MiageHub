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
                  const spans = Array.from(element.querySelectorAll(".contenu")).map(s => s.innerText.trim());
                
                  let type = "Non spécifié (type)";
                  let matiere = "Non spécifié (matière)";
                  let salles = [];
                  let nbEtudiants = "Non spécifié (effectif)";
                  let groupe = "IBGBI"; // toujours IBGBI
                
                  // --- Type ---
                  const typesPossibles = ["REUNION", "CM", "TD", "Soutenance", "EXAMEN", "EVENEMENT"];
                  const typeTrouve = spans.find(txt => typesPossibles.includes(txt.toUpperCase()));
                  if (typeTrouve) {
                    type = typeTrouve;
                  }
                
                  // --- Matière ---
                  if (typeTrouve) {
                    const idxType = spans.indexOf(typeTrouve);
                    if (spans[idxType + 1]) {
                      matiere = spans[idxType + 1];
                    }
                  } else if (spans[0]) {
                    matiere = spans[0];
                  }
                
                  // --- Salles ---
                  salles = spans.filter(txt => txt.includes("IBGBI-"));
                  if (salles.length === 0) {
                    salles = ["Non spécifié (salle)"];
                  }
                
                  // --- Nb étudiants ---
                  const etuRegex = /(\\d+)\\s+étudiants/;
                  const etuTrouve = spans.find(txt => etuRegex.test(txt));
                  if (etuTrouve) {
                    nbEtudiants = etuTrouve.match(etuRegex)[0]; // ex: "20 étudiants"
                  }
                
                  const cours = {
                    horaire: title,
                    type,
                    matiere,
                    salle: salles.join(" | "),
                    groupe,
                    nbEtudiants,
                    contenuSpans: spans.join(" | ")
                  };
                
                  // --- Date (on garde ton code de base intact) ---
                  const spanDate = element.querySelector(".sr-only");
                  const texteDate = spanDate?.innerText || "";
                  const regexDate = /Cours du (\\d{1,2}) ([A-Za-zÀ-ÖØ-öø-ÿ]+)/;
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

        System.out.println("result JSON : " + resultJson);

        // Utilisation de Jackson pour parser le JSON en Map Java
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(resultJson, Map.class);
    }
}
