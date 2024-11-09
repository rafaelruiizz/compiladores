import java.util.List;

// Enumeración para representar los posibles estados del analizador sintáctico
enum ParserState {
    BEGIN, 
    FINISH, 
    ERROR
}

// Clase Parser para analizar la secuencia de tokens y verificar la sintaxis de una consulta
public class Parser {
    private final List<Token> tokens;   // Lista de tokens generada por el analizador léxico
    private int tokensIndex = 0;        // Índice actual en la lista de tokens 
    private Token lookahead;            // Token que se está analizando actualmente
    private ParserState state;             // Estado actual del analizador

// Constructor que inicializa la lista de tokens y el primer token a analizar
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokensIndex = 0;
        this.lookahead = tokens.get(tokensIndex);
        this.state = ParserState.BEGIN;
    }

    // Método principal de análisis
    public void parse() {
        consulta();  // Llama a la regla principal para analizar la consulta
        
        // Verifica si el análisis terminó correctamente o si hubo un error
        if (state != ParserState.ERROR && lookahead.tipo == TipoToken.EOF) {
            System.out.println("== PARSED SUCCESSFULLY ==");
        } else if (state != ParserState.ERROR) {
            error("Error de sintaxis al final de la consulta.");
        }
    }

    // Método para comparar el token actual con el tipo esperado
    private void match(TipoToken type) {
        if (lookahead.tipo == type) { //Si el token que estamos analizando coincide con uno del HM
            tokensIndex++;  // Avanza al siguiente token si coincide el tipo
            if (tokensIndex < tokens.size()) {
                lookahead = tokens.get(tokensIndex);    // Actualiza lookahead al siguiente token
            } else {
                state = ParserState.FINISH; // Marca el estado como FINISH si ya no hay más tokens
            }
        } else {
            state = ParserState.ERROR;  // Cambia el estado a ERROR si no coincide el tipo esperado
            error("Se esperaba: " + type + " pero se encontró: " + lookahead.tipo);
        }
    }

    // Implementación de las reglas gramaticales para SQL

    // Q → select D from T W
    private void consulta() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.SELECT) {  // La consulta debe comenzar con SELECT
            match(TipoToken.SELECT);
            d(); /* ******* REVISAR IMPORTANTE ******* */
            if (lookahead.tipo == TipoToken.FROM) {
                match(TipoToken.FROM);
                t();  /* ******* REVISAR IMPORTANTE ******* */
                w();  /* ******* REVISAR IMPORTANTE ******* */
                if (lookahead.tipo == TipoToken.SEMICOLON) {
                    match(TipoToken.SEMICOLON);
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

    // D → distinct P | P
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
    private void p() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.STAR) {
            match(TipoToken.STAR);
        } else {
            f();
        }
    }

    // F → Expr F1
    private void f() {
        if (state == ParserState.ERROR) return;

        expr();
        f1();
    }

    // F1 → , Expr F1 | ε
    private void f1() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            expr();
            f1();   // Recursivo para más columnas separadas por coma
        }
    }

    // T → id T3
    private void t() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
            match(TipoToken.IDENTIFICADOR);
            t3();
        } else {
            error("Se esperaba un nombre de tabla después de 'FROM'");
        }
    }

    // T3 → , T | ε
    private void t3() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            t();  // Permite múltiples tablas separadas por coma
        }
    }

    // W → where Expr | ε
    private void w() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.WHERE) {
            match(TipoToken.WHERE);
            expr();
        }
    }

        // Reglas para expresiones lógicas (condiciones)

    // Expr → LogicOr
    private void expr() {
        logicOr();
    }

    // LogicOr → LogicAnd LogicOr1
    private void logicOr() {
        logicAnd();
        while (lookahead.tipo == TipoToken.OR) {
            match(TipoToken.OR);
            logicAnd();
        }
    }

    // LogicAnd → Equality LogicAnd1
    private void logicAnd() {
        equality();
        while (lookahead.tipo == TipoToken.AND) {
            match(TipoToken.AND);
            equality();
        }
    }

    // Equality → Comparison Equality1
    private void equality() {
        comparison();
        while (lookahead.tipo == TipoToken.EQUAL || lookahead.tipo == TipoToken.NE) {
            match(lookahead.tipo);
            comparison();
        }
    }

    // Comparison → Term Comparison1
    private void comparison() {
        term();
        while (lookahead.tipo == TipoToken.LT || lookahead.tipo == TipoToken.LE
                || lookahead.tipo == TipoToken.GT || lookahead.tipo == TipoToken.GE) {
            match(lookahead.tipo);
            term();
        }
    }

    // Term → Factor Term1
    private void term() {
        factor();
        while (lookahead.tipo == TipoToken.PLUS || lookahead.tipo == TipoToken.MINUS) {
            match(lookahead.tipo);
            factor();
        }
    }

    // Factor → Unary Factor1
    private void factor() {
        unary();
        while (lookahead.tipo == TipoToken.STAR || lookahead.tipo == TipoToken.SLASH) {
            match(lookahead.tipo);
            unary();
        }
    }

    // Unary → ! Unary | - Unary | Primary
    private void unary() {
        if (lookahead.tipo == TipoToken.NOT || lookahead.tipo == TipoToken.MINUS) {
            match(lookahead.tipo);
            unary();
        } else {
            primary();
        }
    }

    // Primary → true | false | null | number | string | id AliasOpc | ( Expr )
    private void primary() {
        if (lookahead.tipo == TipoToken.TRUE || lookahead.tipo == TipoToken.FALSE
                || lookahead.tipo == TipoToken.NULL || lookahead.tipo == TipoToken.NUMERO
                || lookahead.tipo == TipoToken.CADENA || lookahead.tipo == TipoToken.IDENTIFICADOR) {
            match(lookahead.tipo);
        } else if (lookahead.tipo == TipoToken.LEFT_PAREN) {
            match(TipoToken.LEFT_PAREN); // Coincide con el paréntesis de apertura
            expr(); // Evalúa la expresión dentro de los paréntesis
            if (lookahead.tipo == TipoToken.RIGHT_PAREN) {
                match(TipoToken.RIGHT_PAREN); // Coincide con el paréntesis de cierre
            } else {
                error("Se esperaba ')' para cerrar la expresión entre paréntesis.");
            }
        } else {
            error("Expresión no válida: se esperaba '(' o un IDENTIFICADOR.");
        }
    }

    private void error(String mensaje) {
        System.err.println("[Línea " + lookahead.linea + "] Error: " + mensaje);
        state = ParserState.ERROR;
        synchronize();
    }

    // Función para sincronizar después de un error
    private void synchronize() {
        tokensIndex++;
        while (tokensIndex < tokens.size()) {
            lookahead = tokens.get(tokensIndex);
            if (lookahead.tipo == TipoToken.FROM || lookahead.tipo == TipoToken.WHERE || lookahead.tipo == TipoToken.SEMICOLON) {
                state = ParserState.BEGIN;
                return;
            }
            tokensIndex++;
        }
    }
}


