package douban;
import java.io.File;
import java.io.IOException;

public class DownloadTask implements Runnable {
		private DoubanSong song;
		private String dir;
		private PublishCallBack callBack;

		public DownloadTask(DoubanSong song,String dirPath,PublishCallBack callBack) {
			this.song = song;
			dir=dirPath;
			this.callBack=callBack;
		}
		
		public DownloadTask(final DoubanSong song,String dirPath)
		{
			this(song, dirPath,new PublishCallBack() {
			 	int latestPercent=0;
				@Override
				public void publish(long downSize, long fullSize,
						int percent) {
					if(percent!=latestPercent)
					{
						System.out.println("download "+song.title+" "+percent);
						latestPercent=percent;
					}
				}
			} );
		}

		@Override
		public void run() {
			try {
				Downloader.downLoad(song.url, new File(dir,song.title + ".mp3").getAbsolutePath(),
						Downloader.DEFAULT_BLOCK_SIZE,callBack
						);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}