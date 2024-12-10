import java.util.List;

// Clase base abstracta para expresiones

import java.util.*;

abstract class Expression {
  public abstract Object solve();
}

class ExprId extends Expression {
  final Token name;

  public ExprId(Token name) {
    this.name = name;
  }

  public Object solve() {
    return null;
  }
}

class ExprArith extends Expression {
  final Expression left;
  final Token operator;
  final Expression right;

  public ExprArith(Expression left, Token operator, Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public Object solve() {
    return null;
  }
}

class ExprCallFunction extends Expression {
  final Expression callee;
  final List<Expression> arguments;

  public ExprCallFunction(Expression callee, List<Expression> arguments) {
    this.callee = callee;
    this.arguments = arguments;
  }

  public Object solve() {
    return null;
  }
}

class ExprGrouping extends Expression {
  final Expression expression;

  public ExprGrouping(Expression e) {
    this.expression = e;
  }

  public Object solve() {
    return null;
  }
}

class ExprLiteral extends Expression {
  final Object value;

  public ExprLiteral(Object value) {
    this.value = value;
  }

  public Object solve() {
    return null;
  }
}

class ExprLogical extends Expression {
  public Object solve() {
    return null;
  }
}

class ExprRelational extends Expression {
  public Object solve() {
    return null;
  }
}

class ExprUnary extends Expression {
  final Token operator;
  final Expression right;

  public ExprUnary(Token operator, Expression right) {
    this.operator = operator;
    this.right = right;
  }

  public Object solve() {
    return null;
  }
}

class ExprField extends Expression {
  final Token alias;
  final Token field;

  public ExprField(Token alias, Token field) {
    this.alias = alias;
    this.field = field;
  }

  public Object solve() {
    return null;
  }
}

class ExprTable extends Expression {
  final Token alias;
  final Token name;

  public ExprTable(Token alias, Token name) {
    this.alias = alias;
    this.name = name;
  }

  public Object solve() {
    return null;
  }
}

abstract class Statement {
  public abstract void execute();
}
