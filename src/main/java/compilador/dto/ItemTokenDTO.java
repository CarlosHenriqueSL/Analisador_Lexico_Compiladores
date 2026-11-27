package compilador.dto;

/**
 * Representa um par {@code <lexema, token>} reconhecido pelo Analisador Léxico,
 * acompanhado do número da linha em que ocorreu no código-fonte.
 *
 * @param linha  Número da linha (base 1) de ocorrência do token.
 * @param lexema Cadeia de caracteres lida do código-fonte.
 * @param token  Categoria ou nome simbólico do token reconhecido.
 */
public record ItemTokenDTO(int linha, String lexema, String token) {

    /**
     * Formata o item no padrão exigido pela especificação da I Unidade: {@code <lexema, token>}.
     *
     * @return Representação textual formatada do par léxico.
     */
    public String formatarPar() {
        return "<" + lexema + ", " + token + ">";
    }
}