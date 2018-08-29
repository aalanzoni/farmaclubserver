/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andres Lanzoni
 */
public class Utilidades {
    
    public static boolean validarMail(String mail) {
        boolean resultado = false;
        if (mail.compareTo("") != 0) {
            String emailPattern = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@" +
      "[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
            Pattern pattern = Pattern.compile(emailPattern);
            Matcher m = pattern.matcher(mail);
            if(m.matches()) {
                resultado = true;
            }
            else {
                resultado = false;
            }
        }
        return resultado;
    }
    
    public static void main(String a[]){
        System.out.println("Salida: "+Utilidades.validarMail("aalanzoni@gmail."));
    }


    
}
