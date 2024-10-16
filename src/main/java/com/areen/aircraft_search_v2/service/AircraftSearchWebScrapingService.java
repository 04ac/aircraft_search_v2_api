package com.areen.aircraft_search_v2.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.areen.aircraft_search_v2.model.AircraftDetailsModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service // constructor-based dependency injection
public class AircraftSearchWebScrapingService {
    private Document document;

    public AircraftDetailsModel getAircraftDetails(String reg) {
        try {
            this.document = this.getJsoupDocument(reg);
        } catch (IOException e) {
//           logger.error("Data not found!");
        }

        if (document == null) {
            return new AircraftDetailsModel();
        }

        AircraftDetailsModel ret = new AircraftDetailsModel();
        ret.setAircraftDetails(this.getAircraftDetailsHelper(reg));
        ret.setRemarks(this.getRemarksHelper());

        return ret;
    }


    // Helper Methods Below -----------------------------------------------

    private HashMap<String, String> getAircraftDetailsHelper(String reg) {

        try {
            this.document = getJsoupDocument(reg);
        } catch (IOException e) {
//            logger.error("Data not found!");
        }

        if (document == null) {
            return new HashMap<>();
        }

        Elements tables = document.select("table");
        Element aircraftTable = tables.get(2);  // Assuming this is the correct table

        // Extract headers from the table
        HashMap<String, String> aircraftData = new HashMap<>();
        Elements headersElements = aircraftTable.select("th");
        String[] headersArray = new String[headersElements.size()];

        for (int i = 0; i < headersElements.size(); i++) {
            Element header = headersElements.get(i);
            String title = header.attr("title");
            headersArray[i] = title.isEmpty() ? header.text().trim() : title;
        }

        // Extract row data from the table
        Elements rows = aircraftTable.select("tr");
        int headerIndex = 0;
        for (Element row : rows) {
            Elements cells = row.select("td");
            for (Element cell : cells) {
                if (headerIndex < headersArray.length) {
                    String key = headersArray[headerIndex];
                    String value = cell.text().trim();
                    aircraftData.put(capitalizeFirstLetter(key), value);
                    headerIndex++;
                }
            }
            if (headerIndex >= headersArray.length) {
                break;
            }
        }
        return cleanMap(aircraftData);
    }

    private ArrayList<String> getRemarksHelper() {
        if (document == null) {
            return new ArrayList<>();
        }

        Elements tables = document.select("table");
        Element aircraftTable = tables.get(2);
        ArrayList<String> remarks = new ArrayList<>();

        Elements remarkRows = aircraftTable.select("tr:has(td[colspan=2][align=right])");

        for (Element remarkRow : remarkRows) {
            String remark = remarkRow.select("td[colspan=17]").text().trim();
            remarks.add(capitalizeFirstLetter(remark));
        }
        return remarks;
    }

    private Document getJsoupDocument(String reg) throws IOException {
        String url = "https://www.airframes.org/reg/" + reg;

        // Define custom headers and cookies
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("Accept-Encoding", "gzip, deflate, br, zstd");
        headers.put("Accept-Language", "en-US,en;q=0.9,en-IN;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("Cookie", "PHPSESSID=tqqvmd4feousqgujf9nl8jma5k; afc1=17d841c9c8c6");
        headers.put("Host", "www.airframes.org");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0");
        headers.put("sec-ch-ua", "\"Microsoft Edge\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"");
        headers.put("sec-ch-ua-platform", "\"Windows\"");

        // Fetch the webpage content with Jsoup
        return Jsoup.connect(url).headers(headers).get();
    }

    private HashMap<String, String> cleanMap(HashMap<String, String> mp) {

        mp.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isEmpty()
                || entry.getValue() == null || entry.getValue().isEmpty());
        return mp;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
