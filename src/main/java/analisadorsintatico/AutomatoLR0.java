package analisadorsintatico;

import gramatica.GramaticaMiniPascal;
import gramatica.Producao;

import java.util.*;

/**
 * Autômato LR(0): cada estado é um conjunto de "itens". Um item é uma regra com um ponto (•)
 * que marca até onde o lado direito já foi lido. Ex.: COMANDO -> if EXPR • then COMANDO
 *
 * Contrato: a produção de índice 0 da gramática é a produção aumentada S' -> PROG.
 */
public class AutomatoLR0 {

    /** Regra + posição do ponto (0 = antes do primeiro símbolo). */
    public record Item(Producao producao, int ponto) {
        public boolean completo() { return ponto >= producao.direita().size(); }

        /** Símbolo logo depois do ponto, ou null se o item está completo. */
        public String simboloAposPonto() { return completo() ? null : producao.direita().get(ponto); }

        public Item avancar() { return new Item(producao, ponto + 1); }

        @Override
        public String toString() {
            List<String> d = new ArrayList<>(producao.direita());
            d.add(Math.min(ponto, d.size()), "•");
            return producao.esquerda() + " -> " + String.join(" ", d);
        }
    }

    private final GramaticaMiniPascal g;
    private final List<Set<Item>> estados = new ArrayList<>();
    private final List<Map<String, Integer>> transicoes = new ArrayList<>();   // estado -> (símbolo -> estado)

    public AutomatoLR0(GramaticaMiniPascal g) {
        this.g = g;
        if (g.getProducoes().isEmpty()) {
            throw new IllegalArgumentException("a gramática não possui produções");
        }
        construir();
    }

    /** Fecho: se há um item com o ponto antes de um não-terminal A, entram também todas as regras de A com o ponto no início. */
    private Set<Item> fecho(Set<Item> itens) {
        Set<Item> resultado = new LinkedHashSet<>(itens);
        Deque<Item> fila = new ArrayDeque<>(itens);
        while (!fila.isEmpty()) {
            String s = fila.poll().simboloAposPonto();
            if (s != null && g.ehNaoTerminal(s)) {
                for (Producao p : g.producoesDe(s)) {
                    Item novo = new Item(p, 0);
                    if (resultado.add(novo)) fila.add(novo);
                }
            }
        }
        return resultado;
    }

    /** Estado alcançado a partir de "itens" lendo o símbolo x. */
    private Set<Item> irPara(Set<Item> itens, String x) {
        Set<Item> nucleo = new LinkedHashSet<>();
        for (Item it : itens) if (x.equals(it.simboloAposPonto())) nucleo.add(it.avancar());
        return fecho(nucleo);
    }

    private void construir() {
        Map<Set<Item>, Integer> indice = new HashMap<>();
        Set<Item> inicial = fecho(Set.of(new Item(g.getProducoes().get(0), 0)));
        estados.add(inicial);
        indice.put(inicial, 0);
        transicoes.add(new LinkedHashMap<>());

        for (int i = 0; i < estados.size(); i++) {          // a lista cresce durante o laço
            Set<String> simbolos = new LinkedHashSet<>();
            for (Item it : estados.get(i)) if (!it.completo()) simbolos.add(it.simboloAposPonto());
            for (String x : simbolos) {
                Set<Item> destino = irPara(estados.get(i), x);
                Integer j = indice.get(destino);
                if (j == null) {
                    j = estados.size();
                    estados.add(destino);
                    indice.put(destino, j);
                    transicoes.add(new LinkedHashMap<>());
                }
                transicoes.get(i).put(x, j);
            }
        }
    }

    public int numEstados() { return estados.size(); }

    /** Itens do estado (visão somente leitura). */
    public Set<Item> itens(int estado) { return Collections.unmodifiableSet(estados.get(estado)); }

    /** Transições do estado (visão somente leitura). */
    public Map<String, Integer> transicoes(int estado) { return Collections.unmodifiableMap(transicoes.get(estado)); }
}
