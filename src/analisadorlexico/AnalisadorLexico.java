package analisadorlexico;

import java.util.*;
import tabelasimbolos.TabelaSimbolos;

public class AnalisadorLexico {
    private final String codigoFonte;
    private int posicao;
    private int linha;
    private int coluna;
    private final TabelaSimbolos tabela;

    // [CORREÇÃO 2] Guardam o estado (linha/coluna) imediatamente ANTES da
    // última chamada a lerCaractere(). São usados por retroceder() para
    // desfazer corretamente a leitura de uma quebra de linha ('\n').
    private int linhaAnterior;
    private int colunaAnterior;

    private static final Set<String> OPERADORES_LOGICOS = new HashSet<>(Arrays.asList("AND", "OR", "NOT"));

    public AnalisadorLexico(String codigoFonte, TabelaSimbolos tabelaSimbolos) {
        this.codigoFonte = codigoFonte;
        this.tabela = tabelaSimbolos;
        this.posicao = 0;
        this.linha = 1;
        this.coluna = 1;
        this.linhaAnterior = 1;
        this.colunaAnterior = 1;
    }

    private char lerCaractere() {
        if (posicao >= codigoFonte.length()) return '\0';
        linhaAnterior = linha;
        colunaAnterior = coluna;
        char c = codigoFonte.charAt(posicao++);
        if (c == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }
        return c;
    }

    // [CORREÇÃO 2] Antes, retroceder() apenas decrementava "coluna", nunca
    // desfazia o incremento de "linha" feito por lerCaractere() ao consumir um
    // '\n'. Isso fazia com que a mesma quebra de linha fosse contada duas
    // vezes sempre que um token terminava logo antes dela (ex.: identificador
    // seguido de fim de linha), corrompendo o número de linha de todos os
    // tokens seguintes. Agora restauramos o estado exato salvo em
    // lerCaractere(), o que também corrige o caso especial de retroceder()
    // ser chamado logo após uma leitura em fim de arquivo (ver Correção 1).
    private void retroceder() {
        if (posicao > 0) {
            posicao--;
            linha = linhaAnterior;
            coluna = colunaAnterior;
        }
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();

        while (posicao < codigoFonte.length()) {
            char c = lerCaractere();
            if (Character.isWhitespace(c)) continue;

            if (c == '/' && posicao < codigoFonte.length() && codigoFonte.charAt(posicao) == '*') {
                // [CORREÇÃO 4] Guarda a posição de ABERTURA do comentário
                // (linha/coluna do '/') para reportar corretamente o erro
                // caso ele nunca seja fechado.
                int linhaInicial = linha;
                int colInicialComentario = coluna - 1;
                extrairComentarios(tokens, linhaInicial, colInicialComentario);
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
                // [CORREÇÃO 4] Passa a linha inicial explicitamente.
                extrairString(tokens, linha, colInicial);
                continue;
            }
            if (c == '\'') {
                extrairChar(tokens, linha, colInicial);
                continue;
            }
            if (c == '.') {
                // [CORREÇÃO 3] Reconhece o operador de intervalo ".." (usado em
                // declarações como ARRAY[1..10]) antes de tratar um único
                // ponto como fim de programa.
                if (posicao < codigoFonte.length() && codigoFonte.charAt(posicao) == '.') {
                    lerCaractere();
                    tokens.add(new Token("..", Tokens.SIMBOLO_ESPECIAL, linha, colInicial));
                } else {
                    tokens.add(new Token(".", Tokens.FIM, linha, colInicial));
                }
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
            } else if (tokenNaTabela != null) {
                // [CORREÇÃO 6] O identificador já existe na tabela: apenas
                // emitimos o token da ocorrência atual, sem sobrescrever a
                // entrada existente (evita perder o registro original a cada
                // nova ocorrência do mesmo identificador).
                tokens.add(new Token(lexema, Tokens.IDENTIFICADOR, linha, colInicial));
            } else {
                Token novoToken = new Token(lexema, Tokens.IDENTIFICADOR, linha, colInicial);
                tabela.inserir(lexemaUpper, novoToken);
                tokens.add(novoToken);
            }
        }
    }

    private void extrairString(List<Token> tokens, int linhaInicial, int colInicial) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        boolean fechou = false;

        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            stringBuilder.append(prox);
            // [CORREÇÃO 5] Suporte a caractere de escape: um '\' consome o
            // próximo caractere sem interpretá-lo como fechamento da string
            // (ex.: "a\"b" não fecha prematuramente no \").
            if (prox == '\\' && posicao < codigoFonte.length()) {
                stringBuilder.append(lerCaractere());
                continue;
            }
            if (prox == '"') {
                fechou = true;
                break;
            }
        }
        if (fechou) {
            // [CORREÇÃO 4] Usa a linha em que a string COMEÇOU, não a linha
            // atual (que pode já ter avançado se a string for malformada e
            // atravessar múltiplas linhas).
            tokens.add(new Token(stringBuilder.toString(), Tokens.CONSTANTE_STRING, linhaInicial, colInicial));
        } else {
            tokens.add(new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colInicial));
        }
    }

    private void extrairChar(List<Token> tokens, int linhaInicial, int colInicial) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\'');
        boolean fechou = false;

        while (posicao < codigoFonte.length()) {
            char prox = lerCaractere();
            stringBuilder.append(prox);
            if (prox == '\\' && posicao < codigoFonte.length()) {
                stringBuilder.append(lerCaractere());
                continue;
            }
            if (prox == '\'') {
                fechou = true;
                break;
            }
        }
        if (fechou) {
            tokens.add(new Token(stringBuilder.toString(), Tokens.CONSTANTE_CHAR, linhaInicial, colInicial));
        } else {
            tokens.add(new Token(stringBuilder.toString(), Tokens.DESCONHECIDO, linhaInicial, colInicial));
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
                // [CORREÇÃO 3] Se o ponto for seguido de OUTRO ponto, não é
                // parte de um número real: é o operador de intervalo "..".
                // Nesse caso devolvemos o ponto e encerramos o número aqui.
                if (posicao < codigoFonte.length() && codigoFonte.charAt(posicao) == '.') {
                    retroceder();
                    break;
                }
                isReal = true;
                stringBuilder.append(prox);
            } else if ((prox == 'e' || prox == 'E') && !isReal) {
                isReal = true;
                stringBuilder.append(prox);
                // [CORREÇÃO 1] Só tenta ler o caractere opcional de sinal do
                // expoente (+/-) se ainda houver caracteres no arquivo. Antes,
                // se o 'e'/'E' fosse o último caractere do código-fonte,
                // lerCaractere() retornava '\0' SEM avançar "posicao", e o
                // retroceder() seguinte decrementava "posicao" mesmo assim —
                // fazendo o laço reler o mesmo 'e' para sempre (loop infinito
                // / travamento do programa).
                if (posicao < codigoFonte.length()) {
                    char nextOpcional = lerCaractere();
                    if (nextOpcional == '+' || nextOpcional == '-') {
                        stringBuilder.append(nextOpcional);
                    } else {
                        retroceder();
                    }
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

    private void extrairComentarios(List<Token> tokens, int linhaInicial, int colInicial) {
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
            // [CORREÇÃO 4] Reporta a posição de ABERTURA do comentário
            // (antes: reportava linha/coluna do FIM do arquivo, apontando
            // para o lugar errado ao depurar).
            tokens.add(new Token("/*", Tokens.DESCONHECIDO, linhaInicial, colInicial));
        }
    }
}