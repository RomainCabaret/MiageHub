package com.GlassFishJSF.model;


import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "COURS")
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATE_COURS")
    private Date date;

    @Column(name = "HEURE_DEBUT")
    private Timestamp timestampDebut;

    @Column(name = "HEURE_FIN")
    private Timestamp timestampFin;

    @Column(name = "MATIERE")
    private String matiere;

    @Column(name = "TYPE_COURS")
    private String typeCours;

    @Column(name = "SALLE")
    private String salle;

    @Column(name = "GROUPE")
    private String groupe;

    @Column(name = "NB_ETUDIANTS")
    private Integer nbEtudiants;

    @Column(name = "CONTENT")
    private String content;


//   ----------- GETTER / SETTER -----------


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Timestamp getTimestampDebut() {
        return timestampDebut;
    }

    public void setTimestampDebut(Timestamp timestampDebut) {
        this.timestampDebut = timestampDebut;
    }

    public Timestamp getTimestampFin() {
        return timestampFin;
    }

    public void setTimestampFin(Timestamp timestampFin) {
        this.timestampFin = timestampFin;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getTypeCours() {
        return typeCours;
    }

    public void setTypeCours(String typeCours) {
        this.typeCours = typeCours;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public Integer getNbEtudiants() {
        return nbEtudiants;
    }

    public void setNbEtudiants(Integer nbEtudiants) {
        this.nbEtudiants = nbEtudiants;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
