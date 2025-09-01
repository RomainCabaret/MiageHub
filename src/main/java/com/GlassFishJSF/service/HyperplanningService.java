package com.GlassFishJSF.service;

import com.GlassFishJSF.dao.CoursDAO;
import com.GlassFishJSF.model.Cours;
import com.GlassFishJSF.scraping.SemaineHelper;
import com.GlassFishJSF.scraping.WebScraperHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;


@ApplicationScoped
public class HyperplanningService {

    @Inject
    private CoursDAO coursDAO;

    private final String TARGET_URL = "https://edt-univ-evry.hyperplanning.fr/hp/invite?login=true";
    private static final Logger logger = Logger.getLogger(HyperplanningService.class.getName());


    public HyperplanningService() {
        // Obligatoire pour CDI
    }

    public HyperplanningService(CoursDAO coursDAO) {
        this.coursDAO = coursDAO;
    }

    public void scrapAllWeeks() {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\chromeDriver\\chromedriver.exe");

        WebDriverManager.chromedriver().setup();


        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,901");
        options.addArguments("--remote-allow-origins=*");


        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get(TARGET_URL);

            // Sélection du groupe
            wait.until(ExpectedConditions.elementToBeClickable(By.id("GInterface.Instances[1].Instances[0].bouton"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("GInterface.Instances[1].Instances[0]_1"))).click();

            WebElement editInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("GInterface.Instances[1].Instances[1].bouton_Edit")));
            editInput.clear();
            editInput.sendKeys("M1ILWY142", Keys.ENTER);
            Thread.sleep(1500);

            // Boucle sur toutes les semaines
            LocalDate debut = LocalDate.of(2024, 9, 2);
            LocalDate fin = LocalDate.of(2025, 7, 7);
            LocalDate date = debut;

