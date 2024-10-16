package com.areen.aircraft_search_v2.controller;

import com.areen.aircraft_search_v2.AircraftSearchV2Application;
import com.areen.aircraft_search_v2.model.AircraftDetailsModel;
import com.areen.aircraft_search_v2.service.AircraftSearchWebScrapingService;
import com.areen.aircraft_search_v2.service.OCRService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
public class AircraftSearchController {

    private final AircraftSearchWebScrapingService webScrapingService;
    private final OCRService ocrService;

    AircraftSearchController(AircraftSearchWebScrapingService webScrapingService, OCRService ocrService) {
        this.webScrapingService = webScrapingService;
        this.ocrService = ocrService;
    }

    @GetMapping("/api/reg/{registration}")
    public AircraftDetailsModel getAircraftDetails(@PathVariable String registration) {
        return webScrapingService.getAircraftDetails(registration);
    }

    // POST request that accepts base64image and returns list of AircraftDetailsModel
    @PostMapping("/api/image")
    public ResponseEntity<List<AircraftDetailsModel>> getAircraftDetailsFromImage(@RequestParam("file") MultipartFile file) {

        try {
            // Step 1: Extract registration numbers from the image using OCR
            List<String> registrationNumbers = ocrService.getRegistrationNumbersFromImage(file);

            // Handle possible null value from OCR service
            if (registrationNumbers == null || registrationNumbers.isEmpty()) {
                // Return an empty list if no registration numbers were found
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<AircraftDetailsModel> aircraftDetailsList = registrationNumbers.stream()
                    .map(registration -> {
                        try {
                            return webScrapingService.getAircraftDetails(registration);
                        } catch (Exception e) {
                            // Log the error or handle it as necessary
                            System.err.println("Failed to fetch details for registration: " + registration + " due to: " + e.getMessage());
                            return null;  // Returning null to indicate failure for this particular registration
                        }
                    })
                    .filter(Objects::nonNull)  // Remove any null results from the stream
                    .collect(Collectors.toList());

            // Step 3: Return the list of aircraft details as the response
            return ResponseEntity.ok(aircraftDetailsList);
        } catch (Exception e) {
            // Handle any errors (e.g., invalid image, OCR failure, etc.)
            System.out.println(Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(500).build();
        }
    }
}
