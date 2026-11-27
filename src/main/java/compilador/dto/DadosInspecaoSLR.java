package compilador.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO imutável contendo as estruturas formais do analisador SLR(1) para exibição na aba de inspeção:
 * produções da gramática, conjuntos FIRST e FOLLOW e entradas da Tabela SLR(1) (ACTION/GOTO).
 *
 * @param producoes        Lista formatada das produções da {@code GramaticaMiniPascal}.
 * @param conjuntosFirst   Mapa de cada símbolo não-terminal para seu conjunto {@code FIRST}.
 * @param conjuntosFollow  Mapa de cada símbolo não-terminal para seu conjunto {@code FOLLOW}.
 * @param linhasTabelaSLR  Linhas da tabela de parsing SLR(1) (Estado, Símbolo e Ação/Desvio).
 */
public record DadosInspecaoSLR(
        List<LinhaProducaoDTO> producoes,
        List<LinhaFirstFollowDTO> conjuntosFirstFollow,
        List<LinhaTabelaSLRDTO> linhasTabelaSLR,
        Map<String, String> resumoMetricas
) {
    public DadosInspecaoSLR {
        producoes = List.copyOf(producoes);
        conjuntosFirstFollow = List.copyOf(conjuntosFirstFollow);
        linhasTabelaSLR = List.copyOf(linhasTabelaSLR);
        resumoMetricas = Map.copyOf(resumoMetricas);
    }

    /** Representa uma regra de produção {@code id: ladoEsquerdo -> ladoDireito}. */
    public record LinhaProducaoDTO(int id, String ladoEsquerdo, String ladoDireito) {}

    /** Representa os conjuntos {@code FIRST} e {@code FOLLOW} de um símbolo não-terminal. */
    public record LinhaFirstFollowDTO(String naoTerminal, String first, String follow) {}

    /** Representa uma célula da matriz {@code ACTION / GOTO} do autômato LR(0)/SLR(1). */
    public record LinhaTabelaSLRDTO(int estado, String simbolo, String tipoTabela, String acao) {}
}