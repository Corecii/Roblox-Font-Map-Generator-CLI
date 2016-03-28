import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;


public class CmdController {
	static String[][] help = {
			{"--upload\n-u","\n\tWhether or not to upload the images to roblox, 1 or true","\n\t-u false\n"},
			{"--roblosecurity\n-r","\n\tYour .ROBLOSECURITY cookie that's used to upload images to your account\n\tThis is required if you set -u or --upload","\n\t-r COOKIEGOESHERE\n"},
			{"--local\n-l","\n\tWhether or not to save to a file","\n\t-f true\n"},
			{"--font\n-f","\n\tThe name of the font to generate for","\n\t-f Arial","\n\t-f \"Times New Roman\"\n"},
			{"--size\n-s","\n\tTakes up to three arguments: minSize, maxSize, and iterationLength","\n\t-s 16","\n\t-s 16 20","\n\t-s 10 40 10\n"},
			{"--image\n-i","\n\tThe name of the image file. FONT, SIZE, and LOC have replacements","\n\t-i FONT-SIZE-LOC.png","\n\t-i FONT-image-SIZE.png\n"},
			{"--json\n-j","\n\tThe name of the json file. Same replacements as image","\n\t-j FONT-SIZE-LOC.json","\n\t-i FONT-image-SIZE.json\n"},
			{"--table\n-t","\n\tWhether or not to print a lua table of the names, sizes, decal ids, and their image ids to the output","\n\tOnly works in upload mode.","\n\t-l true\n"},
			{"--wait\n-w","\n\tThe amount of time to wait between each generation/upload. Suggested 2 for uploads.","\n\t-w 0","\n\t-w 5\n"}
	};
	public static void main(String[] argsR) throws IOException, InterruptedException {
		boolean isUpload = false;
		boolean isLocal = true;
		String fontName = "Arial";
		String roblosecurity = null;
		int sz0 = 16;
		int sz1 = 16;
		int itr = 1;
		int waitTime = 0;
		String fontImagName = "FONT-LOC-SIZE.png";
		String fontJsonName = "FONT-LOC-SIZE.json";
		boolean outputLua = true;
		String next = "";
		int num = 0;
		for (String str : argsR) {
			switch (str) {
				case "--upload":
				case "-u":
					next = "u";
					num = 0;
					break;
				case "--roblosecurity":
				case "-r":
					next = "r";
					num = 0;
					break;
				case "--local":
				case "-l":
					next = "l";
					num = 0;
					break;
				case "--font":
				case "-f":
					next = "f";
					num = 0;
					break;
				case "--size":
				case "-s":
					next = "s";
					num = 0;
					break;
				case "--image":
				case "-i":
					next = "i";
					num = 0;
					break;
				case "--json":
				case "-j":
					next = "j";
					num = 0;
					break;
				case "--table":
				case "--t":
					next = "t";
					num = 0;
					break;
				case "--wait":
				case "-w":
					next = "w";
					num = 0;
					break;
			}
			if (num != 0) {
				switch (next) {
					case "u":
						isUpload = str.equals("1") || str.toLowerCase().equals("true");
						break;
					case "r":
						roblosecurity = str;
						break;
					case "l":
						isLocal = str.equals("1") || str.toLowerCase().equals("true");
						break;
					case "f":
						fontName = str;
					case "s":
						switch (num) {
							case 1:
								sz0 = Integer.parseInt(str);
								sz1 = sz0;
								break;
							case 2:
								sz1 = Integer.parseInt(str);
								break;
							case 3:
								itr = Integer.parseInt(str);
								break;
						}
						break;
					case "i":
						fontImagName = str;
						break;
					case "j":
						fontJsonName = str;
					case "t":
						outputLua = str.equals("1") || str.toLowerCase().equals("true");
					case "w":
						waitTime = Integer.parseInt(str);
				}
			}
			num++;
		}
		if (argsR.length == 0) {
			System.out.println("Args:");
			for (String[] strs : help)
				for (String str : strs)
					System.out.print(str);
			return;
		}
		int[] sizes;
		{
			sizes = new int[Math.max(sz0, sz1) - Math.min(sz0, sz1) + 1];
			int loc = 0;
			for (int i = Math.min(sz0, sz1); i < Math.max(sz0, sz1); i+=itr) {
				sizes[loc++] = i;
			}
			sizes[loc] = Math.max(sz0,  sz1);
		}
		String token = null;
		if (isUpload)
			if (roblosecurity != null) {
				token = Uploader.getToken(roblosecurity);
			} else {
				System.out.println("In order to upload, you must provide a .roblosecurity cookie! Upload aborted.");
				return;
			}
				
		String name = fontName.replaceAll("\\W","");
		String lua = "{\n";
		int loc = 0;
		for (int size : sizes) {
			String nname1 = fontImagName.replaceAll("FONT", name).replaceAll("SIZE", String.valueOf(size)).replaceAll("LOC", String.valueOf(loc));
			String nname2 = fontJsonName.replaceAll("FONT", name).replaceAll("SIZE", String.valueOf(size)).replaceAll("LOC", String.valueOf(loc));
			BufferedImage image = FontMapGenerator.generateFontMap(fontName, size);
			String json = FontMapGenerator.getLastJSONData();
			System.out.println((loc + 1) + "/" + sizes.length);
			if (isLocal) {
				ImageIO.write((RenderedImage) image, "png", new File(nname1));
		        PrintWriter printWriter = new PrintWriter(nname2, "UTF-8");
		        printWriter.print(json);
		        printWriter.close();
			}
			if (isUpload) {
				Uploader.uploadResponse res = Uploader.upload(nname1, json, roblosecurity, token, image);
				for (int i = 0; i < 11 && !res.success; i++) {
					if (i == 10) {
						System.out.println("Aborting! Uploads are failing.");
						return;
					}
					if (res.message != null && res.message.equals("You are uploading too much, please try again later.")) {
						TimeUnit.SECONDS.sleep(5);
						i--;
					} else
						System.out.println("\tUpload failed! Retrying. " + (i + 1) + "/10");
					res = Uploader.upload(nname1, json, roblosecurity, token, image);
				}
				lua += "{\""+name+"\", "+size+", "+res.assetId+", "+res.backingAssetId+"}\n";
			}
			if (waitTime > 0)
				TimeUnit.SECONDS.sleep(waitTime);
			loc++;
		}
		lua = lua+"}\n";
		if (outputLua)
			System.out.println(lua);
	}

}
