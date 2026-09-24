package analisadorlexico;

import java.util.Objects;

public class Token {
    private String lexema;
    private Tokens tipo;
    private int linha;
    private int coluna;

    public Token(String lexema, Tokens tipo, int linha, int coluna) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.linha = linha;
        this.coluna = coluna;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public Tokens getTipo() {
        return tipo;
    }

    public void setTipo(Tokens tipo) {
        this.tipo = tipo;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

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
