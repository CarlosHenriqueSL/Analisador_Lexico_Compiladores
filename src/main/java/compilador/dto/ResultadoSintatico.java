package compilador.dto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsula o contrato de entrada e saída da Análise Sintática SLR(1) (II Unidade):
 * devolve exatamente {@code "OK"} quando válido, ou o conjunto completo de erros
 * indicando tipo e linha de ocorrência.
 *
 * @param valido          {@code true} se o programa pertence à gramática Mini_Pascal sem erros.
 * @param saidaOficial    Exatamente {@code "OK"} em caso de sucesso, ou a listagem de erros em caso de falha.
 * @param erros           Lista estruturada contendo erros léxicos e sintáticos detectados.
 * @param tempoExecucaoMs Tempo gasto na análise sintática SLR(1) em milissegundos.
 */
public record ResultadoSintatico(
        boolean valido,
        String saidaOficial,
        List<ItemErroDTO> erros,
        long tempoExecucaoMs
) {
    public ResultadoSintatico {
        erros = List.copyOf(erros);
    }

    /**
     * Cria uma instância representando aceitação sintática plena ({@code "OK"}).
     *
     * @param tempoExecucaoMs Tempo de execução em milissegundos.
     * @return Resultado sintático válido.
     */
    public static ResultadoSintatico sucesso(long tempoExecucaoMs) {
        return new ResultadoSintatico(true, "OK", List.of(), tempoExecucaoMs);
    }

    /**
     * Cria uma instância representando falha léxica ou sintática, formatando a saída oficial.
     *
     * @param erros           Lista não vazia de erros encontrados.
     * @param tempoExecucaoMs Tempo de execução em milissegundos.
     * @return Resultado sintático inválido com mensagens detalhadas.
     */
    public static ResultadoSintatico falha(List<ItemErroDTO> erros, long tempoExecucaoMs) {
        String textoErros = erros.stream()
                .map(ItemErroDTO::formatarMensagem)
                .collect(Collectors.joining(System.lineSeparator()));
        return new ResultadoSintatico(false, textoErros, erros, tempoExecucaoMs);
    }
}