/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 *
 * @author Andres Lanzoni
 */
public class Configuracion {
    private static String ip = "";
    private static String imagenes = "";
    private static String baseBackup="";
    private static String reportes = "";
    private static String log = "";
    private static String urlCotiza = "";    
    private static String driver = "";
    private static String server = "";
    private static String puerto = "";
    private static String instancia = "";
    private static String db = "";
    private static String user = "";
    private static String pass = "";
    private static String salida_mail = "";
    private static String error_mail = "";
    
    
    private Logger logger;

    
    
    public Configuracion() {
        try{
            //\restfull-web-services-app-master\src\main\resources\farmacia.properties
            String resourceName = "farmacia.properties"; 
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties propiedades = new Properties();
            InputStream resourceStream = loader.getResourceAsStream(resourceName);
            propiedades.load(resourceStream);
            
            this.logger = Logger.getLogger(Configuracion.class.getName());
                        
            ip = propiedades.get("ip").toString();        
            imagenes = propiedades.get("imagenes").toString();
            baseBackup = propiedades.get("baseBackup").toString();
            reportes = propiedades.get("reportes").toString();
            log = propiedades.get("log").toString();
            urlCotiza = propiedades.get("urlCotiza").toString();            
            driver = propiedades.get("driver").toString();
            server = propiedades.get("servidor").toString();
            puerto = propiedades.get("puerto").toString();
            instancia = propiedades.get("instancia").toString();
            db = propiedades.get("base").toString();
            user = propiedades.get("usuario").toString();
            pass = propiedades.get("pass").toString();
            salida_mail = propiedades.get("salida_mail").toString();
            error_mail = propiedades.get("error_mail").toString();
            
            try {
                FileHandler fileHandler = new FileHandler(Configuracion.getLog(), true);
                fileHandler.setFormatter(new SimpleFormatter());
                this.logger.addHandler(fileHandler);
            }
            catch (Exception e) {
                e.printStackTrace();
                this.logger.log(Level.SEVERE, "Error creando manejador de log", e);
            }
            this.logger.log(Level.INFO, "Preferencias Cargadas");
        }
        catch(Exception ioe){
            ioe.printStackTrace();
            this.logger.log(Level.SEVERE, "Error cargando preferencias", ioe);
        }
    }
    
    public static void main(String a[]){
        Configuracion c = new Configuracion();
    }
    
    
    /**
     * @return String
     */
    public static String getIp() {
            return ip;
    }
    /**
     * @return String
     */
    public static String getImagenes() {
            return imagenes;
    }

    public static String getBaseBackup() {
            return baseBackup;
    }

    public static String getReportes() {
            return reportes;
    }

    public static String getLog() {
        return log;
    }

    public static String getUrlCotiza() {
        return urlCotiza;
    }

    public static String getServer() {
        return server;
    }

    public static String getPuerto() {
        return puerto;
    }

    public static String getDb() {
        return db;
    }

    public static String getUser() {
        return user;
    }

    public static String getPass() {
        return pass;
    }

    public static String getDriver() {
        return driver;
    }

    public Logger getLogger() {
        return logger;
    }

    public static String getInstancia() {
        return instancia;
    }

    public static String getSalida_mail() {
        return salida_mail;
    }

    public static String getError_mail() {
        return error_mail;
    }    
}
