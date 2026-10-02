package br.edu.fiap.marketplace.controller;

import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoRequest;
import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoResponse;
import br.edu.fiap.marketplace.service.ConfirmacaoPagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class ConfirmacaoPagamentoController {

    private final ConfirmacaoPagamentoService pagamentoService;

    public ConfirmacaoPagamentoController(ConfirmacaoPagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    @Operation(summary = "Cria uma nova confirmação de pagamento pendente para um carrinho")
    @ApiResponse(responseCode = "201", description = "Confirmação de pagamento criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Carrinho ou usuário não encontrado")
    @ApiResponse(responseCode = "409", description = "Carrinho já possui confirmação ou ID de pagamento duplicado")
    public ResponseEntity<ConfirmacaoPagamentoResponse> criar(@Valid @RequestBody ConfirmacaoPagamentoRequest request) {
        ConfirmacaoPagamentoResponse response = pagamentoService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de uma confirmação de pagamento pelo ID")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public ResponseEntity<ConfirmacaoPagamentoResponse> buscarPorId(@PathVariable Long id) {
        ConfirmacaoPagamentoResponse response = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Aprova um pagamento pendente, reduz o estoque do produto e finaliza o carrinho (transacional)")
    @ApiResponse(responseCode = "200", description = "Pagamento aprovado, estoque reduzido e carrinho finalizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Estoque insuficiente ou pagamento já processado")
    public ResponseEntity<ConfirmacaoPagamentoResponse> aprovar(@PathVariable Long id) {
        ConfirmacaoPagamentoResponse response = pagamentoService.aprovar(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/recusar")
    @Operation(summary = "Recusa um pagamento pendente")
    @ApiResponse(responseCode = "200", description = "Pagamento recusado com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Pagamento já processado")
    public ResponseEntity<ConfirmacaoPagamentoResponse> recusar(@PathVariable Long id) {
        ConfirmacaoPagamentoResponse response = pagamentoService.recusar(id);
        return ResponseEntity.ok(response);
    }
}
