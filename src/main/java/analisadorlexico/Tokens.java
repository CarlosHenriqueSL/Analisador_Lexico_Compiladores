package analisadorlexico;

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

    Tokens(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}