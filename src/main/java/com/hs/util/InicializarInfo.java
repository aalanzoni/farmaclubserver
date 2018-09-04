/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.sql.Statement;

/**
 *
 * @author Andres Lanzoni
 */
public class InicializarInfo {
    private ConexionDirecta cd;
    
    
    public InicializarInfo(){
        super();
        cd = ConexionDirecta.getConexion();
    }
    
    private void insertarProductos(){
        Statement stmt = null;
        boolean paso = true;
        for (int i = 1; i < 1001; i++) {
            String codigo = String.format("AL_%06d", i);
            int puntos = 100 + i;
            String sql =
            "INSERT INTO [dbo].[TARART] "+
           "([empre_tarart] "+
           ",[codigo_tarart]"+
           ",[descri_tarart]"+
           ",[puntos_tarart]"+
           ",[estado_tarart]"+
           ",[fec_vto_tarart]"+
           ",[foto_tarart]"+
           ",[foto_2_tarart]"+
           ",[foto_3_tarart_500]"+
           ",[comen_tarart]"+
           ",[resto_tarart]) values"+           
           "(1 " +
           ", '" + codigo + "'" +
           ", 'no nulo' " +
           "," + puntos +
           ",0" +
           ",convert(datetime,'18-06-12 10:34:09 PM',5)" +
           ",'D:\\viagra.jpg'" +
           ",'D:\\viagra_gde.jpg'" +
           ",'D:\\viagra_peq.jpg'" +
           ",null" +
           ",null)";
//            System.out.println("SQL = "+sql);
//            System.exit(i);
            try{
                stmt = cd.getConnection().createStatement();
                paso = stmt.execute(sql);
            }
            catch(Exception e){
                e.printStackTrace();
                System.exit(i);
            }
        }
    }
    
    public static void main(String a[]){
//        System.out.println(""+String.format("AL_%06d",4587)) ;
        InicializarInfo ii = new InicializarInfo();
        ii.insertarProductos();
    }
    
}
