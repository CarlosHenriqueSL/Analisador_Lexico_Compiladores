package util;

import analisadorlexico.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorArquivos {
    public static String lerArquivo(String caminhoDestino) throws IOException {
        return Files.readString(Paths.get(caminhoDestino));
    }

    public static void escreverArquivo(String caminhoDestino, List<Token> tokens) throws IOException {
        List<String> linhas = tokens.stream()
                .map(Token::toString)
                .collect(Collectors.toList());

        Files.write(Paths.get(caminhoDestino), linhas);
    }
}
