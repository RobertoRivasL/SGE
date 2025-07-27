package informviva.gest.seguridad;

/*
 * @author Roberto Rivas
 * @version 2.2
 */

import informviva.gest.config.LoggingFilter;
import informviva.gest.service.ServicioUsuarioDetalle;
import informviva.gest.util.RolesConstantes;
import informviva.gest.util.RutasConstantes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de seguridad para la aplicación.
 * Define reglas de acceso, configuración de autenticación y otros aspectos de seguridad.
 *
 * @author Roberto Rivas
 * @version 2.2
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ConfiguracionSeguridad {

    private final ServicioUsuarioDetalle servicioUsuarioDetalle;
    private final LoggingFilter loggingFilter;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor con inyección de dependencias
     * Principio de Inversión de Dependencias: Recibe el passwordEncoder desde configuración externa
     */
    @Autowired
    public ConfiguracionSeguridad(ServicioUsuarioDetalle servicioUsuarioDetalle,
                                  LoggingFilter loggingFilter,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.servicioUsuarioDetalle = servicioUsuarioDetalle;
        this.loggingFilter = loggingFilter;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Configura el filtro de seguridad principal
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                // Configuración CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")) // Desactivar CSRF solo para APIs

                // Configuración de autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers(RutasConstantes.RUTAS_PUBLICAS).permitAll()
                        .requestMatchers(RutasConstantes.RUTAS_ESTATICAS).permitAll()

                        // Rutas de API (requieren autorización)
                        .requestMatchers(RutasConstantes.API_AUTH).permitAll()
                        .requestMatchers(RutasConstantes.API_PRODUCTOS).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.PRODUCTOS, RolesConstantes.VENTAS)
                        .requestMatchers(RutasConstantes.API_VENTAS).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.VENTAS)
                        .requestMatchers(RutasConstantes.API_CLIENTES).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.VENTAS)
                        .requestMatchers(RutasConstantes.API_REPORTES).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.GERENTE)
                        .requestMatchers("/exportacion/**").hasAnyRole("ADMIN", "GERENTE", "VENTAS")
                        .requestMatchers("/exportacion/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/exportacion/api/clientes/**").hasAnyRole("ADMIN", "GERENTE", "VENTAS")
                        .requestMatchers("/exportacion/api/productos/**").hasAnyRole("ADMIN", "GERENTE", "PRODUCTOS", "VENTAS")
                        .requestMatchers("/exportacion/api/ventas/**").hasAnyRole("ADMIN", "GERENTE", "VENTAS")
                        .requestMatchers("/exportacion/api/reportes/**").hasAnyRole("ADMIN", "GERENTE")

                        // Rutas de respaldos (solo para administradores)
                        .requestMatchers("/admin/respaldos/**").hasRole(RolesConstantes.ADMIN)

                        // Rutas del panel de administración
                        .requestMatchers(RutasConstantes.PANEL).hasRole(RolesConstantes.ADMIN)
                        .requestMatchers(RutasConstantes.USUARIOS).hasRole(RolesConstantes.ADMIN)
                        .requestMatchers(RutasConstantes.ADMIN).hasRole(RolesConstantes.ADMIN)

                        // Rutas funcionales por módulo
                        .requestMatchers(RutasConstantes.PRODUCTOS).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.PRODUCTOS)
                        .requestMatchers(RutasConstantes.VENTAS).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.VENTAS)
                        .requestMatchers(RutasConstantes.CLIENTES).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.VENTAS)
                        .requestMatchers(RutasConstantes.REPORTES).hasAnyRole(
                                RolesConstantes.ADMIN, RolesConstantes.GERENTE, RolesConstantes.VENTAS)

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated())

                // Configuración de formulario de login
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/inicio", true)  // Cambiar de "/panel" o "/admin/usuarios" a "/inicio"
                        .failureUrl("/login?error=true")
                        .permitAll())

                // Configuración de logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                // Configuración de recordar sesión
                .rememberMe(remember -> remember
                        .key("InformViv@SecureKey2025")
                        .tokenValiditySeconds(86400) // 24 horas
                        .userDetailsService(servicioUsuarioDetalle))

                // Configuración de manejo de sesiones
                .sessionManagement(session -> session
                        .maximumSessions(2)                // Máximo 2 sesiones simultáneas
                        .expiredUrl("/login?expired=true") // Redirección cuando expira la sesión
                        .maxSessionsPreventsLogin(false))  // Permite nuevos logins, expirando el más antiguo

                // Agregar filtro de logging personalizado
                .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class);
        // @formatter:on

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(servicioUsuarioDetalle)
                .passwordEncoder(passwordEncoder); // Usa el campo inyectado (sin paréntesis)
        return authBuilder.build();
    }
}