package douban;
	/**
	 * 通知下载完成情况
	 * 
	 * @author 13leaf
	 * 
	 */
	public interface PublishCallBack {
		/**
		 * @param downSize
		 *            已下载字节大小
		 * @param fullSize
		 *            总下载字节大小
		 * @param percent
		 *            下载百分比。公式如下
		 *            Math.floor(downSize/fullSize*100)。注意不是百分比例,如返回50,则表示完成百分之50
		 */
		void publish(long downSize, long fullSize, int percent);
	}