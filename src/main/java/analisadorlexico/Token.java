package analisadorlexico;

import java.util.Objects;

/**
 * Representa um token identificado durante a análise léxica.
 * Cada instância guarda o lexema reconhecido, o tipo semântico do token
 * e a localização original no código-fonte para apoiar diagnósticos.
 */
public class Token {
    private String lexema;
    private Tokens tipo;
    private int linha;
    private int coluna;

    /**
     * Cria um novo token com o lexema, categoria e localização informados.
     *
     * @param lexema sequência de caracteres reconhecida pelo analisador
     * @param tipo categoria sintática do token
     * @param linha linha em que o token começa no código-fonte
     * @param coluna coluna em que o token começa no código-fonte
     */
    public Token(String lexema, Tokens tipo, int linha, int coluna) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.linha = linha;
        this.coluna = coluna;
    }

    /**
     * Retorna o texto reconhecido pelo token.
     *
     * @return lexema associado ao símbolo encontrado
     */
    public String getLexema() {
        return lexema;
    }

    /**
     * Atualiza o lexema do token.
     *
     * @param lexema novo texto do token
     */
    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    /**
     * Retorna a categoria do token.
     *
     * @return enumeração que descreve o tipo do token
     */
    public Tokens getTipo() {
        return tipo;
    }

    /**
     * Define a categoria do token.
     *
     * @param tipo nova categoria do token
     */
    public void setTipo(Tokens tipo) {
        this.tipo = tipo;
    }

    /**
     * Retorna a linha inicial em que o token aparece.
     *
     * @return número da linha do código-fonte
     */
    public int getLinha() {
        return linha;
    }

    /**
     * Define a linha inicial do token.
     *
     * @param linha nova linha de referência
     */
    public void setLinha(int linha) {
        this.linha = linha;
    }

    /**
     * Retorna a coluna inicial em que o token aparece.
     *
     * @return número da coluna do código-fonte
     */
    public int getColuna() {
        return coluna;
    }

    /**
     * Define a coluna inicial do token.
     *
     * @param coluna nova coluna de referência
     */
    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        return String.format("<%s, %s>", lexema, tipo.getDescricao());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Token token)) return false;
        return getLinha() == token.getLinha() && getColuna() == token.getColuna() && Objects.equals(getLexema(), token.getLexema()) && getTipo() == token.getTipo();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLexema(), getTipo(), getLinha(), getColuna());
    }
}
