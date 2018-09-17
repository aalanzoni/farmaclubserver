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
    
    /**
     * Funcion que pasada la fecha en formato DB (aaaammdd) la retorna en formato ddmmaaaa
     * @param fechaBD
     * @return
     */
    public static int diaMesAnio(int fechaBD) {
        int resultado = 0;
        if (fechaBD > 0) {
            String fechaS = Integer.toString(fechaBD);
            String anioS = fechaS.substring(0, 4);
            String mesS = fechaS.substring(4, 6);
            String diaS = fechaS.substring(6, 8);
            int anio = Integer.parseInt(anioS);
            int mes = Integer.parseInt(mesS);
            int dia = Integer.parseInt(diaS);
            resultado = dia * 1000000;
            resultado = resultado + (mes * 10000);
            resultado += anio;
        }
        return resultado;
    }
    
    /**
     * Procedimiento que transforma un String dia/mes/anio en un 
     * entero enformato anio mes dia
     * @param fecha
     * @return 
     */
    public static int obtenerFecha(String fecha){
        int resultado = 0;
        if(fecha.isEmpty() || fecha.trim().length() == 0)
            return resultado;
        
        String [] dma = fecha.split("/");
        
        int anio = Integer.parseInt(dma[2]);
        int mes = Integer.parseInt(dma[1]);
        int dia = Integer.parseInt(dma[0]);
        
        resultado = anio * 10000;
        resultado = resultado + (mes * 100);
        resultado += dia;
        
        return resultado;
    }

    
    public static void main(String a[]){
//        System.out.println("Salida: "+Utilidades.validarMail("aalanzoni@gmail."));
//        Utilidades.sendErrorMail("Ésto es una prueba de lo que puede ser");
        System.out.println("Fecha: " +Utilidades.obtenerFecha("1/9/2018"));
    }


    
}

