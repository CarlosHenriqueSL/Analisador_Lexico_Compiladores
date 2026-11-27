package compilador.dto;

/**
 * Representa um erro léxico ou sintático detectado durante a compilação,
 * contendo obrigatoriamente o tipo do erro e o número da linha de ocorrência.
 *
 * @param linha           Número da linha (base 1) onde o erro foi identificado.
 * @param tipoErro        Classificação do erro (ex.: "Erro Léxico", "Erro Sintático").
 * @param tokenEncontrado Lexema ou token que desencadeou a falha.
 * @param descricao       Mensagem explicativa detalhando o problema e/ou tokens esperados.
 */
public record ItemErroDTO(
        int linha,
        String tipoErro,
        String tokenEncontrado,
        String descricao
) {
    /**
     * Formata a mensagem de erro de forma clara e padronizada para exibição em console e exportação {@code .txt}.
     *
     * @return Linha de erro formatada.
     */
    public String formatarMensagem() {
        return String.format("[Linha %d] %s: %s", linha, tipoErro, descricao);
    }
}