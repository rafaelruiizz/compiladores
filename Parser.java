import java.util.ArrayList;
import java.util.List;

// Enumeración para representar los posibles estados del analizador sintáctico
enum ParserState {
    BEGIN,
    FINISH,
    ERROR
}

// Clase Parser para analizar la secuencia de tokens y construir el AST
public class Parser {
    private final List<Token> tokens;
    private int tokensIndex = 0;
    private Token lookahead;
    private ParserState state;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.lookahead = tokens.get(tokensIndex);
        this.state = ParserState.BEGIN;
    }

    private void match(TipoToken type) {
        if (lookahead.tipo == type) {
            tokensIndex++;
            lookahead = (tokensIndex < tokens.size()) ? tokens.get(tokensIndex) : new Token(TipoToken.EOF, "", 0);
        } else {
            error("Se esperaba: " + type + ", pero se encontró: " + lookahead.tipo);
        }
    }

    public QueryNode consulta() {
        if (state == ParserState.ERROR) return null;
    
        if (lookahead.tipo == TipoToken.SELECT) {
            match(TipoToken.SELECT);
            SelectNode select = d();
            if (lookahead.tipo == TipoToken.FROM) {
                match(TipoToken.FROM);
                List<FromNode> from = t(); // Cambiado a una lista
                WhereNode where = w();
                if (lookahead.tipo == TipoToken.SEMICOLON) {
                    match(TipoToken.SEMICOLON);
                }
                return new QueryNode(select, from, where); // Cambia QueryNode para aceptar listas de FromNode
            } else {
                error("Se esperaba 'FROM' después de 'SELECT'");
            }
        } else {
            error("Se esperaba 'SELECT' al inicio de la consulta");
        }
        return null;
    }
    

    private SelectNode d() {
        List<ASTNode> fields = new ArrayList<>();
        if (lookahead.tipo == TipoToken.DISTINCT) {
            match(TipoToken.DISTINCT);
        }
        fields.addAll(p()); // Delegar a `p()` para manejar las proyecciones
        return new SelectNode(fields);
    }
    
    private List<ASTNode> p() {
        List<ASTNode> fields = new ArrayList<>();
        if (lookahead.tipo == TipoToken.STAR) { // Manejar el asterisco (*)
            match(TipoToken.STAR);
            fields.add(new StarNode());
        } else {
            fields.addAll(f()); // Delegar a `f()` para manejar las expresiones o funciones
        }
        return fields;
    }
    
    private List<ASTNode> f() {
        List<ASTNode> fields = new ArrayList<>();
        fields.add(expr());
        fields.addAll(f1());
        return fields;
    }
    
    private List<ASTNode> f1() {
        List<ASTNode> fields = new ArrayList<>();
        if (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            fields.add(expr());
            fields.addAll(f1());
        }
        return fields;
    }

    private List<FromNode> t() {
        List<FromNode> tables = new ArrayList<>();
        tables.add(parseTable());
        while (lookahead.tipo == TipoToken.COMA) {
            match(TipoToken.COMA);
            tables.add(parseTable());
        }
        return tables;
    }
    
    private FromNode parseTable() {
        if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
            String table = lookahead.lexema;
            match(TipoToken.IDENTIFICADOR);
            String alias = null;
            if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
                alias = lookahead.lexema;
                match(TipoToken.IDENTIFICADOR); // Consume el alias
            }
            return new FromNode(table, alias);
        } else {
            error("Se esperaba un nombre de tabla.");
            return null;
        }
    }

    private ASTNode expr() {
        ASTNode node;
    
        if (lookahead.tipo == TipoToken.IDENTIFICADOR) {
            String field = lookahead.lexema;
            match(TipoToken.IDENTIFICADOR);
    
            // Manejo de acceso a tablas (schema.tables)
            if (lookahead.tipo == TipoToken.DOT) {
                match(TipoToken.DOT);
                String subField = lookahead.lexema;
                match(TipoToken.IDENTIFICADOR);
                return new FieldNode(field + "." + subField);
            }
    
            // Manejo de funciones (func(...))
            if (lookahead.tipo == TipoToken.LEFT_PAREN) {
                match(TipoToken.LEFT_PAREN);
                List<ASTNode> arguments = new ArrayList<>();
                if (lookahead.tipo != TipoToken.RIGHT_PAREN) { // Procesa argumentos
                    arguments.add(expr());
                    while (lookahead.tipo == TipoToken.COMA) {
                        match(TipoToken.COMA);
                        arguments.add(expr());
                    }
                }
                match(TipoToken.RIGHT_PAREN);
                return new FunctionNode(field, arguments);
            }
    
            return new FieldNode(field); // Nodo para un identificador simple
        } else if (lookahead.tipo == TipoToken.NUMERO) {
            String number = lookahead.lexema;
            match(TipoToken.NUMERO);
            return new NumberNode(number);
        } else if (lookahead.tipo == TipoToken.CADENA) {
            String string = lookahead.lexema;
            match(TipoToken.CADENA);
            return new StringNode(string);
        } else if (lookahead.tipo == TipoToken.LEFT_PAREN) {
            match(TipoToken.LEFT_PAREN);
            node = logicOr(); // Procesa expresiones dentro de paréntesis
            match(TipoToken.RIGHT_PAREN);
            return node;
        } else if (lookahead.tipo == TipoToken.NOT_OPERATOR) {
            match(TipoToken.NOT_OPERATOR);
            return new NotNode(expr());
        } else {
            error("Expresión no válida.");
            return null;
        }
    }
    
    private WhereNode w() {
        if (lookahead.tipo == TipoToken.WHERE) {
            match(TipoToken.WHERE);
            ASTNode condition = logicOr(); // Delegar a `logicOr()` para manejar la lógica
            return new WhereNode(condition);
        }
        return new WhereNode(null);
    }
    private ASTNode logicOr() {
        ASTNode left = logicAnd();
        while (lookahead.tipo == TipoToken.OR) {
            match(TipoToken.OR);
            ASTNode right = logicAnd();
            left = new LogicalExprNode(left, "OR", right);
        }
        return left;
    }
    
    private ASTNode logicAnd() {
        ASTNode left = equality();
        while (lookahead.tipo == TipoToken.AND) {
            match(TipoToken.AND);
            ASTNode right = equality();
            left = new LogicalExprNode(left, "AND", right);
        }
        return left;
    }
    
    private ASTNode equality() {
        ASTNode left = comparison();
        while (lookahead.tipo == TipoToken.EQUAL || lookahead.tipo == TipoToken.NE) {
            String operator = lookahead.lexema;
            match(lookahead.tipo);
            ASTNode right = comparison();
            left = new RelationalExprNode(left, operator, right);
        }
        return left;
    }
    
    private ASTNode comparison() {
        ASTNode left = arithmeticExpr(); // Usa `arithmeticExpr` para cálculos
        while (lookahead.tipo == TipoToken.LT || lookahead.tipo == TipoToken.GT ||
               lookahead.tipo == TipoToken.LE || lookahead.tipo == TipoToken.GE) {
            String operator = lookahead.lexema;
            match(lookahead.tipo);
            ASTNode right = arithmeticExpr();
            left = new RelationalExprNode(left, operator, right);
        }
        return left;
    }
    
    private ASTNode arithmeticExpr() {
        ASTNode left = term();
        while (lookahead.tipo == TipoToken.PLUS || lookahead.tipo == TipoToken.MINUS) {
            String operator = lookahead.lexema;
            match(lookahead.tipo);
            ASTNode right = term();
            left = new ArithmeticExprNode(left, operator, right);
        }
        return left;
    }
    
    private ASTNode term() {
        ASTNode left = factor();
        while (lookahead.tipo == TipoToken.SLASH || lookahead.tipo == TipoToken.STAR) {
            String operator = lookahead.lexema;
            match(lookahead.tipo);
            ASTNode right = factor();
            left = new ArithmeticExprNode(left, operator, right);
        }
        return left;
    }
    
    private ASTNode factor() {
        if (lookahead.tipo == TipoToken.LEFT_PAREN) {
            match(TipoToken.LEFT_PAREN);
            ASTNode node = logicOr();
            match(TipoToken.RIGHT_PAREN);
            return node;
        } else if (lookahead.tipo == TipoToken.NUMERO || lookahead.tipo == TipoToken.CADENA) {
            return expr(); // Procesa valores literales
        } else {
            return expr(); // Llama a `expr` para manejar cualquier nodo
        }
    }
    private void error(String mensaje) {
        System.err.println("Error: " + mensaje);
        state = ParserState.ERROR;
    }
}