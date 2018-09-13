/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hs.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Andres Lanzoni
 */
public class Test {
    
    public static BufferedImage scale(BufferedImage img, int targetWidth, int targetHeight) {

        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;

        int w = img.getWidth();
        int h = img.getHeight();

        int prevW = w;
        int prevH = h;

        do {
            if (w > targetWidth) {
                w /= 2;
                w = (w < targetWidth) ? targetWidth : w;
            }

            if (h > targetHeight) {
                h /= 2;
                h = (h < targetHeight) ? targetHeight : h;
            }

            if (scratchImage == null) {
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);

            prevW = w;
            prevH = h;
            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);

        if (g2 != null) {
            g2.dispose();
        }

        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

    return ret;

}
    
    public static void main(String[] arg) throws IOException {
//        try{
//            File image = new File("c:\\imagenes\\banderas_g.jpg");
//            File salida = new File("c:\\imagenes\\banderas_formateado.jpg");
//            BufferedImage img = ImageIO.read(image); // load image
//            BufferedImage thumbnail = Test.scale(img, 100, 100);
//            
//            //resize to 150 pixels max
////            BufferedImage thumbnail = Scalr.resize(img,
////                                       Scalr.Method.ULTRA_QUALITY,
////                                       Scalr.Mode.FIT_TO_WIDTH,
////                                       100,
////                                       Scalr.THRESHOLD_QUALITY_BALANCED);
//            ImageIO.write(thumbnail, "JPG", salida);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
            File folder = new File("c:\\imagenes\\");
	    File[] listOfFiles = folder.listFiles();
		System.out.println("Total No of Files:"+listOfFiles.length);
		Image img = null;
		BufferedImage tempPNG = null;
		BufferedImage tempJPG = null;
		File newFilePNG = null;
		File newFileJPG = null;
		for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		        img = ImageIO.read(new File("c:\\imagenes\\"+listOfFiles[i].getName()));
                        
                        double aspectRatio = (double) img.getWidth(null)/(double) img.getHeight(null);
                        tempPNG = resizeImage(img, 100, (int) (100/aspectRatio));
                        tempJPG = resizeImage(img, 100, (int) (100/aspectRatio));
                        
		        newFilePNG = new File("c:/imagenes/resize/"+listOfFiles[i].getName()+"_New.png");
		        newFileJPG = new File("c:/imagenes/resize/"+listOfFiles[i].getName()+"_New.jpg");
		        ImageIO.write(tempPNG, "png", newFilePNG);
		        ImageIO.write(tempJPG, "jpg", newFileJPG);
		      }
		}
		System.out.println("DONE");
	}

	/**
	 * This function resize the image file and returns the BufferedImage object that can be saved to file system.
	 */
	public static BufferedImage resizeImage(final Image image, int width, int height) {
            final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.setComposite(AlphaComposite.Src);
            //below three lines are for RenderingHints for better image quality at cost of higher processing time
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.drawImage(image, 0, 0, width, height, null);
            graphics2D.dispose();
            return bufferedImage;
        }
}
