package compilador.dto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsula o resultado completo da execução da Análise Léxica (I Unidade).
 *
 * @param tokens         Lista ordenada de pares {@code <lexema, token>} reconhecidos (sem espaços ou comentários).
 * @param errosLexicos   Lista de erros léxicos encontrados durante a varredura.
 * @param tabelaSimbolos Estado atualizado da Tabela de Símbolos após a varredura.
 * @param tempoExecucaoMs Tempo gasto na análise léxica em milissegundos.
 */
public record ResultadoLexico(
        List<ItemTokenDTO> tokens,
        List<ItemErroDTO> errosLexicos,
        List<ItemSimboloDTO> tabelaSimbolos,
        long tempoExecucaoMs
) {
    public ResultadoLexico {
        tokens = List.copyOf(tokens);
        errosLexicos = List.copyOf(errosLexicos);
        tabelaSimbolos = List.copyOf(tabelaSimbolos);
    }

    /**
     * Gera o conteúdo textual completo para exportação {@code .txt} da análise léxica,
     * exibindo um par {@code <cadeia, token>} por linha conforme especificação da disciplina.
     *
     * @return Texto pronto para gravação em arquivo {@code .txt}.
     */
    public String gerarTextoExportacao() {
        StringBuilder sb = new StringBuilder();
        for (ItemTokenDTO item : tokens) {
            sb.append(item.formatarPar()).append(System.lineSeparator());
        }
        if (!errosLexicos.isEmpty()) {
            sb.append(System.lineSeparator()).append("--- ERROS LÉXICOS DETECTADOS ---").append(System.lineSeparator());
            for (ItemErroDTO erro : errosLexicos) {
                sb.append(erro.formatarMensagem()).append(System.lineSeparator());
            }
        }
        return sb.toString().trim();
    }

    /**
     * Indica se a análise léxica ocorreu sem caracteres desconhecidos ou literais malformados.
     *
     * @return {@code true} se não houver erros léxicos.
     */
    public boolean isSucesso() {
        return errosLexicos.isEmpty();
    }

    /**
     * Retorna apenas a listagem pura de pares {@code <lexema, token>} separados por quebra de linha.
     *
     * @return Listagem pura de tokens.
     */
    public String listarParesPuros() {
        return tokens.stream()
                .map(ItemTokenDTO::formatarPar)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}