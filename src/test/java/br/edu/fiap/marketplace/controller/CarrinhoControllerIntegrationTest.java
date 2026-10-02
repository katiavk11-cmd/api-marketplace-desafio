package br.edu.fiap.marketplace.controller;

import br.edu.fiap.marketplace.dto.CarrinhoRequest;
import br.edu.fiap.marketplace.dto.CarrinhoResponse;
import br.edu.fiap.marketplace.entity.StatusCarrinho;
import br.edu.fiap.marketplace.exception.GlobalExceptionHandler;
import br.edu.fiap.marketplace.service.CarrinhoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CarrinhoControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CarrinhoService carrinhoService;

    @InjectMocks
    private CarrinhoController carrinhoController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(carrinhoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve retornar status 201, header Location e JSON do carrinho para requisição válida")
    void deveRetornar201ELocationQuandoRequisicaoValida() throws Exception {
        CarrinhoRequest request = new CarrinhoRequest(1L, 3L, 2);
        CarrinhoResponse response = new CarrinhoResponse(
                10L, 1L, "Mariana Costa", 3L, "Teclado mecânico",
                new BigDecimal("299.90"), 2, new BigDecimal("599.80"),
                StatusCarrinho.ABERTO, Instant.now());

        when(carrinhoService.criar(any(CarrinhoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/carrinhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/carrinhos/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.produtoId").value(3))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.total").value(599.80))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request ao enviar JSON com dados inválidos")
    void deveRetornar400QuandoJsonInvalido() throws Exception {
        String jsonInvalido = """
                {
                    "usuarioId": 1,
                    "produtoId": 3,
                    "quantidade": 0
                }
                """;

        mockMvc.perform(post("/api/carrinhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos na requisição."));
    }
}
