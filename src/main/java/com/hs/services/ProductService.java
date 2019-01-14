/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.services;

import com.hs.control.ControlProducto;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

/**
 *
 * @author Andres Lanzoni
 */
@Path("/product")
public class ProductService {

    @POST
    @Path("/getFoto")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //public Response validaUsuario(Map<String, String> parametros) throws URISyntaxException {
    public Response getFoto(Map<String, String> parametros) throws URISyntaxException {
        JSONObject resp = new JSONObject();

        String codigo = parametros.get("codigo");
        String para = parametros.get("para");

        if (codigo == null || codigo.isEmpty() || para == null || para.isEmpty()) {
            resp.put("salida", 9);
            resp.put("msj", "No encuentra parametros de entrada (codigo - para (Canje - Promocion)");
            return Response.ok(resp).build();
        }

        try {
            ControlProducto cp = new ControlProducto();
            resp = cp.getProductoCodigo(codigo, para);
            return Response.ok(resp).build();
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("salida", 9);
            resp.put("msj", "Error");
            return Response.ok(resp).build();
        }
    }

    @POST
    @Path("/getProducts")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)    
    public Response getProductos(Map<String, String> parametros) throws URISyntaxException {
        JSONObject resp = new JSONObject();

        String from = parametros.get("from");
        String to = parametros.get("to");
        String orderBy = parametros.get("orderBy");
        String para = parametros.get("para");

        if (from == null || from.isEmpty() || to == null || to.isEmpty() || orderBy == null || orderBy.isEmpty() || para == null || para.isEmpty()) {
            resp.put("salida", 9);
            resp.put("msj", "No encuentra parametros de entrada (from - to - orderBy - para = 'C'anje,'P'romocion)");
            return Response.ok(resp).build();
        }
        int desde = Integer.parseInt(from);
        int hasta = Integer.parseInt(to);

        if (desde > hasta) {
            resp.put("salida", 8);
            resp.put("msj", "Error en parametros (from < to)");
            return Response.ok(resp).build();
        }

        String orden = "";
        String[] parts = orderBy.split(",");
        String var = parts[0];
        String order = parts[1];

        if (var.compareTo("codigo") == 0) {
            orden += "codigo_tarart";
        }

        if (var.compareTo("nombre") == 0) {
            orden += "descri_tarart";
        }

        if (var.compareTo("puntos") == 0) {
            orden += "puntos_tarart";
        }

        if (orden.isEmpty()) {
            resp.put("salida", 9);
            resp.put("msj", "Parametro de Orden Incorrecto (codigo, nombre, puntos)");
            return Response.ok(resp).build();
        }

        if (order.compareTo("asc") == 0) {
            orden += " asc";
        } else {
            orden += " desc";
        }

        try {
            ControlProducto cp = new ControlProducto();
            resp = cp.getProductos(desde, hasta, orden, para);
            return Response.ok(resp).build();
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("salida", 9);
            resp.put("msj", "Error");
            return Response.ok(resp).build();
        }
    }

    @POST
    @Path("/getCanjesXPuntos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCanjesXPuntos(Map<String, String> parametros) throws URISyntaxException {
        JSONObject resp = new JSONObject();

        String tarjeta = parametros.get("tarjeta");
        String from = parametros.get("from");
        String to = parametros.get("to");
        String orderBy = parametros.get("orderBy");

        if (tarjeta == null || tarjeta.isEmpty() || from == null || from.isEmpty() || to == null || to.isEmpty() || orderBy == null || orderBy.isEmpty()) {
            resp.put("salida", 9);
            resp.put("msj", "No encuentra parametros de entrada (tarjeta - from - to - orderBy)");
            return Response.ok(resp).build();
        }
        int desde = Integer.parseInt(from);
        int hasta = Integer.parseInt(to);

        if (desde > hasta) {
            resp.put("salida", 8);
            resp.put("msj", "Error en parametros (from < to)");
            return Response.ok(resp).build();
        }

        String orden = "";
        String[] parts = orderBy.split(",");
        String var = parts[0];
        String order = parts[1];

        if (var.compareTo("codigo") == 0) {
            orden += "codigo_tarart";
        }

        if (var.compareTo("nombre") == 0) {
            orden += "descri_tarart";
        }

        if (var.compareTo("puntos") == 0) {
            orden += "puntos_tarart";
        }

        if (orden.isEmpty()) {
            resp.put("salida", 9);
            resp.put("msj", "Parametro de Orden Incorrecto (codigo, nombre, puntos)");
            return Response.ok(resp).build();
        }

        if (order.compareTo("asc") == 0) {
            orden += " asc";
        } else {
            orden += " desc";
        }

        try {
            ControlProducto cp = new ControlProducto();
            resp = cp.getProductosPuntos(tarjeta, desde, hasta, orden);
            return Response.ok(resp).build();
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("salida", 9);
            resp.put("msj", "Error");
            return Response.ok(resp).build();
        }
    }
}
