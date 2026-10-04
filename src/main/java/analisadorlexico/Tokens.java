package analisadorlexico;

/**
 * Enumeração das categorias léxicas reconhecidas pelo compilador Mini Pascal.
 * Cada constante guarda uma descrição legível para apresentação na interface gráfica.
 */
public enum Tokens {
    PALAVRA_RESERVADA("Palavra reservada"),
    IDENTIFICADOR("Identificador"),
    NUMERO_INTEIRO("Número inteiro"),
    NUMERO_REAL("Número real"),
    OPERADOR_ARITMETICO("Operador aritmético"),
    OPERADOR_RELACIONAL("Operador relacional"),
    OPERADOR_LOGICO("Operador lógico"),
    SIMBOLO_ESPECIAL("Símbolo especial"),
    ATRIBUICAO("Atribuição"),
    FIM("Fim"),
    CONSTANTE_STRING("Constante string"),
    CONSTANTE_CHAR("Constante char"),
    DESCONHECIDO("Desconhecido");

    private final String descricao;

    /**
     * Cria uma categoria de token com sua descrição pública.
     *
     * @param descricao texto usado na exibição dos resultados
     */
    Tokens(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição textual da categoria do token.
     *
     * @return descrição legível do tipo léxico
     */
    public String getDescricao() {
        return descricao;
    }
}