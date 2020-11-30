package Merge;

import KinectPV2.KinectPV2;
import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * @auther Alessio
 * @date 2020/11/22
 **/
public class Merge extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Merge.Merge");
    }

    //Kinect参数变量
    KinectPV2 kinect;
    PeasyCam cam;
    //    CameraState state;
    PImage imgAfterAnalysis;  //要绘制的图案
    PImage cutImage;
    int[] depth;
    int x1 = 100;
    int x2 = 60;
    int y1 = 40;
    int y2 = 100;
    //kinect参数
    static final int MIN_DEPTH = 500;
    static final int MAX_DEPTH = 4500;
    static final int DEPTH_CAMERA_WIDTH = 512;
    static final int DEPTH_CAMERA_HEIGHT = 424;
    static final int RGB_CAMERA_WIDTH = 1920;
    static final int RGB_CAMERA_HEIGHT = 1080;
    //BlobDetection参数
    PImage inputImg;
    int levels = 50;                    // number of contours
    float factor = 1;                     // scale factor
    float elevation = 100;                 // total height of the 3d model
    float colorStart = 0;                // Starting degee of color range in HSB Mode (0-360)
    float colorRange = 255;              // color range / can also be negative
    BlobDetection[] theBlobDetection = new BlobDetection[levels];
    //图像处理参数
    ImageAnalysis imgAly = new ImageAnalysis(this);
    int blurLevel = 5;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        frameRate(10);
        cam = new PeasyCam(this, 200);
        kinect = new KinectPV2(this);
        kinect.enableDepthImg(true);
        kinect.init();
        imgAfterAnalysis = createImage(DEPTH_CAMERA_WIDTH, DEPTH_CAMERA_HEIGHT, RGB);
        cutImage = createImage(DEPTH_CAMERA_WIDTH - x1 - x2, DEPTH_CAMERA_HEIGHT - y1 - y2, GRAY);
        inputImg = createImage(DEPTH_CAMERA_WIDTH - x1 - x2, DEPTH_CAMERA_HEIGHT - y1 - y2, GRAY);
        //初始化得到kinect的深度数据并给blob进行计算，深度摄像头第一次的初始值赋给inputImg
        depth = kinect.getRawDepthData();
        depthDisplayInGray(1, x1, x2, y1, y2);
        //获取初始的kinect深度灰度图
        pushStyle();
        colorMode(HSB, 360, 100, 100);
        getBlob();
        popStyle();
    }

    public void draw() {
        background(255);
        depth = kinect.getRawDepthData();
        //以键盘事件来刷新kinect图像，重新输入计算等高线
        updateDepthImgByKinect();
        translate(-inputImg.width * factor / 2, -inputImg.height * factor / 2);
        for (int i = 0; i < levels; i++) {
            translate(0, 0, elevation / levels);
            drawContours(i);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void updateDepthImgByKinect() {
        if (keyPressed && key == '1') {
            depthDisplayInGray(1, x1, x2, y1, y2);
            //模糊图像
            imgAfterAnalysis = imgAly.blurImg(imgAfterAnalysis, blurLevel);
            cutImage = getCutImg(imgAfterAnalysis,x1,x2,y1,y2);
            inputImg = cutImage;
            System.out.println(inputImg.width);
            getBlob();
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

    public void getBlob() {
        inputImg = imgAfterAnalysis;
        inputImg.loadPixels();
        cam = new PeasyCam(this, 200);
        colorMode(HSB, 360, 100, 100);
        for (int i = 0; i < levels; i++) {
            theBlobDetection[i] = new BlobDetection(inputImg.width, inputImg.height);
            theBlobDetection[i].setThreshold((float) i / levels);
            theBlobDetection[i].computeBlobs(inputImg.pixels);
        }
    }


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
//                int index = x + y * (xLimit[1]-xLimit[0]);
                int index = x + y * (DEPTH_CAMERA_WIDTH);
                int realDistance = depth[index];
                int colorValue = getColorFromDepth(realDistance, 20, 1200, 1500);
                imgAfterAnalysis.pixels[index] = color(colorValue);
            }
        }
        imgAfterAnalysis.updatePixels();
    }

    //将深度摄像头裁剪过的制定区域重新赋给小尺寸的图像
    public PImage getCutImg(PImage img, int x1, int x2, int y1, int y2) {
        int w = DEPTH_CAMERA_WIDTH - x1 - x2;
        int h = DEPTH_CAMERA_HEIGHT - y1 - y2;
        PImage cutImg = new PImage(w, h, GRAY);
        cutImg.loadPixels();
        for (int i = x1; i < DEPTH_CAMERA_WIDTH - x2; i++) {
            for (int j = y1; j < DEPTH_CAMERA_HEIGHT - y2; j++) {
                int tag = i + j * DEPTH_CAMERA_WIDTH;
                cutImg.pixels[(i - x1) + (j - y1) * w] = img.pixels[tag];
            }
        }
        cutImg.updatePixels();
        System.out.println("@@@");
        return cutImg;
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
        return (int) map(layer, 0, num, 255, 0);
    }

    public void mousePressed() {
        saveFrame("grayImg");
    }

}
