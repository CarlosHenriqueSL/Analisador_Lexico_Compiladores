package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utilitário responsável pelas operações de entrada e saída (E/S) de arquivos texto (.txt)
 * do compilador Mini_Pascal, garantindo codificação UTF-8 consistente em ambiente Windows.
 */
public final class GerenciadorArquivos {

    /** Caractere Byte Order Mark (BOM) eventualmente inserido por editores Windows (ex.: Bloco de Notas). */
    private static final String UTF8_BOM = "\uFEFF";

    /**
     * Construtor privado para impedir instanciação desta classe utilitária.
     */
    private GerenciadorArquivos() {
        throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
    }

    /**
     * Lê todo o conteúdo de um arquivo texto utilizando explicitamente UTF-8.
     * Remove o marcador BOM caso esteja presente no início do arquivo.
     *
     * @param caminho Caminho do arquivo a ser lido.
     * @return Conteúdo textual do arquivo normalizado.
     * @throws IOException Caso ocorra falha de leitura ou o arquivo não exista.
     */
    public static String lerArquivo(Path caminho) throws IOException {
        if (caminho == null) {
            throw new IllegalArgumentException("O caminho do arquivo não pode ser nulo.");
        }
        String conteudo = Files.readString(caminho, StandardCharsets.UTF_8);
        if (conteudo.startsWith(UTF8_BOM)) {
            return conteudo.substring(1);
        }
        return conteudo;
    }

    /**
     * Sobrecarga de conveniência para leitura a partir de {@link File}.
     *
     * @param arquivo Instância de {@link File} a ser lida.
     * @return Conteúdo textual em UTF-8.
     * @throws IOException Caso ocorra erro de E/S.
     */
    public static String lerArquivo(File arquivo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("O arquivo informado não pode ser nulo.");
        }
        return lerArquivo(arquivo.toPath());
    }

    /**
     * Grava um conteúdo textual em arquivo utilizando codificação UTF-8.
     * Cria automaticamente os diretórios intermediários caso ainda não existam.
     *
     * @param caminho  Caminho de destino do arquivo.
     * @param conteudo Texto a ser gravado.
     * @throws IOException Caso ocorra falha de escrita em disco.
     */
    public static void salvarArquivo(Path caminho, String conteudo) throws IOException {
        if (caminho == null) {
            throw new IllegalArgumentException("O caminho de destino não pode ser nulo.");
        }
        Path diretorioPai = caminho.getParent();
        if (diretorioPai != null && !Files.exists(diretorioPai)) {
            Files.createDirectories(diretorioPai);
        }
        String textoSeguro = (conteudo != null) ? conteudo : "";
        Files.writeString(caminho, textoSeguro, StandardCharsets.UTF_8);
    }

    /**
     * Sobrecarga de conveniência para gravação a partir de {@link File}.
     *
     * @param arquivo  Arquivo de destino.
     * @param conteudo Texto a ser gravado.
     * @throws IOException Caso ocorra falha de escrita.
     */
    public static void salvarArquivo(File arquivo, String conteudo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("O arquivo de destino não pode ser nulo.");
        }
        salvarArquivo(arquivo.toPath(), conteudo);
    }

    /**
     * Lista todos os arquivos com extensão {@code .txt} existentes em um diretório,
     * ordenados alfabeticamente pelo nome do arquivo.
     *
     * @param diretorio Caminho do diretório a ser inspecionado.
     * @return Lista ordenada de caminhos de arquivos {@code .txt}, ou lista vazia se o diretório não existir.
     */
    public static List<Path> listarArquivosTxt(Path diretorio) {
        if (diretorio == null || !Files.isDirectory(diretorio)) {
            return Collections.emptyList();
        }
        try (Stream<Path> fluxo = Files.list(diretorio)) {
            return fluxo
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".txt"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}