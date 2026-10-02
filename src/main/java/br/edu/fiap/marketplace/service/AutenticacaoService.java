package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.LoginRequest;
import br.edu.fiap.marketplace.dto.TokenResponse;
import br.edu.fiap.marketplace.entity.Usuario;
import br.edu.fiap.marketplace.exception.CredenciaisInvalidasException;
import br.edu.fiap.marketplace.repository.UsuarioRepository;
import br.edu.fiap.marketplace.security.JwtService;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Localiza usuário, compara BCrypt e solicita a emissão do JWT. */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationSeconds;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationSeconds = expirationSeconds;
    }

    public TokenResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos."));

        if (!usuario.isAtivo()) {
            throw new CredenciaisInvalidasException("Usuário inativo não consegue autenticar.");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos.");
        }

        String token = jwtService.gerarToken(usuario);
        Instant expiracao = Instant.now().plusSeconds(expirationSeconds);

        return new TokenResponse(token, "Bearer", expirationSeconds, expiracao);
    }
}
