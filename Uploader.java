import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;


public class Uploader {
	static Pattern assetPattern = Pattern.compile(".*\"AssetId\"\\:(\\d+).*");
	static Pattern backingPattern = Pattern.compile(".*\"BackingAssetId\"\\:(\\d+).*");
	static Pattern messagePattern = Pattern.compile(".*\"Message\"\\:\"([^\"]*)\".*");
	static class uploadResponse {
		boolean success;
		int assetId;
		int backingAssetId;
		String content;
		String message;
		public uploadResponse(boolean success, int assetId, int backingAssetId, String content, String message) {
			this.success = success;
			this.assetId = assetId;
			this.backingAssetId = backingAssetId;
			this.content = content;
			this.message = message;
		}
	}
	public static String getToken(String robloSecurity) throws IOException {
		URL u = new URL("http://data.roblox.com/data/upload/json");
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Length", "0");
		conn.setRequestProperty("Cookie", ".ROBLOSECURITY="+robloSecurity);
		OutputStream os = conn.getOutputStream();
		os.write(new byte[0]); //only way to set content-length that I know of
		String result = conn.getHeaderField("X-CSRF-TOKEN");
		conn.disconnect();
		return result;
	}
	public static uploadResponse upload(String name, String description, String robloSecurity, String token, BufferedImage image) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write((RenderedImage) image, "png", output);
		String encodedData = Base64.getEncoder().encodeToString(output.toByteArray());
		URL u = new URL("http://data.roblox.com/data/upload/json?assetTypeId=13&name=" + URLEncoder.encode(name, "UTF-8") + "&description=" + URLEncoder.encode(description, "UTF-8"));
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Cookie", ".ROBLOSECURITY="+robloSecurity);
		conn.setRequestProperty("X-CSRF-TOKEN", token);
		conn.setRequestProperty("Host", "data.roblox.com");
		conn.setRequestProperty( "Content-Length", String.valueOf(output.size()));
		conn.setRequestProperty( "Content-type", "*/*");
		conn.setRequestProperty("Fiddler-Encoding", "base64");
		OutputStream os = conn.getOutputStream();
		os.write(output.toByteArray());
		conn.setReadTimeout(3000);
		InputStream response = conn.getInputStream();
		java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");
		String content = s.hasNext() ? s.next() : "";
		response.close();
		s.close();
		conn.disconnect();
		boolean wasSuccess = content.matches(".*\"Success\"\\:true.*");
		//boolean failedLogin = content.matches(".*/Login/Default.*");
		Matcher am = assetPattern.matcher(content);
		Matcher bm = backingPattern.matcher(content);
		Matcher mm = messagePattern.matcher(content);
		int assetId = -1, backingId = -1;
		String msg = "";
		if (am.find())
			assetId = Integer.parseInt(am.group(1));
		if (bm.find())
			backingId = Integer.parseInt(bm.group(1));
		if (mm.find())
			msg = mm.group(1);
		//if (failedLogin)
		//	msg = "Login invalid";
		return new uploadResponse(wasSuccess, assetId, backingId, content, msg);
	}
}
