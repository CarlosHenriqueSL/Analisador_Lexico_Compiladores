package tabelasimbolos;

import analisadorlexico.Token;
import analisadorlexico.Tokens;

import java.util.*;

/**
 * Armazena identificadores e palavras reservadas reconhecidos durante a análise léxica.
 * A estrutura permite consultas rápidas por lexema e também mantém a categoria do token
 * associado para uso posterior nas etapas de compilação.
 */
public class TabelaSimbolos {
    private Map<String, Token> tabela;

    /**
     * Cria a tabela de símbolos e inicializa a lista de palavras reservadas do Mini Pascal.
     */
    public TabelaSimbolos() {
        tabela = new HashMap<>();
        inicializarPalavrasReservadas();
    }

    /**
     * Preenche a tabela com as palavras reservadas da linguagem.
     */
    private void inicializarPalavrasReservadas() {
        String[] palavras = {
                "ABSOLUTE", "ARRAY", "BEGIN", "CASE", "CHAR", "CONST", "DIV", "DO", "DOWNTO", "ELSE", "END", "EXTERNAL",
                "FILE", "FOR", "FORWARD", "FUNC", "FUNCTION", "GOTO", "IF", "IMPLEMENTATION", "INTEGER",
                "INTERFACE", "INTERRUPT", "LABEL", "MAIN", "NIL", "NIT", "OF", "PACKED", "PROC", "PROGRAM", "REAL",
                "RECORD", "REPEAT", "SET", "SHL", "SHR", "STRING", "THEN", "TO", "TYPE", "UNIT", "UNTIL", "USES", "VAR", "WHILE",
                "WITH", "XOR"
        };

        for (String palavra: palavras) {
            tabela.put(palavra, new Token(palavra, Tokens.PALAVRA_RESERVADA, 0, 0));
        }
    }

    /**
     * Insere ou atualiza um identificador na tabela de símbolos.
     *
     * @param lexema sequência de caracteres do identificador
     * @param token token associado ao lexema
     */
    public void inserir(String lexema, Token token) {
        tabela.put(lexema, token);
    }

    /**
     * Busca um lexema previamente registrado na tabela.
     *
     * @param lexema identificador ou palavra reservada a ser consultado
     * @return token correspondente, ou null caso não exista
     */
    public Token buscar(String lexema) {
        return tabela.get(lexema);
    }
}
