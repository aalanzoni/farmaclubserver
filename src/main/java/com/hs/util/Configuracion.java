/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.io.File;
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
    private static String root_base = "";
    private static String imagenes = "";
    private static String baseBackup="";
    private static String reportes = "";
    private static String log = "";
    private static String urlCotiza = "";    
    private static String driver = "";
    private static String driver_class = "";
    private static String server = "";
    private static String puerto = "";
    private static String instancia = "";
    private static String db = "";
    private static String user = "";
    private static String pass = "";
    private static String licencia = "";
    private static String salida_mail = "";
    private static String error_mail = "";
    private static String envia_error_mail = "";
    private static String letra_p = "";
    private static String letra_x = "";
    
    private Integer fileSizeLimit = 1024;
    private int fileCount = 5;
    private boolean append = true;
    private int level = 100;
    
    
    private Logger logger;
    
    private static Configuracion config;
    
    public static Configuracion getConfig(){
        if(config == null){
            config = new Configuracion();
        }
        return config;
    }
            
    
    
    public Configuracion() {
        try{
            String resourceName = "farmacia.properties"; 
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties propiedades = new Properties();
            InputStream resourceStream = loader.getResourceAsStream(resourceName);
            propiedades.load(resourceStream);
            
            this.logger = Logger.getLogger(Configuracion.class.getName());
                        
            ip = propiedades.get("ip").toString();
            root_base = propiedades.get("root_base").toString();
            imagenes = propiedades.get("imagenes").toString();
            baseBackup = propiedades.get("baseBackup").toString();
            reportes = propiedades.get("reportes").toString();
            log = propiedades.get("log").toString();
            urlCotiza = propiedades.get("urlCotiza").toString();            
            driver = propiedades.get("driver").toString();
            driver_class = propiedades.get("driver_class").toString();
            server = propiedades.get("servidor").toString();
            puerto = propiedades.get("puerto").toString();
            instancia = propiedades.get("instancia").toString();
            db = propiedades.get("base").toString();
            user = propiedades.get("usuario").toString();
            pass = propiedades.get("pass").toString();
            licencia = propiedades.get("licencia").toString();
            salida_mail = propiedades.get("salida_mail").toString();
            error_mail = propiedades.get("error_mail").toString();
            envia_error_mail = propiedades.get("envia_error_mail").toString();
            letra_p = propiedades.get("P").toString();
            letra_x = propiedades.get("X").toString();
            
            fileSizeLimit = Integer.parseInt(propiedades.get("fileSizeLimit").toString());
            fileCount = Integer.parseInt(propiedades.get("fileCount").toString());
            append = Boolean.parseBoolean(propiedades.get("append").toString());
            level = Integer.parseInt(propiedades.get("level").toString());
            
            //FileHandler fileHandler = new FileHandler(Configuracion.getLog(), fileSizeLimit, fileCount, append);
            FileHandler fileHandler = new FileHandler(Configuracion.getLog(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            this.logger.addHandler(fileHandler);
            switch(level){
                case 1:
                    this.logger.setLevel(Level.ALL);
                    break;
                case 2:
                    this.logger.setLevel(Level.INFO);
                    break;
                case 3:
                    this.logger.setLevel(Level.SEVERE);
                    break;
                default:
                    this.logger.setLevel(Level.ALL);                    
            }
        }
        catch(Exception ioe){
            ioe.printStackTrace();
            this.logger.log(Level.SEVERE, "Error cargando preferencias", ioe);
            Utilidades.sendErrorMail(ioe.getMessage());
        }
        this.logger.log(Level.INFO, "Preferencias Cargadas");
    }
    
    public static void main(String a[]){
        System.out.println("Level.FINEST: " + Level.FINEST);
        try{
            File f = new File("d:\\Debo fotos y videos 20180813\\IMG_20180809_134418.jpg");
            if(f.exists())
                System.out.println("EXISTE HUEVON!!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
//        Configuracion.g(etConfig();
//        Configuracion.getConfig();
//        Configuracion.getConfig();
    }
    
    
    /**
     * @return String
     */
    public String getIp() {
            return ip;
    }

    public String getRoot_base() {
        return root_base;
    }

    public Integer getFileSizeLimit() {
        return fileSizeLimit;
    }

    public int getFileCount() {
        return fileCount;
    }

    public boolean isAppend() {
        return append;
    }

    public int getLevel() {
        return level;
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

    public static String getDriver_class() {
        return driver_class;
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

    public static String getEnvia_error_mail() {
        return envia_error_mail;
    }

    public static String getLicencia() {
        return licencia;
    }

    public static String getLetra_p() {
        return letra_p;
    }

    public static String getLetra_x() {
        return letra_x;
    }
}

