package util;

import analisadorlexico.Token;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Path destino = Paths.get(caminhoDestino);
        // [CORREÇÃO 7] Garante que o diretório de saída exista antes de
        // escrever o arquivo. Antes, se "testes/saidas/" não existisse,
        // Files.write lançava IOException e o programa falhava.
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        Files.write(destino, linhas);
    }
}