            while (!date.isAfter(fin)) {
                try {
                    String idSemaine = SemaineHelper.getIdSemaineFromDate(date);
                    System.out.println("➡️ Semaine " + date + " - bouton " + idSemaine);

                    WebElement boutonSemaine = wait.until(ExpectedConditions.elementToBeClickable(By.id(idSemaine)));
                    js.executeScript("arguments[0].scrollIntoView(true);", boutonSemaine);
                    boutonSemaine.click();
                    Thread.sleep(1000); // attendre chargement de la semaine

                    Map<String, List<Map<String, String>>> currentWeek = WebScraperHelper.extractCoursParJour(driver);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM HH:mm", Locale.FRENCH);

                    for (String jour : currentWeek.keySet()) {
                        for (Map<String, String> map : currentWeek.get(jour)) {
                            Cours cours = new Cours();

                            // 🕓 Extrait uniquement les heures de début et de fin
                            String horaire = map.get("horaire"); // ex: "de 08h30 à 10h00 (01h30)"

                            String fullDateStr = jour + " " + horaire; // "22 avril de 08h30 à 10h00 (01h30)"
                            String[] dateParts =  fullDateStr.split(" ");

                            String day = dateParts[0];
                            String monthName =  dateParts[1];
                            String timeStart = dateParts[3];
                            String timeEnd = dateParts[5];


                            Map<String, String> monthMap = Map.ofEntries(
                                    Map.entry("janvier", "01"),
                                    Map.entry("février", "02"),
                                    Map.entry("mars", "03"),
                                    Map.entry("avril", "04"),
                                    Map.entry("mai", "05"),
                                    Map.entry("juin", "06"),
                                    Map.entry("juillet", "07"),
                                    Map.entry("août", "08"),
                                    Map.entry("septembre", "09"),
                                    Map.entry("octobre", "10"),
                                    Map.entry("novembre", "11"),
                                    Map.entry("décembre", "12")
                            );


                            String monthNum = monthMap.get(monthName.toLowerCase());

                            if (monthNum == null) throw new IllegalArgumentException("Mois invalide");

                            System.out.println("le mois : " + monthNum + " - " + monthName);
                            System.out.println("le jour : " + day);
                            System.out.println("le timeStart : " + timeStart);
                            System.out.println("le timeEnd : " + timeEnd);


                            int monthCours = Integer.parseInt(monthNum);
                            int dayCours = Integer.parseInt(day);

                            int todayMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                            int todayYear = Calendar.getInstance().get(Calendar.YEAR);
                            int schoolYearStart = (todayMonth >= 9) ? todayYear : todayYear - 1;
                            int coursYear = (monthCours >= 9) ? schoolYearStart : schoolYearStart + 1;

                            // Construction de la date complète
                            String dateStr = String.format("%d-%02d-%02d", coursYear, monthCours, dayCours);
                            String timeStartStr = timeStart.replace("h", ":") + ":00";
                            String timeEndStr = timeEnd.replace("h", ":") + ":00";

                            // Conversion en Timestamp
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Timestamp timestampStart = new Timestamp(sdf.parse(dateStr + " " + timeStartStr).getTime());
                            Timestamp timestampEnd = new Timestamp(sdf.parse(dateStr + " " + timeEndStr).getTime());

                            StringBuilder log = new StringBuilder();

                            log.append("\n------ Cours ------\n");
                            log.append("Date d'origine     : ").append(jour).append("\n");
                            log.append("Horaire brut       : ").append(horaire).append("\n");
                            log.append("Date utilisée      : ").append(dateStr).append("\n");
                            log.append("→ Timestamp début  : ").append(timestampStart).append("\n");
                            log.append("→ Timestamp fin    : ").append(timestampEnd).append("\n");




                            log.append("\n===== FIN DU LOG =====");
                            System.out.println(log.toString());

                            java.util.Date coursDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);

                            cours.setDate(coursDate);
                            cours.setTimestampDebut(timestampStart);
                            cours.setTimestampFin(timestampEnd);

                            cours.setTypeCours(map.get("type"));
                            cours.setMatiere(map.get("matiere"));
                            String salleStr = map.getOrDefault("salle", "Non spécifié");
                            salleStr = salleStr.trim().replaceAll("\\s*\\|\\s*", " | "); // nettoyage (espace standard)
                            cours.setSalle(salleStr);
                            cours.setContent(map.get("contenuSpans"));

                            cours.setGroupe(map.getOrDefault("groupe", "Non spécifié"));

                            // 🎯 Conversion nbEtudiants
                            String nbEtudiantsStr = map.get("nbEtudiants");
                            try {
                                if (nbEtudiantsStr != null && nbEtudiantsStr.matches("\\d+")) {
                                    cours.setNbEtudiants(0);
                                } else {
                                    System.out.println("ℹ️ nbEtudiants ignoré : " + nbEtudiantsStr);
                                    cours.setNbEtudiants(null);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Erreur de parsing nbEtudiants : " + nbEtudiantsStr);
                                cours.setNbEtudiants(null);
                            }

                            coursDAO.save(cours);
                        }
                    }


                } catch (Exception e) {
                    System.err.println("⚠️ Erreur sur la semaine " + date + " : " + Arrays.toString(e.getStackTrace()));
                }

                date = date.plusWeeks(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }



    public Map<String, List<Map<String, String>>> getPlanningForDate(LocalDate dateSouhaitee) {

        System.out.println("on rentre " +  dateSouhaitee.toString());

        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\chromeDriver\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless=new"); // Headless = pas d'interface graphique
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,901");
        options.addArguments("--remote-allow-origins=*"); // utile parfois

        WebDriver driver = new ChromeDriver(options);

        System.out.println("Driver crée  ");


        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            System.out.println("Driver instantier ");

            driver.get(TARGET_URL);

            System.out.println("Connecté !");


            wait.until(ExpectedConditions.elementToBeClickable(By.id("GInterface.Instances[1].Instances[0].bouton"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("GInterface.Instances[1].Instances[0]_1"))).click();

            WebElement editInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("GInterface.Instances[1].Instances[1].bouton_Edit")));
            editInput.clear();
            editInput.sendKeys("L3MIAX132", Keys.ENTER);

            System.out.println("encore : "  +editInput.getAttribute("value"));

            Thread.sleep(1500);

            String idSemaine = SemaineHelper.getIdSemaineFromDate(dateSouhaitee);
            WebElement boutonSemaine = wait.until(ExpectedConditions.elementToBeClickable(By.id(idSemaine)));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", boutonSemaine);
            boutonSemaine.click();

            Thread.sleep(1000);

            return WebScraperHelper.extractCoursParJour(driver);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            driver.quit();
        }
    }
}