/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.DeltaShell.cmd;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martin
 */
public class InfoShell {
    private static final String USER_KEY = "user.name";
    private static String HOST_NAME;

    private static final char USER_TERMINATOR = '$';
    private static final char ROOT_TERMINATOR = '#';

    private static File currentDir;
    
    static {
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(InfoShell.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentDir = new File(System.getProperty("user.dir"));
    }
    
    public static String getUserName(){
        return System.getProperty(USER_KEY);
    }
    
    public static String getUserHome(){
        return System.getProperty("user.home");
    }
    
    public static String getHostName(){
        return HOST_NAME;
    }

    public static LinkedList<String> getListAvailableCommands(){
        File cmdFld = new File("/usr/bin");
        final String[] fileNames = cmdFld.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals("X11");
            }
        });
        LinkedList<String> listOrders = new LinkedList<>();
        for (int i = 0; i < fileNames.length; i++) 
            listOrders.add(fileNames[i]);
        
        Collections.sort(listOrders);
        return listOrders;
    }
    
    private static String getCurrentPath() {
        try {
            String strCurDir = currentDir.getCanonicalPath();
            if (strCurDir.equals(getUserHome()))
                return "~";
            else{
                if (getUserName().equals("root"))
                    return strCurDir;
                else if (strCurDir.startsWith(getUserHome())) {
                    String[] curDirSplit = strCurDir.split("/");
                    StringBuilder sbPath = new StringBuilder();
                    sbPath.append('~');
                    for (int i = 3; i < curDirSplit.length; i++)
                        sbPath.append('/').append(curDirSplit[i]);
                    return sbPath.toString();
                }
                else
                    return strCurDir;
            }
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static String getCurrentDir(){
        return System.getProperty("user.dir");
    }
    
    public static boolean setCurrentDir(String dirPath){
        try {
            File tempDir;
            if (dirPath.startsWith("/"))
                tempDir = new File(dirPath);
            else if (dirPath.contains("."))
                tempDir = new File(dirPath);
            else
                tempDir = new File(currentDir.getCanonicalPath()+dirPath);
            
            if (tempDir.exists()){
                currentDir = tempDir;
                System.setProperty("user.dir", currentDir.getCanonicalPath());
            }
            return tempDir.exists();
        } catch (IOException ex) {
            return false;
        }
    }
    
    public static String getInfoShell(){
        String userName = getUserName();
        boolean isRoot = userName.equals("root");
        
        
        return new StringBuilder()
                .append(getUserName()).append('@').append(getHostName()).append(':')
                .append(getCurrentPath()).append(isRoot ? ROOT_TERMINATOR : USER_TERMINATOR)
                .append(' ').toString();
    }
    
}
