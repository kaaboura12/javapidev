package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TenorAPIService {
    private static final String API_KEY = "AIzaSyC8SBMwgE4tvDZugcMClVdEqcJ9tENx9y8";
    private static final String TENOR_API_URL = "https://tenor.googleapis.com/v2/search";
    private static final int LIMIT = 20;
    
    public static class GifResult {
        private String id;
        private String title;
        private String url;
        private String previewUrl;
        
        public GifResult(String id, String title, String url, String previewUrl) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.previewUrl = previewUrl;
        }
        
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getPreviewUrl() { return previewUrl; }
    }
    
    public List<GifResult> searchGifs(String query) {
        List<GifResult> results = new ArrayList<>();
        
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            URL url = new URL(TENOR_API_URL + "?q=" + encodedQuery + "&key=" + API_KEY + "&limit=" + LIMIT);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray resultsArray = jsonResponse.getAsJsonArray("results");
                
                for (JsonElement resultElement : resultsArray) {
                    JsonObject resultObject = resultElement.getAsJsonObject();
                    String id = resultObject.get("id").getAsString();
                    String title = resultObject.has("title") ? resultObject.get("title").getAsString() : "";
                    
                    // Get media formats
                    JsonObject mediaFormats = resultObject.getAsJsonObject("media_formats");
                    
                    // Get GIF URL (preferring mediumgif or tinygif format for better performance)
                    String gifUrl = "";
                    if (mediaFormats.has("mediumgif")) {
                        gifUrl = mediaFormats.getAsJsonObject("mediumgif").get("url").getAsString();
                    } else if (mediaFormats.has("gif")) {
                        gifUrl = mediaFormats.getAsJsonObject("gif").get("url").getAsString();
                    } else if (mediaFormats.has("tinygif")) {
                        gifUrl = mediaFormats.getAsJsonObject("tinygif").get("url").getAsString();
                    }
                    
                    // Get preview URL (preferring nanogif or tinygif for thumbnails)
                    String previewUrl = "";
                    if (mediaFormats.has("nanogif")) {
                        previewUrl = mediaFormats.getAsJsonObject("nanogif").get("url").getAsString();
                    } else if (mediaFormats.has("tinygif")) {
                        previewUrl = mediaFormats.getAsJsonObject("tinygif").get("url").getAsString();
                    } else if (mediaFormats.has("gif")) {
                        previewUrl = mediaFormats.getAsJsonObject("gif").get("url").getAsString();
                    }
                    
                    results.add(new GifResult(id, title, gifUrl, previewUrl));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    public List<GifResult> getTrendingGifs() {
        return searchGifs("trending");
    }
} 