import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;

    private final List<Token> tokens = new ArrayList<>();

   
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

   
    Scanner(String source) {
        this.source = source + " ";  
    }


    public List<Token> scanTokens() {
        String lexema = ""; 
        char caracter;
        for (int i = 0; i < source.length(); i++) {
            caracter = source.charAt(i);

           
            if (caracter == ' ' || caracter == '\n' || caracter == '\t') {
                continue;
            }

       
            if (Character.isLetter(caracter)) {
                lexema += caracter;

     
                while (i + 1 < source.length() && Character.isLetter(source.charAt(i + 1))) {
                    i++;
                    lexema += source.charAt(i);
                }

                TipoToken tipo = palabrasReservadas.get(lexema);
                if (tipo != null) {

                    tokens.add(new Token(tipo, lexema));
                } else {

                    tokens.add(new Token(TipoToken.IDENTIFICADOR, lexema));
                }

                lexema = "";
            }
        
            else if (Character.isDigit(caracter)) {
         
                while (i < source.length() && Character.isDigit(source.charAt(i))) {
                    lexema += source.charAt(i);
                    i++;
                }
                i--;  
                tokens.add(new Token(TipoToken.NUMERO, lexema));
                lexema = "";
            }
       
            else if (caracter == '"') {
                i++;  
                while (i < source.length() && source.charAt(i) != '"') {
                    lexema += source.charAt(i);
                    i++;
                }
                tokens.add(new Token(TipoToken.CADENA, lexema));  
                lexema = "";
            }
         
            else if (caracter == '=') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '=') {
                    tokens.add(new Token(TipoToken.EQUAL, "=="));
                    i++;  
                } else {
                    tokens.add(new Token(TipoToken.EQUAL, "=")); 
                }
            } else if (caracter == '!') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '=') {
                    tokens.add(new Token(TipoToken.NE, "!="));
                    i++;  
                }
            } else if (caracter == '<') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '=') {
                    tokens.add(new Token(TipoToken.LE, "<="));
                    i++;  
                } else {
                    tokens.add(new Token(TipoToken.LT, "<"));  
                }
            } else if (caracter == '>') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '=') {
                    tokens.add(new Token(TipoToken.GE, ">="));
                    i++;  
                } else {
                    tokens.add(new Token(TipoToken.GT, ">"));  
                }
            }
         
            else if (caracter == '(') {
                tokens.add(new Token(TipoToken.LEFT_PAREN, "("));
            } else if (caracter == ')') {
                tokens.add(new Token(TipoToken.RIGHT_PAREN, ")"));
            } else if (caracter == ',') {
                tokens.add(new Token(TipoToken.COMA, ","));
            } else if (caracter == ';') {
                tokens.add(new Token(TipoToken.SEMICOLON, ";"));
            } else if (caracter == '.') {
                tokens.add(new Token(TipoToken.DOT, "."));
            }
            
            else if (caracter == '+') {
                tokens.add(new Token(TipoToken.PLUS, "+")); 
            } else if (caracter == '-') {
                tokens.add(new Token(TipoToken.MINUS, "-"));  
            } else if (caracter == '*') {
                tokens.add(new Token(TipoToken.STAR, "*"));  
            } else if (caracter == '/') {
                tokens.add(new Token(TipoToken.SLASH, "/"));  
            }
        }

       
        tokens.add(new Token(TipoToken.EOF, "", source.length()));

        return tokens;
    }
}

