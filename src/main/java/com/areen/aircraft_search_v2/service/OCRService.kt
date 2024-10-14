package com.areen.aircraft_search_v2.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.util.LinkedMultiValueMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.web.multipart.MultipartFile
import org.apache.tomcat.util.codec.binary.Base64 // Import for base64 encoding


@Service
class OCRService {

    // Links to the application.properties file, which in turn links to an environment variable
    @Value("\${ocr_service.apiKey}")
    private val apiKey: String? = null;

    fun getRegistrationNumbersFromImage(file: MultipartFile): List<String> {
        val restTemplate = RestTemplate()

        // URL for the API
        val url = "https://api.ocr.space/parse/image"

        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.set("apikey", apiKey)

        // Convert file to Base64
        val base64EncodedFile = Base64.encodeBase64String(file.bytes)

        // Set parameters for the body
        val params = LinkedMultiValueMap<String, Any>()
        params.add("language", "eng")
        params.add("base64Image", "data:image/jpeg;base64,$base64EncodedFile") // Pass base64 image string
        params.add("OCREngine", "2")
        params.add("scale", "true")

        // Create HttpEntity with headers and body
        val requestEntity = HttpEntity(params, headers)

        // Send POST request
        val response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)

        // Deserialize JSON to map
        val gson = Gson()
        val mapAdapter = gson.getAdapter(object : TypeToken<Map<String, Any?>>() {})
        val mp: Map<String, Any?> = mapAdapter.fromJson(response.body)

        // Safely extract ParsedResults from the map
        val parsedResults = mp["ParsedResults"] as? List<Map<String, String>>?

        // Check if parsedResults is null or empty before accessing it
        if (parsedResults.isNullOrEmpty()) {
            throw IllegalArgumentException("No parsed results found in OCR response.")
        }

        // Get the "ParsedText" from the first parsed result
        val parsedText = parsedResults[0]["ParsedText"]

        if (parsedText.isNullOrEmpty()) {
            throw IllegalArgumentException("ParsedText is empty or null.")
        }

        return getRegsFromString(parsedText)
    }

    private fun cleanString(string: String): String {
        // Replace underscores with hyphens
        // Remove all non-alphanumeric and non-hyphen characters
        // Replace spaces around hyphens

        val cleanedString = string.replace("_", "-")
            .replace("[^a-zA-Z0-9 -]".toRegex(), "")
            .replace("\\s*-\\s*".toRegex(), "-")

        return cleanedString
    }

    private fun getRegsFromString(inputString: String): List<String> {
        val pattern = Regex("""\w{1,4}-\w+|HL\w{4}|N\d{1,3}\w{2}|N\d{1,5}|UK\d{5}|JA\w{4}|UR\d{5}|HI\w{3,4}""")

        var words = inputString.split("\n")
        words = words.map { cleanString(it) }

        val registrationNumbers = mutableListOf<String>()

        for (word in words) {
            registrationNumbers.addAll(pattern.findAll(word)
                .map { it.value }
                .toSet()
                .toList())
        }

        return registrationNumbers
    }
}