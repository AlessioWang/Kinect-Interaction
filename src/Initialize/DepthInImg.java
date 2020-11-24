package Initialize;

import KinectPV2.KinectPV2;
import org.openkinect.processing.Kinect;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * @auther Alessio
 * @date 2020/11/22
 **/
public class DepthInImg extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Initialize.DepthInImg");
    }

    KinectPV2 kinect;

    public void settings() {
        size(512, 424);
    }

    public void setup() {
        kinect = new KinectPV2(this);
        kinect.enableDepthImg(true);
        kinect.init();
    }

    public void draw() {
        background(0);
        PImage img = kinect.getDepthImage();
        image(img, 0, 0);
        int skip = 3;
        for (int x = 0; x < img.width; x += skip) {
            for (int y = 0; y < img.height; y += skip) {
                int index = x +y*img.width;
                float b = brightness(img.pixels[index]);
                fill(b);
                rect(x,y,skip,skip);
            }
        }
    }
}
