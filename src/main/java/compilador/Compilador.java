package compilador;

import analisadorlexico.AnalisadorLexico;
import analisadorlexico.Token;
import analisadorlexico.Tokens;
import analisadorsintatico.Acao;
import analisadorsintatico.AnalisadorSintatico;
import analisadorsintatico.AutomatoLR0;
import analisadorsintatico.ErroSintatico;
import analisadorsintatico.TabelaSLR;
import compilador.dto.DadosInspecaoSLR;
import compilador.dto.DadosInspecaoSLR.LinhaFirstFollowDTO;
import compilador.dto.DadosInspecaoSLR.LinhaProducaoDTO;
import compilador.dto.DadosInspecaoSLR.LinhaTabelaSLRDTO;
import compilador.dto.ItemErroDTO;
import compilador.dto.ItemSimboloDTO;
import compilador.dto.ItemTokenDTO;
import compilador.dto.ResultadoCompilacao;
import compilador.dto.ResultadoLexico;
import compilador.dto.ResultadoSintatico;
import gramatica.ConjuntosFirstFollow;
import gramatica.GramaticaMiniPascal;
import gramatica.Producao;
import tabelasimbolos.TabelaSimbolos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fachada (Facade) que integra o Analisador Léxico, a Tabela de Símbolos,
 * a Gramática Mini_Pascal e o Analisador Sintático SLR(1) para a interface gráfica e testes.
 */
public class Compilador {

    private static final List<String> PALAVRAS_RESERVADAS = List.of(
            "program", "var", "integer", "real", "char", "string",
            "procedure", "begin", "end", "if", "then", "else",
            "while", "do", "or", "div", "and", "not"
    );

    private final GramaticaMiniPascal gramatica;
    private final ConjuntosFirstFollow conjuntosFirstFollow;
    private final AutomatoLR0 automatoLR0;
    private final TabelaSLR tabelaSLR;

    public Compilador() {
        this.gramatica = new GramaticaMiniPascal();
        this.conjuntosFirstFollow = new ConjuntosFirstFollow(this.gramatica);
        this.automatoLR0 = new AutomatoLR0(this.gramatica);
        this.tabelaSLR = new TabelaSLR(this.gramatica, this.automatoLR0, this.conjuntosFirstFollow);
    }

    /**
     * Executa somente a Análise Léxica (I Unidade) sobre o código-fonte informado.
     */
    public ResultadoLexico analisarLexico(String codigoFonte) {
        long inicio = System.nanoTime();
        String entrada = codigoFonte != null ? codigoFonte : "";

        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorLexico lexico = new AnalisadorLexico(entrada, tabelaSimbolos);

        List<ItemTokenDTO> tokensReconhecidos = new ArrayList<>();
        List<ItemErroDTO> errosLexicos = new ArrayList<>();
        Map<String, Integer> primeiraLinhaIdentificador = new LinkedHashMap<>();

        while (true) {
            Token t = lexico.proximo();
            if (t.getTipo() == Tokens.EOF) {
                break;
            }

            if (t.getTipo() == Tokens.DESCONHECIDO) {
                errosLexicos.add(new ItemErroDTO(
                        t.getLinha(),
                        "Erro léxico",
                        "'" + t.getLexema() + "'",
                        descreverErroLexico(t) + " (coluna " + t.getColuna() + ")"
                ));
            } else {
                String nomeToken = t.getTipo().name();
                tokensReconhecidos.add(new ItemTokenDTO(t.getLinha(), t.getLexema(), nomeToken));

                if (t.getTipo() == Tokens.IDENTIFICADOR) {
                    primeiraLinhaIdentificador.putIfAbsent(t.getLexema().toLowerCase(), t.getLinha());
                }
            }
        }

        List<ItemSimboloDTO> listaSimbolos = montarListaSimbolos(primeiraLinhaIdentificador);
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000L;

        return new ResultadoLexico(tokensReconhecidos, errosLexicos, listaSimbolos, duracaoMs);
    }

    /**
     * Executa somente a Análise Sintática SLR(1) (II Unidade) sobre o código-fonte informado.
     */
    public ResultadoSintatico analisarSintatico(String codigoFonte) {
        long inicio = System.nanoTime();
        String entrada = codigoFonte != null ? codigoFonte : "";

        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorLexico lexico = new AnalisadorLexico(entrada, tabelaSimbolos);
        AnalisadorSintatico parser = new AnalisadorSintatico(this.tabelaSLR, lexico);

        List<ErroSintatico> errosBrutos = parser.analisar();
        List<ItemErroDTO> errosDTO = new ArrayList<>();

        for (ErroSintatico erro : errosBrutos) {
            String tipoDescricao = erro.tipo() != null ? erro.tipo().getDescricao() : "Erro sintático";
            String tokenEncontrado = extrairTokenEncontrado(erro);
            String descricaoCompleta = erro.mensagem() + " (coluna " + erro.coluna() + ")";

            errosDTO.add(new ItemErroDTO(
                    erro.linha(),
                    tipoDescricao,
                    tokenEncontrado,
                    descricaoCompleta
            ));
        }

        boolean valido = errosBrutos.isEmpty();
        String saidaOficial = valido
                ? "OK"
                : errosBrutos.stream()
                .map(ErroSintatico::toString)
                .collect(Collectors.joining(System.lineSeparator()));

        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000L;
        return new ResultadoSintatico(valido, saidaOficial, errosDTO, duracaoMs);
    }

