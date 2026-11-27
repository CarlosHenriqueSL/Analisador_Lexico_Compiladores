package compilador;

import analisadorlexico.AnalisadorLexico;
import analisadorsintatico.*;
import gramatica.ConjuntosFirstFollow;
import gramatica.GramaticaMiniPascal;
import tabelasimbolos.TabelaSimbolos;
import util.GerenciadorArquivos;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ponto único de entrada do "backend" (a parte que não é tela).
 * Quem usar o compilador (a futura GUI ou o terminal) só precisa desta classe:
 * ela monta a gramática e as tabelas uma vez e, a cada chamada, liga o analisador léxico ao sintático.
 *
 * Terminal:  java -cp target/classes compilador.Compilador entrada.txt [saida.txt]
 * GUI:       String resultado = new Compilador().analisar(textoDoEditor);
 */
public class Compilador {
    private final GramaticaMiniPascal gramatica = new GramaticaMiniPascal();
    private final TabelaSLR tabela = new TabelaSLR(gramatica, new AutomatoLR0(gramatica), new ConjuntosFirstFollow(gramatica));

    /** Analisa o código e devolve a lista de erros (vazia = programa correto). */
    public List<ErroSintatico> analisarSintaxe(String codigoFonte) {
        AnalisadorLexico lexico = new AnalisadorLexico(codigoFonte, new TabelaSimbolos());
        return new AnalisadorSintatico(tabela, lexico).analisar();
    }

    /** Devolve exatamente "OK" se o programa está correto; senão, um erro por linha. */
    public String analisar(String codigoFonte) {
        List<ErroSintatico> erros = analisarSintaxe(codigoFonte);
        if (erros.isEmpty()) return "OK";
        return erros.stream().map(ErroSintatico::toString).collect(Collectors.joining(System.lineSeparator()));
    }

    /** Conflitos encontrados ao montar a tabela SLR(1) (para o relatório). */
    public List<String> conflitosDaTabela() { return tabela.conflitos(); }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: Compilador <entrada.txt> [saida.txt]");
            return;
        }
        String resultado = new Compilador().analisar(GerenciadorArquivos.lerArquivo(args[0]));
        new PrintStream(System.out, true, StandardCharsets.UTF_8).println(resultado);
        if (args.length > 1) Files.writeString(Paths.get(args[1]), resultado + System.lineSeparator());
    }
}