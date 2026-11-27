package compilador.dto;

/**
 * Representa uma entrada registrada na Tabela de Símbolos de Mini_Pascal.
 *
 * @param indice         Posição ou identificador interno na tabela.
 * @param lexema         Cadeia textual do símbolo (palavra reservada ou identificador).
 * @param categoria      Classificação do símbolo (ex.: "Palavra Reservada" ou "Identificador").
 * @param token          Token associado na gramática.
 * @param linhaDeclaracao Linha da primeira aparição no código-fonte (0 para palavras reservadas pré-carregadas).
 */
public record ItemSimboloDTO(
        int indice,
        String lexema,
        String categoria,
        String token,
        int linhaDeclaracao
) {
}