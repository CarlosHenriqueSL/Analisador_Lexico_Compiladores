package analisadorlexico;

import org.junit.jupiter.api.Test;
import tabelasimbolos.TabelaSimbolos;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalisadorLexicoTest {
    @Test
    void reconhecePalavrasReservadasEOperadores() {
        List<Token> tokens = analisar("PROGRAM demo; VAR x: INTEGER; BEGIN x := 10 MOD 3; END.");

        assertEquals(Tokens.PALAVRA_RESERVADA, tokens.get(0).getTipo());
        assertEquals(Tokens.PALAVRA_RESERVADA, tokens.get(6).getTipo());
        assertEquals(Tokens.ATRIBUICAO, tokens.get(10).getTipo());
        assertEquals(Tokens.OPERADOR_ARITMETICO, tokens.get(12).getTipo());
        assertEquals(Tokens.FIM, tokens.get(tokens.size() - 1).getTipo());
    }

    @Test
    void ignoraComentariosEConservaPosicaoInicialDoToken() {
        List<Token> tokens = analisar("/* comentario */\n\n  begin\n    x := 1;");

        assertEquals(5, tokens.size());
        assertEquals("begin", tokens.get(0).getLexema());
        assertEquals(3, tokens.get(0).getLinha());
        assertEquals(3, tokens.get(0).getColuna());
        assertEquals(4, tokens.get(3).getLinha());
    }

    @Test
    void reconheceNumerosReaisStringsCharsERelacionais() {
        List<Token> tokens = analisar("24.40e-04 >= 3.14 \"texto\" 'a' <>");

        assertEquals(Tokens.NUMERO_REAL, tokens.get(0).getTipo());
        assertEquals(Tokens.OPERADOR_RELACIONAL, tokens.get(1).getTipo());
        assertEquals(Tokens.NUMERO_REAL, tokens.get(2).getTipo());
        assertEquals(Tokens.CONSTANTE_STRING, tokens.get(3).getTipo());
        assertEquals(Tokens.CONSTANTE_CHAR, tokens.get(4).getTipo());
        assertEquals(Tokens.OPERADOR_RELACIONAL, tokens.get(5).getTipo());
    }

    @Test
    void sinalizaLexemasMalformadosSemPararAnalise() {
        List<Token> tokens = analisar("1e+ @ \"sem fim");

        assertEquals(Tokens.DESCONHECIDO, tokens.get(0).getTipo());
        assertEquals(Tokens.DESCONHECIDO, tokens.get(1).getTipo());
        assertEquals(Tokens.DESCONHECIDO, tokens.get(2).getTipo());
        assertTrue(tokens.get(0).getLexema().startsWith("1e+"));
    }

    @Test
    void exportacaoUsaDescricaoHumanaDoToken() {
        Token token = analisar("if").get(0);

        assertEquals("<if, Palavra reservada>", token.toString());
    }

    private List<Token> analisar(String codigo) {
        return new AnalisadorLexico(codigo, new TabelaSimbolos()).analisarTudo();
    }
}
