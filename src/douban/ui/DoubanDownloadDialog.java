package douban.ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import douban.DoubanSong;
import douban.DownloadTask;
import douban.PublishCallBack;


public class DoubanDownloadDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static interface DownloadCallBack
	{
		void onDone();
	}
	
	private final static class DownloadEntry extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 603110385756123344L;
		
		JLabel taskName = new JLabel();
		JProgressBar progressBar = new JProgressBar();
		JButton funcBtn = new JButton();

		DownloadWorker worker;
		
		DownloadCallBack mCallBack;

		public DownloadEntry(DoubanSong song,DownloadCallBack callBack) {
			add(taskName);
			taskName.setText(song.title);
			add(progressBar);
			progressBar.setStringPainted(true);
			add(funcBtn);
			mCallBack=callBack;

			worker = new DownloadWorker(song, progressBar);
			worker.execute();
		}
		
		public DownloadWorker getWorker() {
			return worker;
		}

		class DownloadWorker extends SwingWorker<Boolean, Integer> {
			DoubanSong song;
			JProgressBar progressBar;

			public DownloadWorker(DoubanSong song, JProgressBar progressBar) {
				this.song = song;
				this.progressBar = progressBar;

				funcBtn.setText("取消");
				funcBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancel(true);
					}
				});
			}

			@Override
			protected Boolean doInBackground() throws Exception {
				DownloadTask task = new DownloadTask(song, "musics",
						new PublishCallBack() {
							@Override
							public void publish(long downSize, long fullSize,
									int percent) {
								if (DownloadWorker.this.isCancelled()) {
									throw new RuntimeException("cancel task");
								}else {
									DownloadWorker.this.publish(percent);
								}
							}
						});
				task.run();
				return true;
			}

			@Override
			protected void process(List<Integer> chunks) {
				progressBar.setString("%" + chunks.get(0));
				progressBar.setValue(chunks.get(0));
			}

			@Override
			protected void done() {
				final File target = new File("musics/" + song.title + ".mp3");
				if (!isCancelled()) {
					funcBtn.setText("打开");
					funcBtn.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								startFile(target);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					});
					progressBar.setValue(100);
					progressBar.setString("下载完成");
				}else {
					progressBar.setString("取消下载");
					target.delete();
				}
				mCallBack.onDone();
			}
			
			private void startFile(File target) throws IOException{
				StringBuilder path=new StringBuilder(target.getAbsolutePath());
				int blank=path.indexOf(" ");
				if(blank!=-1){
					int firstSperator=0;
					for(int i=blank;i>=0;i--)
					{
						if(path.charAt(i)==File.separatorChar)
						{
							firstSperator=i+1;
							break;
						}
					}
					path=path.insert(firstSperator, '\"').append('\"');
				}
				Runtime.getRuntime().exec(String.format("cmd /c start %s", path));
			}
		}
	}
	
	ArrayList<DownloadEntry> entries=new ArrayList<DoubanDownloadDialog.DownloadEntry>();
	
	public DoubanDownloadDialog(DoubanLauncher launcher,
			final ArrayList<DoubanSong> downloadSong) {
		super(launcher, true);
		
		DownloadCallBack handler=new DownloadCallBack() {
			int completeCount=0;
			int totalTaskCount=downloadSong.size();
			
			@Override
			public void onDone() {
				completeCount++;
				setTitle(String.format("下载中,请稍候 (%s-%s)",completeCount,totalTaskCount));
				if(completeCount==totalTaskCount)
				{
					setTitle("下载完成!");
					JOptionPane.showMessageDialog(DoubanDownloadDialog.this, "下载完成!");
				}
			}
		};
		
		setTitle("下载中,请稍候");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocation(200, 200);

		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 30));
		JScrollPane scrollPane = new JScrollPane(panel);
		add(scrollPane, BorderLayout.CENTER);

		for (DoubanSong doubanSong : downloadSong) {
			DownloadEntry entry=new DownloadEntry(doubanSong,handler);
			entries.add(entry);
			panel.add(entry);
			System.out.println("add " + doubanSong.dump());
		}

		// setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(400, 400);
		setVisible(true);
		pack();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		//cancel all task
		for(DownloadEntry entry :entries)
		{
			entry.getWorker().cancel(true);
		}
	}

}
