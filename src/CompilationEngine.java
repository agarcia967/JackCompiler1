/**@(#)CompilationEngine.java
 * @author Anthony R. Garcia
 * @version 1.00 2015/4/22
 */
package edu.miracosta.cs220;

///// THIS IS A PARSER! /////
public class CompilationEngine {
  private static final boolean DEBUG = false;
  private static final String[] KEYWORD_CONSTS = {"true","false","null","this"};
  private static final String OPERATORS = "+-*/&|<>=";
  private static final String UNARY_OPS = "~-";
  private static final String FILE_WRITE_EXT = ".xml";
  private String inFile;
  private String outFile;
  private XMLWriter writer;
  private JackTokenizer tokenizer;

  public CompilationEngine(String inFile) {
    this.inFile = inFile;
    int index = inFile.indexOf(".jack");
    if(index<=0){
      System.out.println("File extensions must be '.jack'.\n" +
        "Incompatible file: " + inFile);
      System.exit(0);
    }
    this.outFile = inFile.substring(0,index) + FILE_WRITE_EXT;
    this.tokenizer = new JackTokenizer(this.inFile);
    this.writer = new XMLWriter(this.outFile);

    ///// TOKENIZER WORKS /////
    while(tokenizer.hasMoreTokens() && DEBUG){
      String token = tokenizer.nextToken();
      System.out.println("<" + tokenizer.tokenType().toString() + ">" +
        token + "</" + tokenizer.tokenType().toString() + ">");
    }

    compileClass();
    this.writer.close();
  }

  /**Compiles a complete class.
   *@post Writes a class to file.
   */
  private void compileClass(){
    writer.writeNonTerminal("classDec",true);
    String token;

    //check token is 'class' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("class")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected class declaration.");
    }

