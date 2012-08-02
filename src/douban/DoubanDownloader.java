package douban;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class DoubanDownloader {

	private static HttpClient mClient;

	private static volatile boolean hasLogin;

	private static String DEFAULT_USER = "doubanUser122@163.com";

	private static String DEFAULT_USER_PASSWORD = "douban1233";

	private static ExecutorService executor = Executors.newCachedThreadPool();

	public static HttpClient getClient() {
		if (mClient == null)
			mClient = new DefaultHttpClient();
		return mClient;
	}

	public static String fetchContextUrl(String subjectUrl) {
		Pattern pattern = Pattern
				.compile("<a.+?href=\"(http://douban.fm/\\S+)\">豆瓣FM</a>");
		try {
			String content = httpGet(subjectUrl);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * contextUrl除了专辑外,可能还加入了其它推荐音乐。默认将过滤掉多余内容
	 * @param contextUrl
	 * @return
	 */
	public static DoubanPlayList fetchPlayerList(String contextUrl) {
		return fetchPlayerList(contextUrl,true);
	}
	
	public static DoubanPlayList fetchPlayerList(String contextUrl,boolean filtered)
	{
		Pattern pattern = Pattern
				.compile("^http.+?(context=channel:(.+)\\|.+)$");
		Matcher matcher = pattern.matcher(contextUrl);
		if (matcher.find()) {
			try {
				URI uri = new URI("http",// scheme
						null,// userInfo
						"douban.fm",// host
						80,// port
						"/j/mine/playlist",// path
						"type=n&h=&channel=" + matcher.group(2) + "&context="
						+ matcher.group(1)
						+ "&from=mainsite&r=866ceb62db",// query
						null// fragment
						);
				String playList = httpGet(uri);
				Gson gson = new Gson();
				DoubanPlayList doubanPlayList=gson.fromJson(playList, DoubanPlayList.class);
				if(filtered)
					doubanPlayList.filter(doubanPlayList.song.get(0).albumtitle);//根据首个元素来过滤整个列表
				return doubanPlayList;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
		
	}

	/**
	 * 返回是否登录成功
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	public static boolean login(String email, String password) {
		try {
			JsonParser parser = new JsonParser();
			String response = httpGet("https://www.douban.com/j/app/login?app_name=radio_android&version=588"
					+ "&email="
					+ URLEncoder.encode(email, "utf8")
					+ "&password=" + URLEncoder.encode(password, "utf8") + "");
			JsonElement element = parser.parse(response);
			int responseCode = element.getAsJsonObject().get("r").getAsInt();
			hasLogin = responseCode == 0;
			return hasLogin;
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 登录默认用户
	 * 
	 * @return
	 */
	public static boolean login() {
		return login(DEFAULT_USER, DEFAULT_USER_PASSWORD);
	}

	/**
	 * 下载至本目录下
	 * @param song
	 */
	public static void downloadSong(DoubanSong song,String dir,PublishCallBack callBack) {
		if (!hasLogin && !login())
		{
			throw new RuntimeException("还未登录！");
		}
		executor.execute(new DownloadTask(song,dir,callBack));
		System.out.println("download "+song.title+" complete!");
	}
	
	public static void downloadSong(DoubanSong song,String dir) {
		downloadSong(song, dir,null);
	}
	
	public static void downloadPlayerList(DoubanPlayList playList,String dir)
	{
		if (!hasLogin && !login())
		{
			throw new RuntimeException("还未登录！");
		}
		for(DoubanSong song :playList.song)
		{
			downloadSong(song, dir);
		}
		System.out.println("download playList complete!");
	}

	private static String httpGet(String uri) throws ParseException,
			IOException {
		HttpClient client = getClient();
		HttpResponse response = client.execute(new HttpGet(uri));
		return EntityUtils.toString(response.getEntity());
	}
	
	public static void downloadSubject(String subjectUrl)
	{
		String contextUrl=fetchContextUrl(subjectUrl);
		DoubanPlayList playList=fetchPlayerList(contextUrl);
		downloadPlayerList(playList, "let's go");
	}

	private static String httpGet(URI uri) throws ParseException, IOException {
		HttpClient client = getClient();
		HttpResponse response = client.execute(new HttpGet(uri));
		return EntityUtils.toString(response.getEntity());
	}

	public static void main(String[] args) throws ClientProtocolException,
			IOException, URISyntaxException {
//		downloadSubject("");
//		executor.shutdown();
//		DoubanPlayList playList=fetchPlayerList("http://douban.fm/?context=channel:0|subject_id:1404750");
//		for(DoubanSong song:playList.song)
//			System.out.println(song.url);
	}

}
