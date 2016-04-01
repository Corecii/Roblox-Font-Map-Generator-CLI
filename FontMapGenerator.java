/*
 * Decompiled with CFR 0_114.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

public class FontMapGenerator {
    private static String lastJSON;

    public static String arrayToString(int[] arrn) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrn.length; ++i) {
            stringBuffer.append(String.valueOf(arrn[i]));
            if (i >= arrn.length - 1) continue;
            stringBuffer.append(",");
        }
        return stringBuffer.toString();
    }

    public static String getLastJSONData() {
        return lastJSON;
    }

    public static BufferedImage generateFontMap(String string, int n) {
        int n2;
        int n3;
        Object object;
        char c;
        int n4 = 10;
        int n5 = 48;
        int n6 = 97;
        int n7 = 65;
        int[] arrn = new int[]{46, 44, 47, 63, 33, 58, 59, 39, 36, 37, 40, 41, 91, 93, 123, 125, 60, 62, 34, 64, 35, 94, 38, 42, 95, 45, 43, 61, 92, 124, 126, 96};
        int[] arrn2 = new int[26];
        int[] arrn3 = new int[26];
        int[] arrn4 = new int[10];
        int[] arrn5 = new int[arrn.length];
        Font font = new Font(string, 0, n);
        FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        int n8 = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
        int n9 = 0;
        int n10 = (n8 + n4) * 5;
        for (int i = 0; i < 26; ++i) {
            n9 += fontMetrics.charWidth(n7 + i);
        }
        BufferedImage bufferedImage = new BufferedImage(n9, n10, 2);
        Graphics2D graphics2D = (Graphics2D)bufferedImage.getGraphics();
        RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setRenderingHints(renderingHints);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(font);
        int n11 = 0;
        for (n3 = 0; n3 < 26; ++n3) {
            c = (char)(n7 + n3);
            graphics2D.drawString(String.valueOf(c), n11, fontMetrics.getMaxAscent() + n4);
            arrn2[n3] = n2 = fontMetrics.charWidth(c);
            n11 += n2;
        }
        n11 = 0;
        for (n3 = 0; n3 < 26; ++n3) {
            c = (char)(n6 + n3);
            graphics2D.drawString(String.valueOf(c), n11, (fontMetrics.getMaxAscent() + n4) * 2 + fontMetrics.getMaxDescent());
            arrn3[n3] = n2 = fontMetrics.charWidth(c);
            n11 += n2;
        }
        n11 = 0;
        for (n3 = 0; n3 < 10; ++n3) {
            c = (char)(n5 + n3);
            graphics2D.drawString(String.valueOf(c), n11, (fontMetrics.getMaxAscent() + n4) * 3 + fontMetrics.getMaxDescent() * 2);
            arrn4[n3] = n2 = fontMetrics.charWidth(c);
            n11 += n2;
        }
        n11 = 0;
        for (n3 = 0; n3 < arrn.length; ++n3) {
            c = (char)arrn[n3];
            graphics2D.drawString(String.valueOf(c), n11, (fontMetrics.getMaxAscent() + n4) * 4 + fontMetrics.getMaxDescent() * 3);
            arrn5[n3] = n2 = fontMetrics.charWidth(c);
            n11 += n2;
        }
        string = string.replaceAll("\\W", "");

        object =
        	"{\n"
        		+ "\t\"name\":\"" + string + "\",\n"
	        	+ "\t\"imageWidth\":" + bufferedImage.getWidth() + ",\n"
	        	+ "\t\"imageHeight\":" + bufferedImage.getHeight() + ",\n"
	        	+ "\t\"uploadVersion\":1,\n" //change this is ROBLOX changes how it scales images.
	        	+ "\t\"maxWidth\":1020,\n"   //quick hack to tell the Lua script how to scale the image
	        	+ "\t\"maxHeight\":1020,\n"  //update these when roblox's image size restrictions change
	        	+ "\t\"size\":" + n + ",\n"
	        	+ "\t\"padding\":" + n4 + ",\n"
	        	+ "\t\"height\":" + n8 + ",\n"
	        	+ "\t\"spaceWidth\":" + fontMetrics.charWidth(' ') + ",\n"
	        	+ "\t\"widths\":{\n"
	        		+ "\t\t\"upperAlpha\":[" + FontMapGenerator.arrayToString(arrn2) + "],\n"
	        		+ "\t\t\"lowerAlpha\":[" + FontMapGenerator.arrayToString(arrn3) + "],\n"
	        		+ "\t\t\"numerical\":[" + FontMapGenerator.arrayToString(arrn4) + "],\n"
	        		+ "\t\t\"punctuation\":[" + FontMapGenerator.arrayToString(arrn5) + "]\n"
	        	+ "\t}\n"
        	+ "}";

        lastJSON = (String) object;
        return bufferedImage;
    }
}
