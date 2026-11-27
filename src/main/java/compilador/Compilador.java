package compilador;

import analisadorlexico.AnalisadorLexico;
import analisadorlexico.Token;
import analisadorlexico.Tokens;
import analisadorsintatico.Acao;
import analisadorsintatico.AnalisadorSintatico;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fachada (Facade) principal do compilador da linguagem Mini_Pascal.
 * <p>
 * Centraliza a coordenação entre:
 * <ul>
 *     <li><b>Analisador Léxico</b> ({@link AnalisadorLexico}) e <b>Tabela de Símbolos</b> ({@link TabelaSimbolos});</li>
 *     <li><b>Gramática Mini_Pascal</b> ({@link GramaticaMiniPascal}), conjuntos <b>FIRST/FOLLOW</b> ({@link ConjuntosFirstFollow})
 *         e <b>Tabela SLR(1)</b> ({@link TabelaSLR});</li>
 *     <li><b>Analisador Sintático SLR(1)</b> ({@link AnalisadorSintatico}).</li>
 * </ul>
 * Isola totalmente os detalhes internos do compilador da camada de interface gráfica (JavaFX).
 */
public class Compilador {

    private final GramaticaMiniPascal gramatica;
    private final ConjuntosFirstFollow conjuntosFirstFollow;
    private final TabelaSLR tabelaSLR;
    private final DadosInspecaoSLR dadosInspecaoSLR;

    /**
     * Inicializa o compilador construindo uma única vez as estruturas estáticas da gramática,
     * os conjuntos FIRST e FOLLOW e a tabela de análise SLR(1).
     */
    public Compilador() {
        this.gramatica = new GramaticaMiniPascal();
        this.conjuntosFirstFollow = new ConjuntosFirstFollow(this.gramatica);
        this.tabelaSLR = new TabelaSLR(this.gramatica, this.conjuntosFirstFollow);
        this.dadosInspecaoSLR = construirDadosInspecaoSLR();
    }

    /**
     * Executa apenas a fase de Análise Léxica (I Unidade) sobre o código-fonte informado.
     *
     * @param codigoFonte Texto do programa em Mini_Pascal.
     * @return {@link ResultadoLexico} contendo os pares {@code <lexema, token>}, erros léxicos e tabela de símbolos.
     */
    public ResultadoLexico analisarLexico(String codigoFonte) {
        String fonteNormalizado = normalizarQuebrasDeLinha(codigoFonte);
        long inicio = System.nanoTime();

        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorLexico lexer = new AnalisadorLexico(fonteNormalizado, tabelaSimbolos);

        List<ItemTokenDTO> tokensReconhecidos = new ArrayList<>();
        List<ItemErroDTO> errosLexicos = new ArrayList<>();

        Token tokenAtual;
        while ((tokenAtual = lexer.proximoToken()) != null && !ehFimDeArquivo(tokenAtual)) {
            String lexema = extrairLexema(tokenAtual);
            String nomeToken = extrairNomeToken(tokenAtual);
            int linha = extrairLinha(tokenAtual);

            if (ehTokenErro(tokenAtual)) {
                errosLexicos.add(new ItemErroDTO(
                        linha,
                        "Erro Léxico",
                        lexema,
                        "Símbolo desconhecido ou literal malformado: '" + lexema + "'"
                ));
            } else {
                tokensReconhecidos.add(new ItemTokenDTO(linha, lexema, nomeToken));
            }
        }

        List<ItemSimboloDTO> simbolos = extrairTabelaSimbolos(tabelaSimbolos, tokensReconhecidos);
        long duracaoMs = Math.max(1L, (System.nanoTime() - inicio) / 1_000_000L);

        return new ResultadoLexico(tokensReconhecidos, errosLexicos, simbolos, duracaoMs);
    }

