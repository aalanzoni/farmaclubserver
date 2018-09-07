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
    
    public static void sendErrorMail(String msj){
        Configuracion conf = Configuracion.getConfig();
        if(conf.getEnvia_error_mail().compareTo("1") == 0){
            Thread hilo = new Thread(new Mail(null, null, msj, null, null, null, Constantes.MAIL_ERROR));
            hilo.start();
        }
    }
    
    public static void main(String a[]){
//        System.out.println("Salida: "+Utilidades.validarMail("aalanzoni@gmail."));
        Utilidades.sendErrorMail("Ésto es una prueba de lo que puede ser");
    }


    
}
