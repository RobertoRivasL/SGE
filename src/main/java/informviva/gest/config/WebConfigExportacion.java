package informviva.gest.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web para registrar interceptores de exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Configuration
public class WebConfigExportacion implements WebMvcConfigurer {

    private final ExportacionInterceptor exportacionInterceptor;

    @Autowired
    public WebConfigExportacion(ExportacionInterceptor exportacionInterceptor) {
        this.exportacionInterceptor = exportacionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(exportacionInterceptor)
                .addPathPatterns(
                        "/reportes/clientes/exportar/**",
                        "/admin/usuarios/export/**",
                        "/**/exportar/**",
                        "/**/export/**",
                        "/**/*.pdf",
                        "/**/*.xlsx",
                        "/**/*.csv"
                )
                .excludePathPatterns(
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/api/auth/**"
                );
    }
}
