/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.control;

import com.hs.util.ConexionDirecta;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.json.simple.JSONObject;


/**
 *
 * @author Andres Lanzoni
 */
public final class ControlUsuario {
    
    public ControlUsuario(){
        super();    
    }
    
    public static JSONObject existeTarjeta(String tarjeta) throws Exception{
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try{
            //ConexionDirecta cd = new ConexionDirecta();
            ConexionDirecta con = ConexionDirecta.getConexion();
            
            if(con != null){
                String sql = "select 1 as existe, nom_datos9 from DATOS9(nolock) where estado_datos9 = 0 and codtar_datos9 = '" + tarjeta + "'";
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst())
                    while (rs.next()) {
                        int existe = rs.getInt("existe");
                        if(existe == 1){
                            resul.put("existe", 1);
                            resul.put("usuario", rs.getString("nom_datos9"));
                            resul.put("salida", "OK");
                            resul.put("msj", "Tarjeta Localizada");
                        }
                        else{
                            resul.put("existe", 0);
                            resul.put("usuario", "");
                            resul.put("salida", 9);
                            resul.put("msj", "Tarjeta no Encontrada");
                        }
                    }
                else{//no hay resultados
                    resul.put("existe", 0);
                    resul.put("usuario", "");
                    resul.put("salida", 9);
                    resul.put("msj", "Tarjeta no Encontrada");
                }
            }
        }
        catch(Exception e){
            resul.put("existe", 0);
            resul.put("usuario", "");
            resul.put("salida", 9);
            resul.put("msj", e.getMessage());
            throw e;
        }
        finally{
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs = null;
        }
        return resul;
    }
    
    public static int getPuntos (String tarjeta) throws Exception{
        Statement stmt = null;
        ResultSet rs = null;
        int puntos = 0;
        try{
            //ConexionDirecta cd = new ConexionDirecta();
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                String sql = "SELECT sum(puntos_hiscre) as puntos FROM HISCRE (nolock) where codtar_hiscre = '" + tarjeta +"'"; 
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    puntos = rs.getInt("puntos");
                }
                stmt.close();
            }
        }
        catch(Exception e){
            throw e;
        }
        finally{
            if (stmt != null){
                stmt.close();
                stmt = null;
            }
            if (rs != null){
                rs.close();
                rs = null;
            }
        }
        return puntos;
    }
    
    
    
    
    public static JSONObject updateUsuario(Map<String, String> parametros) throws Exception{
        JSONObject resultado = new JSONObject();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String tarjeta = parametros.get("tarjeta").toString();
            String user = parametros.get("usuario").toString();
            String pass = parametros.get("pass").toString();
            System.out.println(tarjeta + user + pass);

            if(ControlUsuario.existeUsuario(user)){
                System.out.println("Existe");
                resultado.put("salida", 9);
                resultado.put("msj", "Ya existe el usuario ingresado");
                return resultado;
            }

            //ConexionDirecta cd = new ConexionDirecta();
            ConexionDirecta c = ConexionDirecta.getConexion();
            stmt = c.getConnection().createStatement();
            
            String sql = "update datos9 set usuario_datos9 = '" + user +"', ";
            sql += "password_datos9 = '" + pass +"' ";
            sql += "where codtar_datos9 = '" + tarjeta +"'";
            
            System.out.println("SQL: "+sql);
            
            stmt.executeUpdate(sql);
            
            resultado.put("salida", 1);
            resultado.put("actualizaciones", stmt.getUpdateCount());
            resultado.put("msj", "Usuario Actualizado Exitosamente");
        }
        catch(SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());

            resultado.put("salida", 9);
            resultado.put("msj", "Error al actualizar usuario "+ ex.getMessage());
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
        return resultado;
    }
    
    public static boolean existeUsuario(String usuario) throws Exception {
        boolean res = true;
        ResultSet rs = null;
        Statement stmt = null;
        try{
            ConexionDirecta c = ConexionDirecta.getConexion();
            stmt = c.getConnection().createStatement();
            String sql = "select 1 as existe from datos9(nolock) where usuario_datos9 = '" + usuario + "' and estado_datos9 = 0";
            rs = stmt.executeQuery(sql);
            if(rs.isBeforeFirst())
                while (rs.next()) {
                    if(rs.getInt("existe") == 1)
                        return res;
                    break;
                }
            else
                res = false;
        }
        catch(Exception e){
            throw e;
        }
        finally{
            if(stmt != null){
                stmt.close();
                stmt = null;
            }
            if(rs != null){
                rs.close();
                rs = null;
            }
            return res;
        }
    }
    
    public static JSONObject validaUsuario(String nombre, String pass, boolean primera){
        JSONObject resul = new JSONObject();
        try{
            //ConexionDirecta cd = new ConexionDirecta();
            ConexionDirecta c = ConexionDirecta.getConexion();
            if(c != null){
                String SQL = "SELECT 1 as valido, codtar_datos9, nom_datos9, descuento_datos9 FROM datos9 (nolock) where ";
                
                if (primera)  //Primer loggin valida contra la tarjeta de puntos.
                    SQL += "codtar_datos9 = '" + nombre + "'";
                else
                    SQL += "usuario_datos9 = '" + nombre + "'";
                
                SQL += " and password_datos9 = '" + pass + "'";
                SQL += " COLLATE Latin1_General_CS_AS";
                
                Statement stmt = c.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(SQL);

                if(rs.isBeforeFirst())
                    while (rs.next()) {
                        String localizado = rs.getString("valido");

                        if(localizado.compareTo("1") == 0){
                            resul.put("salida", 1);
                            resul.put("tarjeta", rs.getString("codtar_datos9"));
                            resul.put("nombre", rs.getString("nom_datos9"));
                            resul.put("msj", "OK");
                        }else{
                            resul.put("salida", 9);
                            resul.put("tarjeta", 0);
                            resul.put("nombre", "");
                            resul.put("puntos", 0);
                            resul.put("msj", "Usuario No localizado");
                        }

                        break;
                    }
                else{
                    resul.put("salida", 9);
                    resul.put("tarjeta", 0);
                    resul.put("nombre", "");
                    resul.put("puntos", 0);
                    resul.put("msj", "Usuario No localizado");
                }
                stmt.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            return resul;
        }
    }
    
    public static void main (String args[]){
        ControlUsuario cu = new ControlUsuario();
        String tarjeta = "12345678";
        System.out.println("Tarjeta: "+tarjeta);
        try{
            System.out.println("Puntos: " + cu.getPuntos(tarjeta));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

