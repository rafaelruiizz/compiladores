public class PrinterQuery {
    public void visit(QueryNode node) {
        System.out.println("*Query:");
        System.out.println("  - Select:");
        if (node.select != null) {
            node.select.accept(this);
        } else {
            System.out.println("    - null");
        }

        System.out.println("  - From:");
        if (node.from != null) {
            for (FromNode table : node.from) {
                table.accept(this);
            }
        } else {
            System.out.println("    - null");
        }

        System.out.println("  - Where:");
        if (node.where != null) {
            node.where.accept(this);
        } else {
            System.out.println("    - null");
        }
    }

    public void visit(SelectNode node) {
        for (ASTNode field : node.fields) {
            field.accept(this);
        }
    }

    public void visit(FromNode node) {
        System.out.println("    - TableExpr<" + node.table + (node.alias != null ? " AS " + node.alias : "") + ">");
    }

    public void visit(WhereNode node) {
        if (node.condition != null) {
            node.condition.accept(this);
        } else {
            System.out.println("    - null");
        }
    }

    public void visit(ArithmeticExprNode node) {
        System.out.println("    - ArithmeticExpr:");
        System.out.println("      Left:");
        node.left.accept(this);
        System.out.println("      Operator: " + node.operator);
        System.out.println("      Right:");
        node.right.accept(this);
    }

    public void visit(StarNode node) {
        System.out.println("    - *");
    }

    public void visit(FieldNode node) {
        System.out.println("    - FieldExpr: " + node.field);
    }

    public void visit(NumberNode node) {
        System.out.println("    - Number: " + node.number);
    }

    public void visit(StringNode node) {
        System.out.println("    - String: " + node.value);
    }

    public void visit(FunctionNode node) {
        System.out.println("    - Function: " + node.name);
        System.out.println("      Arguments:");
        for (ASTNode arg : node.arguments) {
            arg.accept(this);
        }
    }

    public void visit(LogicalExprNode node) {
        System.out.println("    - LogicalExpr: " + node.operator);
        System.out.println("      Left:");
        node.left.accept(this);
        System.out.println("      Right:");
        node.right.accept(this);
    }

    public void visit(RelationalExprNode node) {
        System.out.println("    - RelationalExpr: " + node.operator);
        System.out.println("      Left:");
        node.left.accept(this);
        System.out.println("      Right:");
        node.right.accept(this);
    }

    public void visit(NotNode node) {
        System.out.println("    - NotExpr:");
        System.out.println("      Operand:");
        node.operand.accept(this);
    }
}


