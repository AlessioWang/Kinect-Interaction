package Merge;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther Alessio
 * @date 2020/11/25
 **/
public class ImageAnalysis {
    PApplet applet;

    public ImageAnalysis(PApplet app) {
        this.applet = app;
    }

    public PImage blurImg(PImage img, int level) {
        img.filter(11, level);
        return img;
    }

    public List<Integer> getGrayInImg(PImage img) {
        List<Integer> grayList = new ArrayList<>();
        for (int i = 0; i < img.width; i++) {
            for (int j = 0; j < img.height; j++) {
                int index = i + j * img.width;
                int gray = img.pixels[index];
                grayList.add(gray);
            }
        }
        return grayList;
    }

    //得到一个等差数列,为之后的取相同位置的灰度做基础
    private List<Double> getPositionInArray(int num) {
        List<Double> doubleList = new ArrayList<>();
        double step = (double) 1 / num;
        for (double i = 0; i < 1; i += step) {
            doubleList.add(i);
        }
        return doubleList;
    }

    private List<Double> getGrayInList(PImage img, int num) {
        List<Double> grayValueInPosition = new ArrayList<>();   //指定位置的灰度值的list，需要return
        List<Integer> grayList = getGrayInImg(img);             //输入图片的灰度值的集合
        List<Double> indexList = getPositionInArray(num);        //index的list
        for (int i = 0; i < indexList.size(); i++) {
            int index = (int) (indexList.get(i) * img.pixels.length);
            grayList.get(index);
        }
        return grayValueInPosition;
    }

    //对比两张图片的灰度值来确定相似程度，num是灰度list等距取点的个数。取出来的对比点的灰度值在一定的范围的比例来确定图像是否变了
    //num取出来对比的个数、limit波动的范围、per相似的比例
    public boolean imgFlag(PImage img1, PImage img2, int num, int limit, double per) {
        boolean flag = false;
        int counter = 0;
        List<Double> grayList1 = getGrayInList(img1, num);
        List<Double> grayList2 = getGrayInList(img2, num);
        for (int i = 0; i < grayList1.size(); i++) {
            double gray1 = grayList1.get(i);
            double gray2 = grayList2.get(i);
            if (Math.abs(gray1 - gray2) <= limit) {
                counter++;
            }
        }
        if (counter <= (int) (per * num)) {
            flag = true;
        }
        return flag;
    }

}
