import java.util.List;

// Enumeración para representar los posibles estados del analizador sintáctico
enum ParserState {
    BEGIN,    // Estado inicial del análisis
    FINISH,   // Estado de finalización exitosa
    ERROR     // Estado de error en caso de encontrar un problema de sintaxis
}

// Clase Parser para analizar la secuencia de tokens y verificar la sintaxis de una consulta
public class Parser {
    private final List<Token> tokens; // Lista de tokens generada por el analizador léxico
    private int tokensIndex = 0;      // Índice actual en la lista de tokens
    private Token lookahead;          // Token que se está analizando actualmente
    private ParserState state;        // Estado actual del analizador

    // Constructor que inicializa la lista de tokens y el primer token a analizar
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokensIndex = 0;
        this.lookahead = tokens.get(tokensIndex);  // Establece el primer token como lookahead
        this.state = ParserState.BEGIN;            // Inicia en el estado BEGIN
    }

    // Método principal de análisis
    public void parse() {
        consulta();  // Llama a la regla principal para analizar la consulta

        // Verifica si el análisis terminó correctamente o si hubo un error
        if (state != ParserState.ERROR && lookahead.tipo == TipoToken.EOF) {
            System.out.println("== PARSED SUCCESSFULLY ==");  // Mensaje de éxito
        } else if (state != ParserState.ERROR) {
            error("Error de sintaxis al final de la consulta.");
        }
    }

    // Método para comparar el token actual con el tipo esperado
    private void match(TipoToken type) {
        if (lookahead.tipo == type) {
            tokensIndex++;  // Avanza al siguiente token si coincide el tipo
            if (tokensIndex < tokens.size()) {
                lookahead = tokens.get(tokensIndex);  // Actualiza lookahead al siguiente token
            } else {
                state = ParserState.FINISH;  // Marca el estado como FINISH si ya no hay más tokens
            }
        } else {
            state = ParserState.ERROR;  // Cambia el estado a ERROR si no coincide el tipo esperado
            error("Se esperaba: " + type + " pero se encontró: " + lookahead.tipo);
        }
    }

    // Reglas gramaticales de análisis para una consulta SQL
    // Cada método representa una regla en la gramática

    // Regla principal: una consulta SQL
    private void consulta() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.SELECT) {  // La consulta debe comenzar con SELECT
            match(TipoToken.SELECT);
            d();
            if (lookahead.tipo == TipoToken.FROM) {
                match(TipoToken.FROM);
                t();
                w();  // Cláusula opcional WHERE
                if (lookahead.tipo == TipoToken.SEMICOLON) {
                    match(TipoToken.SEMICOLON);  // Fin de la consulta con ';'
                } else {
                    error("Se esperaba ';' al final de la consulta.");
                }
            } else {
                error("Se esperaba 'FROM' después de 'SELECT'");
            }
        } else {
            error("Se esperaba 'SELECT' al inicio de la consulta");
        }
    }

    // Regla opcional para manejar DISTINCT después de SELECT
    private void d() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.DISTINCT) {
            match(TipoToken.DISTINCT);
            p();
        } else {
            p();
        }
    }

    // Regla para manejar '*' o una lista de columnas
    private void p() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.STAR) {
            match(TipoToken.STAR);
        } else {
            f();
        }
    }

    // Reglas para listas de expresiones de columnas
    private void f() {
        if (state == ParserState.ERROR) return;

        expr();  // Llama a la regla para una expresión general
        f1();    // Llama a la regla para una lista de expresiones separadas por comas
    }

    private void f1() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            expr();
            f1();  // Recursivo para más columnas separadas por coma
        }
    }

    // Regla para nombres de tablas después de FROM
    private void t() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
            match(TipoToken.IDENTIFICADOR);
            t3();
        } else {
            error("Se esperaba un nombre de tabla después de 'FROM'");
        }
    }

    private void t3() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            t();  // Permite múltiples tablas separadas por coma
        }
    }

    // Regla opcional para la cláusula WHERE
    private void w() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.WHERE) {
            match(TipoToken.WHERE);
            expr();  // Llama a la regla para una expresión lógica
        }
    }

    // Reglas para expresiones lógicas (condiciones)
    private void expr() {
        logicOr();  // Expresión lógica que puede ser una serie de OR
    }

    private void logicOr() {
        logicAnd();
        while (lookahead.tipo == TipoToken.OR) {
            match(TipoToken.OR);
            logicAnd();
        }
    }

    private void logicAnd() {
        equality();
        while (lookahead.tipo == TipoToken.AND) {
            match(TipoToken.AND);
            equality();
        }
    }

    private void equality() {
        comparison();
        while (lookahead.tipo == TipoToken.EQUAL || lookahead.tipo == TipoToken.NE) {
            match(lookahead.tipo);
            comparison();
        }
    }

    private void comparison() {
        term();
        while (lookahead.tipo == TipoToken.LT || lookahead.tipo == TipoToken.LE
                || lookahead.tipo == TipoToken.GT || lookahead.tipo == TipoToken.GE
                || lookahead.tipo == TipoToken.EQUAL) {
            match(lookahead.tipo);
            term();
        }
    }

    private void term() {
        factor();
        while (lookahead.tipo == TipoToken.PLUS || lookahead.tipo == TipoToken.MINUS) {
            match(lookahead.tipo);
            factor();
        }
    }

    private void factor() {
        unary();
        while (lookahead.tipo == TipoToken.STAR || lookahead.tipo == TipoToken.SLASH) {
            match(lookahead.tipo);
            unary();
        }
    }

    private void unary() {
        if (lookahead.tipo == TipoToken.NOT || lookahead.tipo == TipoToken.MINUS) {
            match(lookahead.tipo);
            unary();
        } else {
            primary();
        }
    }

    private void primary() {
        // Valores literales o identificadores
        if (lookahead.tipo == TipoToken.TRUE || lookahead.tipo == TipoToken.FALSE
                || lookahead.tipo == TipoToken.NULL || lookahead.tipo == TipoToken.NUMERO
                || lookahead.tipo == TipoToken.CADENA || lookahead.tipo == TipoToken.IDENTIFICADOR) {
            match(lookahead.tipo);
        } else if (lookahead.tipo == TipoToken.LEFT_PAREN) {  // Expresión entre paréntesis
            match(TipoToken.LEFT_PAREN);
            expr();
            match(TipoToken.RIGHT_PAREN);
        } else {
            error("Expresión no válida: se esperaba '(' o un IDENTIFICADOR.");
        }
    }

    // Método para manejar errores de sintaxis
    private void error(String mensaje) {
        System.err.println("[Línea " + lookahead.linea + "] Error: " + mensaje);
        state = ParserState.ERROR;
        synchronize();  // Llama a sincronización para intentar recuperar el análisis
    }

    // Método para reanudar el análisis después de un error
    private void synchronize() {
        tokensIndex++;
        while (tokensIndex < tokens.size()) {
            lookahead = tokens.get(tokensIndex);
            // Reanuda el análisis en palabras clave importantes
            if (lookahead.tipo == TipoToken.FROM || lookahead.tipo == TipoToken.WHERE || lookahead.tipo == TipoToken.SEMICOLON) {
                state = ParserState.BEGIN;
                return;
            }
            tokensIndex++;
        }
    }
}

