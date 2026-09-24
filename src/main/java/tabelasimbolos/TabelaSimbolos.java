package tabelasimbolos;

import analisadorlexico.Token;
import analisadorlexico.Tokens;

import java.util.*;

public class TabelaSimbolos {
    private Map<String, Token> tabela;

    public TabelaSimbolos() {
        tabela = new HashMap<>();
        inicializarPalavrasReservadas();
    }

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

    public void inserir(String lexema, Token token) {
        tabela.put(lexema, token);
    }

    public Token buscar(String lexema) {
        return tabela.get(lexema);
    }
}
