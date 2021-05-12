package util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ImageResizer {
    public static ImageIcon resizeImage(String location,int width, int height){
        ImageIcon imageIcon = new ImageIcon(location);
        Image image = imageIcon.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }
    public static ImageIcon resizeImage(URL imgLocation, int width, int height){
        if (imgLocation != null) {
            ImageIcon imageIcon = new ImageIcon(imgLocation);
            Image image = imageIcon.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        }else {
            return null;
        }
    }
}
