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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;


public class Uploader {
	static Pattern assetPattern = Pattern.compile(".*\"AssetId\"\\:(\\d+).*");
	static Pattern backingPattern = Pattern.compile(".*\"BackingAssetId\"\\:(\\d+).*");
	static Pattern messagePattern = Pattern.compile(".*\"Message\"\\:\"([^\"]*)\".*");

	static Pattern viewStatePattern = Pattern.compile("id=\"__VIEWSTATE\"\\s*value=\"([^\"]*)\"");
	static Pattern eventValidationPattern = Pattern.compile("id=\"__EVENTVALIDATION\"\\s*value=\"([^\"]*)\"");
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
		conn.setRequestProperty("User-Agent", "Roblox/WinInet");
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
	public static uploadResponse uploadAsset(String type, boolean isPublic, int uploadAssetId, String name, String description, String robloSecurity, String token, String txt) throws IOException {
		URL u = new URL("http://data.roblox.com/Data/Upload.ashx?type=" + type + "&ispublic=" + (isPublic ? "true" : "false") + "&json=1&assetId=" + uploadAssetId + "&name=" + URLEncoder.encode(name, "UTF-8") + "&description=" + URLEncoder.encode(description, "UTF-8"));
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Cookie", ".ROBLOSECURITY="+robloSecurity);
		conn.setRequestProperty("X-CSRF-TOKEN", token);
		conn.setRequestProperty("Host", "data.roblox.com");
		conn.setRequestProperty( "Content-Length", String.valueOf(txt.getBytes().length));
		conn.setRequestProperty( "Content-type", "*/*");
		conn.setRequestProperty("User-Agent", "Roblox/WinInet");
		OutputStream os = conn.getOutputStream();
		os.write(txt.getBytes());
		conn.setReadTimeout(3000);
		InputStream response = conn.getInputStream();
		java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");
		String content = s.hasNext() ? s.next() : "";
		response.close();
		s.close();
		conn.disconnect();
		boolean wasSuccess = true;
		Matcher am = assetPattern.matcher(content);
		Matcher mm = messagePattern.matcher(content);
		int assetId = -1, backingId = -1;
		String msg = "";
		if (am.find())
			assetId = Integer.parseInt(am.group(1));
		else
			wasSuccess = false;
		if (mm.find())
			msg = mm.group(1);
		return new uploadResponse(wasSuccess, assetId, 0, content, msg);
	}
	public static int configure(int assetId, String name, String description, boolean comments, int genre, String robloSecurity) throws Exception {
		String eventTarget = "ctl00$cphRoblox$SubmitButtonTop";
		String viewState = null, eventValidation = null;
		{
			URL u = new URL("http://www.roblox.com/My/Item.aspx?ID=" + assetId);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Cookie", ".ROBLOSECURITY="+robloSecurity);
			conn.setRequestProperty("Host", "data.roblox.com");
			InputStream response = conn.getInputStream();
			java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");
			String content = s.hasNext() ? s.next() : "";
			s.close();
			response.close();
			conn.disconnect();
			Matcher vsm = viewStatePattern.matcher(content);
			Matcher evm = eventValidationPattern.matcher(content);
			if (vsm.find())
				viewState = vsm.group(1);
			if (evm.find())
				eventValidation = evm.group(1);
		}

		if (viewState == null || eventValidation == null)
			throw new Exception("Could not grab __VIEWSTATE and __EVENTVALIDATION from the page.");

		String boundary = UUID.randomUUID().toString();
		URL u = new URL("http://www.roblox.com/My/Item.aspx?ID=" + assetId);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Cookie", ".ROBLOSECURITY="+robloSecurity);
		conn.setRequestProperty("Host", "data.roblox.com");
		conn.setRequestProperty( "Content-type", "multipart/form-data; boundary="+boundary);
		OutputStream out = conn.getOutputStream();
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "__EVENTTARGET" + "\"\r\n\r\n").getBytes());
		out.write(eventTarget.getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "__VIEWSTATE" + "\"\r\n\r\n").getBytes());
		out.write(viewState.getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "__EVENTVALIDATION" + "\"\r\n\r\n").getBytes());
		out.write(eventValidation.getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "ctl00$cphRoblox$NameTextBox" + "\"\r\n\r\n").getBytes());
		out.write(name.getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "ctl00$cphRoblox$DescriptionTextBox" + "\"\r\n\r\n").getBytes());
		out.write(description.getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "ctl00$cphRoblox$fuUploadContent" + "\"\r\n\r\n").getBytes());
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "ctl00$cphRoblox$EnableCommentsCheckBox" + "\"\r\n\r\n").getBytes());
		out.write((comments ? "on" : "off").getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "GenreButtons2" + "\"\r\n\r\n").getBytes());
		out.write(("" + genre).getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		out.write(("Content-Disposition: form-data; name=\"" + "ctl00$cphRoblox$actualGenreSelection" + "\"\r\n\r\n").getBytes());
		out.write(("" + genre).getBytes("UTF-8"));
		out.write(("\r\n").getBytes("UTF-8"));
		out.write(("--" + boundary + "--" + "\r\n").getBytes("UTF-8"));
		conn.setReadTimeout(3000);
		return conn.getResponseCode();
	}
}
