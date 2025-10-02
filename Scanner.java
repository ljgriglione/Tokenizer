import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.io.FileNotFoundException;

public final class Scanner {
    // Constant token codes
    private static final int TK_INT = 31;
    private static final int TK_ID = 32;
    private static final int TK_EOF = 33;
    private static final int TK_ERR = 34;


    private final BufferedReader br;
    private boolean endReached = false;

    // Lists to hold tokens and lexemes
    private List<Integer> tokens = new ArrayList<>();
    private List<String> lexemes = new ArrayList<>();
    private int cursor = 0;

    // Table for reserved words and symbols
    private static final Map<String,Integer> RESERVED = new HashMap<>();
    private static final Map<String,Integer> SYMBOLS = new HashMap<>();
    static {
      RESERVED.put("program", 1);
      RESERVED.put("begin", 2);
      RESERVED.put("end", 3);
      RESERVED.put("int", 4);
      RESERVED.put("if", 5);
      RESERVED.put("then", 6);
      RESERVED.put("else", 7);
      RESERVED.put("while", 8);
      RESERVED.put("loop", 9);
      RESERVED.put("read", 10);
      RESERVED.put("write", 11);

      SYMBOLS.put(";", 12);
      SYMBOLS.put(",", 13);
      SYMBOLS.put("=", 14);
      SYMBOLS.put("!", 15);
      SYMBOLS.put("[", 16);
      SYMBOLS.put("]", 17);
      SYMBOLS.put("&&", 18);
      SYMBOLS.put("||", 19);
      SYMBOLS.put("(", 20);
      SYMBOLS.put(")", 21);
      SYMBOLS.put("+", 22);
      SYMBOLS.put("-", 23);
      SYMBOLS.put("*", 24);
      SYMBOLS.put("!=", 25);
      SYMBOLS.put("==", 26);
      SYMBOLS.put("<", 27);
      SYMBOLS.put(">", 28);
      SYMBOLS.put("<=", 29);
      SYMBOLS.put(">=", 30);
}
    public Scanner(String filename) throws IOException {
      // Open the file with BufferedReader
      String firstLine;
      try {
        br = new BufferedReader(new FileReader(filename));
        firstLine = br.readLine();
      } catch (FileNotFoundException e) {
        System.err.println("File not found: " + filename);
      }
      // Tokenize the first line
      tokenizeLine(firstLine);
    }
  
