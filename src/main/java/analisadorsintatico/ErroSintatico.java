package analisadorsintatico;

public record ErroSintatico(Tipo tipo, int linha, int coluna, String mensagem) {
    public enum Tipo {
        ERRO_LEXICO("Erro léxico"),
        TOKEN_INESPERADO("Token inesperado"),
        FALTA_PONTO_VIRGULA("Ponto e vírgula ausente"),
        FALTA_PONTO_FINAL("Ponto final ausente"),
        BEGIN_END_DESBALANCEADO("begin/end desbalanceado"),
        EXPRESSAO_MALFORMADA("Expressão malformada"),
        FIM_DE_ARQUIVO_INESPERADO("Fim de arquivo inesperado"),
        CODIGO_APOS_FIM_DO_PROGRAMA("Código após o fim do programa");

        private final String descricao;

        Tipo(String descricao) { this.descricao = descricao; }

        public String getDescricao() { return descricao; }
    }

    @Override
    public String toString() {
        return "Linha " + linha + ", coluna " + coluna + ": [" + tipo.getDescricao() + "] " + mensagem;
    }
}
