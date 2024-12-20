public class Token {
    final TipoToken tipo;
    final String lexema;
    final Object literal;
    final int linea;

    public Token(TipoToken tipo, String lexema, Object literal, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.literal = literal;
        this.linea = linea;
    }

    public Token(TipoToken tipo, String lexema, int linea) {
        this(tipo, lexema, null, linea);
    }

    @Override
    public String toString() {//Hola Rafa :)
        return tipo + " " + lexema + " " + (literal != null ? literal.toString() : "");
    }
}
