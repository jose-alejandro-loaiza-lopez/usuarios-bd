package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.PrecioHistorico;
import co.uceva.usuariosservice.domain.repository.IPrecioHistoricoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrecioHistoricoServiceImpl implements IPrecioHistoricoService {

    private final IPrecioHistoricoRepository precioHistoricoRepository;

    public PrecioHistoricoServiceImpl(IPrecioHistoricoRepository precioHistoricoRepository) {
        this.precioHistoricoRepository = precioHistoricoRepository;
    }

    @Override
    public List<PrecioHistorico> getPriceHistory(String productId) {
        return precioHistoricoRepository.findByProductIdOrderByFechaGuardadoDesc(productId);
    }

    @Override
    public PrecioHistorico addPrice(String productId, Double precio) {
        PrecioHistorico ph = new PrecioHistorico();
        ph.setProductId(productId);
        ph.setPrecio(precio);
        ph.setFechaGuardado(LocalDateTime.now());
        return precioHistoricoRepository.save(ph);
    }
}
