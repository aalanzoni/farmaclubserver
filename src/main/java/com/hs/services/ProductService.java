/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.services;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public Response getCanjes(@Context UriInfo info) {
        JSONObject resp = new JSONObject();
        String from = info.getQueryParameters().getFirst("from");
        String to = info.getQueryParameters().getFirst("to");
        List<String> orderBy = info.getQueryParameters().get("orderBy");
        resp.put("from", from);
        resp.put("to", to);
        resp.put("orderby", orderBy.toString());
        return Response.ok(resp).build();
//        return Response
//               .status(200)
//               .entity("getCanjes is called, from : " + from + ", to : " + to
//                    + ", orderBy" + orderBy.toString()).build();
    }
}
