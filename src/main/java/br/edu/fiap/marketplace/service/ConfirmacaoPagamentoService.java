package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoRequest;
import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoResponse;
import br.edu.fiap.marketplace.entity.Carrinho;
import br.edu.fiap.marketplace.entity.CatalogoProduto;
import br.edu.fiap.marketplace.entity.ConfirmacaoPagamento;
import br.edu.fiap.marketplace.entity.StatusCarrinho;
import br.edu.fiap.marketplace.entity.Usuario;
import br.edu.fiap.marketplace.exception.ConflitoNegocioException;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.exception.RegraNegocioException;
import br.edu.fiap.marketplace.repository.CarrinhoRepository;
import br.edu.fiap.marketplace.repository.CatalogoProdutoRepository;
import br.edu.fiap.marketplace.repository.ConfirmacaoPagamentoRepository;
import br.edu.fiap.marketplace.repository.UsuarioRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gerencia a criação e o processamento de confirmações de pagamento. */
@Service
public class ConfirmacaoPagamentoService {

    private final ConfirmacaoPagamentoRepository pagamentoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoProdutoRepository produtoRepository;

    public ConfirmacaoPagamentoService(
            ConfirmacaoPagamentoRepository pagamentoRepository,
            CarrinhoRepository carrinhoRepository,
            UsuarioRepository usuarioRepository,
            CatalogoProdutoRepository produtoRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ConfirmacaoPagamentoResponse criar(ConfirmacaoPagamentoRequest request) {
        Carrinho carrinho = carrinhoRepository.findById(request.carrinhoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado com ID: " + request.carrinhoId()));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + request.usuarioId()));

        if (!carrinho.getUsuario().getId().equals(usuario.getId())) {
            throw new RegraNegocioException("O usuário informado não é o dono do carrinho.");
        }

        if (carrinho.getStatus() != StatusCarrinho.ABERTO) {
            throw new RegraNegocioException("Apenas carrinhos ABERTO podem receber confirmação de pagamento.");
        }

        if (pagamentoRepository.findByCarrinhoId(carrinho.getId()).isPresent()) {
            throw new ConflitoNegocioException("Carrinho já possui uma confirmação de pagamento.");
        }

        String idPagamentoTrim = request.idPagamento().trim();
        if (pagamentoRepository.existsByIdPagamento(idPagamentoTrim)) {
            throw new ConflitoNegocioException("ID de pagamento já utilizado.");
        }

        BigDecimal valorPago = carrinho.calcularTotal();

        ConfirmacaoPagamento pagamento = new ConfirmacaoPagamento(carrinho, usuario, idPagamentoTrim, valorPago);
        ConfirmacaoPagamento salvo = pagamentoRepository.save(pagamento);
        return ConfirmacaoPagamentoResponse.de(salvo);
    }

    @Transactional
    public ConfirmacaoPagamentoResponse aprovar(Long id) {
        ConfirmacaoPagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Confirmação de pagamento não encontrada com ID: " + id));

        Carrinho carrinho = pagamento.getCarrinho();
        CatalogoProduto produto = carrinho.getProduto();

        // Tenta dar baixa no estoque. Se faltar saldo, baixaEstoque lança RegraNegocioException e a transação faz rollback integral.
        produto.baixarEstoque(carrinho.getQuantidade());
        produtoRepository.save(produto);

        pagamento.aprovar();
        carrinhoRepository.save(carrinho);
        ConfirmacaoPagamento salvo = pagamentoRepository.save(pagamento);

        return ConfirmacaoPagamentoResponse.de(salvo);
    }

    @Transactional
    public ConfirmacaoPagamentoResponse recusar(Long id) {
        ConfirmacaoPagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Confirmação de pagamento não encontrada com ID: " + id));

        pagamento.recusar();
        ConfirmacaoPagamento salvo = pagamentoRepository.save(pagamento);

        return ConfirmacaoPagamentoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public ConfirmacaoPagamentoResponse buscarPorId(Long id) {
        ConfirmacaoPagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Confirmação de pagamento não encontrada com ID: " + id));
        return ConfirmacaoPagamentoResponse.de(pagamento);
    }
}
