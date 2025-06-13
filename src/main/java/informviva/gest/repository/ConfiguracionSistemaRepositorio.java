package informviva.gest.repository;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionSistemaRepositorio extends JpaRepository<ConfiguracionSistema, Long> {
    // Como solo habrá una configuración, podemos obtener la primera
    ConfiguracionSistema findFirstByOrderByIdAsc();
}
