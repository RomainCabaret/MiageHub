package com.GlassFishJSF.beans;

import com.GlassFishJSF.dto.ProgressDTO;
import com.GlassFishJSF.service.ProgressService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@ApplicationScoped
public class ProgressBean implements Serializable {

    @Inject
    private ProgressService progressService;


    // ------ TIME BAR -------
    private ProgressDTO minuteBar;
    private ProgressDTO hourBar;
    private ProgressDTO monthBar;
    private ProgressDTO yearBar;
    private ProgressDTO centuryBar;

    // ------ EXAMEN BAR -------
    private ProgressDTO examBar;
    private ProgressDTO sessionBar;


    @PostConstruct
    public void init() {
        updateTimestamps();
    }

    public void updateTimestamps() {

        // ------ TIME BAR -------
        this.minuteBar = progressService.calculateMinuteProgress();
        this.hourBar = progressService.calculateHourProgress();
        this.monthBar = progressService.calculateMonthProgress();
        this.yearBar = progressService.calculateYearProgress();
        this.centuryBar = progressService.calculateCenturyProgress();

        // ------ EXAMEN BAR -------
        this.examBar = progressService.calculateExamInterval();
        this.sessionBar = progressService.calculateGlobalSession();
    }

    // Getters
    public ProgressDTO getHourBar() {return hourBar;}
    public ProgressDTO getMonthBar() {return monthBar;}
    public ProgressDTO getYearBar() {return yearBar;}
    public ProgressDTO getCenturyBar() {return centuryBar;}
    public ProgressDTO getExamBar() { return examBar; }
    public ProgressDTO getSessionBar() { return sessionBar; }
    public ProgressDTO getMinuteBar() { return minuteBar; }

}