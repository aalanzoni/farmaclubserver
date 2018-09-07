package com.hs.services;

import com.hs.control.ControlUsuario;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.hs.model.User;
import com.hs.util.Configuracion;
import com.hs.util.Constantes;
import com.hs.util.Mail;
import java.io.File;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import org.json.simple.JSONObject;

/**
 * UserService represent a Jersey resources that will be
 * manipulated by different HTTP methods.
 *
 * @author Andres Lanzoni
 *
 */
//http://localhost:8080/restfull-web-services-app-master/rest
@Path("/user")
public class UserService {

	/**
	 * Method handling HTTP GET requests, returned object will be
	 * sent to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/stub")
        //public Response updateUser(@PathParam("id") final String id, final User user)  throws URISyntaxException
	public String getStub() {
            return "lalala";
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/all")
	public Response getAllUsers(@QueryParam("page") @DefaultValue("1") final String page) {
		// # See all the users in the system
		// > curl -X GET http://localhost:8080/RESTfulApp/rest/user/all?page=3
		List<User> users = new ArrayList<User>();
		final User user1 = new User("1", "Tom", "Jenkins", "tom.jenkins@gmail.com");
		final User user2 = new User("2", "Red", "Balloon", "red.balloon@gmail.com");
                final User user3 = new User("3", "Red", "Balloon", "red.balloon@gmail.com");
                final User user4 = new User("4", "Red", "Balloon", "red.balloon@gmail.com");
                final User user5 = new User("5", "Red", "Balloon", "red.balloon@gmail.com");

		users.add(user1);
		users.add(user2);
                users.add(user3);
                users.add(user4);
                users.add(user5);

	    return Response.ok(users).build();
	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(final User user) throws URISyntaxException {
		// # Create and save new user
		// > curl -X POST http://localhost:8080/RESTfulApp/rest/user/create
		return Response.ok(user).build();
	}

//        @GET
//        @Path("/existeTarjeta")
//        @Consumes(MediaType.TEXT_PLAIN)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response existeTarjeta(String tarjeta) throws URISyntaxException {
//            JSONObject resp = new JSONObject();
//            if(tarjeta.length() > 1){
//                try{
//                    resp = ControlUsuario.existeTarjeta(tarjeta);
//                }
//                catch(Exception e){
//                    resp = new JSONObject();
//                    resp.put("salida", 9);
//                    resp.put("msj", e.toString());
//                    e.printStackTrace();
//                }
//            }
//            return Response.ok(resp).build();
//        }

        //http://localhost:8080/restfull-web-services-app-master/rest/user/valida
        @POST
        @Path("/valida")
        @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validaUsuario(Map<String, String> parametros) throws URISyntaxException {

            JSONObject resp = new JSONObject();

            if(!parametros.containsKey("usuario") || !parametros.containsKey("pass")){

                resp.put("salida", 9);
                resp.put("tarjeta", "");
                resp.put("nombre", "");
                resp.put("puntos", 0);
                resp.put("msj", "No encuentra parametros de entrada (Usuario - Pass)");

                return Response.ok(resp).build();
            }else{
                String usuario = parametros.get("usuario");
                String pass = parametros.get("pass");
                try{
                    resp = ControlUsuario.validaUsuario(usuario, pass, false);

                    if(resp.get("salida").toString().compareTo("1") == 0){

                        String tarjeta = resp.get("tarjeta").toString();

                        int puntos = 0;
                        int aux = Integer.parseInt(resp.get("salida").toString());
                        if(aux == 1)//Usuario validado
                            puntos = ControlUsuario.getPuntos(tarjeta);

                        resp.put("puntos", puntos);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    resp.clear();
                    resp = new JSONObject();
                    resp.put("salida", 9);
                    resp.put("tarjeta", "");
                    resp.put("nombre", "");
                    resp.put("puntos", 0);
                    resp.put("msj", "Salida: " + e.toString());
                }
            }

            return Response.ok(resp).build();
        }

        //http://localhost:8080/restfull-web-services-app-master/rest/user/validaIni
        @POST
        @Path("/validaIni")
        @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
        /**
         * Valida sobre nro de tarjeta y pass que es dado por la Farmacia.
         */
	public Response validaUsuarioIni(Map<String, String> parametros) throws Exception {

            JSONObject resp = new JSONObject();

            if(!parametros.containsKey("tarjeta") || !parametros.containsKey("pass")){

                resp.put("salida", 9);
                resp.put("tarjeta", "");
                resp.put("nombre", "");
                resp.put("puntos", 0);
                resp.put("msj", "No encuentra parametros de entrada (Tarjeta - Pass)");

                return Response.ok(resp).build();
            }else{
                String tarjeta = parametros.get("tarjeta");
                String pass = parametros.get("pass");
                try{
                    resp = ControlUsuario.validaUsuario(tarjeta, pass, true);
                    int puntos = 0;
                    int aux = Integer.parseInt(resp.get("salida").toString());
                    if(aux == 1)//Usuario validado
                        puntos = ControlUsuario.getPuntos(tarjeta);

                    resp.put("puntos", puntos);

//                  Enviamos el Mail
                    String correo = resp.get("correo").toString();
                    if(correo != null && !correo.trim().isEmpty()){
                        Configuracion config = Configuracion.getConfig();
                        config.getLogger().log(Level.INFO, "Mail de Bienvenida a Tarjeta: " + tarjeta +" Correo: " + correo);

                        Stack<String> destinatarios = new Stack<String>();
                        destinatarios.add(correo);

                        Thread hilo = new Thread(new Mail(destinatarios, null, null, null, tarjeta, pass, Constantes.MAIL_BIENVENIDA));
                        hilo.start();
                    }
                }
                catch(Exception e){
                    resp.clear();
                    resp = new JSONObject();
                    resp.put("salida", 9);
                    resp.put("tarjeta", "");
                    resp.put("nombre", "");
                    resp.put("puntos", 0);
                    resp.put("msj", "Salida: " + e.toString());
                }
            }

            return Response.ok(resp).build();
        }
        //http://localhost:8080/restfull-web-services-app-master/rest/user/licencia
        @GET
        @Path("/licencia")
	@Produces("application/pdf")
        public Response getLicencia(){
            String file_licencia = Configuracion.getConfig().getLicencia();
            Response.ResponseBuilder response;
            if(file_licencia.isEmpty() == false){
                File file = new File(file_licencia);
                if(file.exists()){
                    response = Response.ok((Object) file);
                    response.header("Content-Disposition",
				"attachment; filename=licencia.pdf");
                    return response.build();
                }
            }
            response = Response.ok((Object) null);
            return response.build();
        }

