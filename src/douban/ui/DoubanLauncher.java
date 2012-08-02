package douban.ui;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import douban.DoubanDownloader;
import douban.DoubanPlayList;
import douban.DoubanSong;

public class DoubanLauncher extends JFrame implements ActionListener{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTextField subjectUrl=new JTextField("请输入欲下载的豆瓣专辑地址");
	
	JButton fetchSubject=new JButton("获取下载列表");
	
	JButton clearList=new JButton("清除列表");
	
	JButton download=new JButton("下载");
	
	JLabel status=new JLabel("状态");
	
	DefaultListModel songModel=new DefaultListModel();

	JList list=new JList(songModel);
	
	public DoubanLauncher()
	{
		super("豆瓣音乐下载工具 v1.1 13leaf");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		BorderLayout layout=(BorderLayout) getLayout();
		layout.setVgap(30);
		initLayout();
		setVisible(true);
	}
	
	private void initLayout() {
		setLocation(200,200);
		setSize(500,500);
		fetchSubject.addActionListener(this);
		download.addActionListener(this);

		
		JPanel panel=new JPanel();
		panel.add(subjectUrl);panel.add(fetchSubject);panel.add(clearList);
		clearList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				songModel.clear();
			}
		});
		subjectUrl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				subjectUrl.selectAll();
			}
		});
		subjectUrl.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER)
				{
					actionPerformed(new ActionEvent(fetchSubject, 0,""));//mock click
				}
			}
		});
		
		add(panel,BorderLayout.NORTH);
		
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setComponentPopupMenu(getContextMenu());
		JScrollPane scrollPane=new JScrollPane(list);
		add(scrollPane,BorderLayout.CENTER);
		
		GridBagLayout layout=new GridBagLayout();
		JPanel panel2=new JPanel(layout);
		GridBagConstraints constraints=new GridBagConstraints();constraints.anchor=GridBagConstraints.CENTER;
		layout.setConstraints(download, constraints);panel2.add(download);
		constraints.anchor=GridBagConstraints.EAST;constraints.weightx=0;
		layout.setConstraints(status, constraints);panel2.add(status);
		add(panel2,BorderLayout.SOUTH);
	}
	
	private JPopupMenu getContextMenu() {
		JPopupMenu menu=new JPopupMenu();
		menu.add("删除").addActionListener(listContextHandler);
		return menu;
	}
	
	ActionListener listContextHandler=new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("删除"))
			{
				int[] selected=list.getSelectedIndices();
				if(selected.length!=0)
				{
					ArrayList<DoubanSong> deleteFlag=new ArrayList<DoubanSong>();
					for(int i=0;i<selected.length;i++)
					{
						deleteFlag.add((DoubanSong) songModel.get(selected[i]));
					}
					for (DoubanSong doubanSong : deleteFlag) {
						songModel.removeElement(doubanSong);
					}
				}
			}
		}
	};

	public static void main(String[] args) {
		new DoubanLauncher();
	}
	
	/**
	 * 抓取列表,更新ui
	 * @author 13leaf
	 *
	 */
	class FetchListWorker extends SwingWorker<DoubanPlayList, Integer>
	{
		private final String subjectUrl;
		
		public FetchListWorker(String subjectUrl)
		{
			this.subjectUrl=subjectUrl;
			status.setText("正在获取列表信息...");
		}
		
		@Override
		protected DoubanPlayList doInBackground() throws Exception {
			String contextUrl=DoubanDownloader.fetchContextUrl(subjectUrl);
			if(contextUrl==null){
				return null;
			}else {
				return DoubanDownloader.fetchPlayerList(contextUrl);
			}
				
		}
		
		@Override
		protected void done() {
			super.done();
			if(isCancelled()) return;
			try {
				DoubanPlayList playList=get();
				if(playList==null){
					showErrorMessage("该链接不是豆瓣专辑下载地址或者获得专辑列表失败!请检查网络后重试");
					return;
				}else {
					for(DoubanSong song:playList.song)
					{
						if(!songModel.contains(song))
							songModel.addElement(song);
					}
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			status.setText("获取列表完成...当前列表大小为:"+songModel.size());
		}
	}
	
	FetchListWorker currentFetchWorker;

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==fetchSubject)
		{
			String request=subjectUrl.getText().trim();
			if(currentFetchWorker==null)//first
			{
				currentFetchWorker=new FetchListWorker(request);
				currentFetchWorker.execute();
				return;
			}
			if(currentFetchWorker.getState()==StateValue.DONE)
			{
				currentFetchWorker=new FetchListWorker(request);
				currentFetchWorker.execute();
			}
		}else if(e.getSource()==download){
			//do download
			ArrayList<DoubanSong> songs=new ArrayList<DoubanSong>();
			for(int i=0;i<songModel.size();i++)
				songs.add((DoubanSong) songModel.get(i));
			new DoubanDownloadDialog(this, songs);
		}
	}
	
	
	
	private void showErrorMessage(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "豆瓣专辑下载器", JOptionPane.ERROR_MESSAGE);
	}
	
}
