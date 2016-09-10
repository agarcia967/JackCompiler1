/**@(#)JackAnalyzer.java
 * @author Anthony R. Garcia
 * @version 1.00 2015/4/22
 */
package edu.miracosta.cs220;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class JackAnalyzer {
  private static ArrayList<String> inputFileNames;

  public static void main(String[] args){
    Scanner keyboard = new Scanner(System.in);
    String input = "";
    boolean valid = false;
    CompilationEngine parser;
    inputFileNames = new ArrayList<String>();

    System.out.println("Filenames must end with '.jack' and directories must end with '/'.");
    while(!valid){
      System.out.print("Enter a filename or directory: ");
      input = keyboard.nextLine();
      setFileName(input);
      valid = true;
      if(inputFileNames.size()<=0){
        System.out.println("There are no '.jack' files in this location.");
        valid = false;
      }
    }

    for(String inFile : inputFileNames){
      System.out.println("\nCompiling '" + inFile + "'... ");
      parser = new CompilationEngine(inFile);
    }
  }

  public static void setFileName(String filename){
    filename = filename.trim().replace("\\","/");
    if(filename.endsWith(".jack")){
      inputFileNames.add(filename);
    }
    else if(filename.endsWith("/")){
      File dir = new File(filename);
      for(File file : dir.listFiles()) {
        if(file.getName().endsWith((".jack"))) {
          System.out.println("JACK file found: \'" + file.getName() + "\'");
          inputFileNames.add(dir.toString()+"/"+file.getName());
        }
      }
    }
    else{
      System.out.println("\nFilenames must end with '.jack' and directories must end with '/'.");
    }
  }
}