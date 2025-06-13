package informviva.gest.repository;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Rol findByNombre(String nombre);
}
