package gui;

/**
 * Ponto de entrada principal da aplicação desktop (Launcher) para execução em Fat-JAR no Windows.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        CompiladorApp.iniciar(args);
    }
}