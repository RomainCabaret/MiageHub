package com.GlassFishJSF.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class IziaTaskDTO implements Serializable {

    String label;
    ZonedDateTime start;
    ZonedDateTime end;

    public IziaTaskDTO(){}
    public IziaTaskDTO(String label, ZonedDateTime start, ZonedDateTime end) {
        this.label = label;
        this.start = start;
        this.end = end;
    }

    public ZonedDateTime getEnd() {return end;}
    public ZonedDateTime getStart() {return start;}
    public String getLabel() {return label;}
}
