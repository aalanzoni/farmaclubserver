/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
/**
 *
 * @author Andres Lanzoni
 */
public class Mail extends Thread {
    private String remitente;
    private String password;
    private Stack<String> destinatarios;
    private String servidorSMTP;
    private String puertoSalida;
    private String autenticacion;
    private String tls;
    private String asunto;
    private String mensaje;
    private String adjunto;
    private String imagenes;
    private String debug;
    
    private String tarjeta; //reseteo
    private String pass;//reseteo
    
    
    private int tipo;
    
    private Configuracion conf;
    private Logger logger;
    
    
    public Mail(Stack<String> destinatarios, String asunto, String mensaje, String adjunto, String tarjeta, String pass,
            int tipo) {

        this.destinatarios = destinatarios;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.adjunto = adjunto;
        this.tipo = tipo;
        this.tarjeta = tarjeta;
        this.pass = pass;
        this.conf = Configuracion.getConfig();
    }
    
    private void enviarMensaje() throws Exception{
        Properties props = this.getParametros();
        Session mailSession = Session.getInstance(props, new SMTPAuthenticator(this.remitente, this.password, true));
        mailSession.setDebug(debug.equals("true"));
        Message mensaje = new MimeMessage(mailSession);
        try{ 
            // Emisor del mensaje
            mensaje.setFrom(new InternetAddress(this.remitente));
           
            for (int i = 0; i < this.destinatarios.size(); i++) {
                String cte = this.destinatarios.elementAt(i);
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(cte));
            }
            
            switch (this.tipo){
                case Constantes.MAIL_ERROR:
                    this.mailError(mensaje);
                    break;
                
                case Constantes.MAIL_BIENVENIDA:
                    this.mailBienvenida(mensaje);
                    break;
                
                case Constantes.MAIL_PASS_CHANG:
                    this.mailReseteoPass(mensaje, pass, tarjeta);
                    break;
            }
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void mailReseteoPass(Message mensaje, String pass, String tarjeta) throws Exception{
        mensaje.setSubject("FARMACLUB");
        Multipart multipart = new MimeMultipart("related");
        String linea = "Estimado Usuario: <br>";
        linea += "Se nos ha solicitado el reinicio de su clave para ";
        linea += "la tarjeta Nº " + tarjeta + ". <br> <br>";
        linea += "<b>Password:</b> " + pass + "<br> <br><br>";
        linea += "En el proximo inicio de sesion le solicitaremos que ingrese con su numero de Tarjeta de Socio y password Asignado.<br><br>";
        linea += "Lo saludamos Atte <b>FARMACLUB</b>";

        BodyPart texto = new MimeBodyPart();
        texto.setContent(linea,"text/html");
        multipart.addBodyPart(texto);
        
        mensaje.setContent(multipart);
        // Enviar el mensaje
        Transport.send(mensaje);
    }
    
    private void mailBienvenida(Message mensaje) throws Exception{
        mensaje.setSubject("Bienvenido a FARMACLUB");
        // Crear un Multipart de tipo multipart/related
        Multipart multipart = new MimeMultipart("related");

        // Leer el fichero HTML
        String fichero = "";
        String linea;
        
        BufferedReader br = new BufferedReader (new FileReader(this.mensaje));
        while ((linea = br.readLine()) != null)
            fichero += linea;

        br.close();
        
        // Rellenar el MimeBodyPart con el fichero e indicar que es un fichero HTML
        BodyPart texto = new MimeBodyPart();
        texto.setContent(fichero,"text/html");
        multipart.addBodyPart(texto);

        if(this.imagenes.isEmpty() == false){
            List<String> im = Arrays.asList(imagenes.split("\\s*;\\s*"));
            for (int i = 0; i < im.size(); i++) {
                String imagen = im.get(i);
                // Procesar la imagen
                MimeBodyPart imagenBP = new MimeBodyPart();
                imagenBP.attachFile(imagen);
                imagenBP.setHeader("Content-ID","<figura1>");
                multipart.addBodyPart(imagenBP);
            }
        }
        
        mensaje.setContent(multipart);
        try{
            // Enviar el mensaje
            Transport.send(mensaje);
        }
        catch(javax.mail.SendFailedException  mx){
            StringBuilder errorSB = null;
 
            if(mx.getInvalidAddresses() != null) {
                errorSB = new StringBuilder();
                for(Address email: mx.getInvalidAddresses()) {
                    errorSB.append(email.toString());
                    errorSB.append(", ");
                }
                conf.getLogger().log(Level.WARNING, "Invalid Address Found: "+ errorSB);
            }

            if(mx.getValidSentAddresses() != null) {
                errorSB = new StringBuilder();
                for(Address email: mx.getValidSentAddresses()) {
                    errorSB.append(email.toString());
                    errorSB.append(", ");
                }
                conf.getLogger().log(Level.WARNING, "Email sent to valid address: "+ errorSB);
            }

            if(mx.getValidUnsentAddresses() != null) {
                errorSB = new StringBuilder();
                for(Address email: mx.getValidUnsentAddresses()) {
                    errorSB.append(email.toString());
                    errorSB.append(", ");
                }
                conf.getLogger().log(Level.WARNING, "Email not sent to valid address: "+ errorSB);
            }
        }
        catch(javax.mail.MessagingException mx) { 
            conf.getLogger().log(Level.WARNING, "Email not sent: "+ mx.getMessage());
        } 
        catch (Exception ex) { 
            conf.getLogger().log(Level.WARNING, "Email not sent: "+ ex.getMessage());
        }
    }
    
    private void mailError(Message mensaje) throws Exception{
        mensaje.setSubject("Error en Servidor");

        // Crear un Multipart de tipo multipart/related
        Multipart multipart = new MimeMultipart("related");

        // Rellenar el MimeBodyPart con el fichero e indicar que es un fichero HTML
        BodyPart texto = new MimeBodyPart();
        texto.setContent(this.mensaje + "<br><br>" + "Verificar LOG adjunto","text/html");
        multipart.addBodyPart(texto);

        this.adjunto = this.conf.getLog();
        if(this.adjunto != null && this.adjunto.length() > 0){
            BodyPart adjuntoBP = new MimeBodyPart();
            adjuntoBP.setDataHandler(new DataHandler(new FileDataSource(this.adjunto)));            
            adjuntoBP.setFileName("server.log");
            multipart.addBodyPart(adjuntoBP);
        }
        mensaje.setContent(multipart);
        // Enviar el mensaje
        Transport.send(mensaje);
    }
   

    private Properties getParametros(){
        Properties props = System.getProperties();
        try{
            //\restfull-web-services-app-master\src\main\resources\mail.properties
            String resourceName = "mail.properties";
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties propiedades = new Properties();
            InputStream resourceStream = loader.getResourceAsStream(resourceName);
            propiedades.load(resourceStream);

            this.logger = Logger.getLogger(Mail.class.getName());

            remitente = propiedades.get("remitente").toString();
            password = propiedades.get("password").toString();
            try{
                password = Crypto.Desencriptar(password);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            servidorSMTP = propiedades.get("servidorSMTP").toString();
            puertoSalida = propiedades.get("puertoSalida").toString();
            autenticacion = propiedades.get("autenticacion").toString();
            tls = propiedades.get("starttls").toString();
            imagenes = propiedades.get("mail_imagenes").toString();
            debug = propiedades.get("debug").toString();

            if(this.tipo == Constantes.MAIL_ERROR){
                this.destinatarios = new Stack<String>();
                String casillas = propiedades.get("error_mail").toString();
                System.out.println("Casillas: "+casillas);
                List<String> cuentas = Arrays.asList(casillas.split("\\s*;\\s*"));
                for (Iterator<String> iterator = cuentas.iterator(); iterator.hasNext();) {
                    String next = iterator.next();
                    this.destinatarios.add(next);                    
                }
                System.out.println("Destinatarios: "+destinatarios.toString());
            }
            
            if(this.tipo == Constantes.MAIL_BIENVENIDA){
                this.mensaje = propiedades.get("msj_bienvenida").toString();
            }
            props.put("mail.smtp.host", this.servidorSMTP);        // El servidor SMTP de Google    
            props.put("mail.smtp.user", this.remitente);           // Nombre del usuario
            props.put("mail.smtp.clave", this.password);           // La clave de la cuenta
            props.put("mail.smtp.auth", this.autenticacion);       // Usar autenticación mediante usuario y clave
            props.put("mail.smtp.starttls.enable", this.tls);      // Para conectar de manera segura al servidor SMTP
            props.put("mail.smtp.port", this.puertoSalida);        // El puerto SMTP seguro de Google
        }
        catch(Exception ioe){
            ioe.printStackTrace();
            this.logger.log(Level.SEVERE, "Error cargando preferencias Mail", ioe);
        }
        return props;
    }

    @Override
    public void run() {
        try {
            this.enviarMensaje();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) {
        //destinatarios.add("aalanzoni@gmail.com");
        String asunto = "ERROR EN SERVIDOR";
        Stack des = new Stack();
        des.add("aalanzoni@gmail.com");
        
        
        
        String mensaje = "Se ha producido un error,<br><br> <b>VERIFICAR EL LOG ADJUNTO</b>";
        try {
            //JOptionPane.showMessageDialog(null, "Antes de llamarlo", "INFO", JOptionPane.INFORMATION_MESSAGE);
            //Stack<String> destinatarios, String asunto, String mensaje, String adjunto, int tipo
            Stack<String> destinatarios = new Stack<String>();
//            destinatarios.add("aalanzoni@gmail.com");
            //destinatarios.add("jeceiza@outlook.com");
            //destinatarios.add("hellsing952@gmail.com");
            
            Thread hilo = new Thread(new Mail(destinatarios, asunto, mensaje, "", "123", "1234", Constantes.MAIL_ERROR));
            hilo.start();
            
            System.out.println("Sigue el otro hilo");
            int i = 0;
            while(hilo.isAlive()){
                i ++;
                System.out.println("Esperando: " + i + " segundos");
                sleep(1000);
            }
            
            System.out.println("Mails enviado");
            System.exit(0);
            
            //System.exit(0);

            //JOptionPane.showMessageDialog(null, "El hilo sigue", "INFO", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
