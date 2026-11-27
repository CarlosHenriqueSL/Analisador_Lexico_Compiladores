package gui;

import compilador.Compilador;
import compilador.dto.DadosInspecaoSLR;
import compilador.dto.DadosInspecaoSLR.LinhaFirstFollowDTO;
import compilador.dto.DadosInspecaoSLR.LinhaProducaoDTO;
import compilador.dto.DadosInspecaoSLR.LinhaTabelaSLRDTO;
import compilador.dto.ItemErroDTO;
import compilador.dto.ItemSimboloDTO;
import compilador.dto.ItemTokenDTO;
import compilador.dto.ResultadoCompilacao;
import compilador.dto.ResultadoLexico;
import compilador.dto.ResultadoSintatico;
import gui.componentes.EditorCodigoComLinhas;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.layout.VBox;
import util.GerenciadorArquivos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador FXML da interface principal do Compilador Mini_Pascal.
 * Conecta os componentes declarados em {@code /fxml/compiler_view.fxml} à Facade {@link Compilador}.
 */
public class CompiladorController implements Initializable {

    // Barra de ferramentas e editor
    @FXML private ComboBox<String> comboExemplos;
    @FXML private Label lblArquivoAtivo;
    @FXML private EditorCodigoComLinhas editorCodigo;
    @FXML private TabPane abasResultados;
    @FXML private Tab abaSintatico;
    @FXML private Tab abaLexico;
    @FXML private Label lblStatusRodape;

    // Aba 1: Análise Sintática SLR(1)
    @FXML private VBox cardBannerSintatico;
    @FXML private Label lblVereditoSintatico;
    @FXML private Label lblDetalhesSintatico;
    @FXML private TableView<ItemErroDTO> tabelaErros;
    @FXML private TableColumn<ItemErroDTO, Number> colErroLinha;
    @FXML private TableColumn<ItemErroDTO, String> colErroTipo;
    @FXML private TableColumn<ItemErroDTO, String> colErroToken;
    @FXML private TableColumn<ItemErroDTO, String> colErroDescricao;
    @FXML private TextArea areaSaidaOficialSintatica;

    // Aba 2: Análise Léxica
    @FXML private Label lblResumoLexico;
    @FXML private TableView<ItemTokenDTO> tabelaTokens;
    @FXML private TableColumn<ItemTokenDTO, Number> colTokenLinha;
    @FXML private TableColumn<ItemTokenDTO, String> colTokenLexema;
    @FXML private TableColumn<ItemTokenDTO, String> colTokenTipo;
    @FXML private TableColumn<ItemTokenDTO, String> colTokenPar;

    // Aba 3: Tabela de Símbolos
    @FXML private TextField campoBuscaSimbolos;
    @FXML private CheckBox chkOcultarReservadas;
    @FXML private TableView<ItemSimboloDTO> tabelaSimbolos;
    @FXML private TableColumn<ItemSimboloDTO, Number> colSimboloIdx;
    @FXML private TableColumn<ItemSimboloDTO, String> colSimboloLexema;
    @FXML private TableColumn<ItemSimboloDTO, String> colSimboloCategoria;
    @FXML private TableColumn<ItemSimboloDTO, String> colSimboloToken;
    @FXML private TableColumn<ItemSimboloDTO, String> colSimboloLinha;

    // Aba 4: Inspeção SLR(1)
    @FXML private Label lblMetricasSLR;
    @FXML private TableView<LinhaProducaoDTO> tblProducoes;
    @FXML private TableColumn<LinhaProducaoDTO, Number> colProdId;
    @FXML private TableColumn<LinhaProducaoDTO, String> colProdEsq;
    @FXML private TableColumn<LinhaProducaoDTO, String> colProdDir;

    @FXML private TableView<LinhaFirstFollowDTO> tblFirstFollow;
    @FXML private TableColumn<LinhaFirstFollowDTO, String> colFfNt;
    @FXML private TableColumn<LinhaFirstFollowDTO, String> colFfFirst;
    @FXML private TableColumn<LinhaFirstFollowDTO, String> colFfFollow;