    //check token is IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      if(JackTokenizer.VALID_IDENTIFIER_START.contains(tokenizer.currentToken().charAt(0)+"")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() +
          ": Identifier should start with '_', A-Z, or a-z.");
      }
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
    }

    //check token is '{' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("{")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '{'. Found: " + token);
    }

    //while next token is 'static' | 'field'
    while(tokenizer.peekToken()!=null && (tokenizer.peekToken().equals("static") || tokenizer.peekToken().equals("field"))){
      //call compileClassVarDec
      compileClassVarDec();
    }

    //while next token is 'constructor' or 'function' or 'method'
    while(tokenizer.peekToken()!=null && (tokenizer.peekToken().equals("constructor") ||
      tokenizer.peekToken().equals("function") || tokenizer.peekToken().equals("method"))){
      //call compileSubroutine
      compileSubroutineDec();
    }

    //check token is '}' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("}")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '}'. Found: " + token);
    }

    //ensure end of file
    token = tokenizer.nextToken();
    if(token!=null){
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected end of file. Found: " + token);
    }

    writer.writeNonTerminal("classDec",false);
  }

  /**Compiles a line of class variable declarations.
   *@post Writes class variable declarations to file.
   */
  private void compileClassVarDec(){
    writer.writeNonTerminal("subroutineDec",true);
    String token;
    //TODO compileClassVarDec
    //token should be 'static' | 'field'
    token = tokenizer.nextToken();
    if(token!=null && (token.equals("static") || token.equals("field"))){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected 'static' or 'field'. Found: " + token);
    }

    //check token is 'int' | 'char' | 'boolean' | IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && (token.equals("int") || token.equals("char") || token.equals("boolean") ||
      tokenizer.tokenType()==TokenType.IDENTIFIER)){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected 'int', 'char', 'boolean', or class name. Found: " + token);
    }

    //check tokenType is IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
    }

    //while next token is ','
    while(tokenizer.peekToken()!=null && tokenizer.peekToken().equals(",")){
      //write ','
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //check token is IDENTIFIER & write
      token = tokenizer.nextToken();
      if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
      }
    }

    //check token is ';' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(";")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ';'. Found: " + token);
    }

    writer.writeNonTerminal("subroutineDec",false);
  }

  /**Compiles a subroutine declaration.
   *@post Writes subroutine declaration to file.
   */
  private void compileSubroutineDec(){
    writer.writeNonTerminal("subroutineDec",true);
    String token;

    //check token is 'constructor' | 'function' | 'method'
    token = tokenizer.nextToken();
    if(token!=null && (token.equals("constructor") || token.equals("function") || token.equals("method"))){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected 'constructor', 'function', or 'method'. Found: " + token);
    }

    //check token is 'void' | 'int' | 'char' | 'boolean' | IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && (token.equals("void") || token.equals("int") || token.equals("char") ||
      token.equals("boolean") || tokenizer.tokenType()==TokenType.IDENTIFIER)){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected 'void', 'int', 'char', 'boolean', or class name. Found: " + token);
    }

    //check tokenType is IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
    }

    //check token is '(' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("(")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '('. Found: " + token);
    }

    //if next token is NOT ')'
    if(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals(")")){
      //call compileParameterList
      compileParameterList();
    }
    //check token is ')' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(")")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
    }

    writer.writeNonTerminal("subroutineBody",true);
    //check token is '{' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("{")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '{'. Found: " + token);
    }

    //while next token is 'var'
    while(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("var")){
      //call compileVarDec
      compileVarDec();
    }

    //if next token is NOT '}'
    if(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals("}")){
      //call compileStatements
      compileStatements();
    }

    //check token is '}' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("}")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '}'. Found: " + token);
    }

    writer.writeNonTerminal("subroutineBody",false);

    writer.writeNonTerminal("subroutineDec",false);
  }

  /**Compiles a list of subroutine parameters.
   *@post Writes a list of subroutine parameters to file.
   */
  private void compileParameterList(){
    writer.writeNonTerminal("parameterList",true);
    String token;

    //check token is 'int' | 'char' | 'boolean' | IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && (token.equals("int") || token.equals("char") || token.equals("boolean") ||
      tokenizer.tokenType()==TokenType.IDENTIFIER)){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected 'int', 'char', 'boolean', or class name. Found: " + token);
    }

    //check token is IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
    }

    //while next token is ','
    while(tokenizer.peekToken()!=null && tokenizer.peekToken().equals(",")){
      //write ','
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //check token is 'int' | 'char' | 'boolean' | IDENTIFIER & write
      token = tokenizer.nextToken();
      if(token!=null && (token.equals("int") || token.equals("char") || token.equals("boolean") ||
        tokenizer.tokenType()==TokenType.IDENTIFIER)){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() +
          ": Expected 'int', 'char', 'boolean', or class name. Found: " + token);
      }

      //check token is IDENTIFIER & write
      token = tokenizer.nextToken();
      if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
      }

    }
    writer.writeNonTerminal("parameterList",false);
  }

  /**Compiles a line of subroutine variable declarations.
   *@post Writes subroutine variable declarations to file.
   */
  private void compileVarDec(){
    writer.writeNonTerminal("variableDec",true);
    String token;

    //check token is 'var' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("var")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected 'var'. Found: " + token);
    }

    //check token is 'void' | 'int' | 'char' | 'boolean' | IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && (token.equals("void") || token.equals("int") || token.equals("char") ||
      token.equals("boolean") || tokenizer.tokenType()==TokenType.IDENTIFIER)){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected 'void', 'int', 'char', 'boolean', or class name. Found: " + token);
    }

    //check token is IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
    }

    //while next token is ','
    while(tokenizer.peekToken()!=null && tokenizer.peekToken().equals(",")){
      //write ','
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //check token is IDENTIFIER & write
      token = tokenizer.nextToken();
      if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
      }
    }

    //check token is ';' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(";")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ';'. Found: " + token);
    }

    writer.writeNonTerminal("variableDec",false);
  }

  /**Compiles a while statement.
   *@pre  Surrounding right token MUST be '}'
   *@post Writes a while statement to file.
   */
  private void compileStatements(){
    writer.writeNonTerminal("statementList",true);
    String token;

    //while next token is not '}'
    while(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals("}")){
      //if token is 'let'
      if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("let")){
        //call compileLet()
        compileLet();
      }
      //else if token is 'if'
      else if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("if")){
        //call compileIf()
        compileIf();
      }
      //else if token is 'while'
      else if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("while")){
        //call compileWhile()
        compileWhile();
      }
      //else if token is 'do'
      else if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("do")){
        //call compileDo()
        compileDo();
      }
      //else if token is 'return'
      else if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("return")){
        //call compileReturn()
        compileReturn();
      }
      //else
      else{
        //expected 'let' | 'if' | 'while' | 'do' | 'return'
        System.out.println("Line " + tokenizer.lineNumber() +
          ": Expected 'let', 'do', 'while', 'if', or 'return'. Found: " + tokenizer.peekToken());
          return;
      }
    }
    writer.writeNonTerminal("statementList",false);
  }

  /**Compiles a do statement.
   *@post Writes a do statement to file.
   */
  private void compileDo(){
    writer.writeNonTerminal("doStatement",true);
    String token;

    //write 'do'
    token = tokenizer.nextToken();
    writer.writeTerminal(tokenizer.tokenType(),token);

    //subroutineCall similar code to compileTerm code
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER && tokenizer.peekToken().equals("(")){
      //write identifier
      writer.writeTerminal(tokenizer.tokenType(),token);

      //write '('
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //call compileExpression()
      compileExpressionList();

      //check token is ')' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals(")")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
      }
    }
    //else if tokenType is IDENTIFIER && next token is '.'
    else if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER &&
      tokenizer.peekToken().equals(".")){
      //write identifier
      writer.writeTerminal(tokenizer.tokenType(),token);

      //write '.'
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //check token is IDENTIFIER & write
      token = tokenizer.nextToken();
      if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
      }

      //check token is '(' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals("(")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected '('. Found: " + token);
      }

      //call compileExpressionList()
      compileExpressionList();

      //check token is ')' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals(")")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
      }
    }
    else {
      System.out.println("Line " + tokenizer.lineNumber() +
        ": Expected identifier, then '.' or '('. Found: " + token);
    }

    //check token is ';' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(";")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ';'. Found: " + token);
    }

    writer.writeNonTerminal("doStatement",false);
  }

  /**Compiles an if statement.
   *@post Writes an if statement to file.
   */
  private void compileIf(){
    writer.writeNonTerminal("ifStatement",true);
    String token;
    //TODO compileIf
    //check token is 'if' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("if")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected 'if'. Found: " + token);
    }

    //check token is '(' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("(")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '('. Found: " + token);
    }

    //call compileExpression()
    compileExpression();

    //check token is ')' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(")")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
    }

    //check token is '{' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("{")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '{'. Found: " + token);
    }

    //if next token is NOT '}'
    if(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals("}")){
      //call compileStatements()
      compileStatements();
    }

    //check token is '}' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("}")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '}'. Found: " + token);
    }

    //if next token is 'else'
    if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("else")){
      //check token is '{' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals("{")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected '{'. Found: " + token);
      }

      //if next token is NOT '}'
      if(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals("}")){
        //call compileStatements()
        compileStatements();
      }

      //check token is '}' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals("}")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected '}'. Found: " + token);
      }
    }

    writer.writeNonTerminal("ifStatement",false);
  }

  /**Compiles a let statement.
   *@post Writes a let statement to file.
   */
  private void compileLet(){
    writer.writeNonTerminal("letStatement",true);
    String token;
    //TODO compileLet
    //check token is 'let' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("let")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected 'let'. Found: " + token);
    }

    //check token is IDENTIFIER & write
    token = tokenizer.nextToken();
    if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
    }

    //if next token is '['
    if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("[")){
      //write '['
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //call compileExpression
      compileExpression();

      //check token is ']' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals("]")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ']'. Found: " + token);
      }
    }

    //check token is '=' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("=")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '='. Found: " + token);
    }

    //call compileExpression
    compileExpression();

    //check token is ';' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(";")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ';'. Found: " + token);
    }

    writer.writeNonTerminal("letStatement",false);
  }

  /**Compiles a while statement.
   *@post Writes a while statement to file.
   */
  private void compileWhile(){
    writer.writeNonTerminal("whileStatement",true);
    String token;

    //check token is 'while' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("while")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected 'while'. Found: " + token);
    }

    //check token is '(' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("(")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '('. Found: " + token);
    }

    //call compileExpression()
    compileExpression();

    //check token is ')' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(")")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
    }

    //check token is '{' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("{")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '{'. Found: " + token);
    }

    //if next token is NOT '}'
    if(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals("}")){
      //call compileStatments()
      compileStatements();
    }

    //check token is '}' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("}")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected '}'. Found: " + token);
    }

    writer.writeNonTerminal("whileStatement",false);
  }

  /**Compiles a return statement.
   *@post Writes a return statement to file.
   */
  private void compileReturn(){
    writer.writeNonTerminal("returnStatement",true);
    String token;

    //check token is 'return' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals("return")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected 'return'. Found: " + token);
    }

    //if next token is not ';'
    if(tokenizer.peekToken()!=null && !tokenizer.peekToken().equals(";")){
      //compile expression
      compileExpression();
    }

    //check token is ';' & write
    token = tokenizer.nextToken();
    if(token!=null && token.equals(";")){
      writer.writeTerminal(tokenizer.tokenType(),token);
    } else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected ';'. Found: " + token);
    }

    writer.writeNonTerminal("returnStatement",false);
  }

  /**Compiles an expression.
   *@post Writes an expression to file.
   */
  private void compileExpression(){
    writer.writeNonTerminal("expression",true);
    String token;

    //call compileTerm()
    compileTerm();

    //while next token is an operator
    while(symbolIsOperator(tokenizer.peekToken())){
      //write operator
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //call compileTerm()
      compileTerm();
    }
    writer.writeNonTerminal("expression",false);
  }

  /**Compiles a list of expressions.
   *@pre  Surrounding right token is expected to be ')'
   *      expressionList will end either way
   *@post Writes an expression list to file.
   */
  private void compileExpressionList(){
    writer.writeNonTerminal("expressionList",true);
    String token;

    //if next token is ')'
    if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals(")")){
      //writeNonTerminal and return
      writer.writeNonTerminal("expressionList",false);
      return;
    }

    //call compileExpression()
    compileExpression();

    //while next token is ','
    while(tokenizer.peekToken().equals(",")){
      //write ','
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //call compileExpression()
      compileExpression();
    }
    writer.writeNonTerminal("expressionList",false);
  }

  /**Compiles a term.
   *@post Writes a term to file.
   */
  private void compileTerm(){
    writer.writeNonTerminal("term",true);
    String token;

    //if next token is '-' or '~'
    if(tokenizer.peekToken()!=null && (tokenizer.peekToken().equals("~") || tokenizer.peekToken().equals("-"))){
      //write token
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);
    }

    //get token
    token = tokenizer.nextToken();
    //if tokenType is INT_CONST
    if(token!=null && tokenizer.tokenType()==TokenType.INT_CONST){
      writer.writeTerminal(tokenizer.tokenType(),token);
    }
    //else if tokenType is STRING_CONST
    else if(token!=null && tokenizer.tokenType()==TokenType.STRING_CONST){
      writer.writeTerminal(tokenizer.tokenType(),tokenizer.stringVal());
    }
    //else if token isKeywordConstant()
    else if(token!=null && tokenIsKeywordConstant(token)){
      writer.writeTerminal(tokenizer.tokenType(),token);
    }
    //else if tokenType is IDENTIFIER && next token is '['
    else if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER && tokenizer.peekToken().equals("[")){
      //write token
      writer.writeTerminal(tokenizer.tokenType(),token);

      //write '['
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //error if next token is ']'
      if(tokenizer.peekToken()!=null && tokenizer.peekToken().equals("]")){
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected expression. Found: " + token);
      }

      //call compileExpression()
      compileExpression();

      //check next token is ']'
      token = tokenizer.nextToken();
      if(token!=null && token.equals("]")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ']'. Found: " + token);
      }
    }
    //else if tokenType is IDENTIFIER && next token is '('
    else if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER && tokenizer.peekToken().equals("(")){
      //write token
      writer.writeTerminal(tokenizer.tokenType(),token);

      //write '('
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //call compileExpression()
      compileExpressionList();

      //check next token is ')' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals(")")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
      }
    }
    //else if tokenType is IDENTIFIER && next token is '.'
    else if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER && tokenizer.peekToken().equals(".")){
      //write token
      writer.writeTerminal(tokenizer.tokenType(),token);

      //write '.'
      token = tokenizer.nextToken();
      writer.writeTerminal(tokenizer.tokenType(),token);

      //check token is IDENTIFIER & write
      token = tokenizer.nextToken();
      if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected identifier. Found: " + token);
      }

      //check token is '(' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals("(")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected '('. Found: " + token);
      }

      //call compileExpressionList()
      compileExpressionList();

      //check token is ')' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals(")")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
      }
    }
    //else if tokenType is IDENTIFIER
    else if(token!=null && tokenizer.tokenType()==TokenType.IDENTIFIER){
      writer.writeTerminal(tokenizer.tokenType(),token);
    }
    //else if token is '('
    else if(token!=null && token.equals("(")){
      //write '('
      writer.writeTerminal(tokenizer.tokenType(),token);

      //call compileExpression()
      compileExpression();

      //check next token is ')' & write
      token = tokenizer.nextToken();
      if(token!=null && token.equals(")")){
        writer.writeTerminal(tokenizer.tokenType(),token);
      } else {
        System.out.println("Line " + tokenizer.lineNumber() + ": Expected ')'. Found: " + token);
      }
    }
    else {
      System.out.println("Line " + tokenizer.lineNumber() + ": Expected term. Found: " + token);
    }
    //
    writer.writeNonTerminal("term",false);
  }
  private boolean tokenIsKeywordConstant(String token){
    for(int i = 0; i<KEYWORD_CONSTS.length; i++){
      if(KEYWORD_CONSTS[i].equals(token)){
        return true;
      }
    }
    return false;
  }
  private boolean symbolIsUnaryOp(String symbol){
    return UNARY_OPS.contains(symbol+"");
  }
  private boolean symbolIsOperator(String symbol){
    return OPERATORS.contains(symbol+"");
  }
}