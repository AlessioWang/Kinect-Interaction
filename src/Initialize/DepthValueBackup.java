package Initialize;

import KinectPV2.KinectPV2;
import blobDetection.BlobDetection;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * @auther Alessio
 * @date 2020/11/22
 **/
public class DepthValueBackup extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Initialize.DepthValueBackup");
    }

    //Kinect参数变量
    KinectPV2 kinect;
    PeasyCam cam;
    PImage imgAfterAnalysis;  //要绘制的图案
    int[] depth;
    //kinect参数
    static final int MIN_DEPTH = 500;
    static final int MAX_DEPTH = 4500;
    static final int DEPTH_CAMERA_WIDTH = 512;
    static final int DEPTH_CAMERA_HEIGHT = 424;
    static final int RGB_CAMERA_WIDTH = 1920;
    static final int RGB_CAMERA_HEIGHT = 1080;
    //BlobDetection参数
    PImage inputImg;
    int levels = 20;                    // number of contours
    float factor = 1;                     // scale factor
    float elevation = 25;                 // total height of the 3d model
    float colorStart = 0;                // Starting degee of color range in HSB Mode (0-360)
    float colorRange = 255;              // color range / can also be negative
    BlobDetection[] theBlobDetection = new BlobDetection[levels];


    public void settings() {
        int w = (int) (2*DEPTH_CAMERA_WIDTH );
        int h = (int) (2*DEPTH_CAMERA_WIDTH );
        size(w, h);
    }

    public void setup() {
        cam = new PeasyCam(this, 400);
        kinect = new KinectPV2(this);
        kinect.enableDepthImg(true);
        kinect.enableColorImg(true);
        kinect.init();
        imgAfterAnalysis = createImage(DEPTH_CAMERA_WIDTH, DEPTH_CAMERA_HEIGHT, RGB);
    }

    public void draw() {
        background(0);
        PImage colorImage = kinect.getColorImage();
        PImage rawDepthImage = kinect.getDepthImage();
        //显示RGB摄像头图像
//        image(colorImage, DEPTH_CAMERA_WIDTH, 0, (float) (1920 * 0.5), (float) (1080 * 0.5));
        image(colorImage, DEPTH_CAMERA_WIDTH, 0, DEPTH_CAMERA_WIDTH, DEPTH_CAMERA_HEIGHT);
        //显示原始深度摄像头图像
        image(rawDepthImage, 0, DEPTH_CAMERA_HEIGHT, DEPTH_CAMERA_WIDTH, DEPTH_CAMERA_HEIGHT);
        imgAfterAnalysis.resize(DEPTH_CAMERA_WIDTH, DEPTH_CAMERA_HEIGHT);
        image(imgAfterAnalysis, 0, 0);
        depth = kinect.getRawDepthData();
        depthDisplayInGray(1, 50, 40, 40, 80);
    }

    //----------------------------------------------------------------------------------------------------------------------
    private int[] getWidthDisplayLimit(int min, int max) {
        return new int[]{min, max};
    }

    private int[] getHeightDisplayLimit(int min, int max) {
        return new int[]{min, max};
    }

    public void depthDisplayInHSB(int step, int x1, int x2, int y1, int y2) {
        imgAfterAnalysis.loadPixels();
        int[] xLimit = getWidthDisplayLimit(x1, DEPTH_CAMERA_WIDTH - x2);
        int[] yLimit = getHeightDisplayLimit(y1, DEPTH_CAMERA_HEIGHT - y2);
        pushStyle();
        colorMode(HSB);
        for (int x = xLimit[0]; x < xLimit[1]; x += step) {
            for (int y = yLimit[0]; y < yLimit[1]; y += step) {
                int index = x + y * DEPTH_CAMERA_WIDTH;
                int realDistance = depth[index];
                int colorValue = getColorFromDepth(realDistance, 10, 1200, 1500);
                imgAfterAnalysis.pixels[index] = color(colorValue, 255, 255);
            }
        }
        popStyle();
        imgAfterAnalysis.updatePixels();
    }

    public void depthDisplayInGray(int step, int x1, int x2, int y1, int y2) {
        imgAfterAnalysis.loadPixels();
        int[] xLimit = getWidthDisplayLimit(x1, DEPTH_CAMERA_WIDTH - x2);
        int[] yLimit = getHeightDisplayLimit(y1, DEPTH_CAMERA_HEIGHT - y2);
        for (int x = xLimit[0]; x < xLimit[1]; x += step) {
            for (int y = yLimit[0]; y < yLimit[1]; y += step) {
                int index = x + y * DEPTH_CAMERA_WIDTH;
                int realDistance = depth[index];
                int colorValue = getColorFromDepth(realDistance, 20, 1200, 1500);
                imgAfterAnalysis.pixels[index] = color(colorValue);
            }
        }
        imgAfterAnalysis.updatePixels();
    }

    public int getColorFromDepth(int realDistance, int depthStep, int depthMin, int depthMax) {
        if (realDistance > depthMax) {
            return 100;
        } else if (realDistance < MIN_DEPTH) {
            return 255;
        }
        int num = (int) (depthMax - depthMin) / depthStep;
        depthStep = (depthMax - depthMin) / num;
        int layer = (realDistance - depthMin) / depthStep;
        return (int) map(layer, 0, num, 0, 255);
    }

    public void mousePressed(){
        saveFrame("grayImg");
    }

}
