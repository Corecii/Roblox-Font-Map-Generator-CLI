import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;


class FontCharacter implements Comparable<FontCharacter>, Comparator<FontCharacter> {
	enum CompareMode {WIDTH, WIDTH_HEIGHT, HEIGHT, HEIGHT_WIDTH, BASELINE, CHARACTER};
	public static CompareMode compareMode = CompareMode.WIDTH_HEIGHT;
	public static int compareMulti = 1;
	char character;
	int width;
	int advance;
	int height;
	int baseline;
	int textXOffset;
	int x, y;

	public FontCharacter() {}

	public FontCharacter(char character, int width, int height, int baseline, int advance, int textXOffset) {
		this.character = character;
		this.width = width;
		this.height = height;
		this.baseline = baseline;
		this.advance = advance;
		this.textXOffset = textXOffset;
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static void setCompareMode(CompareMode mode) {
		compareMode = mode;
	}

	public static void setCompareMultiplier(int multi) {
		compareMulti = multi;
	}

	public int compare(FontCharacter f1, FontCharacter f2) {
		return f1.compareTo(f2);
	}

	public int compareTo(FontCharacter o) {
		switch (compareMode) {
			case WIDTH:
				return compareMulti*(width - o.width);
			case WIDTH_HEIGHT:
				return compareMulti*(width != o.width ? width - o.width : height - o.height);
			case HEIGHT:
				return compareMulti*(height - o.height);
			case HEIGHT_WIDTH:
				return compareMulti*(height != o.height ? height - o.height : width - o.width);
			case BASELINE:
				return compareMulti*(baseline - o.baseline);
			case CHARACTER:
				return compareMulti*(character - o.character);
		}
		return 0;
	}
}

class GeneratorResult {
	Font font;
	FontCharacter[] characters;
	BufferedImage image;
	int imageId;
	public GeneratorResult(Font font, FontCharacter[] characters, BufferedImage image) {
		this.font = font;
		this.characters = characters;
		this.image = image;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public String generateCombinedJSON(GeneratorResult[] results) {
		String str = "{\n"
			+ "\t\"uploadVersion\":2,\n"
			+ "\t\"name\":\"" + font.getFontName().replaceAll("\\W", "") + "\",\n"
			+ "\t\"size\":" + font.getSize() + ",\n"
			+ "\t\"data\":[";
		int i = 0;
		for (GeneratorResult result : results) {
			str +=
				(i++ == 0 ? "\n" : ",\n")
				+ "\t{\n"
					+ "\t\t\"image\":" + result.imageId + ",\n"
					+ "\t\t\"characters\":" + result.generateCharactersInnerJSON() + "\n"
				+ "\t}";
		}
		return str + "\n\t]\n}";
	}

	private String generateCharactersInnerJSON() {
		String str = "[\n";
		for (FontCharacter ch : characters) {
			str += "\t\t{\n";
			str += "\t\t\t\"characterByte\":" + (int) ch.character + ",\n";
			str += "\t\t\t\"width\":" + ch.width + ",\n";
			str += "\t\t\t\"height\":" + ch.height + ",\n";
			str += "\t\t\t\"baseline\":" + ch.baseline + ",\n";
			str += "\t\t\t\"xOffset\":" + ch.textXOffset + ",\n";
			str += "\t\t\t\"advance\":" + ch.advance + ",\n";
			str += "\t\t\t\"x\":" + ch.x + ",\n";
			str += "\t\t\t\"y\":" + ch.y + "\n";
			str += "\t\t},\n";
		}
		str = str.substring(0, str.length() - 2) + "\n";
		str += "\t]";
		return str;
	}

	public String generateJSON() {
		return generateJSON(0);
	}

	public String generateJSON(int imageId) {
		return "{\n"
				+ "\t\"image\":" + imageId +",\n"
				+ "\t\"uploadVersion\":2,\n"
				+ "\t\"name\":\"" + font.getFontName().replaceAll("\\W", "") + "\",\n"
				+ "\t\"size\":" + font.getSize() + ",\n"
				+ "\t\"characters\":" + generateCharactersInnerJSON() + "\n"
			+ "}";
	}
}

public class FontMapGenerator {
	private static String lastJSON;

