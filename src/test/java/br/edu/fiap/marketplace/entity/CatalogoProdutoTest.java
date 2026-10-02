package br.edu.fiap.marketplace.entity;

import br.edu.fiap.marketplace.exception.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogoProdutoTest {

    @Test
    @DisplayName("Deve lançar exceção e manter saldo inalterado ao tentar baixar quantidade superior ao estoque")
    void deveLancarExcecaoEManterEstoqueQuandoBaixaForMaiorQueSaldo() {
        int estoqueInicial = 10;
        CatalogoProduto produto = new CatalogoProduto(
                "Teclado Mecânico",
                "Teclado ABNT2 com iluminação RGB",
                new BigDecimal("299.90"),
                estoqueInicial,
                true);

        int quantidadeInvalida = 15;

        assertThatThrownBy(() -> produto.baixarEstoque(quantidadeInvalida))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Quantidade de baixa inválida ou maior que o estoque disponível.");

        assertThat(produto.getEstoque()).isEqualTo(estoqueInicial);
    }
}