    private void tokenizeLine(String line) {
      if (line == null) {          
        tokens.add(TK_EOF);
        return;
      }else if (line.isEmpty()) {
        tokens.clear();
        lexemes.clear();
        cursor = 0;
        return; // Skip empty lines
      }
      
        // Flags to indicate which sets the current token could belong to
        boolean resWordFlag = false;
        boolean symbolFlag = false;
        boolean intFlag = false;
        boolean idFlag = false;

        // This set will hold possible sets for current token
        Set<String> possibleTokens = new HashSet<>();

        // Current token being built
        StringBuilder currentToken = new StringBuilder();

        // Iterate through characters in the line
        for (int i =0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (Character.isWhitespace(ch) && currentToken.length() == 0) {
                continue; // Skip leading whitespace
            } 
            // Check token possibilities
            else if (Character.isWhitespace(ch)){
                if (possibleTokens.size() == 1){
                  // Only one possibility remains
                  // Finalize token
                    if(resWordFlag && RESERVED.containsKey(currentToken.toString())){
                        tokens.add(RESERVED.get(currentToken.toString()));
                        lexemes.add(currentToken.toString());
                    }else if(symbolFlag && SYMBOLS.containsKey(currentToken.toString())){
                        tokens.add(SYMBOLS.get(currentToken.toString()));
                        lexemes.add(currentToken.toString());
                    }else{
                      // Invalid token
                      tokens.add(TK_ERR);
                      lexemes.add(currentToken.toString());
                    }
                    resWordFlag = symbolFlag = intFlag = idFlag = false;
                    possibleTokens.clear();
                    currentToken.setLength(0);
                    break;
                }else{
                    if(intFlag){
                      tokens.add(TK_INT);
                      lexemes.add(currentToken.toString());
                    }else if(idFlag){
                      tokens.add(TK_ID);
                      lexemes.add(currentToken.toString());
                    }else{
                        // Invalid token
                        tokens.add(TK_ERR);
                        lexemes.add(currentToken.toString());
                        
                    }
                    break;
                }
            
            
            }else{
              // Add character to current token
                currentToken.append(ch);
              
              // 4 main cases for character: Reserved Word, Symbol, Digit, Identifier
        
              // Check digit
              // 3 main cases for digit: Integer, A non leading digit identifier, Invalid token
              if (Character.isDigit(ch)){
                // Current char is a digit
                if (currentToken.length() == 1){
                  intFlag = true;
                }else if (!idFlag){
                  // Invalid token
                  tokens.add(TK_ERR);
                  lexemes.add(currentToken.toString());
                  break;
                }
                // Continue to next character
                continue;

                // Check identifier
                // 3 main cases for identifier: Start of identifier, Middle of identifier, Invalid token
              }else if (Character.isUpperCase(ch)){
                if (currentToken.length() == 1){
                  idFlag = true;
                }else if (!idFlag){
                  // Invalid token
                  tokens.add(TK_ERR);
                  lexemes.add(currentToken.toString());
                  break;
                }
                // Continue to next character
                continue;
              }

              // Check reserved word
              // 3 main cases for reserved word: Start of reserved word, Middle of reserved word, invalid token
              else if (Character.isLowerCase(ch)){
                // Check to see if start of a reserved word
                if (currentToken.length() == 1){
                  resWordFlag = true;
                  // Seeing which reserved words could match
                  for (String rw : RESERVED.keySet()){
                    if (rw.startsWith(currentToken.toString())){
                      possibleTokens.add(rw);
                    }
                  }
                  // If no reserved words start with this character, invalid token
                  if (possibleTokens.size() == 0){
                    tokens.add(TK_ERR);
                    lexemes.add(currentToken.toString());
                    break;
                  }
                // Check to see if middle of reserved word
                }else if (resWordFlag){
                  // Filter possible reserved words
                  String prefix = currentToken.toString();
                  possibleTokens.removeIf(rw -> !rw.startsWith(prefix));

                  // If no reserved words start with this prefix, invalid token
                  if (possibleTokens.size() == 0){
                    tokens.add(TK_ERR);
                    lexemes.add(currentToken.toString());
                    break;
                  }

                }else if (!idFlag){
                  // Invalid token
                  tokens.add(TK_ERR);
                  lexemes.add(currentToken.toString());
                  break;
                }
                // Continue to next character
                continue;
              }

              else if(SYMBOLS.containsKey(currentToken.toString()) || currentToken.toString().equals("&") || currentToken.toString().equals("|")){
                // Can get away with checking to see if the character is in the map 
                // because all symbols are either 1 or 2 characters long and 
                // the only 2 character symbols are &&, ||, !=, ==, <=, >=
                // Only special case is && and || so manually will have to add to set
                
                // Special case for && and || this is when we see the first & or |
                // Three checks to make sure its this case
                if (currentToken.toString().equals("&") && !symbolFlag && !possibleTokens.contains("&&")){
                  possibleTokens.add("&&");
                  continue;
                }else if (currentToken.toString().equals("|") && !symbolFlag && !possibleTokens.contains("||")){
                  possibleTokens.add("||");
                  continue;
                }

                // Now have to check other non special case symbols
                // First to see if its a the first character symbol
                if (currentToken.length() == 1){
                  symbolFlag = true;
                  // Seeing which symbols could match
                  for (String sym : SYMBOLS.keySet()){
                    if (sym.startsWith(currentToken.toString())){
                      possibleTokens.add(sym);
                    }
                  }
                // Must be middle of symbol
                }else if (symbolFlag){
                  // Filter possible symbols
                  String prefix = currentToken.toString();
                  possibleTokens.removeIf(sym -> !sym.startsWith(prefix));
                   
                  if (possibleTokens.size() == 0){
                    tokens.add(TK_ERR);
                    lexemes.add(currentToken.toString());
                    break;
                  }
                }else{
                  // Invalid token
                  tokens.add(TK_ERR);
                  lexemes.add(currentToken.toString());
                  break;
                }

                continue;
              }

              else{
                // Invalid token
                tokens.add(TK_ERR);
                lexemes.add(currentToken.toString());
                break;
              }

            
            

          }

        }
    }

    
    
  
    public int getToken() {
      // return tokens.get(cursor); (or 33 if already EOF)
    }
  
    public void skipToken(){
      // if current is 33 or 34: return;
      // cursor++;
      // if cursor >= tokens.size(): tokenizeLine();
    }
  
    public int intVal() { /* only if getToken()==31 */ }
    public String idName() { /* only if getToken()==32 */ }
  }
  
