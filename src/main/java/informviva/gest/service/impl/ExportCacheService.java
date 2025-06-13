// ===================================================================
// SERVICIO DE CACHE - ExportCacheService.java
// ===================================================================

package informviva.gest.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de cache para optimizar exportaciones frecuentes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ExportCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ExportCacheService.class);

    @Value("${exportacion.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${exportacion.cache.ttl-minutes:60}")
    private int cacheTtlMinutes;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Obtiene estimaciones desde cache o las calcula
     */
    @Cacheable(value = "export-estimations", key = "#tipo + '_' + #formato + '_' + #fechaInicio + '_' + #fechaFin")
    public Map<String, Object> obtenerEstimacionesConCache(String tipo, String formato,
                                                           LocalDate fechaInicio, LocalDate fechaFin,
                                                           Map<String, Object> estimaciones) {
        logger.debug("Calculando estimaciones para cache: {} - {}", tipo, formato);
        return estimaciones;
    }

    /**
     * Almacena datos temporalmente para exportaciones grandes
     */
    public void almacenarDatosTemporales(String clave, Object datos) {
        if (!cacheEnabled) return;

        try {
            CacheEntry entry = new CacheEntry(datos, System.currentTimeMillis() + (cacheTtlMinutes * 60 * 1000));
            cache.put(clave, entry);
            logger.debug("Datos almacenados en cache temporal: {}", clave);
        } catch (Exception e) {
            logger.warn("Error almacenando en cache temporal: {}", e.getMessage());
        }
    }

    /**
     * Recupera datos temporales del cache
     */
    public Object recuperarDatosTemporales(String clave) {
        if (!cacheEnabled) return null;

        try {
            CacheEntry entry = cache.get(clave);
            if (entry != null && entry.expirationTime > System.currentTimeMillis()) {
                logger.debug("Datos recuperados del cache temporal: {}", clave);
                return entry.data;
            } else if (entry != null) {
                cache.remove(clave);
                logger.debug("Entrada de cache expirada removida: {}", clave);
            }
        } catch (Exception e) {
            logger.warn("Error recuperando del cache temporal: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Limpia el cache temporal
     */
    @CacheEvict(value = "export-estimations", allEntries = true)
    public void limpiarCacheEstimaciones() {
        logger.info("Cache de estimaciones limpiado");
    }

    /**
     * Limpia entradas expiradas del cache temporal
     */
    public void limpiarCacheExpirado() {
        long now = System.currentTimeMillis();
        int removidas = 0;

        cache.entrySet().removeIf(entry -> {
            if (entry.getValue().expirationTime <= now) {
                return true;
            }
            return false;
        });

        if (removidas > 0) {
            logger.info("Limpiadas {} entradas expiradas del cache temporal", removidas);
        }
    }

    private static class CacheEntry {
        final Object data;
        final long expirationTime;

        CacheEntry(Object data, long expirationTime) {
            this.data = data;
            this.expirationTime = expirationTime;
        }
    }
}