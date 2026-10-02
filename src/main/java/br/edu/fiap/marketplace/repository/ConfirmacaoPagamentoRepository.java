package br.edu.fiap.marketplace.repository;

import br.edu.fiap.marketplace.entity.ConfirmacaoPagamento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmacaoPagamentoRepository
        extends JpaRepository<ConfirmacaoPagamento, Long> {

    Optional<ConfirmacaoPagamento> findByCarrinhoId(Long carrinhoId);

    boolean existsByIdPagamento(String idPagamento);
}
