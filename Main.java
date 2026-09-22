import java.util.List;

public class Main {
    public static void main(String[] args) {
        String codigoExemplo =
                "program teste;\n" +
                        "var x,y: integer;\n" +
                        "/* inicio do programa */\n" +
                        "begin\n" +
                        "  read(x);\n" +
                        "  if (x>y) then\n" +
                        "    y:=x\n" +
                        "  else\n" +
                        "    y:=-x\n" +
                        "  writeln(x);\n" +
                        "  write(y);\n" +
                        "end.";

        AnalisadorLexico lexer = new AnalisadorLexico(codigoExemplo);
        List<Token> tokens = lexer.analisar();

        System.out.println("Lexema          Token");
        System.out.println("------------------------------------");
        for (Token t : tokens) {
            System.out.println(t.toString());
        }
    }
}