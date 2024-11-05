import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TipoToken> palabrasReservadas;
    static {
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("and", TipoToken.AND);
        palabrasReservadas.put("or", TipoToken.OR);
        palabrasReservadas.put("select", TipoToken.SELECT);
        // Añade todas las palabras reservadas necesarias
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TipoToken.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TipoToken.LEFT_PAREN); break;
            case ')': addToken(TipoToken.RIGHT_PAREN); break;
            case '+': addToken(TipoToken.PLUS); break;
            case '-': addToken(TipoToken.MINUS); break;
            case '/': 
                if (match('/')) {
                    // Comentario de una línea
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Comentario de bloque
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (!isAtEnd()) { advance(); advance(); } // Cierra */
                } else {
                    addToken(TipoToken.SLASH);
                }
                break;
            case '*': addToken(TipoToken.STAR); break;
            case ' ':
            case '\r':
            case '\t':
                break; // Ignora espacios
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.out.println("Error en la línea " + line + ": carácter inesperado.");
                }
                break;
        }
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Detección de números flotantes
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        // Detección de notación exponencial (e.g., 1.23E-5)
        if (peek() == 'E' || peek() == 'e') {
            advance(); // consume E
            if (peek() == '+' || peek() == '-') advance(); // permite E+ o E-
            while (isDigit(peek())) advance();
        }

        String text = source.substring(start, current);
        addToken(TipoToken.NUMERO, Double.parseDouble(text));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TipoToken type = palabrasReservadas.getOrDefault(text, TipoToken.IDENTIFICADOR);
        addToken(type);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            System.out.println("Error: cadena sin cerrar en la línea " + line);
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TipoToken.CADENA, value);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TipoToken type) {
        addToken(type, null);
    }

    private void addToken(TipoToken type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