    /**
     * Executa o pipeline completo (Análise Léxica + Análise Sintática SLR(1)).
     */
    public ResultadoCompilacao compilar(String codigoFonte) {
        ResultadoLexico resLexico = analisarLexico(codigoFonte);
        ResultadoSintatico resSintatico = analisarSintatico(codigoFonte);
        return new ResultadoCompilacao(resLexico, resSintatico);
    }

    /**
     * Retorna os metadados da Gramática Aumentada, conjuntos FIRST/FOLLOW e Tabela SLR(1)
     * para exibição na aba de inspeção didática.
     */
    public DadosInspecaoSLR getDadosInspecaoSLR() {
        List<Producao> producoesGramatica = gramatica.getProducoes();

        // 1. Lista de produções
        List<LinhaProducaoDTO> linhasProducoes = new ArrayList<>();
        Set<String> naoTerminais = new LinkedHashSet<>();

        for (Producao p : producoesGramatica) {
            naoTerminais.add(p.esquerda());
            String ladoDireito = (p.direita() == null || p.direita().isEmpty())
                    ? "ε"
                    : String.join(" ", p.direita());
            linhasProducoes.add(new LinhaProducaoDTO(p.numero(), p.esquerda(), ladoDireito));
        }

        // 2. Conjuntos FIRST e FOLLOW
        List<LinhaFirstFollowDTO> linhasFirstFollow = new ArrayList<>();
        for (String nt : naoTerminais) {
            Set<String> firstSet = conjuntosFirstFollow.first(nt);
            Set<String> followSet = conjuntosFirstFollow.follow(nt);
            linhasFirstFollow.add(new LinhaFirstFollowDTO(
                    nt,
                    formatarConjunto(firstSet),
                    formatarConjunto(followSet)
            ));
        }

        // 3. Tabela SLR(1) (ACTION e GOTO)
        List<LinhaTabelaSLRDTO> linhasTabela = new ArrayList<>();
        int totalEstados = tabelaSLR.numEstados();

        for (int estado = 0; estado < totalEstados; estado++) {
            for (String terminal : tabelaSLR.terminaisEsperados(estado)) {
                Acao acao = tabelaSLR.acao(estado, terminal);
                if (acao != null) {
                    linhasTabela.add(new LinhaTabelaSLRDTO(
                            estado,
                            terminal,
                            "ACTION",
                            acao.toString()
                    ));
                }
            }

            for (String nt : naoTerminais) {
                Integer destino = tabelaSLR.irPara(estado, nt);
                if (destino != null) {
                    linhasTabela.add(new LinhaTabelaSLRDTO(
                            estado,
                            nt,
                            "GOTO",
                            String.valueOf(destino)
                    ));
                }
            }
        }

        // 4. Resumo de métricas
        Map<String, String> resumoMetricas = new LinkedHashMap<>();
        resumoMetricas.put("Total de Produções", String.valueOf(producoesGramatica.size()));
        resumoMetricas.put("Não-Terminais", String.valueOf(naoTerminais.size()));
        resumoMetricas.put("Estados LR(0)/SLR(1)", String.valueOf(totalEstados));
        resumoMetricas.put("Conflitos SLR(1)", String.valueOf(tabelaSLR.conflitos().size()));

        return new DadosInspecaoSLR(linhasProducoes, linhasFirstFollow, linhasTabela, resumoMetricas);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // =========================================================================

    private List<ItemSimboloDTO> montarListaSimbolos(Map<String, Integer> identificadoresEncontrados) {
        List<ItemSimboloDTO> simbolos = new ArrayList<>();
        int indice = 1;

        for (String reservada : PALAVRAS_RESERVADAS) {
            simbolos.add(new ItemSimboloDTO(
                    indice++,
                    reservada,
                    "Palavra Reservada",
                    Tokens.PALAVRA_RESERVADA.name(),
                    0
            ));
        }

        for (Map.Entry<String, Integer> entry : identificadoresEncontrados.entrySet()) {
            simbolos.add(new ItemSimboloDTO(
                    indice++,
                    entry.getKey(),
                    "Identificador",
                    Tokens.IDENTIFICADOR.name(),
                    entry.getValue()
            ));
        }

        return simbolos;
    }

    private String descreverErroLexico(Token t) {
        String lex = t.getLexema();
        if (lex.startsWith("\"")) {
            return "Constante string não fechada: " + lex.stripTrailing();
        }
        if (lex.startsWith("'")) {
            return "Constante char inválida (deve ter exatamente 1 caractere): " + lex.stripTrailing();
        }
        if (lex.startsWith("/*")) {
            return "Comentário de bloco não fechado";
        }
        if (!lex.isEmpty() && Character.isDigit(lex.charAt(0))) {
            return "Número malformado: '" + lex + "'";
        }
        return "Símbolo desconhecido: '" + lex + "'";
    }

    private String extrairTokenEncontrado(ErroSintatico erro) {
        String msg = erro.mensagem();
        if (msg == null || msg.isBlank()) {
            return "Coluna " + erro.coluna();
        }
        if (msg.contains("fim do arquivo")) {
            return "EOF";
        }

        int buscaInicio = msg.startsWith("esperado ';'") ? 12 : 0;
        int aspa1 = msg.indexOf('\'', buscaInicio);
        if (aspa1 >= 0) {
            int aspa2 = msg.indexOf('\'', aspa1 + 1);
            if (aspa2 > aspa1) {
                return msg.substring(aspa1, aspa2 + 1);
            }
        }
        return "Coluna " + erro.coluna();
    }

    private String formatarConjunto(Set<String> conjunto) {
        if (conjunto == null || conjunto.isEmpty()) {
            return "{ }";
        }
        return "{ " + String.join(", ", conjunto) + " }";
    }
}