package co.uceva.usuariosservice.domain.repository;

import co.uceva.usuariosservice.domain.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Interface que hereda de JpaRepository para realizar las
 * operaciones de CRUD paginacion y ordenamiento sobre la entidad Usuarios
 */
public interface IUsuariosRepository extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByEmail(String email);
}