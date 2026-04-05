package com.GlassFishJSF.dto;

import java.io.Serializable;

public class ProgressDTO implements Serializable {

    private long startTime;
    private long endTime;
    private String label;
    private boolean active;

    // Constructeur vide pour la sérialisation
    public ProgressDTO() {}

    public ProgressDTO(long startTime, long endTime, String label, boolean active) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
        this.active = active;
    }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
