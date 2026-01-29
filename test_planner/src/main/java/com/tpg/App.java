package com.tpg;

import javax.swing.SwingUtilities;
import com.tpg.View.UserInterface;

public class App {    
    public static void main(String[] args) {        
        SwingUtilities.invokeLater(UserInterface::new);
    }
   
}

