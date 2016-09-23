package fr.kmai.tntt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoDownloader {

	public String getUrlContent(String urlString) {
		URL url;

		try {
			// get URL content
			url = new URL(urlString);
			URLConnection conn = url.openConnection();

			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine;

			StringBuilder sb = new StringBuilder();

			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}

			return sb.toString();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<String> getAllMatches(String text, String regex) {
		List<String> matches = new ArrayList<String>();
		Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
		while (m.find()) {
			matches.add(m.group(1));
		}
		return matches;
	}

	public List<String> parsePhotoLinks(String url) {
		String content = null;
		try {
			System.out.println("get content from url");
			content = getUrlContent(url);
			System.out.println("parse photo urls from content");
			List<String> urls = getAllMatches(content, "\"[\\d ]+\\.(JPG|jpg)");
			List<String> cleanUrls = new ArrayList<String>();
			if (!url.endsWith("/")) {
				url = url + "/";
			}
			for (String photoUrl : urls) {
				String tmp;
				if (photoUrl.startsWith("\"")) {
					tmp = photoUrl.substring(1);
				} else {
					tmp = photoUrl;
				}
				tmp = tmp.replace(" ", "%20");
				cleanUrls.add(url + tmp);
				System.out.println(url + tmp);
			}
			return cleanUrls;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void downloadPhotos(String originUrl, String folder) {
		try {
			List<String> urls = parsePhotoLinks(originUrl);
			String date = extractDate(originUrl);
			if (!folder.endsWith(File.separator)) {
				folder = folder + File.separator;
			}
			int index = 1;
			for (String onePhotoUrl : urls) {
				System.out.println(index+"/"+urls.size()+" downloading "+onePhotoUrl);
				URL url = new URL(onePhotoUrl);
				InputStream in = new BufferedInputStream(url.openStream());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int n = 0;
				while (-1 != (n = in.read(buf))) {
					out.write(buf, 0, n);
				}
				out.close();
				in.close();
				byte[] response = out.toByteArray();
				// And you may then want to save the image so do:
				
				File fileFolder = new File(folder + date);
				if(!fileFolder.exists()) {
					fileFolder.mkdir();
				}
				
				String fileName = folder + date + File.separator + onePhotoUrl.substring(onePhotoUrl.lastIndexOf('/')+1).replace("%20", " ");
				System.out.println(index+"/"+urls.size()+" write file "+fileName);
				FileOutputStream fos = new FileOutputStream(fileName);

				index++;
				fos.write(response);
				fos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String extractDate(String url) {
		String tmp;
		if (url.endsWith("/")) {
			tmp = url.substring(0, url.length() - 1);
		} else {
			tmp = url;
		}
		return tmp.substring(tmp.lastIndexOf('/')+1);
	}

	public static void main(String[] args) {
		PhotoDownloader pdl = new PhotoDownloader();
		// http://giaoxuvnparis.free.fr/20160910/
		pdl.downloadPhotos("http://giaoxuvnparis.free.fr/20160910/", "/tmp");
	}
}
