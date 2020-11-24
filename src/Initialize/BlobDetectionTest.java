package Initialize;

import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * @auther Alessio
 * @date 2020/11/24
 **/
public class BlobDetectionTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Initialize.BlobDetectionTest");
    }

    PImage inputImg;
    PeasyCam cam;
    int levels = 20;                    // number of contours
    float factor = 1;                     // scale factor
    float elevation = 25;                 // total height of the 3d model
    float colorStart = 0;                // Starting degee of color range in HSB Mode (0-360)
    float colorRange = 255;              // color range / can also be negative
    BlobDetection[] theBlobDetection = new BlobDetection[levels];

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        inputImg = loadImage("E:\\INST.AAA\\Term-1\\Kinect&Sand\\KinectProject\\grayImgMohu.jpg");
        inputImg.loadPixels();
        cam = new PeasyCam(this, 200);
        colorMode(HSB, 360, 100, 100);
        for (int i = 0; i < levels; i++) {
            theBlobDetection[i] = new BlobDetection(inputImg.width, inputImg.height);
            theBlobDetection[i].setThreshold((float) i / levels);
            theBlobDetection[i].computeBlobs(inputImg.pixels);
        }
        System.out.println(levels);
//        BlobDetection blob = new BlobDetection(img.width,img.height);
//        blob.setThreshold(0.5f);
//        blob.computeBlobs(img.pixels);
//        System.out.println(blob.getBlobNb());
    }

    public void draw() {
        background(0);
        translate(-inputImg.width * factor / 2, -inputImg.height * factor / 2);
        for (int i = 0; i < levels; i++) {
            translate(0, 0, elevation / levels);
            drawContours(i);
        }
    }

    public void drawContours(int i) {
        Blob b;
        EdgeVertex eA, eB;
        for (int n = 0; n < theBlobDetection[i].getBlobNb(); n++) {
            b = theBlobDetection[i].getBlob(n);
            if (b != null) {
                stroke((i / levels * colorRange) + colorStart, 100, 100);
                for (int m = 0; m < b.getEdgeNb(); m++) {
                    eA = b.getEdgeVertexA(m);
                    eB = b.getEdgeVertexB(m);
                    if (eA != null && eB != null)
                        line(
                                eA.x * inputImg.width * factor, eA.y * inputImg.height * factor,
                                eB.x * inputImg.width * factor, eB.y * inputImg.height * factor
                        );
                }
            }
        }
    }
}
