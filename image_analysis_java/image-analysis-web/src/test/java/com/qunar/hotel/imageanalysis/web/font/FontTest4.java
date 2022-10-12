package com.xxxxxx.hotel.feedsearch.web.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;


import sun.font.FontDesignMetrics;

public class FontTest4 {



        //可选字体
        String[] fontTypes = {"Arial","Arial Black","AvantGarde Bk BT","Calibri","Times New Roman","宋体","黑体","Arial Unicode MS","Lucida Sans"};
        //随机字符的范围
        char[] codeSeq = {'图', '像', '数', '据', '国', '程'};
        //验证码长度
        private final int CODE_LENGTH = 1;
        //验证码的宽和高
        private int w =50;
        private int h = 50;
        Random random = new Random();

    public static void main(String[] args) throws IOException {
        FontTest4 t  = new FontTest4();
        t.createImage("./test4.jpg");
    }



        /**
         * 生成图像数据
         * */
        private void createImage(String outputPath) throws IOException {

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            //获取图片上下文
            Graphics g = image.getGraphics();
            Graphics2D g2 = (Graphics2D) g;
            /*
             * 生成背景
             */
            createBackground(g2);
            /*
             * 生成字符
             */
            String s = createCharacter(g2);

            g2.dispose();
            ImageIO.write(image, "JPEG", new FileOutputStream(outputPath));

        }
        /**
         * 生成随机颜色
         * @param fc 产生颜色值下限
         * @param bc 产生颜色值上限
         * @return 生成的随机颜色对象
         */
        private Color getRandColor(int fc,int bc) {
            int f = fc;
            int b = bc;
            if(f>255) {
                f=255;
            }
            if (f<1) {
                f=1;
            }
            if(b>255) {
                b=255;
            }
            if (b<1) {
                b=1;
            }
            return new Color(f+random.nextInt(b-f),f+random.nextInt(b-f),f+random.nextInt(b-f));
        }

        private void createBackground(Graphics2D g) {
            // 填充背景
            g.setColor(getRandColor(220,250));
            g.fillRect(0, 0, w, h);
            // 加入干扰线条
//            for (int i = 0; i < 7; i++) {
//                g.setColor(getRandColor(40,150));
//                int x = random.nextInt(w);
//                int y = random.nextInt(h);
//                int x1 = random.nextInt(w);
//                int y1 = random.nextInt(h);
//                g.drawLine(x, y, x1, y1);
//            }
        }

        //生成随机字符并且返回
        private String createCharacter(Graphics2D g) {
            StringBuilder s = new StringBuilder();
            int fontsize;  //生成字符的字体大小
            int charX = 0; //生成字符x的位置
            int chartY = h - 5; //生成字符y的位置
            Color color = g.getColor();
            //旋转或扭曲
            //int rotateOrWarp = random.nextInt(2);
            int rotateOrWarp = 1;
            for (int i = 0; i < CODE_LENGTH; i++) {
                String r = String.valueOf(codeSeq[random.nextInt(codeSeq.length)]);//random.nextInt(10));
                //随机设置字体颜色
                g.setColor(new Color(50 + random.nextInt(100), 50 + random.nextInt(100), 50 + random.nextInt(100)));
                //随机字体大小18-22
                fontsize = Math.abs(18+random.nextInt(4));
                Font font = new Font(fontTypes[random.nextInt(fontTypes.length)],Font.BOLD,fontsize);
                g.setFont(font);

                FontMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
                int charWidth = fontMetrics.stringWidth("M"); //当前随机生成字符的宽度
                int charsRealWidth = charWidth * CODE_LENGTH;
                //第一次循环的时候初始化
                if (i == 0) {
                    if(w > charsRealWidth){
                        charX = (w - charsRealWidth)/2;
                    }
                }
                if(rotateOrWarp == 0){
                    //画旋转文字
                    double radianPercent = 0D;
                    radianPercent =  Math.PI * (random.nextInt(40)/180D);
                    if(random.nextBoolean()) radianPercent = -radianPercent;
                    g.rotate(radianPercent, charX + 9, chartY);
                    g.drawString(r, charX, chartY);
                    g.rotate(-radianPercent, charX + 9, chartY);
                    charX += charWidth;
                }else{
                    g.drawString(r, charX, chartY);
                    charX += charWidth;
                }
                s.append(r);
            }
            if (rotateOrWarp == 1) {
                //扭曲
                shear(g, w, h, color);
            }
            return s.toString();
        }

        // 扭曲方法
        private void shear(Graphics g, int w1, int h1, Color color) {
            System.out.println("扭曲");
            shearX(g, w1, h1, color);
            shearY(g, w1, h1, color);
        }

        private void shearX(Graphics g, int w1, int h1, Color color) {
            int period = random.nextInt(2);

            boolean borderGap = true;
            int frames = 1;
            int phase = random.nextInt(1);

            for (int i = 0; i < h1; i++) {
                double d = (double) (period >> 1)
                        * Math.sin((double) i / (double) period
                        + (6.2831853071795862D * (double) phase)
                        / (double) frames);
                g.copyArea(0, i, w1, 1, (int) d, 0);
                if (borderGap) {
                   // g.setColor(color);
                    g.drawLine((int) d, i, 0, i);
                    g.drawLine((int) d + w1, i, w1, i);
                }
            }

        }

        private void shearY(Graphics g, int w1, int h1, Color color) {

            int period = random.nextInt(8)+8;

            boolean borderGap = true;
            int frames = 20;
            int phase = 7;
            for (int i = 0; i < w1; i++) {
                double d = (double) (period >> 1)
                        * Math.sin((double) i / (double) period
                        + (6.2831853071795862D * (double) phase)
                        / (double) frames);
                g.copyArea(i, 0, 1, h1, 0, (int) d);
                if (borderGap) {
                    //g.setColor(color);
                    g.drawLine(i, (int) d, i, 0);
                    g.drawLine(i, (int) d + h1, i, h1);
                }

            }

        }


}
