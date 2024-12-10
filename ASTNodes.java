import java.util.List;

// Nodo base del AST
abstract class ASTNode {
    public abstract void accept(PrinterQuery printer); // Método para aceptar visitantes
}

// Nodo para consultas completas
class QueryNode extends ASTNode {
    ASTNode select;
    ASTNode from;
    ASTNode where;

    public QueryNode(ASTNode select, ASTNode from, ASTNode where) {
        this.select = select;
        this.from = from;
        this.where = where;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this); // Llama al visitante para este nodo
    }
}

// Nodo para SELECT
class SelectNode extends ASTNode {
    List<ASTNode> fields; // Lista de campos seleccionados

    public SelectNode(List<ASTNode> fields) {
        this.fields = fields;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para FROM
class FromNode extends ASTNode {
    String table; // Nombre de la tabla

    public FromNode(String table) {
        this.table = table;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para expresiones aritméticas
class ArithmeticExprNode extends ASTNode {
    String left;    // Operando izquierdo
    String operator; // Operador
    String right;   // Operando derecho

    public ArithmeticExprNode(String left, String operator, String right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para WHERE
class WhereNode extends ASTNode {
    ASTNode condition; // Condición lógica en el WHERE

    public WhereNode(ASTNode condition) {
        this.condition = condition;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}