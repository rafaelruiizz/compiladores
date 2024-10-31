public enum TipoToken {
    //ID, numeros y cadenas
    IDENTIFICADOR,
    NUMERO,
    CADENA,


    // Palabras reservadas y logicos
    AND, DISTINCT, FALSE, FROM, IS, NOT, NULL, OR, SELECT, TRUE, WHERE,

    //signos de puntuacion
    COMA, SEMICOLON, DOT, LEFT_PAREN, RIGHT_PAREN,
    //Relacionales 
    LT, LE, GT, GE, EQUAL, NE,
    //Aritmeticos
    PLUS, MINUS, STAR, SLASH,

    // Final de cadena
    EOF
}
