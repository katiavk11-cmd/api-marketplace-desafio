package br.edu.fiap.marketplace.controller;

import br.edu.fiap.marketplace.dto.AtualizarQuantidadeRequest;
import br.edu.fiap.marketplace.dto.CarrinhoRequest;
import br.edu.fiap.marketplace.dto.CarrinhoResponse;
import br.edu.fiap.marketplace.service.CarrinhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/carrinhos")
@Tag(name = "Carrinhos")
@SecurityRequirement(name = "bearerAuth")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @PostMapping
    @Operation(summary = "Cria um novo carrinho aberto para o usuário com um produto")
    @ApiResponse(responseCode = "201", description = "Carrinho criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário ou produto não encontrado")
    @ApiResponse(responseCode = "409", description = "Produto inativo ou estoque insuficiente")
    public ResponseEntity<CarrinhoResponse> criar(@Valid @RequestBody CarrinhoRequest request) {
        CarrinhoResponse response = carrinhoService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de um carrinho pelo ID")
    @ApiResponse(responseCode = "200", description = "Carrinho encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Carrinho não encontrado")
    public ResponseEntity<CarrinhoResponse> buscarPorId(@PathVariable Long id) {
        CarrinhoResponse response = carrinhoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Lista todos os carrinhos de um determinado usuário")
    @ApiResponse(responseCode = "200", description = "Lista de carrinhos do usuário")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<List<CarrinhoResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<CarrinhoResponse> carrinhos = carrinhoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(carrinhos);
    }

    @PatchMapping("/{id}/quantidade")
    @Operation(summary = "Altera a quantidade de itens em um carrinho aberto")
    @ApiResponse(responseCode = "200", description = "Quantidade alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Quantidade inválida")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Carrinho não encontrado")
    @ApiResponse(responseCode = "409", description = "Carrinho fechado ou cancelado")
    public ResponseEntity<CarrinhoResponse> atualizarQuantidade(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarQuantidadeRequest request) {
        CarrinhoResponse response = carrinhoService.atualizarQuantidade(id, request.quantidade());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela um carrinho aberto")
    @ApiResponse(responseCode = "204", description = "Carrinho cancelado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Carrinho não encontrado")
    @ApiResponse(responseCode = "409", description = "Carrinho não está aberto")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        carrinhoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
