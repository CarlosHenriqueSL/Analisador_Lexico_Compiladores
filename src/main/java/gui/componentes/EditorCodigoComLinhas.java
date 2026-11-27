package gui.componentes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * Controlador do componente {@code /fxml/code_editor.fxml} que sincroniza
 * a régua lateral de numeração de linhas com a área de edição do código-fonte.
 */
public class EditorCodigoComLinhas extends HBox {

    @FXML
    private TextArea reguaLinhas;

    @FXML
    private TextArea areaCodigo;

    public EditorCodigoComLinhas() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/code_editor.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao carregar /fxml/code_editor.fxml", e);
        }

        reguaLinhas.scrollTopProperty().bindBidirectional(areaCodigo.scrollTopProperty());
        areaCodigo.textProperty().addListener((obs, antigo, novo) -> atualizarNumeracaoLinhas(novo));
    }

    private void atualizarNumeracaoLinhas(String texto) {
        if (texto == null || texto.isEmpty()) {
            reguaLinhas.setText("1");
            return;
        }
        int totalLinhas = 1;
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == '\n') {
                totalLinhas++;
            }
        }
        StringBuilder sb = new StringBuilder(totalLinhas * 4);
        for (int linha = 1; linha <= totalLinhas; linha++) {
            sb.append(linha);
            if (linha < totalLinhas) {
                sb.append('\n');
            }
        }
        reguaLinhas.setText(sb.toString());
    }

    public void irParaLinha(int numeroLinha) {
        String texto = areaCodigo.getText();
        if (texto == null || texto.isEmpty() || numeroLinha < 1) {
            areaCodigo.requestFocus();
            return;
        }

        int linhaAtual = 1;
        int inicioLinha = 0;
        int fimLinha = texto.length();

        for (int i = 0; i < texto.length(); i++) {
            if (linhaAtual == numeroLinha) {
                inicioLinha = i;
                int proximaQuebra = texto.indexOf('\n', i);
                fimLinha = (proximaQuebra >= 0) ? proximaQuebra : texto.length();
                break;
            }
            if (texto.charAt(i) == '\n') {
                linhaAtual++;
                if (linhaAtual == numeroLinha && i + 1 <= texto.length()) {
                    inicioLinha = i + 1;
                    int proximaQuebra = texto.indexOf('\n', inicioLinha);
                    fimLinha = (proximaQuebra >= 0) ? proximaQuebra : texto.length();
                    break;
                }
            }
        }

        areaCodigo.requestFocus();
        areaCodigo.selectRange(inicioLinha, fimLinha);
    }

    public String getTexto() {
        return areaCodigo.getText();
    }

    public void setTexto(String texto) {
        areaCodigo.setText(texto != null ? texto : "");
        areaCodigo.positionCaret(0);
    }
}