package analisadorlexico;

import tabelasimbolos.TabelaSimbolos;
import java.util.*;

public class AnalisadorLexico {

    private final String codigoFonte;
    private int posicao;
    private int linha;
    private int coluna;
    private final TabelaSimbolos tabela;

    private static final Set<String> OPERADORES_LOGICOS = new HashSet<>(Arrays.asList("AND", "OR", "NOT"));

    public AnalisadorLexico(String codigoFonte, TabelaSimbolos tabelaSimbolos) {
        this.codigoFonte = codigoFonte;
        this.tabela = tabelaSimbolos;
        this.posicao = 0;
        this.linha = 1;
        this.coluna = 1;
    }

    private char lerCaractere() {
        if (posicao >= codigoFonte.length()) return '\0';
        char c = codigoFonte.charAt(posicao++);
        if (c == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }
        return c;
    }

    private char olharCaractere() {
        if (posicao >= codigoFonte.length()) return '\0';
        return codigoFonte.charAt(posicao);
    }

    /**
     * Estratégia de correção de erros: "Panic Mode" (Modo Pânico).
     * Símbolos desconhecidos ou malformados não interrompem o analisador.
     * Eles são englobados como DESCONHECIDO, retornados e o analisador avança para o próximo caractere.
     */
    public Token proximoToken() {
        while (posicao < codigoFonte.length()) {
            char c = olharCaractere();

            if (Character.isWhitespace(c)) {
                lerCaractere();
                continue;
            }

            int linhaInicial = linha;
            int colunaInicial = coluna;
            c = lerCaractere();

            if (c == '/' && olharCaractere() == '*') {
                Token tComentario = extrairComentarios(linhaInicial, colunaInicial);
                if (tComentario != null) {
                    return tComentario;
                }
                continue;
            }

            if (Character.isLetter(c)) {
                return extrairIdentificador(c, linhaInicial, colunaInicial);
            }
            if (Character.isDigit(c)) {
                return extrairNumeros(c, linhaInicial, colunaInicial);
            }
            if (c == '"') {
                return extrairString(linhaInicial, colunaInicial);
            }
            if (c == '\'') {
                return extrairChar(linhaInicial, colunaInicial);
            }
            if (c == '.') {
                return new Token(".", Tokens.FIM, linhaInicial, colunaInicial);
            }
            if (c == ':') {
                if (olharCaractere() == '=') {
                    lerCaractere();
                    return new Token(":=", Tokens.ATRIBUICAO, linhaInicial, colunaInicial);
                } else {
                    return new Token(":", Tokens.SIMBOLO_ESPECIAL, linhaInicial, colunaInicial);
                }
            }
            if (c == '<' || c == '>' || c == '=') {
                return extrairOperadorRelacional(c, linhaInicial, colunaInicial);
            }
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                return new Token(String.valueOf(c), Tokens.OPERADOR_ARITMETICO, linhaInicial, colunaInicial);
            }
            if (c == '(' || c == ')' || c == ',' || c == ';') {
                return new Token(String.valueOf(c), Tokens.SIMBOLO_ESPECIAL, linhaInicial, colunaInicial);
            }

            return new Token(String.valueOf(c), Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
        }
        return null;
    }

    /**
     * Método utilitário para a Etapa 1 (Gerar Arquivo).
     * Lê todos os tokens sequencialmente chamando o proximoToken().
     */
    public List<Token> analisarTudo() {
        List<Token> tokens = new ArrayList<>();
        Token t;
        while ((t = proximoToken()) != null) {
            tokens.add(t);
        }
        return tokens;
    }

