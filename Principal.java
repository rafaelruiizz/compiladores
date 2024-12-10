import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Principal {
    static boolean existenErrores = false;

    public static void main(String[] args) throws IOException {
        ejecutarPrompt();
    }

    private static void ejecutarPrompt() throws IOException {
        System.out.println("Bienvenido al parser SQL interactivo.");
        System.out.println("Escribe tu consulta SQL y presiona Enter.");
        System.out.println("Para salir, presiona Ctrl + D.\n");

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print(">>> ");
            String linea = reader.readLine();
            if (linea == null) break;
            ejecutar(linea);
            existenErrores = false;
        }
    }

    private static void ejecutar(String source) {
        if (source.trim().isEmpty()) {
            System.out.println("La entrada está vacía. Escribe una consulta SQL.");
            return;
        }

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        QueryNode ast = parser.consulta();

        if (ast != null) {
            PrinterQuery printer = new PrinterQuery();
            ast.accept(printer);
        } else {
            System.err.println("Error: La consulta tiene errores de sintaxis.");
        }

        // Imprimir tokens generados
        if (!tokens.isEmpty()) {
            System.out.println("Tokens generados:");
            for (Token token : tokens) {
                System.out.println(token);
            }
        }
    }

    static void error(int linea, String mensaje) {
        reportar(linea, "", mensaje);
    }

    private static void reportar(int linea, String donde, String mensaje) {
        System.err.println("[linea " + linea + "] Error " + donde + ": " + mensaje);
        existenErrores = true;
    }
}


