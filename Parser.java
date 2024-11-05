import java.util.List;

enum ParserState {
    BEGIN, FINISH, ERROR
}

public class Parser {
    private final List<Token> tokens;
    private int tokensIndex = 0;
    private Token lookahead;
    private ParserState state;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokensIndex = 0;
        this.lookahead = tokens.get(tokensIndex);
        this.state = ParserState.BEGIN;
    }

    public void parse() {
        consulta();  // Inicia el análisis de una consulta SQL
        
        if (state != ParserState.ERROR && lookahead.tipo == TipoToken.EOF) {
            System.out.println("== PARSED SUCCESSFULLY ==");
        } else {
            error("Error de sintaxis al final de la consulta.");
        }
    }

    private void match(TipoToken type) {
        if (lookahead.tipo == type) {
            tokensIndex++;
            if (tokensIndex < tokens.size()) {
                lookahead = tokens.get(tokensIndex);
            } else {
                state = ParserState.FINISH;
            }
        } else {
            state = ParserState.ERROR;
            error("Se esperaba: " + type + " pero se encontró: " + lookahead.tipo);
        }
    }

    // Implementación de las reglas gramaticales para SQL

    // Regla principal: consulta SQL
    // Q → SELECT D FROM T W
    private void consulta() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.SELECT) {
            match(TipoToken.SELECT);
            d();  // Maneja la cláusula SELECT
            if (lookahead.tipo == TipoToken.FROM) {
                match(TipoToken.FROM);
                t();  // Maneja la cláusula FROM
                w();  // Maneja la cláusula WHERE (opcional)
            } else {
                error("Se esperaba 'FROM' después de 'SELECT'");
            }
        } else {
            error("Se esperaba 'SELECT' al inicio de la consulta");
        }
    }

    // D → DISTINCT P | P
    private void d() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.DISTINCT) {
            match(TipoToken.DISTINCT);
            p();  // Procesa los campos a seleccionar
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
            f1();
        }
    }

    // T → IDENTIFICADOR T3
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
            t();
        }
    }

    // W → WHERE Expr | ε
    private void w() {
        if (state == ParserState.ERROR) return;

        if (lookahead.tipo == TipoToken.WHERE) {
            match(TipoToken.WHERE);
            expr();
        }
    }

    // Expr → LogicOr
    private void expr() {
        logicOr();
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
                || lookahead.tipo == TipoToken.GT || lookahead.tipo == TipoToken.GE) {
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
        if (lookahead.tipo == TipoToken.TRUE || lookahead.tipo == TipoToken.FALSE
                || lookahead.tipo == TipoToken.NULL || lookahead.tipo == TipoToken.NUMERO
                || lookahead.tipo == TipoToken.CADENA || lookahead.tipo == TipoToken.IDENTIFICADOR) {
            match(lookahead.tipo);
        } else if (lookahead.tipo == TipoToken.LEFT_PAREN) {
            match(TipoToken.LEFT_PAREN);
            expr();
            match(TipoToken.RIGHT_PAREN);
        } else {
            error("Expresión no válida: se esperaba '(' o un IDENTIFICADOR.");
        }
    }

    private void error(String mensaje) {
        System.err.println("[Línea " + lookahead.linea + "] Error: " + mensaje);
        state = ParserState.ERROR;
    }
}
