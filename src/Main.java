import analisadorlexico.AnalisadorLexico;
import analisadorlexico.Token;
import tabelasimbolos.TabelaSimbolos;
import util.GerenciadorArquivos;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            String caminhoEntrada = "testes/entradas/teste.txt";
            String caminhoSaida = "testes/saidas/resultado_teste.txt";

            String codigoFonte = GerenciadorArquivos.lerArquivo(caminhoEntrada);

            TabelaSimbolos tabela = new TabelaSimbolos();
            AnalisadorLexico scanner = new AnalisadorLexico(codigoFonte, tabela);

            List<Token> tokens = scanner.analisar();

            GerenciadorArquivos.escreverArquivo(caminhoSaida, tokens);
        } catch (IOException e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}