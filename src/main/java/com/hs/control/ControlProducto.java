/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.control;

import com.hs.util.ConexionDirecta;
import com.hs.util.Configuracion;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.commons.codec.binary.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
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

    public JSONObject getProductosEnCanje(int desde, int hasta, String orden) throws Exception{
        Configuracion conf = Configuracion.getConfig();
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try{
            ConexionDirecta con = ConexionDirecta.getConexion();
            if(con != null){
                String sql = "SELECT "
                        +    "m.empre_tarart, "
                        +    "m.codigo_tarart, "
                        +    "m.descri_tarart, "
                        +    "m.puntos_tarart, "
                        +    "m.fec_vto_tarart, "
                        +    "m.foto_tarart, "
                        +    "m.foto_2_tarart, "
                        +    "m.foto_3_tarart "
                        +    "FROM "
                        +    "(SELECT ROW_NUMBER() "
                        +    "OVER (order by " + orden + " ) as RowNr,"
                        +    "empre_tarart, "
                        +    "codigo_tarart, "
                        +    "descri_tarart, "
                        +    "puntos_tarart, "
                        +    "fec_vto_tarart, "
                        +    "foto_tarart, "
                        +    "foto_2_tarart, "
                        +    "foto_3_tarart "
                        +    "FROM TARART(nolock) "
                        +    "WHERE (estado_tarart = '0' or "
                        +           "estado_tarart is null))m "
                        +    "where RowNr between "+ desde + " and " + hasta
                        +           " and fec_vto_tarart > GETDATE()";

                System.out.println("SQL: " + sql);
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst()){
                    JSONArray productos = new JSONArray();
                    int cant = 0;
                    while (rs.next()) {
                        cant ++;
                        JSONObject cte = new JSONObject();
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        cte.put("codigo",codigo);
                        cte.put("nombre",nombre);
                        cte.put("puntos",puntos);

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
                        }

                        foto = rs.getString("foto_3_tarart");
                        if(foto != null && !foto.isEmpty()){
                            foto = foto.trim();
                            String base64 = this.codificarFoto(foto);
                            if(base64.compareTo("-1") != 0){
                                cte.put("foto_3", base64);
                            }
                            else{
                                conf.getLogger().log(Level.SEVERE, "Foto 3 en BASE64: " + foto + " " + codigo + " " + nombre);
                            }
                        }
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
            byte[] encodedFile = Base64.encodeBase64(codedFile);

            // print out the byte array
            res = Arrays.toString(encodedFile);
        }
        else{//No existe la Foto
            res = "-1";
        }
        return res;
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
             JSONObject salida = cp.getProductosEnCanje(1, 100, "codigo_tarart");
             System.out.println("Salida: "+ salida.get("cantidad"));
             //System.out.println("Salida: "+salida.toJSONString());
//             cp.codificarFoto("D:\\farmacia.jpg");
         }
         catch(Exception e){
             e.printStackTrace();
         }
         System.exit(0);
     }

}
