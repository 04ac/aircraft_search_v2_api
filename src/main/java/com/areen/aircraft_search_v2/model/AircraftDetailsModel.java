package com.areen.aircraft_search_v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null values in JSON
public class AircraftDetailsModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // To maintain compatibility across versions

    private HashMap<String, String> aircraftDetails;
    private ArrayList<String> remarks;


    public HashMap<String, String> getAircraftDetails() {
        return aircraftDetails;
    }

    public void setAircraftDetails(HashMap<String, String> aircraftDetails) {
        this.aircraftDetails = aircraftDetails;
    }

    public ArrayList<String> getRemarks() {
        return remarks;
    }

    public void setRemarks(ArrayList<String> remarks) {
        this.remarks = remarks;
    }
}
