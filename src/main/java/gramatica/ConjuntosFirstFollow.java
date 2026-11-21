package gramatica;

import java.util.*;

public class ConjuntosFirstFollow {
    private final GramaticaMiniPascal g;
    private final Set<String> anulaveis = new HashSet<>();
    private final Map<String, Set<String>> first = new HashMap<>();
    private final Map<String, Set<String>> follow = new HashMap<>();

    public ConjuntosFirstFollow(GramaticaMiniPascal g) {
        this.g = g;
        for (String nt : g.getNaoTerminais()) {
            first.put(nt, new LinkedHashSet<>());
            follow.put(nt, new LinkedHashSet<>());
        }
        calcularAnulaveis();
        calcularFirst();
        calcularFollow();
    }

    private void calcularAnulaveis() {
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Producao p : g.getProducoes()) {
                if (!anulaveis.contains(p.esquerda()) && todosAnulaveis(p.direita(), 0)) {
                    anulaveis.add(p.esquerda());
                    mudou = true;
                }
            }
        }
    }

    // mudar o nome do segundo parametro (oq eh de?)
    private boolean todosAnulaveis(List<String> seq, int de) {
        for (int i = de; i < seq.size(); i++) {
            if (!anulaveis.contains(seq.get(i))) {
                return false;
            }
        }

        return true;
    }

    private void calcularFirst() {
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Producao p : g.getProducoes()) {
                mudou = mudou | first.get(p.esquerda()).addAll(firstDaSequencia(p.direita(), 0));
            }
        }
    }

    private void calcularFollow() {
        follow.get("S'").add(GramaticaMiniPascal.FIM);
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Producao p : g.getProducoes()) {
                List<String> d = p.direita();
                for (int i = 0; i < d.size(); i++) {
                    if (!g.ehNaoTerminal(d.get(i))) continue;

                    mudou = mudou | follow.get(d.get(i)).addAll(firstDaSequencia(d, i + 1));

                    if (todosAnulaveis(d, i + 1)) mudou = mudou | follow.get(d.get(i)).addAll(follow.get(p.esquerda()));
                }
            }
        }
    }

    public Set<String> firstDaSequencia(List<String> seq, int de) {
        Set<String> r = new LinkedHashSet<>();
        for (int i = de; i < seq.size(); i++) {
            String s = seq.get(i);
            if (!g.ehNaoTerminal(s)) { r.add(s); return r; }
            r.addAll(first.get(s));
            if (!anulaveis.contains(s)) return r;
        }
        return r;
    }

    public Set<String> first(String naoTerminal) { return first.get(naoTerminal); }

    public Set<String> follow(String naoTerminal) { return follow.get(naoTerminal); }
}
