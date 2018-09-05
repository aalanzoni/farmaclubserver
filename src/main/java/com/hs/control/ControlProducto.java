/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.control;

import com.hs.util.ConexionDirecta;
import com.hs.util.Configuracion;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Andres Lanzoni
 */
public class ControlProducto {


    public ControlProducto(){
        super();
    }

    public JSONObject getProductosPuntos(String tarjeta, int desde, int hasta, String orden) throws Exception{
        Configuracion conf = Configuracion.getConfig();
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        int ptos = 0;
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                stmt = con.getConnection().createStatement();
                String sql = "SELECT " +
                               "SUM(puntos_hiscre) AS puntos " +
                             "FROM DATOS9(nolock) " +
                             "INNER JOIN HISCRE(nolock) ON " +
                              "codtar_hiscre = codtar_datos9 " +
                             "WHERE codtar_datos9 = '" + tarjeta + "' and " +
                              "(estado_datos9 = '0' or estado_datos9 is null)";
                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    rs.next();
                    ptos = rs.getInt("puntos");
                    System.out.println("======>>>   PUNTOS: " + ptos);

                    if(ptos == 0){
                        resul.put("salida", 2);
                        resul.put("msj", "Tarjeta sin Puntos");
                        return resul;
                    }

                }else{//No hay tarjeta
                    resul.put("salida", 2);
                    resul.put("msj", "Tarjeta sin puntos");
                    return resul;
                }
                rs.close();

                //Productos segun los puntos.
                int cant = 0;
                sql = "SELECT count(1) as cantidad FROM TARART(nolock) WHERE "
                        +           "(estado_tarart = '0' or "
                        +            "estado_tarart is null) and "
                        +            "fec_vto_tarart > GETDATE() and "
                        +            "puntos_tarart <= "+ ptos;

                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    rs.next();
                    cant = rs.getInt("cantidad");
                    System.out.println("======>>>   Cantidad de Productos: " + cant + " para puntos: " + ptos);
                }
                rs.close();

                // Retornar los productos posibles de canjear con los puntos del cliente
                sql = "SELECT "
                        +    "m.empre_tarart, "
                        +    "m.codigo_tarart, "
                        +    "m.descri_tarart, "
                        +    "m.puntos_tarart, "
                        +    "m.fec_vto_tarart, "
                        +    "m.foto_tarart, "
                        +    "m.foto_2_tarart "
//                        +    "m.foto_3_tarart "
                        +    "FROM "
                        +    "(SELECT ROW_NUMBER() "
                        +    "OVER (order by " + orden + " ) as RowNr,"
                        +    "empre_tarart, "
                        +    "codigo_tarart, "
                        +    "descri_tarart, "
                        +    "puntos_tarart, "
                        +    "fec_vto_tarart, "
                        +    "foto_tarart, "
                        +    "foto_2_tarart "
//                        +    "foto_3_tarart "
                        +    "FROM TARART(nolock) "
                        +    "WHERE (estado_tarart = '0' or "
                        +           "estado_tarart is null) and "
                        +           "fec_vto_tarart > GETDATE() and "
                        +           "puntos_tarart <= "+ ptos + ")m "
                        +    "where RowNr between "+ desde + " and " + hasta;

                System.out.println("SQL: " + sql);

                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    JSONArray productos = new JSONArray();

                    while (rs.next()) {
                        JSONObject cte = new JSONObject();
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        cte.put("codigo", codigo);
                        cte.put("nombre", nombre);
                        cte.put("puntos", puntos);

                        String foto = rs.getString("foto_2_tarart");
                        if(foto != null && !foto.isEmpty()){
                            foto = foto.trim();
                            String base64 = this.codificarFoto(foto);
                            if(base64.compareTo("-1") != 0){
                                cte.put("foto_2", base64);
                            }
                            else{
                                conf.getLogger().log(Level.SEVERE, "Foto 2 en BASE64: " + foto + " " + codigo + " " + nombre);
                            }
                        }else
                            cte.put("foto_2", "");

//                        foto = rs.getString("foto_3_tarart");
//                        if(foto != null && !foto.isEmpty()){
//                            foto = foto.trim();
//                            String base64 = this.codificarFoto(foto);
//                            if(base64.compareTo("-1") != 0){
//                                cte.put("foto_3", base64);
//                            }
//                            else{
//                                conf.getLogger().log(Level.SEVERE, "Foto 3 en BASE64: " + foto + " " + codigo + " " + nombre);
//                            }
//                        }else
//                            cte.put("foto_3", "");

                     productos.add(cte);
                    }
                    resul.put("salida", 1);
                    resul.put("msj", "OK");
                    resul.put("cantidad", cant);
                    resul.put("productos", productos);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            resul.put("salida", 9);
            resul.put("msj", e.getMessage());
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
    public JSONObject getProductosEnCanje(int desde, int hasta, String orden) throws Exception{
        Configuracion conf = Configuracion.getConfig();
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                stmt = con.getConnection().createStatement();
                int cant = 0;
                String sql = "SELECT count(1) as cantidad FROM TARART(nolock) WHERE "
                        +           "(estado_tarart = '0' or "
                        +            "estado_tarart is null) and "
                        +            "fec_vto_tarart > GETDATE()";

                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    rs.next();
                    cant = rs.getInt("cantidad");
                }
                rs.close();

                sql = "SELECT "
                        +    "m.empre_tarart, "
                        +    "m.codigo_tarart, "
                        +    "m.descri_tarart, "
                        +    "m.puntos_tarart, "
                        +    "m.fec_vto_tarart, "
                        +    "m.foto_tarart, "
                        +    "m.foto_2_tarart "
