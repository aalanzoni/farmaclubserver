/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.control;

import org.json.simple.JSONObject;

/**
 *
 * @author Andres Lanzoni
 */
public class ControlProducto {
    
    public static JSONObject getProductosEnCanje(String tarjeta) throws Exception{
        JSONObject rest = new JSONObject();
        
/*select m.empre_art, m.codigo_art, m.descri_art 
 from 
(
SELECT ROW_NUMBER() over (order by codigo_art asc) as RowNr, 
       empre_art
      ,[codigo_art]
      ,[descri_art]      
      ,[barra_art]
      ,[rubro1_art]
      ,[rubro2_art]      
      ,[marca_art]      
      ,[peso_art]      
      ,[estado_art]      
      ,[moneda_art]      
      ,[precio_civa_art]
      ,[precio_siva_art]
      ,[cod_por_comis_art]      
  FROM [HighSoft].[dbo].[ARTIC])m
  where RowNr between 100 and 200*/
        
        return rest;
    }
    
}