    /**
     * Executa a Análise Sintática SLR(1) integrada ao Analisador Léxico (II Unidade).
     * <p>
     * Conforme contrato da disciplina:
     * <ul>
     *     <li>Retorna exatamente {@code "OK"} quando o código-fonte é sintaticamente válido;</li>
     *     <li>Retorna o conjunto completo de erros (léxicos e sintáticos), especificando o tipo
     *         e o número da linha de cada ocorrência, caso contrário.</li>
     * </ul>
     *
     * @param codigoFonte Texto do programa em Mini_Pascal.
     * @return {@link ResultadoSintatico} contendo o veredito oficial e a lista estruturada de erros.
     */
    public ResultadoSintatico analisarSintatico(String codigoFonte) {
        String fonteNormalizado = normalizarQuebrasDeLinha(codigoFonte);
        long inicio = System.nanoTime();

        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorLexico lexer = new AnalisadorLexico(fonteNormalizado, tabelaSimbolos);
        AnalisadorSintatico parser = new AnalisadorSintatico(lexer, tabelaSimbolos, tabelaSLR, gramatica);

        List<ErroSintatico> errosBrutos = parser.analisar();
        long duracaoMs = Math.max(1L, (System.nanoTime() - inicio) / 1_000_000L);

        if (errosBrutos == null || errosBrutos.isEmpty()) {
            return ResultadoSintatico.sucesso(duracaoMs);
        }

        List<ItemErroDTO> errosFormatados = errosBrutos.stream()
                .map(this::converterErroSintatico)
                .sorted(Comparator.comparingInt(ItemErroDTO::linha))
                .toList();

        return ResultadoSintatico.falha(errosFormatados, duracaoMs);
    }

    /**
     * Executa o pipeline completo (Análise Léxica + Análise Sintática SLR(1)),
     * consolidando todas as informações necessárias para atualizar todas as abas da interface.
     *
     * @param codigoFonte Texto do programa em Mini_Pascal.
     * @return {@link ResultadoCompilacao} unificando resultados léxico, sintático e tabela de símbolos.
     */
    public ResultadoCompilacao compilar(String codigoFonte) {
        ResultadoLexico resLexico = analisarLexico(codigoFonte);
        ResultadoSintatico resSintatico = analisarSintatico(codigoFonte);
        return new ResultadoCompilacao(resLexico, resSintatico);
    }

