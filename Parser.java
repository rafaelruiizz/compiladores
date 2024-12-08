import java.util.List;

// Enumeración para representar los posibles estados del analizador sintáctico
enum ParserState {
    BEGIN,     // Estado inicial del parser
    FINISH,    // Estado cuando el análisis finaliza correctamente
    ERROR      // Estado cuando ocurre un error de sintaxis.
}

// Clase Parser para analizar la secuencia de tokens y verificar la sintaxis de una consulta SQL
public class Parser {
    private final List<Token> tokens;   // Lista de tokens generada por el scanner (análisis léxico)
    private int tokensIndex = 0;        // Índice actual en la lista de tokens
    private Token lookahead;            // Token que se está analizando actualmente (token de "mira adelante")
    private ParserState state;          // Estado actual del parser (BEGIN, FINISH, ERROR)

    // Constructor que inicializa el parser con la lista de tokens
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokensIndex = 0;
        this.lookahead = tokens.get(tokensIndex);  // Obtiene el primer token
        this.state = ParserState.BEGIN;            // Inicia en el estado BEGIN
    }

    // Método principal de análisis sintáctico
    public void parse() {
        consulta();  // Inicia el análisis con la regla de consulta principal
        
        // Verifica si el análisis terminó correctamente o si hubo un error
        if (state == ParserState.ERROR) {
            System.out.println("PARSING FAILED");  // Muestra mensaje de fallo si hay un error
        } else /*(state == ParserState.FINISH && lookahead.tipo == TipoToken.EOF)*/ {
            System.out.println("== PARSED SUCCESSFULLY ==");
        } /* else {
            System.out.println("Estado final: " + state);  // Depuración del estado final
            System.out.println("Token final: " + lookahead);  // Depuración del último token
        }*/
    }
    

    // Método para comparar el token actual con el tipo esperado
    private void match(TipoToken type) {
        if (lookahead.tipo == type) { // Si el token actual coincide con el tipo esperado
            tokensIndex++;  // Avanza al siguiente token
            if (tokensIndex < tokens.size()) {
                lookahead = tokens.get(tokensIndex);  // Actualiza lookahead al siguiente token
            } else {
                lookahead = new Token(TipoToken.EOF, "", 0); // Forzar EOF si ya no hay más tokens
                state = ParserState.FINISH; // Marca el estado como FINISH si ya no hay más tokens
                System.out.println("Estado cambiado a FINISH en match()");  // Mensaje de depuración
            }
        } else {
            state = ParserState.ERROR;  // Cambia el estado a ERROR si no coincide el tipo esperado
            error("Se esperaba: " + type + " pero se encontró: " + lookahead.tipo);
        }
    }
    

    // Implementación de las reglas gramaticales para SQL

    // Q → select D from T W
    // Regla principal de consulta SQL que debe comenzar con la palabra clave SELECT
    private void consulta() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.SELECT) {  // Verifica que la consulta comience con SELECT
            match(TipoToken.SELECT);
            d();  // Llama a la regla D
            if (lookahead.tipo == TipoToken.FROM) {
                match(TipoToken.FROM);
                t();  // Llama a la regla T
                w();  // Llama a la regla W
                // Punto y coma opcional para finalizar la consulta
                if (lookahead.tipo == TipoToken.SEMICOLON) {
                    match(TipoToken.SEMICOLON);
                }
            } else {
                error("Se esperaba 'FROM' después de 'SELECT'");
            }
        } else {
            error("Se esperaba 'SELECT' al inicio de la consulta");
        }
    }

    // D → distinct P | P
    // Regla que verifica si hay un DISTINCT opcional antes de las proyecciones
    private void d() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.DISTINCT) {
            match(TipoToken.DISTINCT);
            p();
        } else {
            p();
        }
    }

    // P → * | F
    // Regla que permite un asterisco (*) o una lista de expresiones (F) para las columnas seleccionadas
    private void p() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.STAR) {
            match(TipoToken.STAR);
        } else {
            f();
        }
    }

    // F → Expr F1
    // Regla para una lista de expresiones de columna, separadas por comas
    private void f() {
        if (state == ParserState.ERROR) return;

        expr();
        f1();
    }

    // F1 → , Expr F1 | ε
    // Regla recursiva para manejar múltiples expresiones separadas por comas
    private void f1() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            if (lookahead.tipo == TipoToken.IDENTIFICADOR || lookahead.tipo == TipoToken.NUMERO
                    || lookahead.tipo == TipoToken.CADENA || lookahead.tipo == TipoToken.STAR) {
                expr();
                f1();   // Llama a sí misma recursivamente para manejar múltiples comas
            } else {
                error("Se esperaba una expresión válida después de ','");
            }
        }
    }

    // T → id T3
    // Regla para identificar el nombre de la tabla después de FROM
    private void t() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
            match(TipoToken.IDENTIFICADOR);
            // Permite un identificador adicional como alias de tabla
            if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
                match(TipoToken.IDENTIFICADOR); // Soporte para alias de tabla
            }
            t3();  // Llama a la regla T3 para manejar múltiples tablas
        } else {
            error("Se esperaba un nombre de tabla después de 'FROM'");
        }
    }

    // T3 → , IDENTIFICADOR T3 | ε
    // Regla recursiva para manejar múltiples tablas separadas por comas
    private void t3() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
                match(TipoToken.IDENTIFICADOR);
                if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
                    match(TipoToken.IDENTIFICADOR); // Soporte para alias de tabla
                }
                t3(); // Llama recursivamente para manejar más tablas
            } else {
                error("Se esperaba un nombre de tabla después de ','");
            }
        }
    }

    // W → where Expr | ε
    // Regla opcional que analiza la cláusula WHERE seguida de una expresión lógica
    private void w() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.WHERE) {
            match(TipoToken.WHERE);
            expr();
        }
    }

    // Reglas para expresiones lógicas (condiciones)

    // Expr → LogicOr
    // La regla principal para analizar expresiones lógicas
    private void expr() {
        logicOr();
    }
    private void logicOr() {
        if (state == ParserState.ERROR) return;
        logicAnd();
        logicOr1();
        }
    private void logicOr1() {
        if (lookahead.tipo == TipoToken.OR) {
            match(TipoToken.OR);
            logicOr();
            }
        }
    // LogicAnd → Equality (and LogicAnd)*
    // Regla para manejar la operación lógica AND en una expresión
    private void logicAnd() {
        if (state == ParserState.ERROR) return;
        equality();
        logicAnd1();
    }
    private void logicAnd1() {
        if (lookahead.tipo == TipoToken.AND) {
            match(TipoToken.AND);
            logicAnd();
            }
        }
    // Equality → Comparison ((= | !=) Comparison)*
    // Regla para manejar operadores de igualdad y desigualdad en una expresión
    private void equality() {
        comparison();
        equality1();
    }
    private void equality1() {
        if (lookahead.tipo == TipoToken.EQUAL || lookahead.tipo == TipoToken.NE) {
            match(lookahead.tipo); // Consume el operador.
            equality1();           // Permite manejar múltiples operadores consecutivos.
        }
    }
    // Comparison → Term ((< | <= | > | >=) Term)*
    // Regla para manejar operadores de comparación (<, <=, >, >=)
    private void comparison(){
        term();
        comparison1();
    }
    private void comparison1() {
        if (lookahead.tipo == TipoToken.LT || lookahead.tipo == TipoToken.LE || lookahead.tipo == TipoToken.GT || lookahead.tipo == TipoToken.GE) {
            match(lookahead.tipo);
        }
    }
    // Term → Factor ((+ | -) Factor)
    // Regla para manejar operaciones aritméticas de suma y resta
    private void term() {
        factor();
        term1();
    }
    private void term1() {
        if (lookahead.tipo == TipoToken.PLUS || lookahead.tipo == TipoToken.MINUS) {
            match(lookahead.tipo); // Consume el operador.
            term();
        }
    }
    // Factor → Unary ((* | /) Unary)*
    // Regla para manejar operaciones aritméticas de multiplicación y división
    private void factor() {
        unary();
        factor1();
        }
        private void factor1() {
            if (lookahead.tipo == TipoToken.SLASH || lookahead.tipo == TipoToken.STAR) {
                match(lookahead.tipo); // Consume el operador.
                factor();
            }
        }
        
    // Unary → (! | -)? Primary
    // Regla para manejar operadores unarios como NOT y el operador de negación
    private void unary() {
        if (lookahead.tipo == TipoToken.NOT_OPERATOR || lookahead.tipo == TipoToken.MINUS) {
            match(lookahead.tipo); // Consume el operador ! o -
            unary();              // Procesa la siguiente operación unaria.
        } else {
            call(); // Procesa llamadas o valores entre paréntesis.
        }
    }
    
    private void call() {
        primary();
        call1();
        }

    private void call1(){
        if (lookahead.tipo == TipoToken.LEFT_PAREN){
            match(lookahead.tipo); // Consume el operador.
            arguments();
            if (lookahead.tipo == TipoToken.RIGHT_PAREN){
                match(lookahead.tipo);
            }
            else{
                System.err.println("Error: falta cerrar el paréntesis en la línea " + lookahead.linea);
            }
            }
    }
    // Primary → true | false | null | number | string | id AliasOpc | ( Expr )
    // Regla para manejar valores primitivos, identificadores calificados, llamadas a funciones y expresiones entre paréntesis
    private void primary() {
        if (lookahead.tipo == TipoToken.TRUE || lookahead.tipo == TipoToken.FALSE || lookahead.tipo == TipoToken.NULL || 
            lookahead.tipo == TipoToken.NUMERO || lookahead.tipo == TipoToken.CADENA) {
                match(lookahead.tipo);
            }
            else if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
                match(TipoToken.IDENTIFICADOR);
                aliasOpc();
            }
                else if (lookahead.tipo == TipoToken.LEFT_PAREN){
                    match(lookahead.tipo); // Consume el operador.
                    expr();
                        if (lookahead.tipo == TipoToken.RIGHT_PAREN){
                            match(lookahead.tipo);
                        }
                        else{
                        System.err.println("Error: falta cerrar el paréntesis en la línea " + lookahead.linea);
                        }
                }
            } // Hasta aqui

    private void aliasOpc() {
        if (lookahead.tipo == TipoToken.DOT) {
            match(TipoToken.DOT); // Consume el operador '.'
                if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
                    match(TipoToken.IDENTIFICADOR); // Consume el identificador después del punto
                } else {
                    System.err.println("Error: falta un identificador después del '.' en la línea " + lookahead.linea);
                }
            }
        }
            
    private void arguments() {
        expr(); // Procesa el primer argumento.
        arguments1(); // Procesa los argumentos adicionales (si los hay).
    }
        private void arguments1(){
            if (lookahead.tipo == TipoToken.COMA){
                match(TipoToken.COMA);
                expr(); // Procesa el siguiente argumento.
                arguments1(); // Llama recursivamente para manejar más argumentos.
            }
        }

    // Método para imprimir un mensaje de error y cambiar el estado a ERROR
    private void error(String mensaje) {
        System.err.println("== ERROR (Parser): bad token at [" + lookahead.linea + ":" + (tokensIndex + 1) + "] >> " + lookahead.lexema + " <<");
        System.err.println("==== [T] " + mensaje);
        state = ParserState.ERROR;
    }


}
