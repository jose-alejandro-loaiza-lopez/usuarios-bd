package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.PrecioHistorico;

import java.util.List;

public interface IPrecioHistoricoService {
    List<PrecioHistorico> getPriceHistory(String productId);
    PrecioHistorico addPrice(String productId, Double precio);
}