    /**
     * Retorna os dados imutáveis da gramática, conjuntos FIRST/FOLLOW e tabela SLR(1)
     * para consulta na aba de inspeção da interface gráfica.
     *
     * @return {@link DadosInspecaoSLR} pré-calculado na inicialização do compilador.
     */
    public DadosInspecaoSLR getDadosInspecaoSLR() {
        return dadosInspecaoSLR;
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE NORMALIZAÇÃO E ADAPTAÇÃO COM O BACKEND
    // =========================================================================

    /**
     * Normaliza quebras de linha no padrão Windows ({@code \r\n}) para {@code \n},
     * evitando contagem dupla de linhas no analisador léxico e sincronizando com o editor JavaFX.
     */
    private String normalizarQuebrasDeLinha(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\r\n", "\n").replace("\r", "\n");
    }

    private boolean ehFimDeArquivo(Token token) {
        Tokens tipo = token.getTipo();
        return tipo == Tokens.EOF;
    }

    private boolean ehTokenErro(Token token) {
        Tokens tipo = token.getTipo();
        return tipo == Tokens.ERRO;
    }

    private String extrairLexema(Token token) {
        return token.getLexema() != null ? token.getLexema() : "";
    }

    private String extrairNomeToken(Token token) {
        return token.getTipo() != null ? token.getTipo().name() : "DESCONHECIDO";
    }

    private int extrairLinha(Token token) {
        return Math.max(1, token.getLinha());
    }

    private ItemErroDTO converterErroSintatico(ErroSintatico erro) {
        int linha = Math.max(1, erro.getLinha());
        String tipo = (erro.getTipoErro() != null && !erro.getTipoErro().isBlank())
                ? erro.getTipoErro()
                : "Erro Sintático";
        String tokenEncontrado = erro.getTokenEncontrado() != null ? erro.getTokenEncontrado() : "-";
        String mensagem = erro.getMensagem() != null ? erro.getMensagem() : erro.toString();

        return new ItemErroDTO(linha, tipo, tokenEncontrado, mensagem);
    }

    /**
     * Constrói uma visão tabular da Tabela de Símbolos combinando as palavras reservadas
     * pré-carregadas e os identificadores reconhecidos no código-fonte.
     */
    private List<ItemSimboloDTO> extrairTabelaSimbolos(TabelaSimbolos tabela, List<ItemTokenDTO> tokens) {
        List<ItemSimboloDTO> resultado = new ArrayList<>();
        int indice = 1;

        // 1. Registra palavras reservadas conhecidas na TabelaSimbolos
        Set<String> reservadas = tabela.getPalavrasReservadas();
        if (reservadas != null) {
            List<String> ordenadas = reservadas.stream().sorted().toList();
            for (String palavra : ordenadas) {
                resultado.add(new ItemSimboloDTO(
                        indice++,
                        palavra,
                        "Palavra Reservada",
                        "PALAVRA_RESERVADA",
                        0
                ));
            }
        }

        // 2. Registra identificadores únicos encontrados na varredura atual com sua primeira linha
        Map<String, Integer> primeiraLinhaPorIdentificador = new LinkedHashMap<>();
        for (ItemTokenDTO t : tokens) {
            if ("IDENTIFICADOR".equalsIgnoreCase(t.token())) {
                primeiraLinhaPorIdentificador.putIfAbsent(t.lexema(), t.linha());
            }
        }

        for (Map.Entry<String, Integer> entry : primeiraLinhaPorIdentificador.entrySet()) {
            resultado.add(new ItemSimboloDTO(
                    indice++,
                    entry.getKey(),
                    "Identificador",
                    "IDENTIFICADOR",
                    entry.getValue()
            ));
        }

        return resultado;
    }

    /**
     * Extrai e formata as produções, conjuntos FIRST/FOLLOW e entradas ACTION/GOTO
     * para exibição didática na aba "Inspeção SLR(1)".
     */
    private DadosInspecaoSLR construirDadosInspecaoSLR() {
        // 1. Produções da Gramática Mini_Pascal
        List<LinhaProducaoDTO> listaProducoes = new ArrayList<>();
        List<Producao> producoes = gramatica.getProducoes();
        if (producoes != null) {
            for (int i = 0; i < producoes.size(); i++) {
                Producao p = producoes.get(i);
                String esquerda = p.getLadoEsquerdo();
                String direita = (p.getLadoDireito() == null || p.getLadoDireito().isEmpty())
                        ? "ε"
                        : String.join(" ", p.getLadoDireito());
                listaProducoes.add(new LinhaProducaoDTO(i, esquerda, direita));
            }
        }

        // 2. Conjuntos FIRST e FOLLOW
        List<LinhaFirstFollowDTO> listaFirstFollow = new ArrayList<>();
        Set<String> naoTerminais = gramatica.getNaoTerminais();
        if (naoTerminais != null) {
            for (String nt : naoTerminais) {
                Set<String> first = conjuntosFirstFollow.getFirst(nt);
                Set<String> follow = conjuntosFirstFollow.getFollow(nt);
                String firstStr = (first != null && !first.isEmpty())
                        ? "{ " + first.stream().sorted().collect(Collectors.joining(", ")) + " }"
                        : "{ }";
                String followStr = (follow != null && !follow.isEmpty())
                        ? "{ " + follow.stream().sorted().collect(Collectors.joining(", ")) + " }"
                        : "{ }";
                listaFirstFollow.add(new LinhaFirstFollowDTO(nt, firstStr, followStr));
            }
        }

        // 3. Entradas ACTION e GOTO da Tabela SLR(1)
        List<LinhaTabelaSLRDTO> listaTabelaSLR = new ArrayList<>();
        Map<Integer, Map<String, Acao>> tabelaAcao = tabelaSLR.getTabelaAcao();
        if (tabelaAcao != null) {
            tabelaAcao.keySet().stream().sorted().forEach(estado -> {
                Map<String, Acao> linha = tabelaAcao.get(estado);
                if (linha != null) {
                    linha.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(entry -> listaTabelaSLR.add(new LinhaTabelaSLRDTO(
                                    estado,
                                    entry.getKey(),
                                    "ACTION",
                                    entry.getValue().toString()
                            )));
                }
            });
        }

        Map<Integer, Map<String, Integer>> tabelaGoto = tabelaSLR.getTabelaGoto();
        if (tabelaGoto != null) {
            tabelaGoto.keySet().stream().sorted().forEach(estado -> {
                Map<String, Integer> linha = tabelaGoto.get(estado);
                if (linha != null) {
                    linha.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(entry -> listaTabelaSLR.add(new LinhaTabelaSLRDTO(
                                    estado,
                                    entry.getKey(),
                                    "GOTO",
                                    "Estado " + entry.getValue()
                            )));
                }
            });
        }

        Map<String, String> metricas = Map.of(
                "Total de Produções", String.valueOf(listaProducoes.size()),
                "Não-Terminais", String.valueOf(naoTerminais != null ? naoTerminais.size() : 0),
                "Estados LR(0)/SLR(1)", String.valueOf(tabelaAcao != null ? tabelaAcao.size() : 0)
        );

        return new DadosInspecaoSLR(listaProducoes, listaFirstFollow, listaTabelaSLR, metricas);
    }
}