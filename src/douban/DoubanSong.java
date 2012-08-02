package douban;


public class DoubanSong {
	public String picture;
	public String albumtitle;
	public String company;
	public double rating_avg;
	public String public_time;
	public String ssid;
	public String album;
	public String like;
	public String artist;
	public String url;
	public String title;
	public String subtype;
	public String length;
	public String sid;
	
	@Override
	public boolean equals(Object obj) {
		if(null==obj) return false;
		if(!(obj instanceof DoubanSong)) return false;
		DoubanSong song=(DoubanSong) obj;
		return song.url.equals(this.url);
	}
	
	@Override
	public int hashCode() {
		return url.hashCode();
	}
	
	public String dump() {
		return "DoubanSong [picture=" + picture + ", albumtitle=" + albumtitle
				+ ", company=" + company + ", rating_avg=" + rating_avg
				+ ", public_time=" + public_time + ", ssid=" + ssid
				+ ", album=" + album + ", like=" + like + ", artist=" + artist
				+ ", url=" + url + ", title=" + title + ", subtype=" + subtype
				+ ", length=" + length + ", sid=" + sid + "]";
	}
	
	@Override
	public String toString() {
		return title;
	}
}
