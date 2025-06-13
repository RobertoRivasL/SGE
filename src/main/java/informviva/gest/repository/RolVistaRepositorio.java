package informviva.gest.repository;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.RolVista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolVistaRepositorio extends JpaRepository<RolVista, Long> {
    List<RolVista> findTop10ByOrderByFechaVistaDesc();

    List<RolVista> findByRolNombreOrderByFechaVistaDesc(String rolNombre);
}
