package informviva.gest.config;

import informviva.gest.model.Usuario;
import informviva.gest.repository.RepositorioUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class InicializadorUsuarios {
    private static final Logger logger = LoggerFactory.getLogger(InicializadorUsuarios.class);

    @Bean
    CommandLineRunner inicializarUsuarios(RepositorioUsuario repositorioUsuario, BCryptPasswordEncoder codificador) {
        return args -> {
            if (!repositorioUsuario.findByUsername("admin").isPresent()) {
                // Se utiliza el constructor existente que recibe username y password.
                Usuario admin = new Usuario("admin", codificador.encode("admin123"));
                // Se completan los campos obligatorios del modelo
                admin.setNombre("Administrador");
                admin.setApellido("General");
                admin.setEmail("admin@example.com");
                // Se asigna el rol de administrador
                Set<String> roles = new HashSet<>();
                roles.add("ADMIN");
                admin.setRoles(roles);
                // Se marca el usuario como activo
                admin.setActivo(true);
                repositorioUsuario.save(admin);
                logger.info("Usuario administrador creado: admin/admin123");
            } else {
                logger.info("El usuario administrador ya existe.");
            }
        };
    }
}