package br.edu.fiap.marketplace.entity;

import br.edu.fiap.marketplace.exception.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrinhoTest {

    @Test
    @DisplayName("Deve calcular o total multiplicando o preço do produto pela quantidade")
    void deveCalcularTotalCorretamente() {
        Usuario usuario = new Usuario("Mariana", "mariana@example.com", "hash123");
        CatalogoProduto produto = new CatalogoProduto("Mouse", "Mouse óptico", new BigDecimal("150.00"), 20, true);
        Carrinho carrinho = new Carrinho(usuario, produto, 3);

        BigDecimal total = carrinho.calcularTotal();

        assertThat(total).isEqualTo(new BigDecimal("450.00"));
    }

    @Test
    @DisplayName("Deve impedir alteração de quantidade em carrinho finalizado")
    void deveImpedirAlteracaoEmCarrinhoFinalizado() {
        Usuario usuario = new Usuario("Mariana", "mariana@example.com", "hash123");
        CatalogoProduto produto = new CatalogoProduto("Mouse", "Mouse óptico", new BigDecimal("150.00"), 20, true);
        Carrinho carrinho = new Carrinho(usuario, produto, 3);

        carrinho.finalizar();

        assertThatThrownBy(() -> carrinho.alterarQuantidade(5))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Apenas carrinho ABERTO pode alterar quantidade ou ser cancelado.");
    }
}
