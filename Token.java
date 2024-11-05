public class Token {
    //muestra
        final TipoToken tipo;
        final String lexema;
        final Object literal; // Para manejar valores como números o cadenas
        final int linea;
    
        public Token(TipoToken tipo, String lexema, Object literal, int linea) {
            this.tipo = tipo;
            this.lexema = lexema;
            this.literal = literal;
            this.linea = linea;
        }
    
        // Constructor simplificado si solo se necesitan tipo, lexema y línea
        public Token(TipoToken tipo, String lexema, int linea) {
            this(tipo, lexema, null, linea);
        }
    
        @Override
        public String toString() {
            return tipo + " " + lexema + " " + (literal != null ? literal.toString() : "");
        }
    }
    