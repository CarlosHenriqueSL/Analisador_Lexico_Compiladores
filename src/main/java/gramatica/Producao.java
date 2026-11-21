package gramatica;

import java.util.List;

public record Producao(int numero, String esquerda, List<String> direita) {

    @Override
    public String toString() {
        return "(" + numero + ")" + esquerda + " ->" + (direita.isEmpty() ? "ε" : String.join(" ", direita));
    }
}
