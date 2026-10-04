# Analisador Léxico Mini Pascal

Aplicação desktop desenvolvida em Java para realizar a análise léxica de códigos-fonte escritos em uma linguagem baseada em Mini Pascal.

O sistema identifica lexemas, classifica tokens, registra a posição de cada token no código-fonte e permite exportar a listagem gerada para um arquivo de texto.

## Funcionalidades

- Edição de código-fonte diretamente na aplicação.
- Abertura de arquivos com as extensões `.txt`, `.pas` e `.p`.
- Reconhecimento de palavras reservadas, identificadores, números, operadores e símbolos.
- Identificação da linha e da coluna inicial de cada token.
- Exibição dos tokens reconhecidos em uma tabela.
- Detecção de símbolos e lexemas desconhecidos.
- Exportação da listagem de tokens para arquivos `.txt`.
- Exemplo de código Mini Pascal carregado automaticamente ao iniciar a aplicação.

## Tecnologias utilizadas

- Java 17
- JavaFX 21.0.8
- Maven
- CSS para estilização da interface

## Requisitos

Antes de executar o projeto, certifique-se de que os seguintes programas estão instalados:

- JDK 17 ou superior;
- Maven 3.8 ou superior.

As dependências do JavaFX são baixadas automaticamente pelo Maven a partir das configurações presentes no arquivo `pom.xml`.

## Como executar

Na raiz do projeto, execute:

```bash
mvn javafx:run
```

A aplicação será iniciada com um exemplo integrado de código Mini Pascal.

Também é possível executar a classe principal diretamente pela IDE:

```text
gui.AnalisadorLexicoApp
```

## Como utilizar

1. Digite ou edite um código Mini Pascal no painel **Fonte**.
2. Opcionalmente, clique em **Abrir arquivo** para carregar um arquivo `.txt`, `.pas` ou `.p`.
3. Clique em **Analisar código**.
4. Consulte os tokens reconhecidos no painel **Resultado**.
5. Clique em **Exportar tokens** para salvar a listagem em um arquivo `.txt`.

A tabela de resultados apresenta:

- Linha inicial do token;
- Coluna inicial do token;
- Lexema reconhecido;
- Tipo do token.

## Executando os testes

Os testes verificam, entre outros aspectos:

- Reconhecimento de palavras reservadas e operadores;
- Ignorância de comentários de bloco;
- Preservação da posição inicial dos tokens;
- Reconhecimento de números reais e inteiros;
- Reconhecimento de strings e caracteres;
- Reconhecimento de operadores relacionais;
- Tratamento de lexemas malformados;
- Descrição textual dos tipos de tokens.

## Estrutura do projeto

```text
.
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── AnalisadorLexicoApp.java
│   │   │   ├── analisadorlexico
│   │   │   │   ├── AnalisadorLexico.java
│   │   │   │   ├── Token.java
│   │   │   │   └── Tokens.java
│   │   │   ├── gui
│   │   │   │   ├── AnalisadorLexicoApp.java
│   │   │   │   └── Main.java
│   │   │   ├── tabelasimbolos
│   │   │   │   └── TabelaSimbolos.java
│   │   │   └── util
│   │   │       └── GerenciadorArquivos.java
│   │   └── resources
│   │       └── app.css
│   └── test
│       └── java
│           └── analisadorlexico
│               └── AnalisadorLexicoTest.java
└── testes
    ├── entradas
    └── saidas
```

## Tipos de tokens reconhecidos

O analisador léxico reconhece os seguintes tipos:

- **Palavra reservada**
- **Identificador**
- **Número inteiro**
- **Número real**
- **Operador aritmético**
- **Operador relacional**
- **Operador lógico**
- **Símbolo especial**
- **Atribuição**
- **Fim**
- **Constante string**
- **Constante char**
- **Desconhecido**

### Palavras reservadas

A tabela de símbolos é inicializada com palavras reservadas da linguagem, incluindo:

```text
ABSOLUTE, ARRAY, BEGIN, CASE, CHAR, CONST, DIV, DO, DOWNTO,
ELSE, END, EXTERNAL, FILE, FOR, FORWARD, FUNC, FUNCTION, GOTO,
IF, IMPLEMENTATION, INTEGER, INTERFACE, INTERRUPT, LABEL, MAIN,
NIL, NIT, OF, PACKED, PROC, PROGRAM, REAL, RECORD, REPEAT, SET,
SHL, SHR, STRING, THEN, TO, TYPE, UNIT, UNTIL, USES, VAR, WHILE,
WITH, XOR
```

A comparação das palavras reservadas não diferencia letras maiúsculas de minúsculas. O lexema original, entretanto, é preservado na saída.

Os operadores lógicos `AND`, `OR` e `NOT` são reconhecidos como operadores lógicos, enquanto `MOD` é reconhecido como operador aritmético.

Os identificadores podem conter letras, dígitos e `_`, desde que comecem com uma letra.

## Regras de análise

- Espaços, tabulações e quebras de linha são ignorados.
- Comentários de bloco no formato `/* ... */` são ignorados.
- Comentários não finalizados são classificados como desconhecidos.
- Strings devem ser delimitadas por aspas duplas (`"`).
- Caracteres devem ser delimitados por aspas simples (`'`) e conter exatamente um caractere.
- Números reais podem utilizar casas decimais e expoente, como:
  - `3.14`
  - `24.40e-04`
  - `1.5E+2`
- A atribuição é representada pelo operador `:=`.
- O ponto (`.`) é reconhecido como marcador de fim.
- Cada token armazena sua linha e sua coluna inicial.
- Símbolos ou lexemas malformados são classificados como `Desconhecido`, permitindo que a análise continue.

## Recuperação de erros

O analisador utiliza uma estratégia de recuperação de erros conhecida como **Panic Mode**.

Quando encontra um símbolo desconhecido ou um lexema malformado, o analisador:

1. Classifica o trecho como `Desconhecido`;
2. Retorna o token encontrado;
3. Continua a leitura do código-fonte a partir da próxima posição possível.

Dessa forma, vários problemas léxicos podem ser identificados em uma única execução.

## Outros detalhes

Este projeto implementa exclusivamente a etapa de análise léxica.

Ainda não fazem parte deste módulo:

- Análise sintática;
- Construção de uma tabela SLR(1);
- Análise semântica;
- Verificação de tipos;
- Geração de código.

Além disso, os comentários são aceitos somente no formato de bloco `/* ... */`.

## Informações acadêmicas

Projeto desenvolvido em Java para a disciplina de **Compiladores** do curso de **Ciência da Computação**.