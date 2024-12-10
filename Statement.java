import java.util.List;

// Clase base abstracta para las declaraciones SQL

abstract class Statement {
  public abstract void execute();
}

class FromStatement extends Statement {
  public final List<ExprTable> tables;

  public FromStatement(List<ExprTable> tables) {
    this.tables = tables;
  }

  public void execute() {
  }
}

class WhereStatement extends Statement {
  public final Expression expr;

  public WhereStatement(Expression expr) {
    this.expr = expr;
  }

  public void execute() {
  }
}

class SelectStatement extends Statement {
  public final List<Expression> expressions;
  final boolean includesDistinct;
  final boolean includesAsterisk;

  public SelectStatement(List<Expression> expressions, boolean includesDistinct, boolean includesAsterisk) {
    this.expressions = expressions;
    this.includesDistinct = includesDistinct;
    this.includesAsterisk = includesAsterisk;
  }

  public void execute() {

  }
}

class QueryStatement extends Statement {
  final SelectStatement selectStmt;
  final FromStatement fromStmt;
  final WhereStatement whereStmt;

  public QueryStatement(SelectStatement selectStmt, FromStatement fromStmt, WhereStatement whereStmt) {
    this.selectStmt = selectStmt;
    this.fromStmt = fromStmt;
    this.whereStmt = whereStmt;
  }

  public void execute() {

  }
}