	public static final int maxWidth = 1020;
	public static final int maxHeight = 1020;

	public static final int staticPadding = 2;

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

	public static GeneratorResult[] generateFontMapV2(String fontName, int style, int fontSize) throws Exception {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    Font[] fonts = e.getAllFonts();
	    Font font = null;
	    for (Font f : fonts) {
	    	if (f.getFontName().toLowerCase().replaceAll("\\W", "").equals(fontName.toLowerCase().replaceAll("\\W", "")))
	    		font = new Font(f.getFontName(), style, fontSize);
	    }
	    if (font == null)
	    	throw new Exception("Could not find the font `" + fontName + "` or any font matching `" + fontName.toLowerCase().replaceAll("\\W", "") + "`.");

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		BufferedImage image = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
		graphics.setColor(Color.WHITE);
		graphics.setFont(font);
		images.add(image);
		FontMetrics fontMetrics = graphics.getFontMetrics(font);
		ArrayList<FontCharacter> charsList = new ArrayList<FontCharacter>();

		for (int cc = 0; cc <  256; cc++) {
			char c = (char) cc;
			if (font.canDisplay(c)) {
				LineMetrics lineMetrics = font.getLineMetrics("" + c, fontMetrics.getFontRenderContext());
				Rectangle bounds = font.createGlyphVector(fontMetrics.getFontRenderContext(), "" + c).getPixelBounds(null, 0, 0);
				charsList.add(new FontCharacter(
					c,
					(int) Math.ceil(bounds.getWidth()),
					(int) Math.ceil(bounds.getHeight()),
					(int) -bounds.getY(),
					fontMetrics.stringWidth("" + c),
					(int) -bounds.getX()
				));
			}
		}

		FontCharacter[] chars = charsList.toArray(new FontCharacter[0]);

		ArrayList<FontCharacter> sortChars = new ArrayList<FontCharacter>();
		for (FontCharacter f : chars)
			sortChars.add(f);

		FontCharacter.setCompareMode(FontCharacter.CompareMode.WIDTH_HEIGHT); //sort by width then by height
		sortChars.sort(new FontCharacter());

		if (sortChars.size() > 0 && sortChars.get(sortChars.size() - 1).width > maxWidth - staticPadding && sortChars.get(sortChars.size() - 1).height > maxHeight - staticPadding)
			throw new Exception("The font `" + fontName + "` at size `" + fontSize + "` is too big to be exported.");

		ArrayList<GeneratorResult> results = new ArrayList<GeneratorResult>();
		ArrayList<FontCharacter> currentChars = new ArrayList<FontCharacter>();
		int currentX = 0;
		int currentY = staticPadding;
		int bigHeight = 0;
		for (int i = 0; i < sortChars.size(); i++) {
			FontCharacter c = sortChars.get(i);
			if (currentY + c.height + staticPadding > maxHeight) { //if we'd go past the image limit, make a new one
				results.add(new GeneratorResult(font, currentChars.toArray(new FontCharacter[0]), image));
				currentChars = new ArrayList<FontCharacter>();
				image = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
				graphics = (Graphics2D) image.getGraphics();
				graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
				graphics.setColor(Color.WHITE);
				graphics.setFont(font);
				images.add(image);
				currentX = 0;
				currentY = staticPadding;
				bigHeight = 0;
			} else if (currentX + c.width + staticPadding > maxWidth) { //this line can't hold anymore, go to the next
				currentX = 0;
				currentY += bigHeight + staticPadding;
				bigHeight = 0;
			}
			bigHeight = Math.max(bigHeight, c.height);
			currentX += staticPadding;
			graphics.drawString("" + c.character, currentX + c.textXOffset, currentY + c.baseline);
			c.setXY(currentX, currentY);
			currentChars.add(c);
			currentX += c.width;
		}

		FontCharacter.setCompareMode(FontCharacter.CompareMode.CHARACTER);
		currentChars.sort(new FontCharacter());

		results.add(new GeneratorResult(font, currentChars.toArray(new FontCharacter[0]), image));

		return results.toArray(new GeneratorResult[0]);
	}
}
