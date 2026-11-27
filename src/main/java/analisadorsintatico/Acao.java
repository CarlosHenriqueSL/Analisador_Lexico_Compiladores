package analisadorsintatico;

/**
 * Uma célula da tabela ACTION.
 *
 * @param tipo  'S' = shift (empilhar), 'R' = reduce (reduzir), 'A' = accept (aceitar)
 * @param valor shift: estado de destino; reduce: número da regra; accept: ignorado
 */
public record Acao(char tipo, int valor) {

    /** Valida o tipo na criação, para que um typo (ex.: 's' minúsculo) falhe cedo e não vire erro silencioso. */
    public Acao {
        if (tipo != 'S' && tipo != 'R' && tipo != 'A') {
            throw new IllegalArgumentException("tipo de ação inválido: '" + tipo + "' (use 'S', 'R' ou 'A')");
        }
    }

    @Override
    public String toString() {
        return switch (tipo) {
            case 'A' -> "acc";
            case 'S' -> "s" + valor;
            default -> "r" + valor;
        };
    }
}
