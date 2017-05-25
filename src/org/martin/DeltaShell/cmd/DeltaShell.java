/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.DeltaShell.cmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martin
 */
public class DeltaShell {
    private Scanner scanner;
    private InputStream inputStream;

    private static final LinkedList<String> listOrders = InfoShell.getListAvailableCommands();
    private static final Runtime runtime = Runtime.getRuntime();
    private static final PrintStream stdout = System.out;
    private static final PrintStream stderr = System.out;
    
    private static final byte EOF = -1;
    private static final String START = "> ";
    private static final String EXIT_CMD = "quit";
    
    public DeltaShell() {
        inputStream = System.in;
        scanner = new Scanner(inputStream);
    }
    
    private void printStreamOut(InputStream cmdStream){
        int read;
        
        try {
            while ((read = cmdStream.read()) != EOF)
                stdout.print((char)read);
        } catch (IOException ex) {
            stderr.println("Error al leer stream");
        }
    }
    private void printStreamOut(final Process newProc) {
        try {
            InputStream is = newProc.getInputStream();
            if (is.available() > 0)
                printStreamOut(is);
            else
                printStreamOut(newProc.getErrorStream());
        } catch (IOException ex) {
            Logger.getLogger(DeltaShell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void enterData(OutputStream cmdOut){
        String data = scanner.nextLine();
        try {
            cmdOut.write(data.getBytes());
        } catch (IOException ex) {
            stderr.println("Error al escribir datos");
        }
    }
    
    private int getAsciiWordCode(String word){
        char[] letters = word.toCharArray();
        int letLen = letters.length;
        int sumAscii = 0;
        
        for (int i = 0; i < letLen; i++)
            sumAscii+=(int)letters[i];
        return sumAscii;
    }

    private int getSimilLetters(String wordDic, String filter){
        /*int asciiCode1 = getAsciiWordCode(word1);
        int asciiCode2 = getAsciiWordCode(word2);
        if(asciiCode1 == asciiCode2)
            return asciiCode1;
        return asciiCode1 > asciiCode2 ? (asciiCode1-asciiCode2) : (asciiCode2-asciiCode1);*/
        
        int similCounter = 0;
        char[] charsWord1 = wordDic.toCharArray();
        char[] charsWord2 = filter.toCharArray();
        if (charsWord1.length == charsWord2.length)
            similCounter++;
        
        int minorLen = charsWord1.length > charsWord2.length ? 
                charsWord2.length : charsWord1.length;
                
        for (int i = 0; i < minorLen; i++) {
            if (charsWord1[i] == charsWord2[i])
                similCounter++;
        }
        if (similCounter > 0) {
            if (filter.length() > wordDic.length())
                similCounter++;
            else if(wordDic.length() - filter.length() == 1){
                similCounter++;
            }
        }
        
        // Agregar pruebas de secuencias en letras
        return similCounter;
    }
    
    public LinkedList<String> getSimilarOrdersFor(String cmd){
        String moreSimilar = null;
        int maxSimilCode = -1;
        int similCode = 0;
        LinkedList<String> listSimilars = new LinkedList<>();
        
        for (String order : listOrders) {
            // Tengo la diferencia de codigos ascii entre el filtro y la palabra actual
            similCode = getSimilLetters(order, cmd);
            
            // Comparo la diferencia de codigos actual con la minima
            // y si es menor que la minima la palabra mas similar sera word
            if (similCode > maxSimilCode) {
                maxSimilCode = similCode;
                moreSimilar = order;
                listSimilars.add(moreSimilar);
                if (listSimilars.size() > 3)
                    listSimilars.removeFirst();
            }
        }
        return listSimilars;
    }
    
    private void execCommand(String cmd){
        try {
            Process newProc = runtime.exec(cmd);
            newProc.waitFor();
            printStreamOut(newProc);
            
        } catch (IOException ex) {
            stdout.printf("La orden %s no se encuentra disponible, quizas quiso decir: \n", cmd);
            for (Iterator<String> listIt = getSimilarOrdersFor(cmd)
                    .descendingIterator(); listIt.hasNext();)
                stdout.println(listIt.next());
            
        } catch (IllegalArgumentException ex2){
        } catch (InterruptedException ex) {
            Logger.getLogger(DeltaShell.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
    
    private void execCdCmd(String cmd){
        String[] cmdSplit = cmd.split(" ");
        switch (cmdSplit.length) {
            case 2:
                if (cmdSplit[1].equals(" "))
                    InfoShell.setCurrentDir(InfoShell.getUserHome());
                else{
                    boolean isChanged = InfoShell.setCurrentDir(cmdSplit[1]);
                    if (!isChanged)
                        stdout.printf("El directorio %s no existe\n", cmdSplit[1]);
                }   break;
            case 1:
                InfoShell.setCurrentDir(InfoShell.getUserHome());
                break;
            default:
                stdout.println("Cantidad de argumentos invalidos");
                break;
        }
    }
    
    public void execConsole(){
        String cmd;
        while (true) {            
            stdout.print(InfoShell.getInfoShell());
            cmd = scanner.nextLine();
            String cmdOrder;
            if (cmd != null){
                cmdOrder = cmd.split(" ")[0];
                if (cmd.equals(EXIT_CMD)) {
                    stdout.println("Gracias por utilizar DeltaShell");
                    System.exit(0);
                }
                else if (cmdOrder.equals("cd"))
                    execCdCmd(cmd);
                else if(cmdOrder.equals("ls") || cmdOrder.equals("dir"))
                    execCommand(cmdOrder+' '+InfoShell.getCurrentDir());
                // Falta touch, mkdir y rm
                else
                    execCommand(cmd);
            }
        }
    }

}
