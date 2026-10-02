package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.CatalogoProdutoRequest;
import br.edu.fiap.marketplace.dto.CatalogoProdutoResponse;
import br.edu.fiap.marketplace.entity.CatalogoProduto;
import br.edu.fiap.marketplace.exception.ConflitoNegocioException;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.repository.CatalogoProdutoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gerencia as operações de produtos no catálogo do marketplace. */
@Service
public class CatalogoProdutoService {

    private final CatalogoProdutoRepository catalogoProdutoRepository;

    public CatalogoProdutoService(CatalogoProdutoRepository catalogoProdutoRepository) {
        this.catalogoProdutoRepository = catalogoProdutoRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogoProdutoResponse> listarTodos() {
        return catalogoProdutoRepository.findAll().stream()
                .map(CatalogoProdutoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogoProdutoResponse buscarPorId(Long id) {
        CatalogoProduto produto = catalogoProdutoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + id));
        return CatalogoProdutoResponse.de(produto);
    }

    @Transactional
    public CatalogoProdutoResponse cadastrar(CatalogoProdutoRequest request) {
        String nomeTrim = request.nome().trim();
        if (catalogoProdutoRepository.existsByNomeIgnoreCase(nomeTrim)) {
            throw new ConflitoNegocioException("Produto com este nome já cadastrado.");
        }

        CatalogoProduto produto = new CatalogoProduto(
                nomeTrim,
                request.descricao().trim(),
                request.preco(),
                request.estoque(),
                request.ativo());

        CatalogoProduto salvo = catalogoProdutoRepository.save(produto);
        return CatalogoProdutoResponse.de(salvo);
    }

    @Transactional
    public CatalogoProdutoResponse atualizar(Long id, CatalogoProdutoRequest request) {
        CatalogoProduto produto = catalogoProdutoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + id));

        String nomeTrim = request.nome().trim();
        if (!produto.getNome().equalsIgnoreCase(nomeTrim) && catalogoProdutoRepository.existsByNomeIgnoreCase(nomeTrim)) {
            throw new ConflitoNegocioException("Produto com este nome já existe.");
        }

        produto.alterarPreco(request.preco());
        produto.reporEstoque(request.estoque() - produto.getEstoque() > 0 ? request.estoque() - produto.getEstoque() : 0);
        // Direct price and state update
        if (request.ativo()) {
            produto.ativar();
        } else {
            produto.desativar();
        }

        CatalogoProduto salvo = catalogoProdutoRepository.save(produto);
        return CatalogoProdutoResponse.de(salvo);
    }

    @Transactional
    public CatalogoProdutoResponse reporEstoque(Long id, int quantidade) {
        CatalogoProduto produto = catalogoProdutoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + id));

        produto.reporEstoque(quantidade);
        CatalogoProduto salvo = catalogoProdutoRepository.save(produto);
        return CatalogoProdutoResponse.de(salvo);
    }
}
