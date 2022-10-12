package com.xxxxxx.hotel.feedsearch.web.font;

import java.awt.*;

import java.awt.font.FontRenderContext;

import java.awt.font.TextLayout;

import java.awt.geom.AffineTransform;

import java.awt.image.BufferedImage;

import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;



import javax.imageio.ImageIO;



public class FontTest2 {


    public static void main(String[] args) throws IOException, FontFormatException {

//        Font[] fonts = GraphicsEnvironment
//                .getLocalGraphicsEnvironment()
//                .getAllFonts();
//        for (Font font : fonts) {
//            System.out.println(font.getFontName());
//        }


        String inputImagePath = "/home/zhangyu/project/captcha/click_select_bg/bg_27.jpg";

        String outputPath = "./hb1.png";

        String text = "滑";

        System.err.println(text.length());

        addTextToImage(inputImagePath, outputPath, text, new Color(231, 65, 51));

    }



    public static void addTextToImage(String inputImagePath,String outputPath,String text,Color color) throws IOException, FontFormatException {
        File file = new File(inputImagePath);

        Image image = ImageIO.read(file);

        BufferedImage bi = new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_ARGB);

        System.err.println(bi.getWidth());

        float alpha = 1F;

        Graphics2D g2 = bi.createGraphics();

        //高清代码

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);

        g2.setComposite(ac);

        g2.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);

        g2.setColor(color);

        g2.setBackground(Color.BLACK);

        Font font = new Font("微软雅黑",Font.BOLD,50);
        //Font font = Font.createFont(Font.TRUETYPE_FONT, new File("/home/zhangyu/project/font/字魂44号-空心雅黑/字魂44号-空心雅黑.ttf")).deriveFont(50f);

        g2.setFont(font);

        FontRenderContext frc = g2.getFontRenderContext();

        TextLayout tl = new TextLayout(text, font, frc);

        FontMetrics fm = sun.font.FontDesignMetrics.getMetrics(font);

        int stringWidth = fm.stringWidth(text);

        //Shape sha = tl.getOutline(AffineTransform.getTranslateInstance((bi.getWidth()-stringWidth)/2,115));
        Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(0, 50));

        //字体色

        g2.setColor(Color.BLACK);

        //g2.rotate(-30, 0, 0);

        //g2.drawString(text, 0, 50);
        g2.draw(sha);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,0.1f));

        //描边色

        g2.setColor(Color.gray);

        g2.fill(sha);



        ImageIO.write(bi, "PNG", new FileOutputStream(outputPath));

    }

}
