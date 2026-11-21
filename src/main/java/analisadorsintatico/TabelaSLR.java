package analisadorsintatico;

import gramatica.ConjuntosFirstFollow;
import gramatica.GramaticaMiniPascal;
import gramatica.Producao;

import java.util.*;

/**
 * Tabelas ACTION e GOTO do analisador SLR(1).
 *
 * Regras de construção, para cada estado i do autômato LR(0):
 *  - item A -> α • a β  e transição i --a--> j      => ACTION[i][a] = shift j
 *  - item A -> α •  (A diferente de S')              => ACTION[i][a] = reduce A->α, para todo a em FOLLOW(A)
 *  - item S' -> PROG •                               => ACTION[i][$] = accept
 *  - transição i --B--> j com B não-terminal         => GOTO[i][B] = j
 *
 * Conflitos: shift/reduce -> fica o SHIFT (liga o else ao if mais próximo);
 *            reduce/reduce -> fica a regra de menor número. Todos são guardados em conflitos().
 */
public class TabelaSLR {
    private final List<Map<String, Acao>> action = new ArrayList<>();
    private final List<Map<String, Integer>> goTo = new ArrayList<>();
    private final List<String> conflitos = new ArrayList<>();
    private final GramaticaMiniPascal gramatica;

    public TabelaSLR(GramaticaMiniPascal g, AutomatoLR0 automato, ConjuntosFirstFollow ff) {
        this.gramatica = g;
        for (int i = 0; i < automato.numEstados(); i++) {
            action.add(new LinkedHashMap<>());
            goTo.add(new LinkedHashMap<>());
        }
        for (int i = 0; i < automato.numEstados(); i++) {
            for (AutomatoLR0.Item item : automato.itens(i)) {
                if (!item.completo()) continue;
                Producao p = item.producao();
                if (p.numero() == 0) {
                    inserir(i, GramaticaMiniPascal.FIM, new Acao('A', 0));
                } else {
                    for (String a : ff.follow(p.esquerda())) inserir(i, a, new Acao('R', p.numero()));
                }
            }
            for (Map.Entry<String, Integer> t : automato.transicoes(i).entrySet()) {
                if (g.ehNaoTerminal(t.getKey())) goTo.get(i).put(t.getKey(), t.getValue());
                else inserir(i, t.getKey(), new Acao('S', t.getValue()));
            }
        }
    }

    private void inserir(int estado, String terminal, Acao nova) {
        Acao atual = action.get(estado).get(terminal);
        if (atual == null || atual.equals(nova)) {
            action.get(estado).put(terminal, nova);
            return;
        }
        Acao escolhida;
        if (atual.tipo() == 'S') escolhida = atual;
        else if (nova.tipo() == 'S') escolhida = nova;
        else escolhida = atual.valor() <= nova.valor() ? atual : nova;
        action.get(estado).put(terminal, escolhida);
        conflitos.add("estado " + estado + ", terminal '" + terminal + "': " + atual + " x " + nova + " -> mantido " + escolhida);
    }

    /** Regra de número n (usada pelo parser ao reduzir). */
    public Producao regra(int numero) { return gramatica.getProducoes().get(numero); }

    /** Ação para (estado, terminal), ou null (= erro sintático). */
    public Acao acao(int estado, String terminal) { return action.get(estado).get(terminal); }

    /** Estado de destino do GOTO, ou null. */
    public Integer irPara(int estado, String naoTerminal) { return goTo.get(estado).get(naoTerminal); }

    /** Terminais que o estado sabe tratar (usado em "esperado ..."). */
    public Set<String> terminaisEsperados(int estado) { return action.get(estado).keySet(); }

    public int numEstados() { return action.size(); }

    public List<String> conflitos() { return conflitos; }
}