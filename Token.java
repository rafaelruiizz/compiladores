public class Token {

    final TipoToken tipo;
    final String lexema;
    final int linea;

    public Token(TipoToken tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    public Token(TipoToken tipo, String lexema) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = 0;
    }

    public String toString(){
        return tipo + " " + lexema + " ";
    }
}
