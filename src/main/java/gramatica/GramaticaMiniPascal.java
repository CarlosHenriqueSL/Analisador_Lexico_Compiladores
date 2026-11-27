package gramatica;

import java.util.*;

public class GramaticaMiniPascal {
    public static final String FIM = "$";
    public static final String ERRO = "error";

    private final List<Producao> producoes = new ArrayList<>();
    private final Set<String> naoTerminais = new LinkedHashSet<>();

    public GramaticaMiniPascal() {
        montarRegras();
        for (Producao p : producoes) {
            naoTerminais.add(p.esquerda());
        }
    }

    private void novaRegra(String esquerda, String direita) {
        List<String> simbolos = direita.isBlank() ? List.of() : Arrays.asList(direita.trim().split("\\s+"));
        producoes.add(new Producao(producoes.size(), esquerda, simbolos));
    }

    private void montarRegras() {
        novaRegra("S'", "PROG");
        novaRegra("PROG", "program ident ; BLOCO .");
        novaRegra("BLOCO", "DECLVARS DECLPROCS begin LISTACMD end");
        novaRegra("DECLVARS", "DECLVARS SECVAR");
        novaRegra("DECLVARS",  "");
        novaRegra("SECVAR", "var DECLS");
        novaRegra("DECLS", "DECLS DECL");
        novaRegra("DECLS", "DECL");
        novaRegra("DECL", "LISTAID : tipo ;");
        novaRegra("LISTAID", "LISTAID , ident");
        novaRegra("LISTAID", "ident");
        novaRegra("DECLPROCS", "DECLPROCS DECLPROC");
        novaRegra("DECLPROCS", "");
        novaRegra("DECLPROC", "procedure ident PALIST ; BLOCO ;");
        novaRegra("PALIST", "( PARAMS )");
        novaRegra("PALIST", "");
        novaRegra("PARAMS", "PARAMS ; PARAM");
        novaRegra("PARAMS", "PARAM");
        novaRegra("PARAM", "var LISTAID : tipo");
        novaRegra("PARAM", "LISTAID : tipo");
        novaRegra("LISTACMD", "LISTACMD COMANDO ;");
        novaRegra("LISTACMD", "COMANDO ;");
        novaRegra("COMANDO", "ident := EXPR");
        novaRegra("COMANDO", "ident ( LISTAEXPR )");
        novaRegra("COMANDO", "if EXPR then COMANDO");
        novaRegra("COMANDO", "if EXPR then COMANDO else COMANDO");
        novaRegra("COMANDO", "while EXPR do COMANDO");
        novaRegra("COMANDO", "begin LISTACMD end");
        novaRegra("LISTAEXPR", "LISTAEXPR , EXPR");
        novaRegra("LISTAEXPR", "EXPR");
        novaRegra("EXPR", "SIEXPR");
        novaRegra("EXPR", "SIEXPR OPREL SIEXPR");
        novaRegra("OPREL", "=");
        novaRegra("OPREL", "<");
        novaRegra("OPREL", ">");
        novaRegra("OPREL", "<>");
        novaRegra("OPREL", "<=");
        novaRegra("OPREL", ">=");
        novaRegra("SIEXPR", "+ SOMA");
        novaRegra("SIEXPR", "- SOMA");
        novaRegra("SIEXPR", "SOMA");
        novaRegra("SOMA", "SOMA + TERMO");
        novaRegra("SOMA", "SOMA - TERMO");
        novaRegra("SOMA", "TERMO");
        novaRegra("TERMO", "TERMO * FATOR");
        novaRegra("TERMO", "TERMO / FATOR");
        novaRegra("TERMO", "TERMO div FATOR");
        novaRegra("TERMO", "TERMO mod FATOR");
        novaRegra("TERMO", "FATOR");
        novaRegra("FATOR", "num");
        novaRegra("FATOR", "ident");
        novaRegra("FATOR", "( EXPR )");

        novaRegra("PROG", "program error ; BLOCO .");
        novaRegra("DECL", "error ;");
        novaRegra("DECLPROG", "procedure error ; BLOCO ;");
        novaRegra("COMANDO", "error");
    }

    public List<Producao> getProducoes() {
        return producoes;
    }

    public Set<String> getNaoTerminais() {
        return naoTerminais;
    }

    public boolean ehNaoTerminal(String simbolo) {
        return naoTerminais.contains(simbolo);
    }

    // mudar nome do metodo
    public List<Producao> producoesDe(String naoTerminal) {
        List<Producao> r = new ArrayList<>();
        for (Producao p : producoes) {
            if (p.esquerda().equals(naoTerminal)) {
                r.add(p);
            }
        }

        return r;
    }

    // mudar nome do metodo
    public static String nomeAmigavel(String terminal) {
        switch (terminal) {
            case "ident": return "identificador";
            case "num": return "numero";
            case "tipo": return "tipo (integer, real, char ou string)";
            case FIM: return "fim do arquivo";
            default: return "'" + terminal + "'";
        }
    }
}
