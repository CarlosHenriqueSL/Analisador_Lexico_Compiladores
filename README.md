# Analisador Lexico Mini Pascal

Aplicacao JavaFX para leitura de um codigo-fonte Mini Pascal e geracao da listagem de lexemas e tokens reconhecidos.

## Requisitos

- Windows
- JDK 17 ou superior
- Maven 3.8 ou superior

O Maven baixa automaticamente o JavaFX e o JUnit definidos em `pom.xml`.

## Execucao

Na raiz do projeto:

```powershell
mvn javafx:run
```

A interface permite abrir arquivos `.txt`, `.pas` e `.p`, editar o codigo, analisar os tokens e exportar a listagem para um arquivo `.txt`.

Para executar os testes:

```powershell
mvn test
```

## Estrutura

```text
src/main/java/
  gui/
    AnalisadorLexicoApp.java
    Main.java
  analisadorlexico/
    AnalisadorLexico.java
    Token.java
    Tokens.java
  tabelasimbolos/
    TabelaSimbolos.java
  util/
    GerenciadorArquivos.java
src/main/resources/
  app.css
src/test/java/
  analisadorlexico/AnalisadorLexicoTest.java
testes/entradas/
testes/saidas/        # preservada com .gitkeep; resultados sao gerados localmente
```

## Decisoes do analisador

- A tabela de simbolos usa `HashMap`, com busca media O(1), e e inicializada com as palavras reservadas.
- Identificadores aceitam letras, digitos e `_`, desde que comecem por uma letra. Nao ha limite artificial de tamanho; o limite pratico e a memoria disponivel.
- A busca por palavras reservadas nao diferencia maiusculas de minusculas, mas o lexema original e preservado na saida.
- Espacos, tabs, quebras de linha e comentarios `/* ... */` sao descartados.
- O analisador usa recuperacao em modo panico: um simbolo desconhecido ou lexema malformado vira `Desconhecido`, e a leitura continua.
- Cada token preserva linha e coluna iniciais para apoiar diagnostico e a futura etapa sintatica.

## Tokens cobertos

Palavras reservadas, identificadores, inteiros, reais com expoente, operadores aritmeticos, relacionais e logicos, simbolos especiais, atribuicao, fim de programa, strings, chars e simbolos desconhecidos.

`read`, `write` e `writeln` seguem a especificacao fornecida e sao reconhecidos como identificadores, pois nao aparecem na lista de palavras reservadas.

## Limitacoes atuais

Este modulo implementa somente a analise lexica. A analise sintatica SLR(1), a analise semantica e a verificacao de tipos ainda pertencem a etapa seguinte do trabalho.

O analisador aceita comentarios apenas no formato de bloco exigido pela especificacao. Comentarios nao fechados, strings sem aspas finais, chars invalidos e expoentes sem digitos sao reportados como tokens desconhecidos.
