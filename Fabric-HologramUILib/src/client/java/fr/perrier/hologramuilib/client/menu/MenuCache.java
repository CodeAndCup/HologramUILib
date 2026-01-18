package fr.perrier.hologramuilib.client.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache for menu templates to avoid recreating menus from scratch.
 * Stores menu configurations and can quickly instantiate new menus.
 */
public class MenuCache {

    private final Map<String, MenuTemplate> templates;
    private final Map<String, Long> lastAccessTime;
    private final long cacheExpirationMs;

    public MenuCache(long cacheExpirationMs) {
        this.templates = new HashMap<>();
        this.lastAccessTime = new HashMap<>();
        this.cacheExpirationMs = cacheExpirationMs;
    }

    public MenuCache() {
        this(300000); // 5 minutes default
    }

    /**
     * Stores a menu template in the cache.
     */
    public void cacheTemplate(String templateId, MenuTemplate template) {
        templates.put(templateId, template);
        lastAccessTime.put(templateId, System.currentTimeMillis());
    }

    /**
     * Retrieves a menu template from the cache.
     */
    public Optional<MenuTemplate> getTemplate(String templateId) {
        MenuTemplate template = templates.get(templateId);
        if (template != null) {
            lastAccessTime.put(templateId, System.currentTimeMillis());
            return Optional.of(template);
        }
        return Optional.empty();
    }

    /**
     * Checks if a template exists in the cache and is not expired.
     */
    public boolean hasTemplate(String templateId) {
        if (!templates.containsKey(templateId)) {
            return false;
        }

        Long lastAccess = lastAccessTime.get(templateId);
        if (lastAccess == null) {
            return false;
        }

        long age = System.currentTimeMillis() - lastAccess;
        return age < cacheExpirationMs;
    }

    /**
     * Removes a template from the cache.
     */
    public void removeTemplate(String templateId) {
        templates.remove(templateId);
        lastAccessTime.remove(templateId);
    }

    /**
     * Clears all expired templates from the cache.
     */
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        lastAccessTime.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue()) > cacheExpirationMs;
            if (expired) {
                templates.remove(entry.getKey());
            }
            return expired;
        });
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        templates.clear();
        lastAccessTime.clear();
    }

    /**
     * Gets the number of cached templates.
     */
    public int size() {
        return templates.size();
    }

    /**
     * Represents a cached menu template that can be instantiated.
     */
    public static class MenuTemplate {
        private final String configJson;
        private final Map<String, Object> metadata;

        public MenuTemplate(String configJson) {
            this.configJson = configJson;
            this.metadata = new HashMap<>();
        }

        public String getConfigJson() {
            return configJson;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }

        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }
}
