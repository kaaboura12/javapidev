package Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;

public class PexelsAPI {
    private static final String API_URL = "https://api.pexels.com/v1/search";
    // Replace with your actual Pexels API key
    private static final String API_KEY = "PoGSNBWrPL1jhkvmB6yWoCQVTJWj3oh4SYWSKMGZkNkWuhuM5gR4FZrL";
    private static final Gson gson = new Gson();
    
    // Static initialization for SSL connections
    static {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Create all-trusting host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
            System.out.println("SSL configurations initialized for PexelsAPI");
        } catch (Exception e) {
            System.err.println("Failed to setup SSL context: " + e.getMessage());
        }
    }

    /**
     * Represents an image result from Pexels API
     */
    public static class PexelsImage {
        private String url;
        private String photographer;
        private String description;
        private String base64Data;
        private transient Image cachedImage;

        public PexelsImage(String url, String photographer, String description) {
            this.url = url;
            this.photographer = photographer;
            this.description = description;
            this.base64Data = null;
            this.cachedImage = null;
        }

        public String getUrl() { return url; }
        public String getPhotographer() { return photographer; }
        public String getDescription() { return description; }
        
        public void setBase64Data(String base64Data) {
            this.base64Data = base64Data;
        }
        
        public String getBase64Data() {
            return base64Data;
        }
        
        public Image getImage() {
            if (cachedImage == null && base64Data != null) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    cachedImage = new Image(new ByteArrayInputStream(imageBytes));
                } catch (Exception e) {
                    System.err.println("Error creating image from base64: " + e.getMessage());
                }
            }
            return cachedImage;
        }
        
        public boolean isPreloaded() {
            return base64Data != null;
        }
    }

    /**
     * Searches for images using the Pexels API based on the provided query
     * @param query The search query for finding relevant images
     * @param perPage Number of images to return (max 80)
     * @return List of PexelsImage objects containing image details
     * @throws IOException If there's an error communicating with the API
     */
    public static List<PexelsImage> searchImages(String query, int perPage) throws IOException {
        System.out.println("Searching for images with query: " + query);
        
        if (perPage <= 0 || perPage > 80) {
            perPage = 15;
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String urlString = API_URL + "?query=" + encodedQuery + "&per_page=" + perPage;
        System.out.println("Request URL: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", API_KEY);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(10000); // Increased timeout
        conn.setReadTimeout(10000);    // Increased timeout
        conn.setDoInput(true);

        System.out.println("Sending request to Pexels API...");
        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorMessage = readErrorStream(conn);
            System.err.println("API Error Response: " + errorMessage);
            throw new IOException("Failed to search for images: Server returned HTTP response code: " + responseCode +
                "\nResponse: " + errorMessage);
        }

        List<PexelsImage> images = new ArrayList<>();
        String jsonResponse = readInputStream(conn);
        
        // Truncate log for very large responses
        String logResponse = jsonResponse;
        if (logResponse.length() > 1000) {
            logResponse = logResponse.substring(0, 1000) + "... [truncated]";
        }
        System.out.println("API Response: " + logResponse);

        try {
            JsonObject response = gson.fromJson(jsonResponse, JsonObject.class);
            if (!response.has("photos")) {
                System.err.println("No 'photos' field in response");
                return images;
            }

            JsonArray photos = response.getAsJsonArray("photos");
            System.out.println("Found " + photos.size() + " photos");
            
            if (photos != null) {
                for (int i = 0; i < photos.size(); i++) {
                    try {
                        JsonObject photo = photos.get(i).getAsJsonObject();
                        JsonObject src = photo.getAsJsonObject("src");
                        
                        // Try to get the medium size first, fall back to other sizes if not available
                        String imageUrl = null;
                        if (src.has("medium")) {
                            imageUrl = src.get("medium").getAsString();
                        } else if (src.has("small")) {
                            imageUrl = src.get("small").getAsString();
                        } else if (src.has("original")) {
                            imageUrl = src.get("original").getAsString();
                        }

                        if (imageUrl != null) {
                            String photographer = photo.has("photographer") ? 
                                photo.get("photographer").getAsString() : "Unknown";
                            String description = photo.has("alt") ? 
                                photo.get("alt").getAsString() : "";
                            
                            System.out.println("Adding image: " + imageUrl);
                            PexelsImage pexelsImage = new PexelsImage(imageUrl, photographer, description);
                            
                            // Pre-download the image as base64 (this is new)
                            try {
                                String imageBase64 = downloadImageAsBase64(imageUrl);
                                pexelsImage.setBase64Data(imageBase64);
                                System.out.println("Successfully preloaded image #" + i);
                            } catch (Exception e) {
                                System.err.println("Failed to preload image " + imageUrl + ": " + e.getMessage());
                                // Continue with the image even if we couldn't preload it
                            }
                            
                            images.add(pexelsImage);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing photo " + i + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            throw new IOException("Failed to parse API response: " + e.getMessage());
        }

        System.out.println("Returning " + images.size() + " images");
        return images;
    }

    private static String readErrorStream(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }

    private static String readInputStream(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }

    /**
     * Searches for a single image (convenience method)
     * @param query The search query for finding relevant images
     * @return The URL of the first matching image, or null if no images found
     * @throws IOException If there's an error communicating with the API
     */
    public static String searchImage(String query) throws IOException {
        List<PexelsImage> images = searchImages(query, 1);
        return images.isEmpty() ? null : images.get(0).getUrl();
    }

    /**
     * Downloads an image from a URL and converts it to base64
     * @param imageUrl The URL of the image to download
     * @return The base64 encoded image data
     * @throws IOException If there's an error downloading the image
     */
    public static String downloadImageAsBase64(String imageUrl) throws IOException {
        System.out.println("Downloading image from: " + imageUrl);
        
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(10000); // Increased timeout
        conn.setReadTimeout(10000);    // Increased timeout
        conn.setDoInput(true);

        int responseCode = conn.getResponseCode();
        System.out.println("Image download response code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorMessage = readErrorStream(conn);
            System.err.println("Image download error: " + errorMessage);
            throw new IOException("Failed to download image: Server returned HTTP response code: " + responseCode);
        }

        try (InputStream is = conn.getInputStream()) {
            byte[] imageBytes = is.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            System.out.println("Successfully downloaded and encoded image, size: " + imageBytes.length + " bytes");
            return base64;
        } catch (Exception e) {
            System.err.println("Error downloading image: " + e.getMessage());
            throw new IOException("Failed to download image: " + e.getMessage());
        }
    }

    /**
     * Validates if the API key is set
     * @return true if the API key is set and not the default value
     */
    public static boolean isApiKeySet() {
        return API_KEY != null && !API_KEY.isEmpty() && !API_KEY.equals("YOUR_PEXELS_API_KEY");
    }
} 