package douban;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Downloader {

	public final static int DEFAULT_BLOCK_SIZE = 1024*8;

	public static void downLoad(String url, String targetPath, int blockSize,
			PublishCallBack publisher) throws IOException {
		File target = new File(targetPath);
		target.getParentFile().mkdirs();
		OutputStream targetOutputStream = new FileOutputStream(target);
		byte[] block = new byte[blockSize];

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(new HttpGet(url));

		InputStream is = response.getEntity().getContent();

		long downSize = 0;
		long fullSize = response.getEntity().getContentLength();

		try {
			int count;
			while ((count = is.read(block)) != -1) {
				// write block and publish task
				targetOutputStream.write(block, 0, count);
				downSize += count;
				if (publisher != null) {
					publisher.publish(downSize, fullSize, (int) (Math
							.floor(downSize / (double) fullSize * 100)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(targetOutputStream!=null) targetOutputStream.close();
			if(is !=null) is.close();
		}
	}

	public static void downLoad(String url, String targetPath)
			throws IOException {
		downLoad(url, targetPath, DEFAULT_BLOCK_SIZE, null);
	}

	public static void main(String[] args) throws IOException {
		downLoad("http://202.107.35.126:8011/main_setup.exe", "waga.exe");
	}
}
