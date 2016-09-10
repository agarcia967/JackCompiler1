/**@(#)Tokenizer.java
 * @author Anthony R. Garcia
 * @date   2015/04/15
 */
package edu.miracosta.cs220;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class JackTokenizer {
  private final static boolean DEBUG = false;

  public final static String[] VALID_KEYWORDS = {"boolean", "char", "class",
    "constructor", "do", "else", "false", "field", "function", "if", "int",
    "let", "method", "null", "return", "static", "this", "true", "var",
    "void", "while"};

  public final static String VALID_SYMBOLS = "{}()[].,;+-*/&|<>=~";

  private final static int INT_MAX_VAL = 32767;
  private final static int INT_MIN_VAL = 0;

  public final static String VALID_IDENTIFIER_START =
    "_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  public final static String VALID_IDENTIFIER_CHAR =
    VALID_IDENTIFIER_START + "0123456789";

  private final static String LINE_DELIMITER_TOKEN = "#####";

  private Scanner scanner;
  private String rawline;
  private String cleanline;
  private int lineNumber;
  private boolean multiLineComment;

  private ArrayList<String> tokenList;
  private int currentIndex;
  private String currentToken;

  private TokenType tokenType;
  private String keyword;
  private char symbol;
  private String identifier;
  private int intVal;
  private String stringVal;

  /**Opens the file stream and gets ready to tokenize it.
   * @pre  stream must not be null
   * @post tokenList is ready to be read from
   * @arg  the stream to read from
   */
  public JackTokenizer(String inFileName) {
    if(inFileName==null){
      if(DEBUG) System.out.println("Tokenizer Constructor: filename may not be null.");
      System.exit(0);
    }
    FileInputStream inStream = null;
    try{
      if(DEBUG) System.out.print("Opening input file... ");
      inStream = new FileInputStream(inFileName);
      if(DEBUG) System.out.println("Success!");
    } catch(FileNotFoundException e){
      System.out.println("File \"" + inFileName + "\" not found.");
      System.exit(0);
    }
    this.scanner = new Scanner(inStream);
    this.tokenList = new ArrayList<String>();
    this.multiLineComment = false;
    this.lineNumber = 0;

    this.tokenize();

    for(int i = 0; i<tokenList.size() && DEBUG; i++){
      if(tokenList.get(i).equals(LINE_DELIMITER_TOKEN)){
        System.out.println();
      } else {
        System.out.print(tokenList.get(i) + " ");
      }
    }
    //get ready for tokens to be pulled from CompilationEngine
    this.lineNumber = 1;
    this.currentIndex = -1;
    this.currentToken = null;
    this.tokenType = null;
  }//END JackTokenizer(1)

  /**Returns whether the file has more tokens.
   * @pre    tokenList must be initialized
   * @return true if the file has more tokens
   */
  public boolean hasMoreTokens(){
    if(this.currentIndex+1>=tokenList.size()){
      return false;
    }
    return true;
  }

  /**Returns the next token from the file.
   * @pre    tokenList must be initialized
   * @return the current token as a String
   */
  public String nextToken(){
    this.currentIndex++;
    if(this.currentIndex>=this.tokenList.size()){
      return null;
    }
    this.currentToken = this.tokenList.get(this.currentIndex);
    while(this.currentToken.equals(LINE_DELIMITER_TOKEN)){
      if(DEBUG) System.out.println("Tokenizer.next: Skipped line delimiter.");
      this.lineNumber++;
      this.currentIndex++;
      if(this.currentIndex>=this.tokenList.size()){
        return null;
      }
      this.currentToken = this.tokenList.get(this.currentIndex);
    }
    return this.currentToken;
  }//END nextToken()

  /**Prepares the tokens for analyzing by the CompilationEngine.
   * @pre  hasMoreTokens() must be called and return true
   *       this.scanner must be initialized
   * @post intializes the ArrayList<String> of tokens
   */
  private void tokenize(){
    if(DEBUG) System.out.println("tokenize()");
    while(this.scanner.hasNextLine()){
      this.rawline = scanner.nextLine();
      this.lineNumber++;
      if(DEBUG) System.out.println("rawline: \"" + this.rawline + "\"");

      cleanLine();
      if(this.rawline!=null || !this.rawline.isEmpty() || this.rawline.length()>0){

        //remove comments from line, clean up whitespace, & handle multi-line comments

        ///// ACTUAL TOKENIZING /////
        while(!this.cleanline.isEmpty()){
          if(DEBUG) System.out.println("\ncleanline: \'" + this.cleanline + "\'");
          boolean validToken = false;

          //if(DEBUG) System.out.println("   Checking for KEYWORD...");
          //look for a keyword
          int endpoint = 0;
          for(int i = 0; i<VALID_KEYWORDS.length; i++){
            if(this.cleanline.startsWith(VALID_KEYWORDS[i])){
              endpoint = VALID_KEYWORDS[i].length();
              validToken = true;
              break;
            }
          }
          if(!validToken && this.cleanline.startsWith("\"")){
            //it must be a STRING_CONST
            //if(DEBUG) System.out.println("   STRING_CONST found!");
            endpoint = this.cleanline.substring(1,this.cleanline.length()).indexOf("\"")+2;
          }
          else if(!validToken && VALID_SYMBOLS.contains(this.cleanline.charAt(0)+"")){
            //it must be a SYMBOL
            //if(DEBUG) System.out.println("   SYMBOL found!");
            endpoint = 1;
          }
          else if(!validToken){
            //if(DEBUG) System.out.println("   Could be an IDENTIFIER...");
            for(int j = 0; j<this.cleanline.length(); j++){
              char currentChar = this.cleanline.charAt(j);
              if(VALID_IDENTIFIER_CHAR.contains(currentChar+"")){
                endpoint++;
              }
              else break;
            }
          }//END else if(!validToken)

          if(DEBUG) System.out.println(" >>Token: " + this.cleanline.substring(0,endpoint));

          this.tokenList.add(this.cleanline.substring(0,endpoint));
          this.cleanline = this.cleanline.substring(endpoint,this.cleanline.length()).trim();
        }//END tokenizing loop

        if(DEBUG) System.out.println("[Line " + this.lineNumber + " tokenizing complete].\n");
      }//END if valid rawline

      this.tokenList.add(LINE_DELIMITER_TOKEN);
    }//END file loop

  }//end tokenize()

  /**Cleans the rawline and initializes cleanline.
   * @pre  this.rawline must be initialized
   * @post this.cleanline is initialized
   *       this.multiLineComment is set
   */
  public void cleanLine(){
    //handle null values
    if(this.rawline==null || this.rawline.isEmpty() || this.rawline.length()<=0) return;

    //find index of end of line comment
    int commentPosition = this.rawline.indexOf("//");
    //strip it out
    if(commentPosition>=0){
      this.cleanline = this.rawline.substring(0,commentPosition).trim();
    } else {
      this.cleanline = this.rawline.trim();
    }

    //find index of start mutli-line comment
    int startMLCommentPosition = this.cleanline.indexOf("/*");
    //find index of end mutli-line comment
    int endMLCommentPosition   = this.cleanline.indexOf("*/");

    //if we are currently inside of a multi-line comment
    if(this.multiLineComment==true && endMLCommentPosition<0){
      this.cleanline = "";
      return;
    }
    //set starting multiline comment status
    if(startMLCommentPosition>=0) this.multiLineComment = true;
    //set ending multiline comment status
    if(endMLCommentPosition>=0) this.multiLineComment = false;


    //if line contains both && start is less than end
    if(startMLCommentPosition>=0 && endMLCommentPosition>=0 &&
      startMLCommentPosition<endMLCommentPosition) {
      //strip out the comment
      if(DEBUG) System.out.println("Start: " + startMLCommentPosition + " - End: " + endMLCommentPosition);
      this.cleanline = this.cleanline.substring(0,startMLCommentPosition) +
        this.cleanline.substring(endMLCommentPosition+2,this.cleanline.length());
    }
    //else if line contains both
    else if(startMLCommentPosition>=0 && endMLCommentPosition>=0){
      //strip the comments & contain the inner text
      this.cleanline = this.cleanline.substring(endMLCommentPosition, startMLCommentPosition);
    }

    startMLCommentPosition = this.cleanline.indexOf("/*");
    endMLCommentPosition   = this.cleanline.indexOf("*/");

    //set start point to 0
    int startPosition = 0,
    //set end point to .length()
        endPosition = this.cleanline.length();

    //if contains start ml comment
    if(startMLCommentPosition>=0){
      //set end point
      endPosition = startMLCommentPosition;
    }

    //if contains end ml comment
    if(endMLCommentPosition>=0){
      //set start point
      startPosition = endMLCommentPosition+2;
    }

    if(DEBUG) System.out.println("Substringing: " + this.cleanline);
    //return substring from start point to endpoint
    this.cleanline = this.cleanline.substring(startPosition,endPosition).trim();
    if(DEBUG) System.out.println("Returning: " + this.cleanline);
    return;
  }//END cleanLine

  /**Returns the type of the current token.
   * @pre    advance should be called first
   * @return the type of the current token,
   *         this.currentToken is initialized,
   */
  public TokenType tokenType(){

	//check for KEYWORD
    for(int i = 0; i < VALID_KEYWORDS.length; i++){
      if(this.currentToken.equals(VALID_KEYWORDS[i])){
        this.tokenType = TokenType.KEYWORD;
        return this.tokenType;
      }
    }

    //check for INT_CONST
    try{
   	  //if it can be cast to an int
      this.intVal = Integer.parseInt(this.currentToken);
      //it must be an INT_CONST
      this.tokenType = TokenType.INT_CONST;
      return this.tokenType;
    } catch(NumberFormatException e) {
      if(DEBUG) System.out.println("Not an INT_CONST");
      //move on
    }

    //check for STRING_CONST
    if(this.currentToken.startsWith("\"")){
      this.stringVal = this.currentToken.substring(1,this.currentToken.length()-1);
      this.tokenType = TokenType.STRING_CONST;
      return this.tokenType;
    }

    //check for SYMBOL
    else if(VALID_SYMBOLS.contains(this.currentToken)){
      this.symbol = this.currentToken.charAt(0);
      this.tokenType = TokenType.SYMBOL;
      return this.tokenType;
    }

    //check for IDENTIFIER
    else{
      if(VALID_IDENTIFIER_START.contains(this.currentToken.charAt(0)+"")){
      	for(int i = 0; i<this.currentToken.length(); i++){
      	  if(!VALID_IDENTIFIER_CHAR.contains(currentToken.charAt(0)+"")){
            if(DEBUG) System.out.println("Found unidentifiable token: " + this.currentToken);
            System.exit(0);
      	  }
      	}
      }
      this.tokenType = TokenType.IDENTIFIER;
      return this.tokenType;
    }
  }//END tokenType()

  /**Returns the current line number.
   * @return the line number int value
   */
  public int lineNumber(){
    return this.lineNumber;
  }

  /**Returns the current token.
   * @return the String value of the current token.
   */
  public String currentToken(){
    return this.currentToken;
  }

  /**Returns the next token.
   * @return the String value of the next token
   *         null if next token does not exist
   */
  public String peekToken(){
    int increment = 1;
    if(this.currentIndex+1>=this.tokenList.size()){
      return null;
    }
    while(this.tokenList.get(this.currentIndex+increment).equals(LINE_DELIMITER_TOKEN)){
      if(DEBUG) System.out.println("Tokenizer.peek: Skipped line delimiter: " + increment);
      increment++;
    }
    return this.tokenList.get(this.currentIndex+increment);
  }

  /**Returns the keyword String value of the current token.
   * @pre    this.tokenType must be KEYWORD
   * @post   will EXIT program if token is not KEYWORD
   * @return the enum keyword of the current token
   */
  public String keyword(){
    this.tokenTypeCheck(this.tokenType, TokenType.KEYWORD);
    return this.keyword;
  }

  /**Returns the char value of the current char token.
   * @pre    this.tokenType must be SYMBOL
   * @post   will EXIT program if token is not SYMBOL
   * @return the char of the current token
   */
  public char symbol(){
    this.tokenTypeCheck(this.tokenType, TokenType.SYMBOL);
    return this.symbol;
  }

  /**Returns the String value of the current identifier token.
   * @pre    this.tokenType must be IDENTIFIER
   * @post   will EXIT program if token is not IDENTIFIER
   * @return the String value of the current token
   */
  public String identifier(){
    this.tokenTypeCheck(this.tokenType, TokenType.IDENTIFIER);
    return this.identifier;
  }

  /**Returns the int value of the current int constant token.
   * @pre    this.tokenType must be INT_CONST
   * @post   will EXIT program if token is not INT_CONST
   * @return the int value of the current token
   */
  public int intVal(){
    this.tokenTypeCheck(this.tokenType, TokenType.INT_CONST);
    return this.intVal;
  }

  /**Returns the String value of the current string constant token.
   * @pre    this.tokenType must be STRING_CONST
   * @post   will EXIT program if token is not STRING_CONST
   * @return the String value of the current token
   */
  public String stringVal(){
    this.tokenTypeCheck(this.tokenType, TokenType.STRING_CONST);
    return stringVal;
  }

  /**Stops the program with an error message upon token comparison failure.
   * @post WILL EXIT program if current!=expected
   */
  private void tokenTypeCheck(TokenType current, TokenType expected){
    if(current!=expected){
      System.out.println("ERROR: The current token, of type " +
        current.toString().toUpperCase() +", was expected to be a(n) " +
        expected.toString().toUpperCase() + ".");
      System.exit(0);
    }
  }//END tokenTypeCheck
}//END class JackTokenizer

enum TokenType{
  IDENTIFIER,
  INT_CONST,
  KEYWORD,
  STRING_CONST,
  SYMBOL;

  @Override
  public String toString(){
    switch(this){
      case IDENTIFIER:
        return "identifier";
      case INT_CONST:
        return "integerConstant";
      case KEYWORD:
        return "keyword";
      case STRING_CONST:
        return "stringConstant";
      case SYMBOL:
        return "symbol";
      default:
        return "No string for this Token.";
    }
  }
}//END enum TokenType
