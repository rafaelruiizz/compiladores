import java.util.List;
import java.util.ArrayList;

// Nodo base del AST
abstract class ASTNode {
    public abstract void accept(PrinterQuery printer);
}

// Nodo para consultas completas
class QueryNode extends ASTNode {
    ASTNode select;
    List<FromNode> from; // Cambiado a lista
    ASTNode where;

    public QueryNode(ASTNode select, List<FromNode> from, ASTNode where) {
        this.select = select;
        this.from = from;
        this.where = where;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}


// Nodo para SELECT
class SelectNode extends ASTNode {
    List<ASTNode> fields;

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
    String table;
    String alias;

    public FromNode(String table, String alias) {
        this.table = table;
        this.alias = alias;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para WHERE
class WhereNode extends ASTNode {
    ASTNode condition;

    public WhereNode(ASTNode condition) {
        this.condition = condition;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para expresiones aritméticas
class ArithmeticExprNode extends ASTNode {
    ASTNode left;
    String operator;
    ASTNode right;

    public ArithmeticExprNode(ASTNode left, String operator, ASTNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Nodos izquierdo y derecho no pueden ser nulos.");
        }
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para el asterisco (*)
class StarNode extends ASTNode {
    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para identificadores de campos
class FieldNode extends ASTNode {
    String field;

    public FieldNode(String field) {
        this.field = field;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para valores numéricos
class NumberNode extends ASTNode {
    String number;

    public NumberNode(String number) {
        this.number = number;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para cadenas
class StringNode extends ASTNode {
    String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para funciones
class FunctionNode extends ASTNode {
    String name;
    List<ASTNode> arguments;

    public FunctionNode(String name, List<ASTNode> arguments) {
        this.name = name;
        this.arguments = (arguments != null) ? arguments : new ArrayList<>();
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para expresiones lógicas
class LogicalExprNode extends ASTNode {
    ASTNode left;
    String operator;
    ASTNode right;

    public LogicalExprNode(ASTNode left, String operator, ASTNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Nodos izquierdo y derecho no pueden ser nulos.");
        }
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para expresiones relacionales
class RelationalExprNode extends ASTNode {
    ASTNode left;
    String operator;
    ASTNode right;

    public RelationalExprNode(ASTNode left, String operator, ASTNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Nodos izquierdo y derecho no pueden ser nulos.");
        }
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}

// Nodo para el operador NOT
class NotNode extends ASTNode {
    ASTNode operand;

    public NotNode(ASTNode operand) {
        if (operand == null) {
            throw new IllegalArgumentException("El operando no puede ser nulo.");
        }
        this.operand = operand;
    }

    @Override
    public void accept(PrinterQuery printer) {
        printer.visit(this);
    }
}
