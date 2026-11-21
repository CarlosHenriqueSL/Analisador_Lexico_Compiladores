package analisadorsintatico;

import analisadorlexico.AnalisadorLexico;
import analisadorlexico.Token;
import analisadorlexico.Tokens;
import analisadorsintatico.ErroSintatico.Tipo;
import gramatica.GramaticaMiniPascal;
import gramatica.Producao;

import java.util.*;

import static gramatica.GramaticaMiniPascal.ERRO;
import static gramatica.GramaticaMiniPascal.FIM;

/**
 * Analisador sintático SLR(1) (ascendente, shift-reduce).
 *
 * Funcionamento: uma pilha de estados (e outra, paralela, de símbolos). A cada passo olha-se
 * o estado do topo e o token atual na tabela ACTION:
 *   shift   -> empilha o estado e consome o token
 *   reduce  -> desempilha |lado direito| símbolos, consulta GOTO e empilha o não-terminal
 *   accept  -> programa correto
 *   (vazio) -> erro sintático
 *
 * O analisador léxico é chamado sob demanda (um token por vez), até devolver o token EOF.
 *
 * Recuperação de erros (para achar vários erros numa única passada):
 *  1) Reparo do ';': se inserir um ';' imaginário antes do token atual deixa a análise seguir
 *     (testado numa cópia da pilha), o erro é "falta ;" e a análise continua sem descartar nada.
 *  2) Modo pânico: desempilha até um estado que aceite o símbolo "error" (regras de erro da gramática),
 *     empilha "error" e descarta tokens até achar um que a tabela aceite. Sincronizadores: ";" e "end".
 *  3) Se o erro se repete na mesma posição (sem progresso), o token é descartado: a análise sempre termina.
 */
public class AnalisadorSintatico {
    private final TabelaSLR tabela;
    private final AnalisadorLexico lexico;

    private final Deque<Integer> estados = new ArrayDeque<>();    // topo = peek()
    private final Deque<String> simbolos = new ArrayDeque<>();
    private final List<ErroSintatico> erros = new ArrayList<>();
    private Token atual;                 // token que está sendo analisado
    private Token anterior;              // token consumido imediatamente antes
    private String virtual;              // terminal imaginário (o ';' do reparo)
    private int pos = -1;                // número de ordem do token atual
    private int posUltimoErro = -2;
    private boolean aposErroLexico;      // um símbolo desconhecido foi descartado logo antes do token atual

    public AnalisadorSintatico(TabelaSLR tabela, AnalisadorLexico lexico) {
        this.tabela = tabela;
        this.lexico = lexico;
    }

    /** Executa a análise e devolve os erros encontrados (lista vazia = programa correto). */
    public List<ErroSintatico> analisar() {
        estados.push(0);
        avancar();
        while (true) {
            String a = virtual != null ? virtual : terminalAtual();
            Acao acao = tabela.acao(estados.peek(), a);
            if (acao == null) {
                if (!tratarErro()) break;
            } else if (acao.tipo() == 'S') {
                estados.push(acao.valor());
                simbolos.push(a);
                if (virtual != null) virtual = null;
                else avancar();
            } else if (acao.tipo() == 'R') {
                reduzir(tabela.regra(acao.valor()));
            } else {
                break;   // accept
            }
        }
        erros.sort(Comparator.comparingInt(ErroSintatico::linha).thenComparingInt(ErroSintatico::coluna));
        return erros;
    }

    // ---------------------------------------------------------------- entrada

    /** Converte o token do léxico no terminal da gramática (ident, num, tipo, ou o próprio lexema). */
    private String terminalAtual() {
        String lex = atual.getLexema().toLowerCase();
        switch (atual.getTipo()) {
            case IDENTIFICADOR: return "ident";
            case NUMERO_INTEIRO:
            case NUMERO_REAL:   return "num";
            case EOF:           return FIM;
            case PALAVRA_RESERVADA:
                return (lex.equals("integer") || lex.equals("real") || lex.equals("char") || lex.equals("string"))
                        ? "tipo" : lex;
            default:            return lex;   // operadores/símbolos; o que não existe na gramática vira erro na tabela
        }
    }

