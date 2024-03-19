package com.lastlight.utils;

import com.lastlight.common.RedisConstant;
import com.lastlight.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

@Component
public class CheckCodeUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //对外提供的功能有三个，分别把他们定义成方法，并且是public；
//生成验证码；
//输出验证码；
//对外提供验证码的正确答案；
//生成验证码(BufferedImage)是一个带缓冲区的图像类
    private int width=300;
    private int height=150;

    private String codes="23456789qwertyuiopkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM";//验证码字符库；
    private String []ziti= {"宋体","华文楷体","黑体","微软雅黑","楷体_GB2312"};
    //生成验证文本；
    public String text;
    //定义一个随机数生成器；
    private Random s=new Random();

    //生产随机字符；
    char randomchar() {
        int index=s.nextInt(codes.length());
        return codes.charAt(index);

    }

//获取随机颜色；

    private Color randomColor() {
//RGB三个颜色组合在一起；
        int red=s.nextInt(150);
        int green=s.nextInt(150);
        int blue=s.nextInt(150);
        return new Color(red,green,blue);

    }
//获取随机背景颜色；

    private Color raColor() {
//RGB三个颜色组合在一起；
        int rs=s.nextInt(151)+90;
        int se=s.nextInt(151)+90;
        int po=s.nextInt(151)+90;
        return new Color(rs,se,po);

    }

    private Color bgColor = raColor();//设置随机背景颜色

    private Font randomFont() {//生成随机字体的方法；
        int indx=s.nextInt(ziti.length);//字体名称；
        String zit =ziti[indx];//字体名称；
        int style=s.nextInt(4);
        int size=s.nextInt(10)+100;
        return new Font(zit,style,size);


    }

    // 画干扰线
    private void drawLine(BufferedImage image) {
        int num=6;//干扰线数量；
        Graphics2D g2=(Graphics2D) image.getGraphics();//得到画笔；

        for (int i = 0; i < num; i++) {
            int x1 = s.nextInt(width/2); // 起点 x 坐标
            int y1 = s.nextInt(height/2); // 起点 y 坐标
            int x2 = s.nextInt(width); // 终点 x 坐标
            int y2 = s.nextInt(height); // 终点 y 坐标
            g2.setStroke(new BasicStroke(2F));//设置线条特征，1.5f为线的宽度；
            g2.setColor(randomColor());//干扰线颜色；
            g2.drawLine(x1, y1, x2, y2); //画线；
        }
    }


    // 画噪点
    private void zapdian(BufferedImage image) {
        int num=700;//干扰噪点数量；

        Graphics2D g2=(Graphics2D) image.getGraphics();//得到画笔；

        for (int i = 0; i < num; i++) {
            int x1 = s.nextInt(width); // 起点 x 坐标
            int y1 = s.nextInt(width); // 起点 y 坐标
            g2.setStroke(new BasicStroke(2F));//设置线条特征，1.5f为线的宽度；
            g2.setColor(randomColor());//干噪点颜色；
            g2.drawOval(x1, y1, 2, 2); //画线；


        }
    }



    public BufferedImage createImage() {//绘制验证码

        System.out.println("createImage");
        BufferedImage image=new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);//TYPE_INT_BGR(red);
//填充颜色；
        Graphics2D g2=(Graphics2D) image.getGraphics();//获取对应这个图片的一只画笔；
        g2.setColor(bgColor);//让画笔取色；
        g2.fillRect(0,0,width,height);//填充一个矩型；
        g2.setColor(new Color(250, 0, 0));//在构造一个颜色；
        g2.drawRect(0, 0, width-1, height-1);//画一个矩形框；
        StringBuilder stringBuilders = new StringBuilder();

        for(int i=0;i<4;i++) {//循环四此
            String s=randomchar()+"";
            stringBuilders.append(s);
            g2.setColor(randomColor());//让画笔在取一个颜色；
            g2.setFont(randomFont());//设置一个字体
            int x=width/4*i;
            int y=height/2+20;
            g2.drawString(s, x, y);//向图片上写字
        }
        text= stringBuilders.toString();
        drawLine(image); // 绘制干扰线
        zapdian(image);//绘制噪点
        return image;
    }

    //输出验证码
//第一个参数内存中的图片
//第二个参数，输出的目标可以是文件，也可以是网页 OutputStream
    public void output(BufferedImage image, OutputStream out) throws IOException {

        System.out.println("output");
        ImageIO.write(image,"JPEG",out);//输出矩形图片，这个方法可能会出现异常，所以我也要声明一下，我的output方法也可能会出现IOException异常；
    }

//对外提供验证码的正确答案

    public String getText() {
        return text;
    }

    /**
     * 生成n位的数字随机验证码
     * @param n
     */
    public void createCode(int n){
        String res = new String();
        Random random = new Random();
        while(n-- > 0) {
            int num = random.nextInt(0, 10);
            res += num;
        }
        text = res;
    }

    public boolean checkCode(String code, String codeKey){
        try {
            String codeData = String.valueOf(redisTemplate.opsForValue().get(codeKey));
            if (codeData.equalsIgnoreCase(code)) {
                return true;
            }
            return false;
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new CustomException("校验验证码失败");
        }
    }
}