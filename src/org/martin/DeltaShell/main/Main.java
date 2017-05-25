/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.DeltaShell.main;

import java.net.UnknownHostException;
import org.martin.DeltaShell.cmd.DeltaShell;

/**
 *
 * @author martin
 */
public class Main {
    public static void main(String[] args) throws UnknownHostException {
        DeltaShell shell = new DeltaShell();
        shell.execConsole();
    }
}
