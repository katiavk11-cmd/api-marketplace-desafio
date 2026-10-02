package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.CarrinhoRequest;
import br.edu.fiap.marketplace.dto.CarrinhoResponse;
import br.edu.fiap.marketplace.entity.Carrinho;
import br.edu.fiap.marketplace.entity.CatalogoProduto;
import br.edu.fiap.marketplace.entity.Usuario;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.exception.RegraNegocioException;
import br.edu.fiap.marketplace.repository.CarrinhoRepository;
import br.edu.fiap.marketplace.repository.CatalogoProdutoRepository;
import br.edu.fiap.marketplace.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordena as operações sobre carrinhos de compras. */
@Service
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoProdutoRepository catalogoProdutoRepository;

    public CarrinhoService(
            CarrinhoRepository carrinhoRepository,
            UsuarioRepository usuarioRepository,
            CatalogoProdutoRepository catalogoProdutoRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoProdutoRepository = catalogoProdutoRepository;
    }

    @Transactional
    public CarrinhoResponse criar(CarrinhoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + request.usuarioId()));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuário inativo não pode criar carrinho.");
        }

        CatalogoProduto produto = catalogoProdutoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + request.produtoId()));

        if (!produto.isAtivo()) {
            throw new RegraNegocioException("Produto inativo não pode ser adicionado ao carrinho.");
        }

        if (request.quantidade() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }

        if (produto.getEstoque() < request.quantidade()) {
            throw new RegraNegocioException("Estoque insuficiente para adicionar ao carrinho.");
        }

        Carrinho carrinho = new Carrinho(usuario, produto, request.quantidade());
        Carrinho salvo = carrinhoRepository.save(carrinho);
        return CarrinhoResponse.de(salvo);
    }

    @Transactional
    public CarrinhoResponse atualizarQuantidade(Long id, int novaQuantidade) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado com ID: " + id));

        carrinho.alterarQuantidade(novaQuantidade);
        Carrinho salvo = carrinhoRepository.save(carrinho);
        return CarrinhoResponse.de(salvo);
    }

    @Transactional
    public CarrinhoResponse cancelar(Long id) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado com ID: " + id));

        carrinho.cancelar();
        Carrinho salvo = carrinhoRepository.save(carrinho);
        return CarrinhoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public CarrinhoResponse buscarPorId(Long id) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado com ID: " + id));
        return CarrinhoResponse.de(carrinho);
    }

    @Transactional(readOnly = true)
    public List<CarrinhoResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + usuarioId);
        }
        return carrinhoRepository.findByUsuarioId(usuarioId).stream()
                .map(CarrinhoResponse::de)
                .toList();
    }
}
