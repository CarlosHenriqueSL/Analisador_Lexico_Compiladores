package util;

import analisadorlexico.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centraliza as operações de leitura e gravação de arquivos do projeto.
 * Essa classe permite carregar o código-fonte e exportar a listagem de tokens
 * gerada pela análise léxica para um arquivo de texto.
 */
public class GerenciadorArquivos {
    /**
     * Lê o conteúdo de um arquivo de texto informado.
     *
     * @param caminhoDestino caminho absoluto ou relativo do arquivo
     * @return conteúdo em formato de texto
     * @throws IOException caso ocorra algum erro de leitura
     */
    public static String lerArquivo(String caminhoDestino) throws IOException {
        return Files.readString(Paths.get(caminhoDestino));
    }

    /**
     * Escreve a listagem de tokens em um arquivo de texto.
     *
     * @param caminhoDestino destino do arquivo a ser gerado
     * @param tokens lista de tokens que será exportada
     * @throws IOException caso ocorra algum erro de escrita
     */
    public static void escreverArquivo(String caminhoDestino, List<Token> tokens) throws IOException {
        List<String> linhas = tokens.stream()
                .map(Token::toString)
                .collect(Collectors.toList());

        Files.write(Paths.get(caminhoDestino), linhas);
    }
}
