package br.edu.fiap.marketplace.repository;

import br.edu.fiap.marketplace.entity.CatalogoProduto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoProdutoRepository extends JpaRepository<CatalogoProduto, Long> {

    Optional<CatalogoProduto> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
