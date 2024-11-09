import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Clase Scanner que actúa como analizador léxico para convertir un texto en una lista de tokens
public class Scanner {
    private final String source;  // Fuente de entrada (código a analizar)
    private final List<Token> tokens = new ArrayList<>();  // Lista para almacenar los tokens encontrados
    private int start = 0;  // Marca el inicio del lexema actual
    private int current = 0;  // Marca la posición actual en el código
    private int line = 1;  // Rastrea la línea actual para los mensajes de error

    // Mapa que asocia palabras reservadas con sus tipos de tokens
    private static final Map<String, TipoToken> palabrasReservadas;
    static {
        // Inicialización del mapa de palabras reservadas
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

    // Constructor que inicializa el analizador léxico con el texto fuente
    public Scanner(String source) {
        this.source = source;
    }

    // Método principal para iniciar el análisis léxico
    public List<Token> scanTokens() {
        while (!isAtEnd()) {  // Recorre el código fuente hasta llegar al final
            start = current;  // Marca el inicio de un nuevo lexema
            scanToken();  // Escanea el siguiente token
        }
        tokens.add(new Token(TipoToken.EOF, "", null, line));  // Añade un token de fin de archivo
        return tokens;  // Devuelve la lista de tokens encontrados
    }

    // Método que identifica el tipo de token según el carácter actual
    private void scanToken() {
        char c = advance();  // Avanza al siguiente carácter
        switch (c) {
            case '(': addToken(TipoToken.LEFT_PAREN); break;  // Paréntesis izquierdo
            case ')': addToken(TipoToken.RIGHT_PAREN); break;  // Paréntesis derecho
            case '+': addToken(TipoToken.PLUS); break;  // Operador suma
            case '-': addToken(TipoToken.MINUS); break;  // Operador resta
            case '*': addToken(TipoToken.STAR); break;  // Operador multiplicación
            case '/':
                if (match('/')) {  // Comentario de línea
                    while (peek() != '\n' && !isAtEnd()) advance();  // Ignora hasta el final de línea
                } else if (match('*')) {  // Comentario multilínea
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {  // Busca el cierre "*/"
                        if (peek() == '\n') line++;  // Aumenta la línea si encuentra un salto
                        advance();
                    }
                    if (!isAtEnd()) { advance(); advance(); }  // Avanza después de "*/"
                } else {
                    addToken(TipoToken.SLASH);  // Agrega el operador división si no es comentario
                }
                break;
            case ',': addToken(TipoToken.COMA); break;  // Coma
            case ';': addToken(TipoToken.SEMICOLON); break;  // Punto y coma
            case '.': addToken(TipoToken.DOT); break;  // Punto
            case '=': addToken(TipoToken.EQUAL); break;  // Operador igual
            case ' ':
            case '\r':
            case '\t':
                break;  // Ignora espacios y tabulaciones
            case '\n':
                line++;  // Aumenta el número de línea para el control de errores
                break;
            case '"': string(); break;  // Comienza una cadena de texto
            default:
                if (isDigit(c)) {
                    number();  // Llama al método para procesar un número
                } else if (isAlpha(c)) {
                    identifier();  // Llama al método para procesar un identificador o palabra reservada
                } else {
                    System.out.println("Error en la línea " + line + ": carácter inesperado.");  // Error para caracteres no reconocidos
                }
                break;
        }
    }

    // Método para reconocer y agregar números (incluye decimales y notación científica)
    private void number() {
        while (isDigit(peek())) advance();  // Avanza mientras haya dígitos

        if (peek() == '.' && isDigit(peekNext())) {  // Reconoce números decimales
            advance();
            while (isDigit(peek())) advance();
        }

        if (peek() == 'E' || peek() == 'e') {  // Reconoce notación científica
            advance();
            if (peek() == '+' || peek() == '-') advance();
            while (isDigit(peek())) advance();
        }

        String text = source.substring(start, current);  // Extrae el texto del número
        addToken(TipoToken.NUMERO, Double.parseDouble(text));  // Agrega el token de número con su valor literal
    }

    // Método para reconocer identificadores o palabras reservadas
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();  // Avanza mientras haya caracteres alfanuméricos
        String text = source.substring(start, current);
        TipoToken type = palabrasReservadas.getOrDefault(text, TipoToken.IDENTIFICADOR);  // Verifica si es una palabra reservada
        addToken(type);  // Agrega el token de identificador o palabra reservada
    }

    // Método para reconocer cadenas de texto
    private void string() {
        while (peek() != '"' && !isAtEnd()) {  // Avanza mientras no se cierre la cadena con comillas
            if (peek() == '\n') line++;  // Incrementa la línea si encuentra un salto
            advance();
        }
        if (isAtEnd()) {  // Error si no se cierra la cadena
            System.out.println("Error: cadena sin cerrar en la línea " + line);
            return;
        }
        advance();  // Avanza después de la comilla de cierre
        String value = source.substring(start + 1, current - 1);  // Extrae el texto de la cadena sin las comillas
        addToken(TipoToken.CADENA, value);  // Agrega el token de cadena con su valor literal
    }

    // Métodos auxiliares para verificar y avanzar en el texto fuente

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;  // Verifica el siguiente carácter
        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);  // Muestra el carácter actual sin avanzar
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);  // Muestra el siguiente carácter sin avanzar
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';  // Verifica si es un dígito
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';  // Verifica si es letra o guion bajo
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);  // Verifica si es alfanumérico
    }

    private char advance() {
        return source.charAt(current++);  // Avanza al siguiente carácter y lo devuelve
    }

    private boolean isAtEnd() {
        return current >= source.length();  // Verifica si se alcanzó el final del texto fuente
    }

    // Métodos para añadir tokens a la lista con o sin valor literal

    private void addToken(TipoToken type) {
        addToken(type, null);
    }

    private void addToken(TipoToken type, Object literal) {
        String text = source.substring(start, current);  // Extrae el texto del token
        tokens.add(new Token(type, text, literal, line));  // Crea un nuevo token y lo agrega a la lista
    }
}

