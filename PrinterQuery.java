class PrinterQuery {
  public void visit(QueryNode node) {
      System.out.println("*Query:");
      System.out.println("- Select:");
      node.select.accept(this);
      System.out.println("- From:");
      node.from.accept(this);
      System.out.println("- Where:");
      if (node.where != null) {
          node.where.accept(this);
      } else {
          System.out.println("  - null");
      }
  }

  public void visit(SelectNode node) {
      for (ASTNode field : node.fields) {
          field.accept(this);
      }
  }

  public void visit(FromNode node) {
      System.out.println("  - TableExpr<" + node.table + ">");
  }

  public void visit(WhereNode node) {
      if (node.condition != null) {
          node.condition.accept(this);
      } else {
          System.out.println("  - null");
      }
  }

  public void visit(ArithmeticExprNode node) {
      System.out.println("  - ArithmeticExpr: " + node.left + " " + node.operator + " " + node.right);
  }
}

