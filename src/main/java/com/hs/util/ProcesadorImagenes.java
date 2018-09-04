/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Andres Lanzoni
 */
public class ProcesadorImagenes {
    /** Logger de la clase */
    // private static Logger logger =
    private Configuracion conf;
    
    /** Opciones de renderizado para las imagenes */
    private RenderingHints opciones = new RenderingHints(null);

    /** Constructor de la clase */
    public ProcesadorImagenes() {

        // Cargo las opciones de renderizado que me apetezcan
        opciones.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        opciones.put(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        opciones.put(RenderingHints.KEY_DITHERING,RenderingHints.VALUE_DITHER_DISABLE);
        opciones.put(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        opciones.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        opciones.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        opciones.put(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_NORMALIZE);
        opciones.put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Devuelve la lista de formatos disponibles a leer por ImageIO
     * 
     * @return un array de strings con los mismos.
     */
    public String[] dameFormatosUsables() {
        return ImageIO.getReaderFormatNames();
    }

    /**
     * Calcula el factor de escala minimo y en base a eso escala la imagen segun
     * dicho factor.
     * 
     * @param nMaxWidth
     *            maximo tamaño para el ancho
     * @param nMaxHeight
     *            nmaximo tamaño para el alto
     * @param imagen
     *            Imagen que vamos a escalar
     * @return Devuelve la imagen escalada para poderla trastocar o null si hay
     *         error
     */
    public BufferedImage escalarATamanyo(final BufferedImage imagen, final int maximoAncho, final int maximoAlto) {
        // Comprobacion de parametros
        if (imagen == null || maximoAlto == 0 || maximoAncho == 0) {
            return null;
        }

        // Capturo ancho y alto de la imagen
        int anchoImagen = imagen.getHeight();
        int altoImagen = imagen.getWidth();

        // Calculo la relacion entre anchos y altos de la imagen
        double escalaX = (double) maximoAncho / (double) anchoImagen;
        double escalaY = (double) maximoAlto / (double) altoImagen;

        // Tomo como referencia el minimo de las escalas
        double fEscala = Math.min(escalaX, escalaY);

        // Devuelvo el resultado de aplicar esa escala a la imagen
        return escalar(fEscala, imagen);
    }

    /**
     * Escala una imagen en porcentaje.
     * 
     * @param factorEscala
     *            ejemplo: factorEscala=0.6 (escala la imagen al 60%)
     * @param srcImg
     *            una imagen BufferedImage
     * @return un BufferedImage escalado
     */
    public BufferedImage escalar(final double factorEscala, final BufferedImage srcImg) {

        // Comprobacion de parametros
        if (srcImg == null) {
            return null;
        }

        // Compruebo escala nula
        if (factorEscala == 1) {
            return srcImg;
        }

        // La creo con esas opciones
        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(factorEscala, factorEscala), opciones);

        // Devuelve el resultado de aplicar el filro sobre la imagen
        return op.filter(srcImg, null);
    }

    /**
     * Metodo que guarda una imagen en disco
     * 
     * @param imagen
     *            Imagen a almacenar en disco
     * @param rutaFichero
     *            Ruta de la imagen donde vamos a salvar la imagen
     * @param formato
     *            Formato de la imagen al almacenarla en disco
     * @return Booleano indicando si se consiguio salvar con exito la imagen
     */
    /** Metodo que guarda una imagen en disco 
	 * @param imagen Imagen a almacenar en disco
	 * @param rutaFichero Ruta de la imagen donde vamos a salvar la imagen
	 * @param formato Formato de la imagen al almacenarla en disco
	 * @return Booleano indicando si se consiguio salvar con exito la imagen
	 */
	public boolean salvarImagen(final BufferedImage imagen, final String rutaFichero, final String formato) { 
            // Comprobacion de parametros
            if (imagen != null && rutaFichero != null && formato != null) { 
                try {
                    ImageIO.write( imagen, formato, new File( rutaFichero ));
                    return true;
                } catch (Exception e){
                    // Fallo al guardar				
                    conf.getLogger().log(Level.SEVERE, "Error al guardar imagen en : "+rutaFichero);
                }
                return false;
            }
            return false;
	}

    /**
     * Metodo principal de la clase. Usado como prueba
     * 
     * @param args
     *            Argumentos del metodo
     */
    public static void main(String args[]) {

        // Variables locales
        BufferedImage imagen;

        try {
            String name = "viagra";
            String extension = ".jpg";
            imagen = ImageIO.read(new File("D:/" + name + extension));
            ProcesadorImagenes pi = new ProcesadorImagenes();

            // Escalo algunas imagenes como pruebas
//            BufferedImage imagen800_600 = pi.escalarATamanyo(imagen, 800, 600);
            BufferedImage imagenSnap_Shot = pi.escalarATamanyo(imagen, 50, 50);
            BufferedImage imagenMediana = pi.escalarATamanyo(imagen, 100, 100);

            // Las salvo en disco
            
            //pi.salvarImagen(imagen800_600,"D:\\imagenG.jpg","jpg");
            pi.salvarImagen(imagenMediana, "D:/" + name + "_gde.jpg", "jpg");
            //pi.salvarImagen(imagenSnap_Shot,"D:\\imagenP.jpg","jpg");
            pi.salvarImagen(imagenSnap_Shot, "D:/" + name + "_peq.jpg", "jpg");
            //pi.salvarImagen(imagenMediana,"D:\\imagenA.gif","jpg");
            
            // Extraigo la lista de formatos capaces de leer
            String[] formatos = pi.dameFormatosUsables();

            // los voy mostrando
            for (int i = 0; i < formatos.length; i++) {
                System.out.println(formatos[i].toString());
            }

            // Final del metodo con exito
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