    private Token extrairIdentificador(char c, int linhaInicial, int colunaInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        while (Character.isLetterOrDigit(olharCaractere()) || olharCaractere() == '_') {
            char prox = lerCaractere();
            stringBuilder.append(prox);
        }
        String lexema = stringBuilder.toString();
        String lexemaUpper = lexema.toUpperCase(Locale.ROOT);

        if (OPERADORES_LOGICOS.contains(lexemaUpper)) {
            return new Token(lexema, Tokens.OPERADOR_LOGICO, linhaInicial, colunaInicial);
        } else if (lexemaUpper.equals("MOD")) {
            return new Token(lexema, Tokens.OPERADOR_ARITMETICO, linhaInicial, colunaInicial);
        } else {
            Token tokenNaTabela = tabela.buscar(lexemaUpper);
            if (tokenNaTabela != null && tokenNaTabela.getTipo() == Tokens.PALAVRA_RESERVADA) {
                return new Token(lexema, Tokens.PALAVRA_RESERVADA, linhaInicial, colunaInicial);
            } else {
                Token novoToken = new Token(lexema, Tokens.IDENTIFICADOR, linhaInicial, colunaInicial);
                tabela.inserir(lexemaUpper, novoToken);
                return novoToken;
            }
        }
    }

    private Token extrairString(int linhaInicial, int colunaInicial) {
        StringBuilder stringBuilder = new StringBuilder("\"");
        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            stringBuilder.append(prox);
            if (prox == '"') {
                return new Token(stringBuilder.toString(), Tokens.CONSTANTE_STRING, linhaInicial, colunaInicial);
            }
            if (prox == '\n') {
                return new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
            }
        }
        return new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
    }

    private Token extrairChar(int linhaInicial, int colunaInicial) {
        StringBuilder stringBuilder = new StringBuilder("'");
        int quantidadeCaracteres = 0;
        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            stringBuilder.append(prox);
            if (prox == '\'') {
                Tokens tipo = quantidadeCaracteres == 1 ? Tokens.CONSTANTE_CHAR : Tokens.DESCONHECIDO;
                return new Token(stringBuilder.toString(), tipo, linhaInicial, colunaInicial);
            }
            if (prox == '\n') {
                return new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
            }
            quantidadeCaracteres++;
        }
        return new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
    }

    private Token extrairOperadorRelacional(char c, int linhaInicial, int colunaInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        char prox = olharCaractere();
        if ((c == '<' && (prox == '=' || prox == '>')) || (c == '>' && prox == '=')) {
            stringBuilder.append(lerCaractere());
        }
        return new Token(stringBuilder.toString(), Tokens.OPERADOR_RELACIONAL, linhaInicial, colunaInicial);
    }

    private Token extrairNumeros(char c, int linhaInicial, int colunaInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        boolean real = false;

        while (Character.isDigit(olharCaractere())) {
            stringBuilder.append(lerCaractere());
        }

        if (olharCaractere() == '.' && Character.isDigit(caractereSeguinte())) {
            real = true;
            stringBuilder.append(lerCaractere());
            while (Character.isDigit(olharCaractere())) {
                stringBuilder.append(lerCaractere());
            }
        }

        if (olharCaractere() == 'e' || olharCaractere() == 'E') {
            stringBuilder.append(lerCaractere());
            if (olharCaractere() == '+' || olharCaractere() == '-') {
                stringBuilder.append(lerCaractere());
            }
            int digitosExpoente = 0;
            while (Character.isDigit(olharCaractere())) {
                stringBuilder.append(lerCaractere());
                digitosExpoente++;
            }
            if (digitosExpoente == 0) {
                while (posicao < codigoFonte.length() && Character.isLetterOrDigit(olharCaractere())) {
                    stringBuilder.append(lerCaractere());
                }
                return new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
            }
            real = true;
        }

        return new Token(stringBuilder.toString(), real ? Tokens.NUMERO_REAL : Tokens.NUMERO_INTEIRO, linhaInicial, colunaInicial);
    }

    private char caractereSeguinte() {
        int indiceSeguinte = posicao + 1;
        if (indiceSeguinte >= codigoFonte.length()) return '\0';
        return codigoFonte.charAt(indiceSeguinte);
    }

    private static StringBuilder getStringBuilder(char c) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(c);
        return stringBuilder;
    }

    private Token extrairComentarios(int linhaInicial, int colunaInicial) {
        lerCaractere();
        while (posicao < codigoFonte.length()) {
            char atual = lerCaractere();
            if (atual == '*' && olharCaractere() == '/') {
                lerCaractere();
                return null;
            }
        }
        return new Token("/*", Tokens.DESCONHECIDO, linhaInicial, colunaInicial);
    }
}