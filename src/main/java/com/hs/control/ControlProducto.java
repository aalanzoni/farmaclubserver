/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.control;

import com.hs.util.ConexionDirecta;
import com.hs.util.Configuracion;
import static com.hs.util.Test.resizeImage;
import com.hs.util.Utilidades;
import java.awt.Image;
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
    static Configuracion conf = Configuracion.getConfig();

    public ControlProducto() {
        super();
    }

    public JSONObject getProductoCodigo(String code, String para) throws Exception {        
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try {
            ConexionDirecta con = ConexionDirecta.getConexion();
            String sql;
            if (con != null) {
                sql = "SELECT "
                        + "empre_tarart, "
                        + "codigo_tarart, "
                        + "descri_tarart, "
                        + "puntos_tarart, "
                        + "fec_vto_tarart, "
                        + "foto_tarart, "
                        + "foto_gde_b64_tarart "
                        + "FROM "
                        + "TARART(nolock) "
                        + "WHERE codigo_tarart = '" + code + "' and "
                        + "para_tarart = '" + para + "'";
                
                conf.getLogger().log(Level.INFO, "procedimiento getProductoCodigo, code: {0} para: {1} SQL: {2}", 
                        new Object[]{code, para, sql});
                stmt = con.getConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {

                    while (rs.next()) {
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        resul.put("codigo", codigo);
                        resul.put("nombre", nombre);
                        resul.put("puntos", puntos);
                        resul.put("salida", 1);
                        
                        String ft = rs.getString("foto_gde_b64_tarart");
                        if(ft != null && !ft.trim().isEmpty()){
                            resul.put("foto", ft);
                            break;
                        }else{
                            String foto = this.getPath(rs.getString("foto_tarart"));
                            if (foto != null && !foto.trim().isEmpty()) {
                                foto = foto.trim();
                                String base64 = this.codificarFoto(foto);
                                if (base64.compareTo("-1") != 0) {
                                    resul.put("foto", base64);
                                    this.actualizaB64(2, base64, rs.getInt("empre_tarart"), codigo, para);
                                } else {
                                    conf.getLogger().log(Level.SEVERE, "Foto en BASE64: " + foto + " " + codigo + " " + nombre);
                                }
                            } else {
                                resul.put("foto", "");
                                resul.put("salida", 9);
                                resul.put("msj", "Foto no localizada");
                            }                        
                            break;
                        }
                    }
                }
                else{
                    resul.put("salida", 2);
                    resul.put("msj", "No existe Producto");
                    resul.put("codigo", 0);
                    resul.put("nombre", "");
                    resul.put("puntos", 0);
                    resul.put("foto", "");
                    return resul;
                }                
            }
        } catch (Exception e) {
            conf.getLogger().log(Level.SEVERE,"procedimiento getProductoCodigo, code: " + code + " para: "+ para , e);
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("msj", e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }
        return resul;
    }

    public JSONObject getProductosPuntos(String tarjeta, int desde, int hasta, String orden) throws Exception {        
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        int ptos = 0;
        try {
            ConexionDirecta con = ConexionDirecta.getConexion();
            if (con != null) {
                stmt = con.getConnection().createStatement();
                String sql = "SELECT "
                        + "SUM(puntos_hiscre) AS puntos "
                        + "FROM DATOS9(nolock) "
                        + "INNER JOIN HISCRE(nolock) ON "
                        + "codtar_hiscre = codtar_datos9 "
                        + "WHERE codtar_datos9 = '" + tarjeta + "' and "
                        + "(estado_datos9 = '0' or estado_datos9 is null)";
                conf.getLogger().log(Level.INFO, "procedimiento getProductoPuntos, tarjeta: {0} desde: {1} hasta: {2} orden: {3} SQL: {4}",
                        new Object[]{tarjeta, desde, hasta, orden, sql});
                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    rs.next();
                    ptos = rs.getInt("puntos");
                    if (ptos == 0) {
                        JSONArray productos = new JSONArray();
                        resul.put("salida", 2);
                        resul.put("msj", "Tarjeta sin Puntos");
                        resul.put("cantidad", 0);
                        resul.put("productos", productos);
                        return resul;
                    }

                } else {//No hay tarjeta
                    JSONArray productos = new JSONArray();
                    resul.put("salida", 2);
                    resul.put("msj", "Tarjeta sin Puntos");
                    resul.put("cantidad", 0);
                    resul.put("productos", productos);
                    return resul;
                }
                rs.close();

                //Total de Productos segun los puntos, luego se paginan segun parametros.
                int cant = 0;
                sql = "SELECT count(1) as cantidad FROM TARART(nolock) WHERE "
                        + "(estado_tarart <> 'SUSPENDIDO' or "
                        + "estado_tarart is null) and "
                        + "fec_vto_tarart > GETDATE() and "
                        + "puntos_tarart <= " + ptos
                        + " and para_tarart = 'C'";
                
                conf.getLogger().log(Level.INFO, "procedimiento getProductoPuntos, tarjeta: {0} desde: {1} hasta: {2} orden: {3} SQL: {4}", 
                        new Object[]{tarjeta, desde, hasta, orden, sql});
                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    rs.next();
                    cant = rs.getInt("cantidad");                    
                }
                rs.close();

                // Retornar los productos posibles de canjear con los puntos del cliente
                sql = "SELECT "
                        + "m.empre_tarart, "
                        + "m.codigo_tarart, "
                        + "m.descri_tarart, "
                        + "m.puntos_tarart, "
                        + "m.fec_vto_tarart, "
                        + "m.comen_tarart, "
                        + "m.foto_tarart, "
                        + "m.foto_2_tarart, "
                        + "m.foto_ch_b64_tarart "
                        + "FROM "
                        + "(SELECT ROW_NUMBER() "
                        + "OVER (order by " + orden + " ) as RowNr,"
                        + "empre_tarart, "
                        + "codigo_tarart, "
                        + "descri_tarart, "
                        + "puntos_tarart, "
                        + "fec_vto_tarart, "
                        + "comen_tarart, "
                        + "foto_tarart, "
                        + "foto_2_tarart, "
                        + "foto_ch_b64_tarart "
                        + "FROM TARART(nolock) "
                        + "WHERE (estado_tarart <> 'SUSPENDIDO' or "
                        + "estado_tarart is null) and "
                        + "fec_vto_tarart > GETDATE() and "
                        + "puntos_tarart <= " + ptos + ")m "
                        + "where RowNr between " + desde + " and " + hasta;

                conf.getLogger().log(Level.INFO, "procedimiento getProductoPuntos, tarjeta: {0} desde: {1} hasta: {2} orden: {3} SQL: {4}", 
                        new Object[]{tarjeta, desde, hasta, orden, sql});

                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    JSONArray productos = new JSONArray();

                    while (rs.next()) {
                        JSONObject cte = new JSONObject();
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        cte.put("codigo", codigo);
                        cte.put("nombre", nombre);
                        cte.put("puntos", puntos);
                        cte.put("comentario", rs.getString("comen_tarart"));
                        String foto_ori = rs.getString("foto_tarart");
                        if(foto_ori != null && !foto_ori.trim().isEmpty()){
                            String[] parts = foto_ori.split("\\");
                            if(parts.length > 0)
                                cte.put("foto_nombre", parts[parts.length - 1].trim());
                            else
                                cte.put("foto_nombre", "");
                        }else{
                            cte.put("foto_nombre", "");
                        }
                            
                        String foto_chica = rs.getString("foto_2_tarart");
                        String foto_ch_b64 = rs.getString("foto_ch_b64_tarart");
                        if (foto_chica == null || foto_chica.trim().isEmpty()) {
                            foto_chica = this.redimensionarImgen(foto_ori);
                            guardoFoto(codigo, foto_chica);
                            foto_ch_b64 = null;
                        }

                        if (foto_chica != null && !foto_chica.trim().isEmpty() && foto_ch_b64 == null) {
                            foto_chica = foto_chica.trim();
                            String base64 = this.codificarFoto(foto_chica);
                            if (base64.compareTo("-1") != 0) {
                                cte.put("foto", base64);
                                this.actualizaB64(1, base64, rs.getInt("empre_tarart"), codigo, "C");
                            } else {
                                conf.getLogger().log(Level.SEVERE, "Foto en BASE64: {0} {1} {2}",
                                        new Object[]{foto_chica, codigo, nombre});
                            }
                        } else {
                            if(foto_ch_b64 != null || foto_ch_b64.trim().isEmpty())
                                cte.put("foto", foto_ch_b64);
                            else
                                cte.put("foto", "");
                        }

                        productos.add(cte);
                    }
                    resul.put("salida", 1);
                    resul.put("msj", "OK");
                    resul.put("cantidad", cant);
                    resul.put("productos", productos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("msj", e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }
        return resul;
    }

    private String redimensionarImgen(String foto_ori) {
        Configuracion conf = Configuracion.getConfig();
        String foto = "";

        if (foto_ori == null || foto_ori.trim().isEmpty()) {
            return null;
        }        
        conf.getLogger().log(Level.INFO, "Foto a Redimensionar: " + foto_ori);
        Image img = null;
        BufferedImage tempPNG = null;
        File newFilePNG = null;
        try {
            File i = new File(foto_ori);
            if(i.exists()){
                img = ImageIO.read(i);
                double aspectRatio = (double) img.getWidth(null) / (double) img.getHeight(null);
                tempPNG = resizeImage(img, 100, (int) (100 / aspectRatio));
                foto = foto_ori.trim() + "_New.png";
                newFilePNG = new File(foto);
                ImageIO.write(tempPNG, "png", newFilePNG);
            }
            else{
                conf.getLogger().log(Level.SEVERE, "No existe imagen:: " + foto_ori);
            }
        } catch (IOException e) {
            conf.getLogger().log(Level.SEVERE, "Error al redimensionar FOTO: {0} {1}", 
                    new Object[]{foto_ori, e.getMessage()});
            return null;
        }

        return foto;
    }

    private void guardoFoto(String code, String name) throws Exception {
        Statement stmt = null;
        ConexionDirecta con = ConexionDirecta.getConexion();
        if (con != null) {
            stmt = con.getConnection().createStatement();
            String sql = "update tarart set foto_2_tarart = '" + name + "' where codigo_tarart = '" + code + "'";

            conf.getLogger().log(Level.INFO, "Error al redimensionar FOTO: {0} {1} {2}", 
                    new Object[]{code, name, sql});
            
            stmt.executeUpdate(sql);
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqlEx) {
            }

            stmt = null;
        }
    }

/**
 * 
 * @param desde
 * @param hasta
 * @param orden
 * @param categorias
 * @return
 * @throws Exception 
 */    
    public JSONObject getProductosCategorias(int desde, int hasta, String orden, String para, String categorias) throws Exception{        
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try {
            ConexionDirecta con = ConexionDirecta.getConexion();
            if (con != null) {
                stmt = con.getConnection().createStatement();
                int cant = 0;
                String sql = "with temp as(SELECT DISTINCT empre_tarart, codigo_tarart "+
                             "FROM TARART(NOLOCK) " +
                                            "JOIN ARTCAT(NOLOCK) ON " +
                                 "empre_tarart = empresa_ta_artcat AND "+
                                 "codigo_tarart = codigo_ta_artcat " +
                              "WHERE codigo_ca_artcat IN ("+ categorias+")) "+
                              "select count(1) as cantidad from temp";

                
                conf.getLogger().log(Level.INFO, "procedimiento getProductosCategorias, desde: {0} hasta: {1} orden: {2}  para: {3} categorias: {4} SQL: {5}", 
                        new Object[]{desde, hasta, orden, para, categorias, sql});
                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    rs.next();
                    cant = rs.getInt("cantidad");                    
                }
                rs.close();
                
	        sql = "with temp as(SELECT DISTINCT empre_tarart, codigo_tarart FROM TARART(NOLOCK) " +
	              "JOIN ARTCAT(NOLOCK) ON " +
	              "empre_tarart = empresa_ta_artcat AND " +
                      "codigo_tarart = codigo_ta_artcat " +
                      "WHERE "+
                        "para_tarart = '" + para + "' and "+
                        "codigo_ca_artcat IN ("+categorias+")) " +
                      "select  "+
                      "t.empre_tarart, "+
                      "t.codigo_tarart, "+       
                      "t.descri_tarart, "+
                      "t.puntos_tarart, "+
                      "t.fec_vto_tarart, "+
                      "t.comen_tarart, "+
                      "t.foto_tarart, "+
                      "t.foto_2_tarart, "+
                      "t.foto_ch_b64_tarart "+
	              "from tarart(nolock) as t join temp on "+
                        "t.empre_tarart = temp.empre_tarart and "+
                        "t.codigo_tarart = temp.codigo_tarart";
                
                conf.getLogger().log(Level.INFO, "getProductosCategorias: {0} {1} {2}", 
                    new Object[]{para, categorias, sql});

                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    JSONArray productos = new JSONArray();
                    int ind = 0;
                    while (rs.next()) {
                        ind ++;
                        
                        if(ind < desde)
                            continue;
                        
                        if(ind > hasta)
                            break;
                        
                        JSONObject cte = new JSONObject();
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        cte.put("codigo", codigo);
                        cte.put("nombre", nombre);
                        cte.put("puntos", puntos);
                        cte.put("comentario", rs.getString("comen_tarart"));

                        String foto_ori = this.getPath(rs.getString("foto_tarart"));
                        if(foto_ori != null && !foto_ori.trim().isEmpty()){
                            String[] parts = foto_ori.split("\\\\");
                            if(parts.length > 0)
                                cte.put("foto_nombre", parts[parts.length - 1].trim());
                            else
                                cte.put("foto_nombre", "");
                        }else{
                            cte.put("foto_nombre", "");
                        }
                        String foto_chica = this.getPath(rs.getString("foto_2_tarart"));
                        String foto_ch_b64 = rs.getString("foto_ch_b64_tarart");

                        if (foto_chica == null || foto_chica.trim().isEmpty()) {

                            foto_chica = this.redimensionarImgen(foto_ori);

                            if (foto_chica != null) {
                                guardoFoto(codigo, foto_chica);
                               foto_ch_b64 = null;
                            }
                        }

                        if (foto_chica != null && !foto_chica.trim().isEmpty() && foto_ch_b64 == null) {
                            foto_chica = foto_chica.trim();
                            String base64 = this.codificarFoto(foto_chica);
                            if (base64.compareTo("-1") != 0) {
                                cte.put("foto", base64);
                                this.actualizaB64(1, base64, rs.getInt("empre_tarart"), codigo, "P");
                            } else {
                                cte.put("foto", "");
                                conf.getLogger().log(Level.SEVERE, "getProductosCategorias - BASE64: {0} {1} {2} {3} {4}", 
                                    new Object[]{desde, hasta, orden, categorias, foto_chica});
                            }
                        } else {
                            if(foto_ch_b64 != null || !foto_ch_b64.trim().isEmpty())
                                cte.put("foto", foto_ch_b64);
                            else
                                cte.put("foto", "");
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
        catch (Exception e) {
            conf.getLogger().log(Level.SEVERE, "Error en getProductosEnCanje: " + e.getMessage());
            e.printStackTrace();
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("msj", "Error get canjes: " + e.getMessage());
        } 
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }
        return resul;
    }
    
    
    /**
     * Procedimiento que retorna los productos en Canje o Promocion.
     *
     * @param desde paginado
     * @param hasta paginado
     * @param orden codigo, nombre, puntos - asc desc
     * @param para ('C'anje/'P'romocion)
     * @return
     * @throws Exception
     */
    public JSONObject getProductos(int desde, int hasta, String orden, String para) throws Exception {        
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject resul = new JSONObject();
        try {
            ConexionDirecta con = ConexionDirecta.getConexion();
            if (con != null) {
                stmt = con.getConnection().createStatement();
                int cant = 0;
                String sql = "SELECT count(1) as cantidad FROM TARART(nolock) WHERE "
                        + "(estado_tarart <> 'SUSPENDIDO' or "
                        + "estado_tarart is null) and "
                        + "fec_vto_tarart > GETDATE() and "
                        + "para_tarart = '" + para + "'";
                
                conf.getLogger().log(Level.INFO, "procedimiento getProductos - cantidad, : {0} {1} {2} {3} {4}", 
                        new Object[]{desde, hasta, orden, para, sql});
                
                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    rs.next();
                    cant = rs.getInt("cantidad");
                }
                rs.close();

                sql = "SELECT "
                        + "m.empre_tarart, "
                        + "m.codigo_tarart, "
                        + "m.descri_tarart, "
                        + "m.puntos_tarart, "
                        + "m.fec_vto_tarart, "
                        + "m.comen_tarart, "
                        + "m.foto_tarart, "
                        + "m.foto_2_tarart, "
                        + "m.foto_ch_b64_tarart "
                        + "FROM "
                        + "(SELECT ROW_NUMBER() "
                        + "OVER (order by " + orden + " ) as RowNr,"
                        + "empre_tarart, "
                        + "codigo_tarart, "
                        + "descri_tarart, "
                        + "puntos_tarart, "
                        + "fec_vto_tarart, "
                        + "comen_tarart, "
                        + "foto_tarart, "
                        + "foto_2_tarart, "
                        + "foto_ch_b64_tarart "
                        + "FROM TARART(nolock) "
                        + "WHERE (estado_tarart <> 'SUSPENDIDO' or "
                        + "estado_tarart is null) and "
                        + "fec_vto_tarart > GETDATE() and "
                        + "para_tarart = '" + para + "')m "
                        + "where RowNr between " + desde + " and " + hasta;
                
                conf.getLogger().log(Level.INFO, "procedimiento getProductos - principal, : {0} {1} {2} {3} {4}", 
                        new Object[]{desde, hasta, orden, para, sql});

                rs = stmt.executeQuery(sql);
                if (rs.isBeforeFirst()) {
                    JSONArray productos = new JSONArray();

                    while (rs.next()) {
                        JSONObject cte = new JSONObject();
                        String codigo = rs.getString("codigo_tarart");
                        String nombre = rs.getString("descri_tarart");
                        int puntos = rs.getInt("puntos_tarart");

                        cte.put("codigo", codigo);
                        cte.put("nombre", nombre);
                        cte.put("puntos", puntos);
                        cte.put("comentario", rs.getString("comen_tarart"));

                        String foto_ori = this.getPath(rs.getString("foto_tarart"));
                        if(foto_ori != null && !foto_ori.trim().isEmpty()){
                            String[] parts = foto_ori.split("\\\\");
                            if(parts.length > 0)
                                cte.put("foto_nombre", parts[parts.length - 1].trim());
                            else
                                cte.put("foto_nombre", "");
                        }else{
                            cte.put("foto_nombre", "");
                        }
                        String foto_ch_b64 = rs.getString("foto_ch_b64_tarart");
                        String foto_chica = this.getPath(rs.getString("foto_2_tarart"));

                        if (foto_chica == null || foto_chica.trim().isEmpty()) {

                            foto_chica = this.redimensionarImgen(foto_ori);

                            if (foto_chica != null) {
                                guardoFoto(codigo, foto_chica);
                                foto_ch_b64 = null;
                            }
                        }

                        if (foto_chica != null && !foto_chica.trim().isEmpty() && foto_ch_b64 == null) {
                            foto_chica = foto_chica.trim();
                            String base64 = this.codificarFoto(foto_chica);                            
                            if (base64.compareTo("-1") != 0) {
                                cte.put("foto", base64);
                                this.actualizaB64(1, base64, rs.getInt("empre_tarart"), codigo, "C");
                            } else {
                                cte.put("foto", "");
                                conf.getLogger().log(Level.SEVERE, "Foto en BASE64: " + foto_ori + " " + codigo + " " + nombre);
                            }
                        } else {
                            if(foto_ch_b64 != null || foto_ch_b64.trim().isEmpty())
                                cte.put("foto", foto_ch_b64);
                            else
                                cte.put("foto", "");
                        }

                        productos.add(cte);
                    }
                    resul.put("salida", 1);
                    resul.put("msj", "OK");
                    resul.put("cantidad", cant);
                    resul.put("productos", productos);
                }
                else{
                    JSONArray productos = new JSONArray();
                    resul.put("salida", 2);
                    resul.put("msj", "Tarjeta sin Puntos");
                    resul.put("cantidad", 0);
                    resul.put("productos", productos);
                    return resul;
                }
            }
        } catch (Exception e) {
            conf.getLogger().log(Level.SEVERE, "Error en getProductos: " + e);
            Utilidades.sendErrorMail(e.getMessage());
            resul.put("salida", 9);
            resul.put("msj", "Error get canjes: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }
        return resul;
    }
    
    /**
     * Actuliza tablas DATOS9 con la foto en BASE64 para luego retornar esta
     * sin necesidad de codificarla.
     * @param foto
     * @param base64
     * @param empresa
     * @param codigo
     * @param para
     * @throws Exception 
     */
    private void actualizaB64(int foto, String base64, int empresa, String codigo, String para) throws Exception{
        Statement stmt = null;
        ConexionDirecta con = ConexionDirecta.getConexion();
        if (con != null) {
            stmt = con.getConnection().createStatement();

            String sql1 = "update tarart set ";
            String sql2 = " where empre_tarart = " + empresa + " and " +
                                 "codigo_tarart = '" + codigo + "' and " +
                                 "para_tarart = '" + para + "'";
            String sql = "";
            if(foto == 1)
                sql = sql1 + "foto_ch_b64_tarart = '" + base64 + "'" + sql2;
            else
                sql = sql1 + "foto_gde_b64_tarart = '" + base64 + "'" + sql2;
            
            conf.getLogger().log(Level.INFO, "procedimiento actualiza B64, : {0} {1} {2} {3}", 
                    new Object[]{foto, empresa, codigo, para});
                
            stmt.executeUpdate(sql);
        }
        
    }

    private String codificarFoto(String path) throws Exception {
        ControlProducto tempObject = new ControlProducto();
        Configuracion conf = Configuracion.getConfig();
        String res;
        File fichero = null;

        try {
            fichero = new File(path);
            if(!fichero.exists()){
                conf.getLogger().log(Level.WARNING, "Foto no localizada: " + path);
                res = "-1";
                return res;
            }
        } catch (Exception e) {
            conf.getLogger().log(Level.WARNING, "Foto no localizada: " + path);
            res = "-1";
            return res;
        }
        
        if (fichero.exists()) {
            // convert file to regular byte array
            byte[] codedFile = tempObject.convertFileToByteArray(path);

            // encoded file in Base64
            byte[] encodedFile = Base64.getEncoder().encode(codedFile);

            // print out the byte array
            res = new String(encodedFile, Charset.forName("UTF8"));

        } else {//No existe la Foto
            res = "-1";
        }
        return res;
    }

    public void decodificarBase64(byte[] encodedFile) {
        try {
            byte[] decodedFile = Base64.getDecoder().decode(encodedFile);
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(decodedFile));
            File outputfile = new File("D:\\decode.jpg");
            ImageIO.write(bi, "jpg", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
            Utilidades.sendErrorMail(e.getMessage());
        }

    }

    public byte[] convertFileToByteArray(String filePath) {

        Path path = Paths.get(filePath);

        byte[] codedFile = null;

        try {
            codedFile = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.sendErrorMail(e.getMessage());
        }

        return codedFile;
    }

    /**
     * Procedimiento que dado el path de la foto_chica lo acomoda teniendo en
     * cuenta las unidades mapeadas en el archivo de propiedades
     * farmacia-properties
     *
     * @param path
     * @return String
     */
    private String getPath(String path) {

        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String letra = path.substring(0, 1).toUpperCase();

        if (letra == null) {
            return null;
        }

        Configuracion config = Configuracion.getConfig();
        if (letra.compareTo("P") == 0) {
            path = config.getLetra_p() + path.substring(3);
            return path;
        }

        if (letra.compareTo("X") == 0) {
            path = config.getLetra_x() + path.substring(3);
            return path;
        }

        return path;
    }

    public static void main(String a[]) {
        //ControlProducto cp = new ControlProducto();
        try {
            String path = "X:\\FARMACIA1\\IMAGENES\\47 STREET.JPG";
            String[] parts = path.split("\\");
            System.out.println("Long: "+parts.length);
            System.out.println("tengo: "+parts[parts.length - 1]);
//            System.out.println("salida: "+cp.getProductos(1, 10, "codigo_tarart asc", "P"));
            //System.out.println("PATH: " + cp.getPath("P:\\ACU\\SISTEMA\\IMG_FARMACLUB\\ESMALTE.JPG"));
            
//             String codificado = cp.codificarFoto("D:\\farmacia.jpg");
//             //System.out.println("codificado: " + codificado);
//             byte[] b = codificado.getBytes();
//             cp.decodificarBase64(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
