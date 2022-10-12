package com.xxxxxx.hotel.feedsearch.web.font;

import java.awt.*;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

public class FontTest {
    public static void main(String[] args) throws Exception {
        //Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("/home/zhangyu/project/captcha/font/方正何继云空心字 .TTF")).deriveFont(50f);
        Font customFont = new Font("微软雅黑", Font.ITALIC, 50);
        createImage("纤然",customFont, new File(

                "./a.png"), 200, 64);



    }

// 根据str,font的样式以及输出文件目录

    public static void createImage(String str, Font font, File outFile,

                                   Integer width, Integer height) throws Exception {
// 创建图片

        File file = new File("/home/zhangyu/project/captcha/click_select_bg/bg_27.jpg");

        BufferedImage image = ImageIO.read(file);

        Graphics2D g = (Graphics2D)image.getGraphics();

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);

        g.setComposite(ac);

//        g.setClip(0, 0, width, height);
//
//        g.setColor(Color.black);
//
//        g.fillRect(0, 0, width, height);// 先用黑色填充整张图片,也就是背景
//
//        g.setColor(Color.white);// 在换成黑色

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        g.setFont(font);// 设置画笔字体

        g.setColor(Color.BLACK);





        FontRenderContext frc = g.getFontRenderContext();

        TextLayout tl = new TextLayout(str, font, frc);

        FontMetrics fm = sun.font.FontDesignMetrics.getMetrics(font);


        Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(0, 50));


        g.draw(sha);// 画出字符串

        g.dispose();

        ImageIO.write(image, "png", outFile);// 输出png图片

    }

}
