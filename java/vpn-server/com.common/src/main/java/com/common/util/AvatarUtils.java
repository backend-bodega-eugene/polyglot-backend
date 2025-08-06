package com.common.util;

import com.common.exception.BizException;
import com.common.result.ResultEnum;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Random;

public class AvatarUtils {

    // 可以直接在<img/>标签或者浏览器地址栏预览的base64编码头
    public static final String BASE64_PREFIX = "data:image/png;base64,";

    /**
     * 生成头像的base64编码
     *
     * @param id
     * @return
     */
    public static String createBase64Avatar(int id) {
        try {
            return new String(Base64.getEncoder().encode(create(id)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException(ResultEnum.UNKNOWN_ERROR.getCode(), "生成头像失败");
        }
    }

    /**
     * 根据id生成一个头像，颜色随机。如果是使用hashCode()值的话，值可能为负数。需要要注意。
     *
     * @param id
     * @return
     * @throws IOException
     */
    public static byte[] create(int id) throws IOException {
        int width = 20;
        int grid = 5;
        int padding = width / 2;
        int size = width * grid + width;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D _2d = img.createGraphics();
        _2d.setColor(new Color(240, 240, 240));
        _2d.fillRect(0, 0, size, size);
        _2d.setColor(randomColor(80, 200));
        char[] idChars = createIdent(id);
        int i = idChars.length;
        for (int x = 0; x < Math.ceil(grid / 2.0); x++) {
            for (int y = 0; y < grid; y++) {
                if (idChars[--i] < 53) {
                    _2d.fillRect((padding + x * width), (padding + y * width), width, width);
                    if (x < Math.floor(grid / 2)) {
                        _2d.fillRect((padding + ((grid - 1) - x) * width), (padding + y * width), width, width);
                    }
                }
            }
        }
        _2d.dispose();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static Color randomColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(Math.abs(bc - fc));
        int g = fc + random.nextInt(Math.abs(bc - fc));
        int b = fc + random.nextInt(Math.abs(bc - fc));
        return new Color(r, g, b);
    }

    private static char[] createIdent(int id) {
        BigInteger bi_content = new BigInteger((id + "").getBytes());
        BigInteger bi = new BigInteger(id + "identicon" + id, 36);
        bi = bi.xor(bi_content);
        return bi.toString(10).toCharArray();
    }

    public static void main(String[] args) {
        String avatar = createBase64Avatar("0xA82F889E6581E30d7A9e38c61d3B680701230d89".hashCode());
        System.out.println(BASE64_PREFIX + avatar);
    }
}