    /** Lê o próximo token. Símbolos desconhecidos viram erro léxico e são pulados. */
    private void avancar() {
        anterior = atual;
        aposErroLexico = false;
        while (true) {
            Token t = lexico.proximo();
            if (t.getTipo() == Tokens.DESCONHECIDO) {
                erros.add(new ErroSintatico(Tipo.ERRO_LEXICO, t.getLinha(), t.getColuna(), mensagemLexica(t)));
                aposErroLexico = true;
                continue;
            }
            atual = t;
            pos++;
            return;
        }
    }

    private String mensagemLexica(Token t) {
        String lex = t.getLexema();
        if (lex.startsWith("\"")) return "constante string não fechada: " + lex.stripTrailing();
        if (lex.startsWith("'")) return "constante char inválida (deve ter exatamente 1 caractere): " + lex.stripTrailing();
        if (lex.startsWith("/*")) return "comentário de bloco não fechado";
        if (Character.isDigit(lex.charAt(0))) return "número malformado: '" + lex + "'";
        return "símbolo desconhecido: '" + lex + "'";
    }

    private void reduzir(Producao p) {
        for (int i = 0; i < p.direita().size(); i++) {
            estados.pop();
            simbolos.pop();
        }
        estados.push(tabela.irPara(estados.peek(), p.esquerda()));
        simbolos.push(p.esquerda());
    }

    // ------------------------------------------------------------------ erros

    /** @return false se não dá para continuar a análise. */
    private boolean tratarErro() {
        virtual = null;
        boolean eof = atual.getTipo() == Tokens.EOF;

        if (pos == posUltimoErro) {          // mesmo ponto do erro anterior, sem progresso: descarta o token
            if (eof) return false;
            avancar();
            return recuperarEmModoPanico();
        }
        posUltimoErro = pos;

        boolean faltaPontoVirgula = !eof && simularInsercaoDePontoVirgula();
        if (!aposErroLexico) erros.add(descreverErro(eof, faltaPontoVirgula));   // evita erro em cascata de erro léxico
        if (!simbolos.isEmpty() && simbolos.peek().equals(".")) return false;    // lixo depois do '.' final
        if (faltaPontoVirgula) {
            virtual = ";";
            return true;
        }
        return recuperarEmModoPanico();
    }

    private boolean recuperarEmModoPanico() {
        if (atual.getTipo() == Tokens.EOF) return false;
        // 1) desempilha até um estado que saiba empilhar "error"
        while (tabela.acao(estados.peek(), ERRO) == null || tabela.acao(estados.peek(), ERRO).tipo() != 'S') {
            if (estados.size() == 1) return false;
            estados.pop();
            simbolos.pop();
        }
        estados.push(tabela.acao(estados.peek(), ERRO).valor());
        simbolos.push(ERRO);
        // 2) descarta tokens até um que a tabela aceite (ou 'end', que ganha um ';' imaginário)
        int profundidade = 0;            // begin..end aninhados dentro do trecho descartado
        while (true) {
            String a = terminalAtual();
            if (a.equals(FIM)) return false;
            if (a.equals("begin")) profundidade++;
            else if (a.equals("end") && profundidade > 0) profundidade--;
            else if (profundidade == 0) {
                if (tabela.acao(estados.peek(), a) != null) return true;
                if (a.equals("end") && simularInsercaoDePontoVirgula()) {
                    virtual = ";";
                    return true;
                }
            }
            avancar();
        }
    }

    /** Testa, numa cópia da pilha, se ';' seguido do token atual formam um prefixo válido. */
    private boolean simularInsercaoDePontoVirgula() {
        Deque<Integer> copia = new ArrayDeque<>(estados);
        return simularLeitura(copia, ";") && simularLeitura(copia, terminalAtual());
    }

