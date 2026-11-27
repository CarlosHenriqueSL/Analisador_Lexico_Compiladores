package compilador.dto;

/**
 * Objeto agregador retornado pela Facade {@code Compilador} contendo os resultados
 * consolidados das etapas léxica e sintática SLR(1) para uma execução.
 *
 * @param resultadoLexico    Saída completa da análise léxica e tabela de símbolos.
 * @param resultadoSintatico Saída oficial da análise sintática SLR(1) ({@code "OK"} ou erros).
 */
public record ResultadoCompilacao(
        ResultadoLexico resultadoLexico,
        ResultadoSintatico resultadoSintatico
) {
}