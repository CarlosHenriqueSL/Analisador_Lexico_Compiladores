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

    private void retroceder() {
        if (posicao > 0) {
            posicao--;
            coluna--;
        }
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();

        while (posicao < codigoFonte.length()) {
            char c = lerCaractere();
            if (Character.isWhitespace(c)) continue;

            if (c == '/' && posicao < codigoFonte.length() && codigoFonte.charAt(posicao) == '*') {
                extrairComentarios(tokens);
                continue;
            }

            int colInicial = coluna - 1;

            if (Character.isLetter(c)) {
                extrairIdentificador(c, tokens, colInicial);
                continue;
            }
            if (Character.isDigit(c)) {
                extrairNumeros(c, tokens, colInicial);
                continue;
            }
            if (c == '"') {
                extrairString(tokens, colInicial);
                continue;
            }
            if (c == '\'') {
                extrairChar(tokens, colInicial);
                continue;
            }
            if (c == '.') {
                tokens.add(new Token(".", Tokens.FIM, linha, colInicial));
                continue;
            }
            if (c == ':') {
                if (posicao < codigoFonte.length() && codigoFonte.charAt(posicao) == '=') {
                    lerCaractere();
                    tokens.add(new Token(":=", Tokens.ATRIBUICAO, linha, colInicial));
                } else {
                    tokens.add(new Token(":", Tokens.SIMBOLO_ESPECIAL, linha, colInicial));
                }
                continue;
            }
            if (c == '<' || c == '>' || c == '=') {
                extrairOperadorRelacional(c, tokens, colInicial);
                continue;
            }
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                tokens.add(new Token(String.valueOf(c), Tokens.OPERADOR_ARITMETICO, linha, colInicial));
                continue;
            }
            if (c == '(' || c == ')' || c == ',' || c == ';') {
                tokens.add(new Token(String.valueOf(c), Tokens.SIMBOLO_ESPECIAL, linha, colInicial));
                continue;
            }
            tokens.add(new Token(String.valueOf(c), Tokens.DESCONHECIDO, linha, colInicial));
        }
        return tokens;
    }

    private void extrairIdentificador(char c, List<Token> tokens, int colInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        while(posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            if (Character.isLetterOrDigit(prox) || prox == '_') {
                stringBuilder.append(prox);
            } else {
                retroceder();
                break;
            }
        }
        String lexema = stringBuilder.toString();
        String lexemaUpper = lexema.toUpperCase();

        if (OPERADORES_LOGICOS.contains(lexemaUpper)) {
            tokens.add(new Token(lexema, Tokens.OPERADOR_LOGICO, linha, colInicial));
        } else if (lexemaUpper.equals("MOD")) {
            tokens.add(new Token(lexema, Tokens.OPERADOR_ARITMETICO, linha, colInicial));
        } else {
            Token tokenNaTabela = tabela.buscar(lexemaUpper);
            if (tokenNaTabela != null && tokenNaTabela.getTipo() == Tokens.PALAVRA_RESERVADA) {
                tokens.add(new Token(lexema, Tokens.PALAVRA_RESERVADA, linha, colInicial));
            } else {
                Token novoToken = new Token(lexema, Tokens.IDENTIFICADOR, linha, colInicial);
                tabela.inserir(lexemaUpper, novoToken);
                tokens.add(novoToken);
            }
        }
    }

    private void extrairString(List<Token> tokens, int colInicial) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        boolean fechou = false;

        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            stringBuilder.append(prox);
            if (prox == '"') {
                fechou = true;
                break;
            }
        }
        if (fechou) {
            tokens.add(new Token(stringBuilder.toString(), Tokens.CONSTANTE_STRING, linha, colInicial));
        } else {
            tokens.add(new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linha, colInicial));
        }
    }

    private void extrairChar(List<Token> tokens, int colInicial) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\'');
        boolean fechou = false;

        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            stringBuilder.append(prox);
            if (prox == '\'') {
                fechou = true;
                break;
            }
        }
        if (fechou) {
            tokens.add(new Token(stringBuilder.toString(), Tokens.CONSTANTE_CHAR, linha, colInicial));
        } else {
            tokens.add(new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linha, colInicial));
        }
    }

    private void extrairOperadorRelacional(char c, List<Token> tokens, int colInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        if (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            if ((c == '<' && (prox == '=' || prox == '>')) ||  (c == '>' && prox == '=')) {
                stringBuilder.append(prox);
            } else {
                retroceder();
            }
        }

        String lexema = stringBuilder.toString();
        tokens.add(new Token(lexema, Tokens.OPERADOR_RELACIONAL, linha, colInicial));
    }

    private void extrairNumeros(char c, List<Token> tokens, int colInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        boolean isReal = false;

        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            if (Character.isDigit(prox)) {
                stringBuilder.append(prox);
            } else if (prox == '.' && !isReal) {
                isReal = true;
                stringBuilder.append(prox);
            } else if ((prox == 'e' || prox == 'E') && !isReal) {
                isReal = true;
                stringBuilder.append(prox);
                char nextOpcional = lerCaractere();

                if (nextOpcional == '+' || nextOpcional == '-') {
                    stringBuilder.append(nextOpcional);
                } else {
                    retroceder();
                }
            } else {
                retroceder();
                break;
            }
        }

        String lexema = stringBuilder.toString();
        if (!isReal) {
            tokens.add(new Token(lexema, Tokens.NUMERO_INTEIRO, linha, colInicial));
        } else {
            tokens.add(new Token(lexema, Tokens.NUMERO_REAL, linha, colInicial));
        }
    }

    private static StringBuilder getStringBuilder(char c) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(c);
        return stringBuilder;
    }

    private void extrairComentarios(List<Token> tokens) {
        lerCaractere();
        boolean comentarioFechado = false;

        while (posicao < codigoFonte.length()) {
            char atual = lerCaractere();
            if (atual == '*' && posicao < codigoFonte.length() && codigoFonte.charAt(posicao) == '/') {
                lerCaractere();
                comentarioFechado = true;
                break;
            }
        }

        if (!comentarioFechado) {
            tokens.add(new Token("/*", Tokens.DESCONHECIDO, linha, coluna));
        }
    }
}
