package br.edu.fiap.marketplace.repository;

import br.edu.fiap.marketplace.entity.Carrinho;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    List<Carrinho> findByUsuarioId(Long usuarioId);
}
