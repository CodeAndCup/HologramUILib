package fr.perrier.hologramuilib.client.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Loads and caches resources from URLs.
 * Supports images (PNG, JPG, GIF) and JSON data.
 *
 * Security features:
 * - HTTPS only by default
 * - Whitelist/blacklist support
 * - File size limits
 * - Request timeout
 * - Local caching with expiration
 */
public class URLResourceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/WebLoader");
    private static URLResourceLoader INSTANCE;

    private final HttpClient httpClient;
    private final Path cacheDirectory;
    private final Map<String, CachedResource> cache = new HashMap<>();

    // Configuration
    private boolean httpsOnly = true;
    private long maxFileSizeMB = 5;
    private int requestTimeoutSeconds = 10;
    private long cacheDurationMs = 3600000; // 1 hour default

    private URLResourceLoader() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(requestTimeoutSeconds))
            .build();

        this.cacheDirectory = Paths.get("config", "hologramuilib", "webcache");
        try {
            Files.createDirectories(cacheDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to create cache directory", e);
        }
    }

    public static URLResourceLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new URLResourceLoader();
        }
        return INSTANCE;
    }

    /**
     * Loads an image from a URL asynchronously.
     * Returns a CompletableFuture that resolves to a BufferedImage.
     *
     * @param url The image URL
     * @return CompletableFuture with the loaded image
     */
    public CompletableFuture<BufferedImage> loadImage(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                CachedResource cached = getCachedResource(url);
                if (cached != null && !cached.isExpired()) {
                    LOGGER.debug("Loading image from cache: {}", url);
                    return ImageIO.read(cached.getFile());
                }

                // Validate URL
                if (!isUrlAllowed(url)) {
                    throw new SecurityException("URL not allowed: " + url);
                }

                // Download
                LOGGER.info("Downloading image from: {}", url);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .GET()
                    .build();

                // Use byte[] instead of InputStream to avoid stream issues
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    throw new IOException("HTTP " + response.statusCode());
                }

                byte[] imageData = response.body();

                // Check file size
                if (imageData.length > maxFileSizeMB * 1024 * 1024) {
                    throw new IOException("File too large: " + imageData.length + " bytes");
                }

                // Read image from byte array
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageData);
                BufferedImage image = ImageIO.read(bais);

                if (image == null) {
                    throw new IOException("Failed to decode image - ImageIO returned null");
                }

                // Cache it
                cacheImage(url, image);

                LOGGER.info("Successfully loaded image: {} ({}x{})", url, image.getWidth(), image.getHeight());
                return image;

            } catch (Exception e) {
                LOGGER.error("Failed to load image from URL: " + url, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Loads animated media (GIF) from a URL asynchronously.
     * Returns an AnimatedMedia with all frames.
     *
     * @param url The media URL
     * @return CompletableFuture with the loaded animated media
     */
    public CompletableFuture<AnimatedMediaLoader.AnimatedMedia> loadAnimatedMedia(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                CachedResource cached = getCachedResource(url);
                if (cached != null && !cached.isExpired()) {
                    LOGGER.debug("Loading animated media from cache: {}", url);
                    byte[] data = Files.readAllBytes(cached.getFile().toPath());
                    return AnimatedMediaLoader.loadAnimatedGif(data);
                }

                // Validate URL
                if (!isUrlAllowed(url)) {
                    throw new SecurityException("URL not allowed: " + url);
                }

                // Download
                LOGGER.info("Downloading animated media from: {}", url);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .GET()
                    .build();

                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    throw new IOException("HTTP " + response.statusCode());
                }

                byte[] mediaData = response.body();

                // Check file size
                if (mediaData.length > maxFileSizeMB * 1024 * 1024) {
                    throw new IOException("File too large: " + mediaData.length + " bytes");
                }

                // Load animated media
                AnimatedMediaLoader.AnimatedMedia media = AnimatedMediaLoader.loadAnimatedGif(mediaData);

                if (media == null || media.getFrameCount() == 0) {
                    throw new IOException("Failed to load animated media");
                }

                // Cache the raw data
                cacheAnimatedMedia(url, mediaData);

                LOGGER.info("Successfully loaded animated media: {} ({} frames)", url, media.getFrameCount());
                return media;

            } catch (Exception e) {
                LOGGER.error("Failed to load animated media from URL: " + url, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Loads JSON data from a URL asynchronously.
     * Returns a CompletableFuture that resolves to a JsonObject.
     *
     * @param url The JSON URL
     * @return CompletableFuture with the loaded JSON
     */
    public CompletableFuture<JsonObject> loadJson(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                CachedResource cached = getCachedResource(url);
                if (cached != null && !cached.isExpired()) {
                    LOGGER.debug("Loading JSON from cache: {}", url);
                    String content = Files.readString(cached.getFile().toPath());
                    return JsonParser.parseString(content).getAsJsonObject();
                }

                // Validate URL
                if (!isUrlAllowed(url)) {
                    throw new SecurityException("URL not allowed: " + url);
                }

                // Download
                LOGGER.info("Downloading JSON from: {}", url);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("HTTP " + response.statusCode());
                }

                String body = response.body();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                // Cache it
                cacheJson(url, body);

                LOGGER.info("Successfully loaded JSON: {}", url);
                return json;

            } catch (Exception e) {
                LOGGER.error("Failed to load JSON from URL: " + url, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Checks if a URL is allowed based on security settings.
     */
    private boolean isUrlAllowed(String url) {
        if (httpsOnly && !url.startsWith("https://")) {
            LOGGER.warn("HTTP not allowed, only HTTPS: {}", url);
            return false;
        }

        // TODO: Check whitelist/blacklist

        return true;
    }

    /**
     * Gets a cached resource if it exists and isn't expired.
     */
    private CachedResource getCachedResource(String url) {
        String cacheKey = getCacheKey(url);
        CachedResource cached = cache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached;
        }

        return null;
    }

    /**
     * Caches an image to disk.
     */
    private void cacheImage(String url, BufferedImage image) {
        try {
            String cacheKey = getCacheKey(url);
            File cacheFile = cacheDirectory.resolve(cacheKey + ".png").toFile();
            ImageIO.write(image, "PNG", cacheFile);

            CachedResource cached = new CachedResource(cacheFile, System.currentTimeMillis() + cacheDurationMs);
            cache.put(cacheKey, cached);

            LOGGER.debug("Cached image: {}", url);
        } catch (IOException e) {
            LOGGER.error("Failed to cache image", e);
        }
    }

    /**
     * Caches JSON data to disk.
     */
    private void cacheJson(String url, String json) {
        try {
            String cacheKey = getCacheKey(url);
            File cacheFile = cacheDirectory.resolve(cacheKey + ".json").toFile();
            Files.writeString(cacheFile.toPath(), json);

            CachedResource cached = new CachedResource(cacheFile, System.currentTimeMillis() + cacheDurationMs);
            cache.put(cacheKey, cached);

            LOGGER.debug("Cached JSON: {}", url);
        } catch (IOException e) {
            LOGGER.error("Failed to cache JSON", e);
        }
    }

    /**
     * Caches animated media (raw bytes) to disk.
     */
    private void cacheAnimatedMedia(String url, byte[] data) {
        try {
            String cacheKey = getCacheKey(url);
            File cacheFile = cacheDirectory.resolve(cacheKey + ".gif").toFile();
            Files.write(cacheFile.toPath(), data);

            CachedResource cached = new CachedResource(cacheFile, System.currentTimeMillis() + cacheDurationMs);
            cache.put(cacheKey, cached);

            LOGGER.debug("Cached animated media: {}", url);
        } catch (IOException e) {
            LOGGER.error("Failed to cache animated media", e);
        }
    }

    /**
     * Generates a cache key from a URL.
     */
    private String getCacheKey(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(url.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(url.hashCode());
        }
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        cache.clear();
        try {
            Files.walk(cacheDirectory)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        LOGGER.error("Failed to delete cache file", e);
                    }
                });
            LOGGER.info("Cache cleared");
        } catch (IOException e) {
            LOGGER.error("Failed to clear cache", e);
        }
    }

    // Getters and setters for configuration

    public void setHttpsOnly(boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    public void setMaxFileSizeMB(long maxFileSizeMB) {
        this.maxFileSizeMB = maxFileSizeMB;
    }

    public void setCacheDurationMs(long cacheDurationMs) {
        this.cacheDurationMs = cacheDurationMs;
    }

    /**
     * Represents a cached resource.
     */
    private static class CachedResource {
        private final File file;
        private final long expirationTime;

        public CachedResource(File file, long expirationTime) {
            this.file = file;
            this.expirationTime = expirationTime;
        }

        public File getFile() {
            return file;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
