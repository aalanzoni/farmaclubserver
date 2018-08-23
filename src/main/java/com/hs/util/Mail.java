/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andres Lanzoni
 */
public class Mail extends Thread {
    private String remitente;
    private String password;
    private Stack<String> destinatarios;
    private String servidorSMTP;
    private String puertoSalida;
    private String autenticacion;
    private String tls;
    private String asunto;
    private String mensaje;
    private String adjunto;
    private int error;
    private Logger logger;
    
    private void cargoParametros(){
        try{
            //\restfull-web-services-app-master\src\main\resources\mail.properties
            String resourceName = "mail.properties"; 
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties propiedades = new Properties();
            InputStream resourceStream = loader.getResourceAsStream(resourceName);
            propiedades.load(resourceStream);
            
            this.logger = Logger.getLogger(Mail.class.getName());
                        
            remitente = propiedades.get("remitente").toString();
            password = propiedades.get("password").toString();
            servidorSMTP = propiedades.get("servidorSMTP").toString();
            puertoSalida = propiedades.get("puertoSalida").toString();
            autenticacion = propiedades.get("autenticacion").toString();
            tls = propiedades.get("tls").toString();
            
            String casillas = propiedades.get("error_mail").toString();
            List<String> cuentas = Arrays.asList(casillas.split("\\s*;\\s*"));
            for (Iterator<String> iterator = cuentas.iterator(); iterator.hasNext();) {
                String next = iterator.next();
                destinatarios.add(next);
            }
        }
        catch(Exception ioe){
            ioe.printStackTrace();
            this.logger.log(Level.SEVERE, "Error cargando preferencias Mail", ioe);
        }
    }
    
    public void enviarMail(String asunto, String destinatario, String mensaje, boolean error){
        
    }

    @Override
    public void run() {
        
    }
}
