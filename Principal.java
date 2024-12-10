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
        // Escaneo de tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
    
        // Análisis sintáctico y construcción del AST
        Parser parser = new Parser(tokens);
        QueryNode ast = parser.consulta(); // Construye el AST con el método consulta()
    
        // Verificar si el AST fue generado correctamente
        if (ast != null) {
            // Imprimir el AST
            PrinterQuery printer = new PrinterQuery();
            ast.accept(printer); // Recorre e imprime el AST
        } else {
            System.err.println("Error: No se pudo construir el AST.");
        }
    
        // Imprimir los tokens generados (opcional, para depuración)
        for (Token token : tokens) {
            System.out.println(token);
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
