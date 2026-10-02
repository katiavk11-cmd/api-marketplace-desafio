package br.edu.fiap.marketplace.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException(String mensagem) {
        super(mensagem);
    }
}
