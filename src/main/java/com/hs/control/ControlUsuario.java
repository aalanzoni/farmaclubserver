/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.control;

import com.hs.util.ConexionDirecta;
import com.hs.util.Configuracion;
import com.hs.util.Constantes;
import com.hs.util.Mail;
import com.hs.util.Utilidades;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



/**
 *
 * @author Andres Lanzoni
 */
public final class ControlUsuario {
    static Configuracion conf = Configuracion.getConfig();

    public ControlUsuario(){
        super();
    }

    public static JSONObject existeTarjeta(String tarjeta) throws Exception{
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();

            if(con != null){
                String sql = "select 1 as existe, nom_datos9 from DATOS9(nolock) where estado_datos9 = 0 and codtar_datos9 = '" + tarjeta + "'";
                conf.getLogger().log(Level.INFO,"procedimiento existeTarjeta" + tarjeta + " SQL: " + sql);
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst())
                    while (rs.next()) {
                        int existe = rs.getInt("existe");
                        if(existe == 1){
                            resul.put("existe", 1);
                            resul.put("usuario", rs.getString("nom_datos9"));
                            resul.put("salida", 1);
                            resul.put("msj", "Tarjeta Localizada");
                        }
                        else{
                            resul.put("existe", 0);
                            resul.put("usuario", "");
                            resul.put("salida", 1);
                            resul.put("msj", "Tarjeta no Encontrada");
                        }
                    }
                else{//no hay resultados
                    resul.put("existe", 0);
                    resul.put("usuario", "");
                    resul.put("salida", 1);
                    resul.put("msj", "Tarjeta no Encontrada");
                }
            }
        }
        catch(Exception e){
            resul.put("existe", 0);
            resul.put("usuario", "");
            resul.put("salida", 9);
            resul.put("msj", e.getMessage());
            conf.getLogger().log(Level.SEVERE,"procedimiento existeTarjeta" + tarjeta, e);
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
    
    public static JSONObject updateCategoriasAsignadas(String tarjeta, JSONObject parametros) throws Exception{
        JSONObject resul = new JSONObject();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "";
        int act = 0;
        int del = 0;
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                conf.getLogger().log(Level.INFO,"procedimiento updateCategoriasAsignadas, tarjeta: " + tarjeta );
                ArrayList preferencias = (ArrayList)parametros.get("preferencias");
               
                for (int i = 0; i < preferencias.size(); i++) {
                    
                    LinkedHashMap cte = (LinkedHashMap)preferencias.get(i);
                    
                    int codigo = Integer.parseInt(cte.getOrDefault("codigo", 0).toString());
                    int activa = Integer.parseInt(cte.getOrDefault("activa", 0).toString());
                    if(codigo != 0){
                        if(activa == 1){//se habilito o ya estaba
                            if(!existePreferencia(tarjeta, codigo)){//se habilito
                                sql = "insert into PREDAT values ('"+tarjeta+
                                        "', " + codigo + ", NULL)";
                                System.out.println("SQL Insert: " + sql);
                                stmt = con.getConnection().createStatement();
                                stmt.execute(sql);
                                act ++;
                            }
                        }else{
                            sql = "delete from PREDAT " + 
                                  "where codtar_predat = '" + tarjeta + "' and " +
                                        "codref_predat = " + codigo;
                            System.out.println("SQL Update: " + sql);
                            stmt = con.getConnection().createStatement();
                            stmt.execute(sql);
                            del ++;
                        }                        
                    }
                }
                resul.put("salida", 1);
                resul.put("msj", "Categorias Actulizadas");
                resul.put("Altas", act);
                resul.put("Bajas", del);
            }
        }
        catch(Exception e){
            conf.getLogger().log(Level.SEVERE,"procedimiento getCategoriasAsignadas" + tarjeta, e);
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("msj", "Error actualiza categorias: " + e.getMessage());
            resul.put("Altas", 0);
            resul.put("Bajas", 0);

        }
        finally{
            if(stmt != null){
                stmt.close();
            }
        }
        return resul;
    }
    
    private static boolean existePreferencia(String tarjeta, int preferencia) throws Exception{
        boolean res = false;
        Statement stmt = null;
        ResultSet rs = null;
        ConexionDirecta cd = ConexionDirecta.getConexion();
        if(cd != null){
            String sql = "select 1 as existe "+
                         "from PREDAT with (nolock) "+
                         "where codtar_predat = '" + tarjeta + "' and " +
                         "codref_predat = " + preferencia;
            stmt = cd.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            if(rs.isBeforeFirst())
                res = true;
        }
        if (stmt != null) { 
            stmt.close(); 
        }
        return res;
    }
    
    public static JSONObject getCategoriasAsignadas(String tarjeta) throws Exception{
        JSONObject resul = new JSONObject();
        Statement stmt = null;
        ResultSet rs = null;
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                String sql = "select codigo_prefer, " +
                                    "des_prefer, " +
                                    "codtar_predat " +
                             "from prefer with (nolock) " +
                                "left join PREDAT with (nolock) on " +
                                "codigo_prefer = codref_predat and " +
                                "codtar_predat = " + tarjeta;
                System.out.println("getCategoriasAsignadas: " + sql);
                conf.getLogger().log(Level.INFO,"procedimiento getCategoriasAsignadas, tarjeta: " + tarjeta + " SQL: " + sql);
                
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    JSONArray categorias = new JSONArray();
                    int cant = 0;
                    while (rs.next()) {
                        JSONObject cte = new JSONObject();
                        int codigo = Integer.parseInt(rs.getString("codigo_prefer"));
                        String descripcion = rs.getString("des_prefer");
                        String activa = rs.getString("codtar_predat");
                        cte.put("codigo", codigo);
                        cte.put("descripcion", descripcion);
                        if (activa == null || activa.trim().isEmpty()){
                            cte.put("activa", 0);
                        }
                        else{
                            cte.put("activa", 1);
                        }
                        categorias.add(cte);
                        cant ++;
                    }
                    resul.put("salida", 1);
                    resul.put("msj", "OK");
                    resul.put("cantidad", cant);
                    resul.put("preferencias", categorias);
                }
                
            }
        }
        catch(Exception e){
            //e.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento getCategoriasAsignadas" + tarjeta, e);
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("msj", "Error get categorias: " + e.getMessage());
            resul.put("cantidad", 0);
            resul.put("categorias", null);
        }
        finally{
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
        return resul;
    }

    public static JSONObject resetPass(String tarjeta) throws Exception{
        JSONObject resul = new JSONObject();
        Statement stmt = null;
        ResultSet rs = null;
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                String sql = "select mail_datos9 as correo from datos9 (nolock) where codtar_datos9 = '"+
                        tarjeta +
                        "' and estado_datos9 = 0";
                conf.getLogger().log(Level.INFO,"procedimiento resetPass" + tarjeta + " SQL: " + sql);
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    while (rs.next()) {
                        String correo = rs.getString("correo");
                        if(correo != null && !correo.isEmpty()){
                            correo = correo.trim();
                            if(Utilidades.validarMail(correo)){
                                String pass = "";
                                Random random = new Random();
                                IntStream is = random.ints(4, 0, 9);
                                Iterator iterator = is.iterator();
                                while (iterator.hasNext()){
                                    pass += iterator.next().toString();
                                }
                                sql = "update DATOS9 set password_datos9 = '" +
                                        pass + "', " +
                                        "usuario_datos9 = ''" +
                                        " where codtar_datos9 = '" +
                                        tarjeta +
                                        "' and estado_datos9 = 0";
                                
                                conf.getLogger().log(Level.INFO,"procedimiento resetPass <update> " + tarjeta + " SQL: " + sql);

                                stmt.executeUpdate(sql);

//                              Enviamos el Mail
                                Stack<String> destinatarios = new Stack<String>();
                                destinatarios.add(correo);

                                Thread hilo = new Thread(new Mail(destinatarios, null, null, null, tarjeta, pass, Constantes.MAIL_PASS_CHANG));
                                hilo.start();

                                resul.put("salida", 1);
                                resul.put("pass", pass);
                                resul.put("msj", "Reseteo OK");
                                resul.put("mail", correo);
                                resul.put("reiniciar", 1);
                            }
                            else{//correo invalido
                                resul.put("salida", 4);
                                resul.put("pass", "");
                                resul.put("msj", "Correo invalido: " + correo);
                                resul.put("mail", correo);
                                resul.put("reiniciar", 0);
                            }
                        }
                        else{//No hay correo
                            resul.put("salida", 3);
                            resul.put("pass", "");
                            resul.put("msj", "No hay correo asociado a la Tarjeta");
                            resul.put("mail", "");
                            resul.put("reiniciar", 0);
                        }
                        break;
                    }
                }
                else{//No hay resultados
                    resul.put("salida", 2);
                    resul.put("pass", "");
                    resul.put("msj", "No Existe la tarjeta o esta inhabilitada");
                    resul.put("mail", "");
                    resul.put("reiniciar", 0);
                }
            }
        }
        catch(Exception e){
            //e.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento resetPass " + tarjeta, e);
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("pass", "");
            resul.put("msj", "Error en reseteo de pass: " + e.getMessage());
            resul.put("mail", "");
            resul.put("reiniciar", 0);
        }
        finally{
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
        return resul;
    }

    public static int getPuntos (String tarjeta) throws Exception{
        Statement stmt = null;
        ResultSet rs = null;
        int puntos = 0;
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                String sql = "SELECT sum(puntos_hiscre) as puntos FROM HISCRE (nolock) where codtar_hiscre = '" + tarjeta +"'";                
                sql += " and (estado_hiscre = ' ' or estado_hiscre is null)";
                conf.getLogger().log(Level.INFO,"procedimiento getPuntos - tarjeta: "+ tarjeta +" SQL: "+ sql);
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    puntos = rs.getInt("puntos");
                }
                stmt.close();
            }
        }
        catch(Exception e){
            conf.getLogger().log(Level.SEVERE,"procedimiento getPuntos " + tarjeta, e);
            Utilidades.sendErrorMail(e.getMessage());
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
    
    public static JSONObject updateInfo(Map<String, String> parametros){
        JSONObject resultado = new JSONObject();
        Statement stmt = null;
        try {
            //nombre - tarjeta - correo - telefono - codpos - fecnac - localidad - direccion
            String tarjeta = parametros.get("tarjeta").toString();
            String nombre = parametros.get("nombre").toString();
            String correo = parametros.get("correo").toString();
            String telefono = parametros.get("telefono").toString();
            
            String codpos_s = parametros.get("codpos").trim();            
            int codpos = 0;
            if(!codpos_s.isEmpty() && codpos_s.length() > 0){
                codpos = Integer.parseInt(codpos_s);
            }
            
            String fecnac_s = parametros.get("fecnac").toString().trim();
            int fecnac = Utilidades.obtenerFecha(fecnac_s);
            
            String localidad = parametros.get("localidad").toString();
            String direccion = parametros.get("direccion").toString();
            
            ConexionDirecta c = ConexionDirecta.getConexion();
            stmt = c.getConnection().createStatement();
            
            String sql = "update datos9 set nom_datos9 = '" + nombre + "', "+
                              "mail_datos9 = '" + correo + "', " +
                              "tel1_datos9 = '" + telefono + "', " +
                              "cp_datos9 = " + codpos + ", " +
                              "fec_nac_datos9 = "+ fecnac + ", " +
                              "loc_datos9 = '" + localidad + "', " +
                              "dom_datos9 = '" + direccion + "'" +
                         "where codtar_datos9 = '" + tarjeta + "'";
            
            //System.out.println("sql Update: " + sql);
            conf.getLogger().log(Level.INFO,"procedimiento updateInfo " + parametros +" SQL: "+sql);
            
            stmt.executeUpdate(sql);
            
            resultado.put("salida", 1);
            resultado.put("actualizaciones", stmt.getUpdateCount());
            resultado.put("msj", "Informacion de Usuario Actualizada Exitosamente");
        }
        catch(SQLException ex){
//            System.out.println("sql Exception: " + ex.getMessage());
//            System.out.println("sql State: " + ex.getSQLState());
//            System.out.println("Error Code: " + ex.getErrorCode());
            
            //ex.printStackTrace();
           conf.getLogger().log(Level.SEVERE,"procedimiento updateInfo " + parametros, ex);
            
            Utilidades.sendErrorMail("Error en Actualizacion de informacion de Usuario " + 
                    ex.getMessage()+" " + ex.getSQLState() + " " + ex.getErrorCode());

            resultado.put("salida", 9);
            resultado.put("actualizaciones", 0);
            resultado.put("msj", "Error al Actualizar Informacion de Usuario "+ ex.getMessage());
        }
        catch(Exception ex){
            //ex.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento updateInfo " + parametros, ex);
            
            Utilidades.sendErrorMail("Error en Actualizacion de informacion de Usuario " + ex.getMessage());

            resultado.put("salida", 9);
            resultado.put("actualizaciones", 0);
            resultado.put("msj", "Error al Actualizar Informacion de Usuario "+ ex.getMessage());            
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
        return resultado;
    }

    public static JSONObject updateUsuario(Map<String, String> parametros) throws Exception{
        JSONObject resultado = new JSONObject();
        Statement stmt = null;
        try {
            String tarjeta = parametros.get("tarjeta").toString();
            String user = parametros.get("usuario").toString();
            String pass = parametros.get("pass").toString();

            if(ControlUsuario.existeUsuario(user, null)){
                resultado.put("salida", 9);
                resultado.put("actualizaciones", 0);
                resultado.put("msj", "Ya existe el usuario ingresado");
                return resultado;
            }

            //ConexionDirecta cd = new ConexionDirecta();
            ConexionDirecta c = ConexionDirecta.getConexion();
            stmt = c.getConnection().createStatement();

            String sql = "update datos9 set usuario_datos9 = '" + user +"', ";
            sql += "password_datos9 = '" + pass +"' ";
            sql += "where codtar_datos9 = '" + tarjeta +"'";
            conf.getLogger().log(Level.INFO,"procedimiento updateUsuario " + parametros + " SQL: " + sql);
            stmt.executeUpdate(sql);

            resultado.put("salida", 1);
            resultado.put("actualizaciones", stmt.getUpdateCount());
            resultado.put("msj", "Usuario Actualizado Exitosamente");
        }
        catch(SQLException ex){            
//            System.out.println("SQLException: " + ex.getMessage());
//            System.out.println("SQLState: " + ex.getSQLState());
//            System.out.println("VendorError: " + ex.getErrorCode());
//            ex.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento updateUsuario " + parametros, ex);
            Utilidades.sendErrorMail("Error al actualizar usuario " + 
                    ex.getMessage()+" " + ex.getSQLState() + " " + ex.getErrorCode());

            resultado.put("salida", 9);
            resultado.put("actualizaciones", 0);
            resultado.put("msj", "Error al actualizar usuario "+ ex.getMessage());
        }
        catch(Exception ex){
//            ex.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento updateUsuario " + parametros, ex);
            
            Utilidades.sendErrorMail("Error en Actualizacion de informacion de Usuario " + ex.getMessage());

            resultado.put("salida", 9);
            resultado.put("actualizaciones", 0);
            resultado.put("msj", "Error al actualizar usuario "+ ex.getMessage());            
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
        return resultado;
    }
    
    

    public static boolean existeUsuario(String usuario, String tarjeta) throws Exception {
        boolean res = true;
        ResultSet rs = null;
        Statement stmt = null;
        try{
            ConexionDirecta c = ConexionDirecta.getConexion();
            stmt = c.getConnection().createStatement();
            String sql = "select 1 as existe from datos9(nolock) where usuario_datos9 = '" + usuario + "' and estado_datos9 = 0";

            if (tarjeta != null)
                sql += " and codtar != '"+tarjeta+"'";
            
            conf.getLogger().log(Level.INFO,"procedimiento existeUsuario " + tarjeta + "SQL: " + sql);
            
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
            //e.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento existeUsuario " + tarjeta, e);
            Utilidades.sendErrorMail(e.getMessage());
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
                String sql = "SELECT 1 as valido, " +
                                  "codtar_datos9, " +
                                  "nom_datos9, " +
                                  "descuento_datos9, " +
                                  "mail_datos9, " +
                                  "tel1_datos9, " +
                                  "dom_datos9, " +
                                  "cp_datos9, " +
                                  "loc_datos9, " +
                                  "fec_nac_datos9 " +
                             "FROM datos9 (nolock) where ";

                if (primera)  //Primer loggin valida contra la tarjeta de puntos.
                    sql += "codtar_datos9 = '" + nombre + "'";
                else
                    sql += "usuario_datos9 = '" + nombre + "'";

                sql += " and password_datos9 = '" + pass + "'";
                sql += " COLLATE Latin1_General_CS_AS";

                conf.getLogger().log(Level.INFO,"procedimiento validaUsuario "+nombre + " " + pass + " SQL: " + sql);
                //System.out.println("sql valida: "+ sql);
                
                Statement stmt = c.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.isBeforeFirst())
                    while (rs.next()) {
                        String localizado = rs.getString("valido");

                        if(localizado.compareTo("1") == 0){
                            resul.put("salida", 1);
                            resul.put("tarjeta", rs.getString("codtar_datos9"));
                            resul.put("nombre", rs.getString("nom_datos9"));
                            resul.put("correo", rs.getString("mail_datos9"));
                            resul.put("telefono", rs.getString("tel1_datos9"));
                            resul.put("domicilio", rs.getString("dom_datos9"));
                            resul.put("codpos", rs.getInt("cp_datos9"));
                            resul.put("localidad", rs.getString("loc_datos9"));
                            resul.put("fecnac", Utilidades.diaMesAnioString(rs.getInt("fec_nac_datos9")));
                            resul.put("msj", "OK");
                        }else{
                            resul.put("salida", 9);
                            resul.put("tarjeta", "");
                            resul.put("nombre", "");
                            resul.put("puntos", 0);
                            resul.put("correo", "");
                            resul.put("telefono", "");
                            resul.put("domicilio", "");
                            resul.put("codpos", 0);
                            resul.put("localidad", "");
                            resul.put("fecnac", 0);

                            resul.put("msj", "Usuario No localizado");
                        }

                        break;
                    }
                else{
                    resul.put("salida", 9);
                    resul.put("tarjeta", "");
                    resul.put("nombre", "");
                    resul.put("puntos", 0);
                    resul.put("correo", "");
                    resul.put("telefono", "");
                    resul.put("domicilio", "");
                    resul.put("codpos", 0);
                    resul.put("localidad", "");
                    resul.put("fecnac", 0);
                    resul.put("msj", "Usuario No localizado");
                }
                stmt.close();
            }
        }
        catch(Exception e){
            //e.printStackTrace();
            conf.getLogger().log(Level.SEVERE,"procedimiento validaUsuario " + nombre + " " + pass, e);
            Utilidades.sendErrorMail(e.getMessage());
        }
        finally{
            return resul;
        }
    }

    public static void main (String args[]){

        JSONObject resul = new JSONObject();
        Statement stmt = null;
        ResultSet rs = null;
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            String sql = "select 1 as existe from predat where codtar_predat = '147' and codref_predat = 22";
            stmt = con.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            if(rs.isBeforeFirst())
                System.out.println("EXISTE");
            else
                System.out.println("NO EXISTE");

        }
        catch(Exception e){
            e.printStackTrace();
            
            /***
             * String sql = "select 1 as existe, nom_datos9 from DATOS9(nolock) where estado_datos9 = 0 and codtar_datos9 = '" + tarjeta + "'";
                conf.getLogger().log(Level.INFO,"procedimiento existeTarjeta" + tarjeta + " SQL: " + sql);
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                *  if(rs.isBeforeFirst())
                    while (rs.next()) {
                        String localizado = rs.getString("valido");

                        if(localizado.compareTo("1") == 0){
             */
        }

//        String tarjeta = "12345678";
//        System.out.println("Tarjeta: "+tarjeta);
//        try{
//            System.out.println("Puntos: " + cu.getPuntos(tarjeta));
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
    }
}

