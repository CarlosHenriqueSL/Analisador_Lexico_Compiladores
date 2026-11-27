package analisadorsintatico;

/**
 * Uma célula da tabela ACTION.
 *
 * @param tipo  'S' = shift (empilhar), 'R' = reduce (reduzir), 'A' = accept (aceitar)
 * @param valor shift: estado de destino; reduce: número da regra; accept: ignorado
 */
public record Acao(char tipo, int valor) {
    @Override
    public String toString() { return tipo == 'A' ? "acc" : (tipo == 'S' ? "s" : "r") + valor; }
}