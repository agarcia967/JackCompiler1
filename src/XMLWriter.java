/**@(#)CompilationEngine.java
 * @author Anthony R. Garcia
 * @version 1.00 2015/4/21
 */
package edu.miracosta.cs220;

//import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class XMLWriter {
  private static final boolean DEBUG = false;
  private PrintWriter out;
  private int indent;

  /**Initializes an XMLWriter.
   *@pre  outFileName may not be null
   *@post out file is ready for writing by object
   */
  public XMLWriter(String outFileName) {
    //if(DEBUG) System.out.println(outFileName);
    try{
      //if(DEBUG) System.out.print("Creating output file... ");
      this.out = new PrintWriter(new FileOutputStream(outFileName));
      //if(DEBUG) System.out.println("Success!");
    } catch(FileNotFoundException e){
      System.out.println("File \"" + outFileName + "\" not write accessible.");
      System.exit(0);
    }
    this.indent = 0;
  }

  /**Writes a terminal XML tag.
   * This is a tag that has an end point after the token is written.
   *@arg0 TokenType that will determine the token tag name.
   *@arg1 Literal String that is inside of the XML tag.
   *@post out file is ready for writing by object
   */
  public void writeTerminal(TokenType type, String token){
    writeIndent();
    println("<" + type.toString() + ">" +
        token + "</" + type.toString() + ">");
  }

  /**Writes a non-terminal XML tag.
   * This is a tag that has an undefined end point.
   * If called true, then false, proper tag closures will
   * ensure proper indentations and XML closing.
   *@arg0 Literal String that is the tag name.
   *@arg1 Determines whether to start a new tag indented.
   *@post out file is ready for writing by object
   */
  public void writeNonTerminal(String tagName, boolean isOpeningTag){
    if(!isOpeningTag) { //if its a closing tag
      indent--; //decrease the indentation
    }
    writeIndent();
    print("<" + (isOpeningTag ? "" : "/")); //writes the tag and its '/'
    println(tagName + ">");                 //if the argument calls for it
    if(isOpeningTag){ //if its an opening tag
      indent++; //increase the indentation
    }
  }

  /**Writes the appropriate number of indentations.
   *@pre  indent must be non-negative
   *@post Writes indent spaces to file.
   */
  private void writeIndent(){
    for(int i = 0; i<indent; i++){
      print("  ");
    }
  }

  /**Prints to file or to console depending on DEBUG.
   *@arg0 String to write to outstream.
   *@post Writes string to outstream.
   */
  private void print(String string){
    if(DEBUG){
      System.out.print(string);
    } else {
      this.out.print(string);
    }
  }

  /**Prints string with newline to file or to console depending on DEBUG.
   *@arg0 String to write to outstream.
   *@post Writes string to outstream.
   */
  private void println(String string){
    print(string + "\r\n");
  }

  /**Writes to file and closes the file.
   *@post Output file is written, closed, and may not be written to anymore.
   */
  public void close(){
    this.out.flush();
    this.out.close();
  }
}