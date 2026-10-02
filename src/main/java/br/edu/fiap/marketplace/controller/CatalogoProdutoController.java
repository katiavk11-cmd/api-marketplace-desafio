package br.edu.fiap.marketplace.controller;

import br.edu.fiap.marketplace.dto.AtualizarQuantidadeRequest;
import br.edu.fiap.marketplace.dto.CatalogoProdutoRequest;
import br.edu.fiap.marketplace.dto.CatalogoProdutoResponse;
import br.edu.fiap.marketplace.service.CatalogoProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Catálogo")
public class CatalogoProdutoController {

    private final CatalogoProdutoService catalogoProdutoService;

    public CatalogoProdutoController(CatalogoProdutoService catalogoProdutoService) {
        this.catalogoProdutoService = catalogoProdutoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os produtos do catálogo (público)")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    public ResponseEntity<List<CatalogoProdutoResponse>> listarTodos() {
        List<CatalogoProdutoResponse> produtos = catalogoProdutoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo ID (público)")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<CatalogoProdutoResponse> buscarPorId(@PathVariable Long id) {
        CatalogoProdutoResponse produto = catalogoProdutoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo produto no catálogo (protegido)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "409", description = "Nome de produto já existente")
    public ResponseEntity<CatalogoProdutoResponse> cadastrar(@Valid @RequestBody CatalogoProdutoRequest request) {
        CatalogoProdutoResponse response = catalogoProdutoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um produto (protegido)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<CatalogoProdutoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CatalogoProdutoRequest request) {
        CatalogoProdutoResponse response = catalogoProdutoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estoque")
    @Operation(summary = "Repõe o estoque de um produto (protegido)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Quantidade de reposição inválida")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<CatalogoProdutoResponse> reporEstoque(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarQuantidadeRequest request) {
        CatalogoProdutoResponse response = catalogoProdutoService.reporEstoque(id, request.quantidade());
        return ResponseEntity.ok(response);
    }
}
