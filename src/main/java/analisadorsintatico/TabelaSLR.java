package analisadorsintatico;

import gramatica.ConjuntosFirstFollow;
import gramatica.GramaticaMiniPascal;
import gramatica.Producao;

import java.util.*;

/**
 * Tabelas ACTION e GOTO do analisador SLR(1).
 *
 * Regras de construção, para cada estado i do autômato LR(0):
 *  - item A -> α • a β e transição i --a--> j       => ACTION[i][a] = shift j
 *  - item A -> α •  (A diferente de S')              => ACTION[i][a] = reduce A->α, para todo a em FOLLOW(A)
 *  - item S' -> PROG •                               => ACTION[i][$] = accept
 *  - transição i --B--> j com B não-terminal         => GOTO[i][B] = j
 *
 * Conflitos: accept vence qualquer outra ação; shift/reduce -> fica o SHIFT (liga o else ao if mais próximo);
 *            reduce/reduce -> fica a regra de menor número. Todos são guardados em conflitos().
 *
 * Contratos: a produção de índice 0 é S' -> PROG, e o número de cada produção é igual ao seu índice na lista.
 */
public class TabelaSLR {
    private final List<Map<String, Acao>> action = new ArrayList<>();
    private final List<Map<String, Integer>> goTo = new ArrayList<>();
    private final List<String> conflitos = new ArrayList<>();
    private final GramaticaMiniPascal gramatica;

    public TabelaSLR(GramaticaMiniPascal g, AutomatoLR0 automato, ConjuntosFirstFollow ff) {
        this.gramatica = g;
        validarNumeracao(g);
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

    /** O parser usa numero() como índice em getProducoes(); garante que os dois coincidem. */
    private static void validarNumeracao(GramaticaMiniPascal g) {
        List<Producao> producoes = g.getProducoes();
        for (int i = 0; i < producoes.size(); i++) {
            if (producoes.get(i).numero() != i) {
                throw new IllegalArgumentException("a produção de índice " + i + " tem número " + producoes.get(i).numero()
                        + "; o número deve ser igual ao índice");
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
        if (atual.tipo() == 'A') escolhida = atual;
        else if (nova.tipo() == 'A') escolhida = nova;
        else if (atual.tipo() == 'S') escolhida = atual;
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

    /**
     * Terminais que o estado sabe tratar (usado em "esperado ...").
     * Os que podem ser empilhados (shift) vêm primeiro, pois são o que o programador deveria ter escrito;
     * depois vêm os terminais de reduce (FOLLOW). Devolve uma cópia.
     */
    public Set<String> terminaisEsperados(int estado) {
        Set<String> resultado = new LinkedHashSet<>();
        Map<String, Acao> linha = action.get(estado);
        for (Map.Entry<String, Acao> e : linha.entrySet()) if (e.getValue().tipo() == 'S') resultado.add(e.getKey());
        for (Map.Entry<String, Acao> e : linha.entrySet()) if (e.getValue().tipo() != 'S') resultado.add(e.getKey());
        return resultado;
    }

    public int numEstados() { return action.size(); }

    /** Conflitos resolvidos durante a construção (visão somente leitura). */
    public List<String> conflitos() { return Collections.unmodifiableList(conflitos); }
}
