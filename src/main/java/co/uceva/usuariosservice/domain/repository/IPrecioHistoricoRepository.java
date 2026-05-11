package co.uceva.usuariosservice.domain.repository;

import co.uceva.usuariosservice.domain.model.PrecioHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPrecioHistoricoRepository extends JpaRepository<PrecioHistorico, Long> {
    List<PrecioHistorico> findByProductIdOrderByFechaGuardadoDesc(String productId);
}
