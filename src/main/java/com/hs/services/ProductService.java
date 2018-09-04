/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.services;

import com.hs.control.ControlProducto;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.json.simple.JSONObject;

/**
 *
 * @author Andres Lanzoni
 */
@Path("/product")
public class ProductService {
    
    @POST
    @Path("/getCanjes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //public Response validaUsuario(Map<String, String> parametros) throws URISyntaxException {
    public Response getCanjes(Map<String, String> parametros) throws URISyntaxException{
        JSONObject resp = new JSONObject();
                
        String from = parametros.get("from");
        String to = parametros.get("to");
        String orderBy = parametros.get("orderBy");
        
        if(from == null || from.isEmpty() || to == null || to.isEmpty() || orderBy == null || orderBy.isEmpty()){
            resp.put("salida", 9);
            resp.put("msj", "No encuentra parametros de entrada (from - to -orderBy)");
            return Response.ok(resp).build();
        }
        int desde = Integer.parseInt(from);
        int hasta = Integer.parseInt(to);
        
        if(desde > hasta){
            resp.put("salida", 8);
            resp.put("msj", "Error en parametros (from < to)");
            return Response.ok(resp).build();
        }
        
        String orden = "";
        String[] parts = orderBy.split(",");
        String var = parts[0];
        String order = parts[1];

        if(var.compareTo("codigo") == 0){
            orden += "codigo_tarart";
        }

        if(var.compareTo("nombre") == 0){
            orden += "descri_tarart";
        }

        if(var.compareTo("puntos") == 0){
            orden += "puntos_tarart";
        }

        if(order.compareTo("asc") == 0)
            orden += " asc";
        else
            orden += " desc";
            
        
        System.out.println("Orden: " + orden);
        
        try{
            ControlProducto cp = new ControlProducto();
            resp = cp.getProductosEnCanje(desde, hasta, orden);
            return Response.ok(resp).build();
        }
        catch(Exception e){
            e.printStackTrace();
            resp.put("salida", 9);
            resp.put("msj", "Error");
            return Response.ok(resp).build();            
        }
        
//        return Response
//               .status(200)
//               .entity("getCanjes is called, from : " + from + ", to : " + to
//                    + ", orderBy" + orderBy.toString()).build();
    }
}
