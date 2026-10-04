package analisadorlexico;

import tabelasimbolos.TabelaSimbolos;
import java.util.*;

/**
 * Realiza a análise léxica do código-fonte Mini Pascal.
 * A classe percorre o texto fonte caractere por caractere, reconhece lexemas
 * e produz tokens com informações de linha, coluna e categoria.
 */
public class AnalisadorLexico {

    private final String codigoFonte;
    private int posicao;
    private int linha;
    private int coluna;
    private final TabelaSimbolos tabela;

    private static final Set<String> OPERADORES_LOGICOS = new HashSet<>(Arrays.asList("AND", "OR", "NOT"));

    /**
     * Cria um analisador léxico para um código-fonte específico.
     *
     * @param codigoFonte texto fonte a ser analisado
     * @param tabelaSimbolos tabela de símbolos utilizada para consultar palavras reservadas e identificadores
     */
    public AnalisadorLexico(String codigoFonte, TabelaSimbolos tabelaSimbolos) {
        this.codigoFonte = codigoFonte;
        this.tabela = tabelaSimbolos;
        this.posicao = 0;
        this.linha = 1;
        this.coluna = 1;
    }

    /**
     * Lê o próximo caractere do código-fonte e atualiza as posições de linha e coluna.
     *
     * @return próximo caractere disponível ou '\0' quando houver fim do texto
     */
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

    /**
     * Observa o caractere atual sem avançar o cursor de leitura.
     *
     * @return caractere atual ou '\0' ao final do arquivo
     */
    private char olharCaractere() {
        if (posicao >= codigoFonte.length()) return '\0';
        return codigoFonte.charAt(posicao);
    }

    /**
     * Produz o próximo token disponível no código-fonte.
     *
     * @return objeto Token correspondente ao próximo símbolo reconhecido, ou null ao final do arquivo
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
     * Analisa o código-fonte inteiro e retorna a lista completa de tokens.
     *
     * @return coleção de tokens gerados pela leitura do arquivo
     */
    public List<Token> analisarTudo() {
        List<Token> tokens = new ArrayList<>();
        Token t;
        while ((t = proximoToken()) != null) {
            tokens.add(t);
        }
        return tokens;
    }

    /**
     * Reconhece identificadores, palavras reservadas e operadores lógicos.
     *
     * @param c primeiro caractere do identificador
     * @param linhaInicial linha em que o lexema começa
     * @param colunaInicial coluna em que o lexema começa
     * @return token correspondente ao identificador ou palavra-chave
     */
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

    /**
     * Extrai uma string delimitada por aspas duplas.
     *
     * @param linhaInicial linha em que a string começa
     * @param colunaInicial coluna em que a string começa
     * @return token da constante string ou um token desconhecido em caso de erro
     */
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

    /**
     * Extrai um caractere delimitado por aspas simples.
     *
     * @param linhaInicial linha em que o literal começa
     * @param colunaInicial coluna em que o literal começa
     * @return token do literal char ou desconhecido em caso de erro
     */
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

    /**
     * Reconhece operadores relacionais e comparações compostas, como <=, >= e <>.
     *
     * @param c primeiro caractere do operador
     * @param linhaInicial linha do operador no código-fonte
     * @param colunaInicial coluna do operador no código-fonte
     * @return token do operador relacional
     */
    private Token extrairOperadorRelacional(char c, int linhaInicial, int colunaInicial) {
        StringBuilder stringBuilder = getStringBuilder(c);
        char prox = olharCaractere();
        if ((c == '<' && (prox == '=' || prox == '>')) || (c == '>' && prox == '=')) {
            stringBuilder.append(lerCaractere());
        }
        return new Token(stringBuilder.toString(), Tokens.OPERADOR_RELACIONAL, linhaInicial, colunaInicial);
    }

    /**
     * Extrai valores numéricos inteiros ou reais, incluindo notação científica.
     *
     * @param c primeiro dígito do número
     * @param linhaInicial linha do valor no código-fonte
     * @param colunaInicial coluna do valor no código-fonte
     * @return token do número reconhecido, ou token desconhecido se o literal estiver malformado
     */
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

    /**
     * Retorna o caractere imediatamente após a posição atual.
     *
     * @return próximo caractere ou '\0' se não existir
     */
    private char caractereSeguinte() {
        int indiceSeguinte = posicao + 1;
        if (indiceSeguinte >= codigoFonte.length()) return '\0';
        return codigoFonte.charAt(indiceSeguinte);
    }

    /**
     * Cria um construtor de texto a partir do primeiro caractere do lexema.
     *
     * @param c primeiro caractere do lexema
     * @return instância de StringBuilder com o caractere inicial
     */
    private static StringBuilder getStringBuilder(char c) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(c);
        return stringBuilder;
    }

    /**
     * Ignora comentários em bloco e retorna erro caso o fechamento não exista.
     *
     * @param linhaInicial linha em que o comentário inicia
     * @param colunaInicial coluna em que o comentário inicia
     * @return null quando o comentário é válido, ou token desconhecido quando há falha de fechamento
     */
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