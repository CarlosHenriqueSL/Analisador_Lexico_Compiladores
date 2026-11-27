package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Classe principal da aplicação JavaFX responsável exclusivamente por carregar
 * a interface declarativa em FXML e exibir a janela principal.
 */
public class CompiladorApp extends Application {

    public static void iniciar(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlUrl = getClass().getResource("/fxml/compiler_view.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Arquivo FXML não encontrado em /fxml/compiler_view.fxml");
        }

        Parent raiz = FXMLLoader.load(fxmlUrl);
        Scene cena = new Scene(raiz, 1280, 780);

        URL cssUrl = getClass().getResource("/app.css");
        if (cssUrl != null) {
            cena.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Compilador Mini_Pascal — Analisador Léxico e Sintático SLR(1) (UESB)");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(cena);
        primaryStage.show();
    }
}