    @FXML private TableView<LinhaTabelaSLRDTO> tblSlr;
    @FXML private TableColumn<LinhaTabelaSLRDTO, Number> colSlrEstado;
    @FXML private TableColumn<LinhaTabelaSLRDTO, String> colSlrTabela;
    @FXML private TableColumn<LinhaTabelaSLRDTO, String> colSlrSimbolo;
    @FXML private TableColumn<LinhaTabelaSLRDTO, String> colSlrAcao;

    // Estado e serviços
    private final Compilador compilador = new Compilador();
    private final ObservableList<ItemErroDTO> dadosErros = FXCollections.observableArrayList();
    private final ObservableList<ItemTokenDTO> dadosTokens = FXCollections.observableArrayList();
    private final ObservableList<ItemSimboloDTO> dadosSimbolos = FXCollections.observableArrayList();
    private FilteredList<ItemSimboloDTO> dadosSimbolosFiltrados;

    private Map<String, String> catalogoExemplos;
    private ResultadoCompilacao ultimoResultadoCompilacao;
    private ResultadoLexico ultimoResultadoLexico;
    private File arquivoAtual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabelaErros();
        configurarTabelaTokens();
        configurarTabelaSimbolos();
        carregarDadosInspecaoSLR();
        carregarExemplosNoCombo();
    }

    // =========================================================================
    // CONFIGURAÇÃO DE COLUNAS E FILTROS DAS TABELAS
    // =========================================================================

    private void configurarTabelaErros() {
        colErroLinha.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().linha()));
        colErroTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipoErro()));
        colErroToken.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tokenEncontrado()));
        colErroDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().descricao()));

        colErroTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("erro-lexico", "erro-sintatico");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add(item.toLowerCase().contains("léxico") ? "erro-lexico" : "erro-sintatico");
                }
            }
        });

        tabelaErros.setItems(dadosErros);
        tabelaErros.setRowFactory(tv -> {
            TableRow<ItemErroDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    int linha = row.getItem().linha();
                    editorCodigo.irParaLinha(linha);
                    lblStatusRodape.setText("Editor posicionado na linha " + linha + ".");
                }
            });
            return row;
        });
    }

    private void configurarTabelaTokens() {
        colTokenLinha.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().linha()));
        colTokenLexema.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lexema()));
        colTokenTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().token()));
        colTokenPar.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().formatarPar()));
        tabelaTokens.setItems(dadosTokens);
    }

    private void configurarTabelaSimbolos() {
        colSimboloIdx.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().indice()));
        colSimboloLexema.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lexema()));
        colSimboloCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().categoria()));
        colSimboloToken.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().token()));
        colSimboloLinha.setCellValueFactory(c -> {
            int linha = c.getValue().linhaDeclaracao();
            return new SimpleStringProperty(linha == 0 ? "Pré-carregada" : "Linha " + linha);
        });

        dadosSimbolosFiltrados = new FilteredList<>(dadosSimbolos, s -> true);
        tabelaSimbolos.setItems(dadosSimbolosFiltrados);

        Runnable aplicarFiltro = () -> {
            String termo = campoBuscaSimbolos.getText() != null ? campoBuscaSimbolos.getText().trim().toLowerCase() : "";
            boolean apenasIdentificadores = chkOcultarReservadas.isSelected();
            dadosSimbolosFiltrados.setPredicate(item -> {
                if (apenasIdentificadores && "Palavra Reservada".equalsIgnoreCase(item.categoria())) {
                    return false;
                }
                if (termo.isEmpty()) {
                    return true;
                }
                return item.lexema().toLowerCase().contains(termo)
                        || item.categoria().toLowerCase().contains(termo);
            });
        };

        campoBuscaSimbolos.textProperty().addListener((obs, o, n) -> aplicarFiltro.run());
        chkOcultarReservadas.selectedProperty().addListener((obs, o, n) -> aplicarFiltro.run());
    }

    private void carregarDadosInspecaoSLR() {
        DadosInspecaoSLR dados = compilador.getDadosInspecaoSLR();

        lblMetricasSLR.setText(String.format(
                "Gramática Mini_Pascal Aumentada — Produções: %s | Não-Terminais: %s | Estados LR(0)/SLR(1): %s",
                dados.resumoMetricas().getOrDefault("Total de Produções", "0"),
                dados.resumoMetricas().getOrDefault("Não-Terminais", "0"),
                dados.resumoMetricas().getOrDefault("Estados LR(0)/SLR(1)", "0")
        ));

        colProdId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id()));
        colProdEsq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ladoEsquerdo()));
        colProdDir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ladoDireito()));
        tblProducoes.setItems(FXCollections.observableArrayList(dados.producoes()));

        colFfNt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().naoTerminal()));
        colFfFirst.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().first()));
        colFfFollow.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().follow()));
        tblFirstFollow.setItems(FXCollections.observableArrayList(dados.conjuntosFirstFollow()));

        colSlrEstado.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().estado()));
        colSlrTabela.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipoTabela()));
        colSlrSimbolo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().simbolo()));
        colSlrAcao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().acao()));
        tblSlr.setItems(FXCollections.observableArrayList(dados.linhasTabelaSLR()));
    }

    private void carregarExemplosNoCombo() {
        catalogoExemplos = new LinkedHashMap<>();

        catalogoExemplos.put("[Exemplo] Programa Mínimo Válido",
                """
                program minimo;
                begin
                  x := 10;
                end.
                """);

        catalogoExemplos.put("[Exemplo] Variáveis, Procedimento, If e While",
                """
                program fatorial_demo;
                var n, fat, i: integer;
                procedure calcular(var res: integer; limite: integer);
                begin
                  res := 1;
                  i := 1;
                  while i <= limite do
                  begin
                    res := res * i;
                    i := i + 1;
                  end;
                end;
                begin
                  n := 5;
                  if n >= 0 then
                    calcular(fat, n)
                  else
                    fat := 0;
                end.
                """);

        catalogoExemplos.put("[Exemplo] Programa com Erro Sintático (Falta ';')",
                """
                program erro_sintaxe;
                var a, b: integer
                begin
                  a := 1 + 2 * 3;
                end.
                """);

        carregarArquivosDeTeste(Paths.get("testes", "sintatico", "entradas"), "[Sintático] ");
        carregarArquivosDeTeste(Paths.get("testes", "entradas"), "[Léxico] ");

        comboExemplos.setItems(FXCollections.observableArrayList(catalogoExemplos.keySet()));
    }

    private void carregarArquivosDeTeste(Path diretorio, String prefixo) {
        List<Path> arquivos = GerenciadorArquivos.listarArquivosTxt(diretorio);
        for (Path arquivo : arquivos) {
            try {
                catalogoExemplos.put(prefixo + arquivo.getFileName(), GerenciadorArquivos.lerArquivo(arquivo));
            } catch (IOException ignored) {
            }
        }
    }

    // =========================================================================
    // AÇÕES DA INTERFACE (@FXML)
    // =========================================================================

    @FXML
    private void acaoAbrirArquivo() {
        FileChooser seletor = criarSeletorArquivoTxt("Abrir Código-Fonte Mini_Pascal (.txt)");
        File arquivo = seletor.showOpenDialog(obterJanela());
        if (arquivo == null) {
            return;
        }
        try {
            String conteudo = GerenciadorArquivos.lerArquivo(arquivo);
            this.arquivoAtual = arquivo;
            editorCodigo.setTexto(conteudo);
            lblArquivoAtivo.setText("Arquivo: " + arquivo.getAbsolutePath());
            lblStatusRodape.setText("Arquivo carregado com sucesso (" + arquivo.getName() + ").");
        } catch (IOException ex) {
            exibirAlertaErro("Erro ao Abrir Arquivo", "Não foi possível ler o arquivo:\n" + ex.getMessage());
        }
    }

    @FXML
    private void acaoSalvarCodigo() {
        FileChooser seletor = criarSeletorArquivoTxt("Salvar Código-Fonte Mini_Pascal (.txt)");
        seletor.setInitialFileName(arquivoAtual != null ? arquivoAtual.getName() : "programa_minipascal.txt");
        File arquivo = seletor.showSaveDialog(obterJanela());
        if (arquivo == null) {
            return;
        }
        try {
            GerenciadorArquivos.salvarArquivo(arquivo, editorCodigo.getTexto());
            this.arquivoAtual = arquivo;
            lblArquivoAtivo.setText("Arquivo: " + arquivo.getAbsolutePath());
            lblStatusRodape.setText("Código-fonte salvo em UTF-8: " + arquivo.getName());
        } catch (IOException ex) {
            exibirAlertaErro("Erro ao Salvar Arquivo", "Não foi possível gravar o arquivo:\n" + ex.getMessage());
        }
    }

    @FXML
    private void acaoSelecionarExemplo() {
        String chave = comboExemplos.getValue();
        if (chave != null && catalogoExemplos.containsKey(chave)) {
            editorCodigo.setTexto(catalogoExemplos.get(chave));
            lblArquivoAtivo.setText("Exemplo carregado: " + chave);
            lblStatusRodape.setText("Caso de teste carregado. Clique em 'Compilar (Sintático SLR)' para analisar.");
        }
    }

    @FXML
    private void acaoExecutarLexico() {
        try {
            this.ultimoResultadoLexico = compilador.analisarLexico(editorCodigo.getTexto());
            atualizarAbaLexicaESimbolos(this.ultimoResultadoLexico);
            abasResultados.getSelectionModel().select(abaLexico);
            lblStatusRodape.setText(String.format(
                    "Análise Léxica concluída em %d ms — %d token(s), %d erro(s) léxico(s).",
                    ultimoResultadoLexico.tempoExecucaoMs(),
                    ultimoResultadoLexico.tokens().size(),
                    ultimoResultadoLexico.errosLexicos().size()
            ));
        } catch (Exception ex) {
            exibirAlertaErro("Falha na Análise Léxica", ex.getMessage());
        }
    }

    @FXML
    private void acaoExecutarCompilacaoCompleta() {
        try {
            this.ultimoResultadoCompilacao = compilador.compilar(editorCodigo.getTexto());
            this.ultimoResultadoLexico = this.ultimoResultadoCompilacao.resultadoLexico();

            atualizarAbaLexicaESimbolos(this.ultimoResultadoLexico);
            atualizarAbaSintatica(
                    this.ultimoResultadoCompilacao.resultadoSintatico(),
                    this.ultimoResultadoLexico.tokens().size()
            );
            abasResultados.getSelectionModel().select(abaSintatico);
        } catch (Exception ex) {
            exibirAlertaErro("Falha na Análise Sintática SLR(1)", ex.getMessage());
        }
    }

    @FXML
    private void acaoExportarSintatico() {
        if (ultimoResultadoCompilacao == null) {
            exibirAlertaErro("Exportação Sintática", "Execute a Análise Sintática SLR(1) antes de exportar.");
            return;
        }
        FileChooser seletor = criarSeletorArquivoTxt("Exportar Resultado da Análise Sintática (.txt)");
        seletor.setInitialFileName("saida_sintatico.txt");
        File destino = seletor.showSaveDialog(obterJanela());
        if (destino == null) {
            return;
        }
        try {
            GerenciadorArquivos.salvarArquivo(destino, ultimoResultadoCompilacao.resultadoSintatico().saidaOficial());
            lblStatusRodape.setText("Resultado sintático exportado para: " + destino.getAbsolutePath());
        } catch (IOException ex) {
            exibirAlertaErro("Exportação Sintática", ex.getMessage());
        }
    }

    @FXML
    private void acaoExportarLexico() {
        if (ultimoResultadoLexico == null) {
            exibirAlertaErro("Exportação Léxica", "Execute a Análise Léxica antes de exportar os tokens.");
            return;
        }
        FileChooser seletor = criarSeletorArquivoTxt("Exportar Listagem de Tokens <lexema, token> (.txt)");
        seletor.setInitialFileName("saida_lexico.txt");
        File destino = seletor.showSaveDialog(obterJanela());
        if (destino == null) {
            return;
        }
        try {
            GerenciadorArquivos.salvarArquivo(destino, ultimoResultadoLexico.gerarTextoExportacao());
            lblStatusRodape.setText("Listagem léxica exportada para: " + destino.getAbsolutePath());
        } catch (IOException ex) {
            exibirAlertaErro("Exportação Léxica", ex.getMessage());
        }
    }

    @FXML
    private void acaoLimpar() {
        this.ultimoResultadoCompilacao = null;
        this.ultimoResultadoLexico = null;
        this.arquivoAtual = null;

        editorCodigo.setTexto("");
        dadosErros.clear();
        dadosTokens.clear();
        dadosSimbolos.clear();
        areaSaidaOficialSintatica.clear();

        cardBannerSintatico.getStyleClass().removeAll("status-ok", "status-erro");
        if (!cardBannerSintatico.getStyleClass().contains("status-neutro")) {
            cardBannerSintatico.getStyleClass().add("status-neutro");
        }
        lblVereditoSintatico.setText("AGUARDANDO ANÁLISE");
        lblDetalhesSintatico.setText("Editor e resultados limpos. Insira um código Mini_Pascal para compilar.");
        lblArquivoAtivo.setText("Nenhum arquivo selecionado (buffer em memória)");
        lblStatusRodape.setText("Workspace limpo.");
    }

    // =========================================================================
    // AUXILIARES DE ATUALIZAÇÃO VISUAL
    // =========================================================================

    private void atualizarAbaSintatica(ResultadoSintatico sintatico, int totalTokens) {
        dadosErros.setAll(sintatico.erros());
        areaSaidaOficialSintatica.setText(sintatico.saidaOficial());
        cardBannerSintatico.getStyleClass().removeAll("status-neutro", "status-ok", "status-erro");

        if (sintatico.valido()) {
            cardBannerSintatico.getStyleClass().add("status-ok");
            lblVereditoSintatico.setText("OK");
            lblDetalhesSintatico.setText(String.format(
                    "Programa sintaticamente válido segundo a gramática SLR(1) de Mini_Pascal (%d tokens analisados em %d ms).",
                    totalTokens, sintatico.tempoExecucaoMs()
            ));
            lblStatusRodape.setText("Compilação concluída com sucesso: OK");
        } else {
            cardBannerSintatico.getStyleClass().add("status-erro");
            lblVereditoSintatico.setText("ERRO DE COMPILAÇÃO (" + sintatico.erros().size() + " ocorrência(s))");
            lblDetalhesSintatico.setText(String.format(
                    "Foram detectados erros durante a análise em %d ms. Dê duplo clique em um item abaixo para ir à linha.",
                    sintatico.tempoExecucaoMs()
            ));
            lblStatusRodape.setText("Compilação falhou com " + sintatico.erros().size() + " erro(s) detectado(s).");
        }
    }

    private void atualizarAbaLexicaESimbolos(ResultadoLexico lexico) {
        dadosTokens.setAll(lexico.tokens());
        dadosSimbolos.setAll(lexico.tabelaSimbolos());
        lblResumoLexico.setText(String.format(
                "Total de tokens válidos: %d | Erros léxicos: %d | Tempo: %d ms",
                lexico.tokens().size(), lexico.errosLexicos().size(), lexico.tempoExecucaoMs()
        ));
    }

    private FileChooser criarSeletorArquivoTxt(String titulo) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle(titulo);
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Texto (*.txt)", "*.txt"));

        if (arquivoAtual != null && arquivoAtual.getParentFile() != null && arquivoAtual.getParentFile().exists()) {
            seletor.setInitialDirectory(arquivoAtual.getParentFile());
        } else {
            Path pastaTestes = Paths.get("testes", "sintatico", "entradas");
            seletor.setInitialDirectory(Files.isDirectory(pastaTestes) ? pastaTestes.toFile() : new File(System.getProperty("user.dir")));
        }
        return seletor;
    }

    private Window obterJanela() {
        return editorCodigo.getScene() != null ? editorCodigo.getScene().getWindow() : null;
    }

    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}