//                        +    "m.foto_3_tarart "
                        +    "FROM "
                        +    "(SELECT ROW_NUMBER() "
                        +    "OVER (order by " + orden + " ) as RowNr,"
                        +    "empre_tarart, "
                        +    "codigo_tarart, "
                        +    "descri_tarart, "
                        +    "puntos_tarart, "
                        +    "fec_vto_tarart, "
                        +    "foto_tarart, "
                        +    "foto_2_tarart "
//                        +    "foto_3_tarart "
                        +    "FROM TARART(nolock) "
                        +    "WHERE (estado_tarart = '0' or "
                        +           "estado_tarart is null) and "
                        +           "fec_vto_tarart > GETDATE())m "
                        +    "where RowNr between "+ desde + " and " + hasta;


                System.out.println("SQL: " + sql);

                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    JSONArray productos = new JSONArray();

                    while (rs.next()) {
                        JSONObject cte = new JSONObject();
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        cte.put("codigo", codigo);
                        cte.put("nombre", nombre);
                        cte.put("puntos", puntos);

                        String foto = rs.getString("foto_2_tarart");
                        if(foto != null && !foto.isEmpty()){
                            foto = foto.trim();
                            String base64 = this.codificarFoto(foto);
                            if(base64.compareTo("-1") != 0){
                                cte.put("foto_2", base64);
                            }
                            else{
                                cte.put("foto_2", "");
                                conf.getLogger().log(Level.SEVERE, "Foto 2 en BASE64: " + foto + " " + codigo + " " + nombre);
                            }
                        }else
                            cte.put("foto_2", "");

//                        foto = rs.getString("foto_3_tarart");
//                        if(foto != null && !foto.isEmpty()){
//                            foto = foto.trim();
//                            String base64 = this.codificarFoto(foto);
//                            if(base64.compareTo("-1") != 0){
//                                cte.put("foto_3", base64);
//                            }
//                            else{
//                                conf.getLogger().log(Level.SEVERE, "Foto 3 en BASE64: " + foto + " " + codigo + " " + nombre);
//                            }
//                        }else
//                            cte.put("foto_3", "");

                     productos.add(cte);
                    }
                    resul.put("salida", 1);
                    resul.put("msj", "OK");
                    resul.put("cantidad", cant);
                    resul.put("productos", productos);
                }
            }
        }
        catch(Exception e){
            conf.getLogger().log(Level.SEVERE, "Error en getProductosEnCanje: " + e.getMessage());
            e.printStackTrace();
            resul.put("salida", 9);
            resul.put("msj", "Error get canjes: " + e.getMessage());
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

    private String codificarFoto(String path) throws Exception{
        ControlProducto tempObject = new ControlProducto();
        String res;

        File fichero = new File(path);
        if(fichero.exists()){
            // convert file to regular byte array
            byte[] codedFile = tempObject.convertFileToByteArray(path);

            // encoded file in Base64
            byte[] encodedFile = Base64.getEncoder().encode(codedFile);

            // print out the byte array
            res = new String(encodedFile, Charset.forName("UTF8"));

            //res = Arrays.toString(encodedFile);
        }
        else{//No existe la Foto
            res = "-1";
        }
        return res;
    }

    public void decodificarBase64(byte[] encodedFile){
        try{
            byte[] decodedFile = Base64.getDecoder().decode(encodedFile);
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(decodedFile));
            File outputfile = new File("D:\\decode.jpg");
            ImageIO.write(bi, "jpg", outputfile);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

     public byte[] convertFileToByteArray(String filePath) {

         Path path = Paths.get(filePath);

        byte[] codedFile = null;

        try {
            codedFile = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return codedFile;
    }

     public static void main(String a[]){
         ControlProducto cp = new ControlProducto();
         try{
             String codificado = cp.codificarFoto("D:\\farmacia.jpg");
             //System.out.println("codificado: " + codificado);
             byte[] b = codificado.getBytes();
             cp.decodificarBase64(b);
         }
         catch(Exception e){
             e.printStackTrace();
         }
         System.exit(0);
     }
}