        //http://localhost:8080/restfull-web-services-app-master/rest/user/existeusu
        @POST
        @Path("/existeusu")
        @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response existeUsuario(Map<String, String> parametros) throws Exception {
            JSONObject resp = new JSONObject();
            if(!parametros.containsKey("usuario")){
                resp.put("salida", 9);
                resp.put("msj", "No encuentra parametros de entrada (usuario)");
            }
            else{
                try{
                    String usu = parametros.get("usuario");
                    if(ControlUsuario.existeUsuario(usu, null)){
                        resp.put("salida", 0);
                        resp.put("msj", "Ya Existe el usuario en DB");
                    }else{
                        resp.put("salida", 1);
                        resp.put("msj", "NO Existe el usuario en DB");
                    }
                }
                catch(Exception e){
                    resp.put("salida", 9);
                    resp.put("msj", "Salida: " + e.toString());
                }
            }
            return Response.ok(resp).build();
        }

        //http://localhost:8080/restfull-web-services-app-master/rest/user/actualiza
        @POST
        @Path("/actualiza")
        @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response actualizaUsuario(Map<String, String> parametros) throws Exception {

            JSONObject resp = new JSONObject();

            if(!parametros.containsKey("tarjeta") || !parametros.containsKey("usuario") || !parametros.containsKey("pass")){
                resp.put("salida", 9);
                resp.put("tarjeta", "");
                resp.put("nombre", "");
                resp.put("msj", "No encuentra parametros de entrada (Tarjeta - Usuario - Pass)");

                return Response.ok(resp).build();
            }else{
                try{
                    resp = ControlUsuario.updateUsuario(parametros);
                }
                catch(Exception e){}//ignore
            }

            return Response.ok(resp).build();
        }

        @POST
        @Path("/existe_tarjeta")
        @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response existeTarjeta(Map<String, String> parametros) throws Exception {

            JSONObject resp = new JSONObject();

            if(!parametros.containsKey("tarjeta")){
                resp.put("existe", 0);
                resp.put("usuario", "");
                resp.put("salida", 9);
                resp.put("msj", "No encuentra parametros de entrada (Tarjeta)");

                return Response.ok(resp).build();
            }else{
                try{
                    String tarjeta = parametros.get("tarjeta");
                    resp = ControlUsuario.existeTarjeta(tarjeta);
                }
                catch(Exception e){}//ignore
            }

            return Response.ok(resp).build();
        }


        @POST
        @Path("/reset_pass")
        @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetPass(Map<String, String> parametros) throws Exception {

            JSONObject resp = new JSONObject();

            if(!parametros.containsKey("tarjeta")){
                resp.put("salida", 9);
                resp.put("pass", "");
                resp.put("msj", "No encuentra parametros de entrada (Tarjeta)");
                resp.put("mail", "");
                resp.put("reiniciar", 0);
                System.out.println("Falta Parametro tarjeta!!!");

                return Response.ok(resp).build();
            }else{
                try{
                    String tarjeta = parametros.get("tarjeta");
                    resp = ControlUsuario.resetPass(tarjeta);
                }
                catch(Exception e){}//ignore
            }

            return Response.ok(resp).build();
        }

        @GET
        @Path("/u")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validaUsuarioU() throws URISyntaxException {

            JSONObject resp = new JSONObject();

            resp.put("salida", 1);
            resp.put("tarjeta", "12345678");
            resp.put("nombre", "Jonathan Caverna");
            resp.put("puntos", -100);
            resp.put("msj", "OK Nigga");

            return Response.ok(resp).build();

        }




//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/id/{id}")
//	public Response getUserById(@PathParam("id") final String id) throws URISyntaxException {
//		// # Get the information of the user
//		// > curl -X GET http://localhost:8080/RESTfulApp/rest/user/id/12345
//		final User user = new User("12345", "Max", "Liano", "maxliano@gmail.com");
//		return Response.ok(user).build();
//	}

	// POST is used create a new resource, PUT method updates the state of a known resource.
	@PUT
	@Path("/id/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("id") final String id, final User user)  throws URISyntaxException {
		// # Update user information
		// > curl -X PUT http://localhost:8080/RESTfulApp/rest/user/id/12345
		return Response.ok(user).build();
	}

	@DELETE
	@Path("/id/{id}")
	public Response deleteUser(@PathParam("id") final String id) throws URISyntaxException {
		// create a new ResponseBuilder with an OK status and appropriate a Response instance
		// Delete user
		// > curl -X DELETE http://localhost:8080/RESTfulApp/rest/user/id/12345
		return Response.ok().entity("User with ".concat(id).concat(" is deleted successfully")).build();
	}
}