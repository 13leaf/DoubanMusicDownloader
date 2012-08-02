package douban;


import java.util.ArrayList;

public class DoubanPlayList {

	public String r;
	public ArrayList<DoubanSong> song;

	public DoubanPlayList filter(String albumTitle) {
		if (albumTitle != null) {
			ArrayList<DoubanSong> filtered = new ArrayList<DoubanSong>();
			for (DoubanSong aSong : song) {
				if (aSong.albumtitle.equals(albumTitle))
					filtered.add(aSong);
			}
			song = filtered;
		}
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for (DoubanSong aSong: song) {
			sb.append(aSong.dump()+",");
		}
		return sb.toString();
	}
}
