/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 *
 * @author Andres Lanzoni
 */
public class ConexionDirecta {
    private Connection conexion;
    private Configuracion conf;
    private static ConexionDirecta condir;
    
    private ConexionDirecta(){
        try {
            conf = Configuracion.getConfig();
            
            String connectionUrl = conf.getDriver() + "//";
            connectionUrl += conf.getServer();
            if(conf.getInstancia().length() > 1){
                connectionUrl += "\\" + conf.getInstancia() + ";";
            }
            else{
                connectionUrl +=  ":" + conf.getPuerto() + ";";
            }
            connectionUrl += "database=" + conf.getDb()+ ";";
            connectionUrl += "username=" + conf.getUser() + ";";
            connectionUrl += "password=" + conf.getPass();

            conf.getLogger().log(Level.INFO, "URL: "+connectionUrl);
            conexion = DriverManager.getConnection(connectionUrl);
            conf.getLogger().log(Level.INFO, "CONEXION ESTABLECIDA");
        } 
        catch (SQLException ex){
            conf.getLogger().log(Level.SEVERE,"ERROR EN CONEXIONA A DB",ex);
        }
    }
    
    
    
    public static ConexionDirecta getConexion() {
 
        if (condir == null) { 
            condir = new ConexionDirecta();
            System.out.println("nueva");
        }else{
            System.out.println("activa");
        }
        return condir;
    }
    
    public Connection getConnection(){
        return this.conexion;
    }
            
     
    public static void main (String a[]){
        try{
            
            ConexionDirecta c = ConexionDirecta.getConexion();
            String SQL = "SELECT codtar_datos9, nom_datos9, descuento_datos9 FROM datos9 (nolock)";
            Statement stmt = c.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            while (rs.next()) {
                System.out.println(rs.getString("codtar_datos9") + " " + rs.getString("nom_datos9") + " "+ rs.getBigDecimal("descuento_datos9"));
            }
            stmt.close();
            c = ConexionDirecta.getConexion();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

