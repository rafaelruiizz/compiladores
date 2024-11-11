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
        palabrasReservadas.put("from", TipoToken.FROM);
        palabrasReservadas.put("where", TipoToken.WHERE);
        palabrasReservadas.put("distinct", TipoToken.DISTINCT);
        palabrasReservadas.put("false", TipoToken.FALSE);
        palabrasReservadas.put("is", TipoToken.IS);
        palabrasReservadas.put("not", TipoToken.NOT);
        palabrasReservadas.put("null", TipoToken.NULL);
        palabrasReservadas.put("true", TipoToken.TRUE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // Si solo hay comentarios o espacios en blanco, asegúrate de que solo se agregue EOF.
        if (tokens.isEmpty() || (tokens.size() == 1 && tokens.get(0).tipo == TipoToken.EOF)) {
            tokens.clear(); // Limpiar los tokens en caso de que haya solo comentarios o espacios
        }

        tokens.add(new Token(TipoToken.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TipoToken.LEFT_PAREN); break;
            case ')': addToken(TipoToken.RIGHT_PAREN); break;
            case '+': addToken(TipoToken.PLUS); break;
            case '-':
                if (match('-')) { // Comentario de una línea
                    skipSingleLineComment();
                } else {
                    addToken(TipoToken.MINUS); // Solo añade MINUS si no es un comentario
                }
                break;
            case '*': addToken(TipoToken.STAR); break;
            case '/':
                if (match('*')) { // Comentario de varias líneas
                    skipMultiLineComment();
                } else {
                    addToken(TipoToken.SLASH);
                }
                break;
            case ',': addToken(TipoToken.COMA); break;
            case ';': addToken(TipoToken.SEMICOLON); break;
            case '.': addToken(TipoToken.DOT); break;
            case '=': addToken(TipoToken.EQUAL); break;
            case '>':
                addToken(match('=') ? TipoToken.GE : TipoToken.GT);
                break;
            case '<':
                addToken(match('=') ? TipoToken.LE : TipoToken.LT);
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
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

    private void skipSingleLineComment() {
        while (peek() != '\n' && !isAtEnd()) advance();
    }

    private void skipMultiLineComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // Avanza el '*'
                advance(); // Avanza el '/'
                return; // Termina el comentario de varias líneas
            }
            // Incrementa la línea si encuentra un salto de línea
            if (peek() == '\n') {
                line++;
            }
            advance(); // Avanza al siguiente carácter
        }
        // Si se alcanza el final del archivo, considera el comentario como cerrado implícitamente
        System.out.println("Advertencia: Comentario de varias líneas sin cerrar al final de la entrada.");
    } 

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        if (peek() == 'E' || peek() == 'e') {
            advance();
            if (peek() == '+' || peek() == '-') advance();
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

