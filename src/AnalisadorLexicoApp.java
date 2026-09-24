import analisadorlexico.AnalisadorLexico;
import analisadorlexico.Token;
import analisadorlexico.Tokens;
import tabelasimbolos.TabelaSimbolos;
import util.GerenciadorArquivos;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class AnalisadorLexicoApp extends Application {
	private final TextArea editor = new TextArea();
	private final TableView<Token> tabelaTokens = new TableView<>();
	private final ObservableList<Token> tokens = FXCollections.observableArrayList();
	private final Label arquivoLabel = new Label("Nenhum arquivo aberto");
	private final Label statusLabel = new Label("Pronto para analisar");
	private final Label resumoLabel = new Label("0 tokens");

	@Override
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("app-root");

		root.setTop(criarCabecalho(stage));
		root.setCenter(criarAreaPrincipal());
		root.setBottom(criarRodape());

		carregarExemplo();

		Scene scene = new Scene(root, 1180, 760);
		scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
		stage.setTitle("Mini Pascal | Analisador Léxico");
		stage.setMinWidth(800);
		stage.setMinHeight(560);
		stage.setScene(scene);
		stage.show();
	}

	private VBox criarCabecalho(Stage stage) {
		Label titulo = new Label("Analisador Léxico");
		titulo.getStyleClass().add("app-title");
		Label subtitulo = new Label("Mini Pascal  •  leitura de lexemas e tokens");
		subtitulo.getStyleClass().add("app-subtitle");

		Button abrir = criarBotao("Abrir arquivo", "button-secondary");
		abrir.setOnAction(event -> abrirArquivo(stage));
		Button analisar = criarBotao("Analisar código", "button-primary");
		analisar.setOnAction(event -> analisarCodigo());
		Button exportar = criarBotao("Exportar tokens", "button-secondary");
		exportar.setOnAction(event -> exportarTokens(stage));

		HBox identidade = new HBox(12, criarMarca(), new VBox(2, titulo, subtitulo));
		identidade.setAlignment(Pos.CENTER_LEFT);
		HBox acoes = new HBox(10, abrir, analisar, exportar);
		acoes.setAlignment(Pos.CENTER_RIGHT);
		HBox barra = new HBox(20, identidade, acoes);
		barra.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(identidade, Priority.ALWAYS);
		barra.getStyleClass().add("header-bar");

		arquivoLabel.getStyleClass().add("file-label");
		VBox cabecalho = new VBox(14, barra, arquivoLabel);
		cabecalho.getStyleClass().add("header");
		return cabecalho;
	}

	private Label criarMarca() {
		Label marca = new Label("LX");
		marca.getStyleClass().add("brand-mark");
		return marca;
	}

	private HBox criarAreaPrincipal() {
		VBox fonteBox = new VBox(10, criarTituloPainel("Fonte", "Código Mini Pascal"), editor);
		fonteBox.getStyleClass().add("panel");
		VBox.setVgrow(editor, Priority.ALWAYS);

		configurarTabela();
		VBox tokensBox = new VBox(10, criarTituloPainel("Resultado", "Tokens reconhecidos"), tabelaTokens);
		tokensBox.getStyleClass().add("panel");
		VBox.setVgrow(tabelaTokens, Priority.ALWAYS);

		HBox area = new HBox(14, fonteBox, tokensBox);
		area.getStyleClass().add("content-area");
		HBox.setHgrow(fonteBox, Priority.ALWAYS);
		HBox.setHgrow(tokensBox, Priority.ALWAYS);
		fonteBox.setPrefWidth(520);
		tokensBox.setPrefWidth(620);
		return area;
	}

	private HBox criarTituloPainel(String titulo, String descricao) {
		Label nome = new Label(titulo);
		nome.getStyleClass().add("panel-title");
		Label detalhe = new Label(descricao);
		detalhe.getStyleClass().add("panel-detail");
		HBox tituloBox = new HBox(9, nome, detalhe);
		tituloBox.setAlignment(Pos.BASELINE_LEFT);
		return tituloBox;
	}

	private void configurarTabela() {
		tabelaTokens.setItems(tokens);
		tabelaTokens.setPlaceholder(new Label("Execute uma análise para visualizar os tokens"));
		tabelaTokens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Token, Number> linha = new TableColumn<>("Linha");
		linha.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getLinha()));
		linha.setMaxWidth(75);
		TableColumn<Token, Number> coluna = new TableColumn<>("Col.");
		coluna.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getColuna()));
		coluna.setMaxWidth(65);
		TableColumn<Token, String> lexema = new TableColumn<>("Lexema");
		lexema.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLexema()));
		TableColumn<Token, String> tipo = new TableColumn<>("Token");
		tipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo().name()));
		tabelaTokens.getColumns().setAll(linha, coluna, lexema, tipo);
	}

	private HBox criarRodape() {
		resumoLabel.getStyleClass().add("summary-label");
		statusLabel.getStyleClass().add("status-label");
		HBox rodape = new HBox(14, statusLabel, resumoLabel);
		rodape.setAlignment(Pos.CENTER_LEFT);
		rodape.getStyleClass().add("footer");
		return rodape;
	}

	private Button criarBotao(String texto, String estilo) {
		Button botao = new Button(texto);
		botao.getStyleClass().add(estilo);
		botao.setTooltip(new Tooltip(texto));
		return botao;
	}

	private void abrirArquivo(Stage stage) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Abrir código Mini Pascal");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de texto", "*.txt", "*.pas", "*.p"));
		File arquivo = chooser.showOpenDialog(stage);
		if (arquivo == null) return;

		try {
			editor.setText(Files.readString(arquivo.toPath()));
			arquivoLabel.setText(arquivo.getName());
			tokens.clear();
			resumoLabel.setText("0 tokens");
			statusLabel.setText("Arquivo carregado. Pronto para analisar");
		} catch (IOException exception) {
			mostrarErro("Não foi possível abrir o arquivo", exception.getMessage());
		}
	}

	private void analisarCodigo() {
		if (editor.getText().isBlank()) {
			mostrarErro("Código vazio", "Digite ou abra um código-fonte antes de analisar.");
			return;
		}

		try {
			List<Token> resultado = new AnalisadorLexico(editor.getText(), new TabelaSimbolos()).analisarTudo();
			tokens.setAll(resultado);
			long erros = resultado.stream().filter(token -> token.getTipo() == Tokens.DESCONHECIDO).count();
			resumoLabel.setText(resultado.size() + (resultado.size() == 1 ? " token" : " tokens"));
			statusLabel.setText(erros == 0 ? "Análise concluída sem erros léxicos" : erros + " símbolo(s) desconhecido(s) encontrado(s)");
			statusLabel.getStyleClass().removeAll("status-success", "status-warning");
			statusLabel.getStyleClass().add(erros == 0 ? "status-success" : "status-warning");
		} catch (RuntimeException exception) {
			mostrarErro("Falha na análise léxica", exception.getMessage());
		}
	}

	private void exportarTokens(Stage stage) {
		if (tokens.isEmpty()) {
			mostrarErro("Nada para exportar", "Execute uma análise antes de exportar os tokens.");
			return;
		}
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Salvar listagem de tokens");
		chooser.setInitialFileName("tokens.txt");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo de texto", "*.txt"));
		File arquivo = chooser.showSaveDialog(stage);
		if (arquivo == null) return;
		try {
			GerenciadorArquivos.escreverArquivo(arquivo.getAbsolutePath(), tokens);
			statusLabel.setText("Listagem exportada para " + arquivo.getName());
		} catch (IOException exception) {
			mostrarErro("Não foi possível exportar", exception.getMessage());
		}
	}

	private void carregarExemplo() {
		editor.setText("program exemplo;\nvar x, y: integer;\nbegin\n  read(x);\n  if (x > y) then\n    y := x;\n  else\n    y := -x;\n  writeln(y);\nend.");
		arquivoLabel.setText("Exemplo integrado");
	}

	private void mostrarErro(String titulo, String mensagem) {
		Alert alerta = new Alert(Alert.AlertType.ERROR);
		alerta.setTitle("Analisador Léxico");
		alerta.setHeaderText(titulo);
		alerta.setContentText(mensagem == null ? "Ocorreu um erro inesperado." : mensagem);
		alerta.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