    /** Faz reduções na pilha-cópia até conseguir um shift do terminal t (true) ou encontrar erro (false). */
    private boolean simularLeitura(Deque<Integer> pilha, String t) {
        for (int passo = 0; passo < 10_000; passo++) {
            Acao a = tabela.acao(pilha.peek(), t);
            if (a == null) return false;
            if (a.tipo() == 'S') {
                pilha.push(a.valor());
                return true;
            }
            if (a.tipo() != 'R') return false;
            Producao p = tabela.regra(a.valor());
            if (pilha.size() <= p.direita().size()) return false;
            for (int i = 0; i < p.direita().size(); i++) pilha.pop();
            Integer destino = tabela.irPara(pilha.peek(), p.esquerda());
            if (destino == null) return false;
            pilha.push(destino);
        }
        return false;
    }

    // ----------------------------------------------------------- mensagens

    private ErroSintatico descreverErro(boolean eof, boolean faltaPontoVirgula) {
        List<String> esperados = new ArrayList<>(tabela.terminaisEsperados(estados.peek()));
        esperados.remove(ERRO);
        String encontrado = eof ? "fim do arquivo" : "'" + atual.getLexema() + "'";
        int linha = atual.getLinha(), coluna = atual.getColuna();
        String a = terminalAtual();

        if (!simbolos.isEmpty() && simbolos.peek().equals(".")) {
            return new ErroSintatico(Tipo.CODIGO_APOS_FIM_DO_PROGRAMA, linha, coluna,
                    "código inesperado (" + encontrado + ") depois do '.' que encerra o programa");
        }
        if (contar("begin") > contar("end") && (eof || a.equals("."))) {   // blocos já reduzidos saem da pilha
            return new ErroSintatico(Tipo.BEGIN_END_DESBALANCEADO, linha, coluna,
                    encontrado + " alcançado com 'begin' sem o 'end' correspondente");
        }
        if (eof) {
            if (esperados.contains(".")) {
                return new ErroSintatico(Tipo.FALTA_PONTO_FINAL, linha, coluna, "esperado '.' ao final do programa");
            }
            return new ErroSintatico(Tipo.FIM_DE_ARQUIVO_INESPERADO, linha, coluna,
                    "fim do arquivo inesperado; esperado " + listar(esperados));
        }
        if (faltaPontoVirgula) {
            if (anterior != null) {      // o ';' faltou logo depois do token anterior
                linha = anterior.getLinha();
                coluna = anterior.getColuna() + anterior.getLexema().length();
            }
            return new ErroSintatico(Tipo.FALTA_PONTO_VIRGULA, linha, coluna, "esperado ';' antes de " + encontrado);
        }
        if (a.equals("end") || a.equals("begin")) {
            return new ErroSintatico(Tipo.BEGIN_END_DESBALANCEADO, linha, coluna,
                    encontrado + " inesperado (bloco vazio ou begin/end desbalanceado); esperado " + listar(esperados));
        }
        boolean inicioDeExpressao = esperados.contains("num") && esperados.contains("(");
        if (inicioDeExpressao && esperados.stream().allMatch(
                t -> t.equals("ident") || t.equals("num") || t.equals("(") || t.equals("+") || t.equals("-"))) {
            return new ErroSintatico(Tipo.EXPRESSAO_MALFORMADA, linha, coluna,
                    "operando esperado (identificador, número ou '(') mas encontrado " + encontrado);
        }
        return new ErroSintatico(Tipo.TOKEN_INESPERADO, linha, coluna,
                "token " + encontrado + " inesperado; esperado " + listar(esperados));
    }

    private int contar(String simbolo) {
        int n = 0;
        for (String s : simbolos) if (s.equals(simbolo)) n++;
        return n;
    }

    private String listar(List<String> terminais) {
        if (terminais.isEmpty()) return "(nada)";
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < Math.min(8, terminais.size()); i++) nomes.add(GramaticaMiniPascal.nomeAmigavel(terminais.get(i)));
        return String.join(", ", nomes) + (terminais.size() > 8 ? " ou outros" : "");
    }
}