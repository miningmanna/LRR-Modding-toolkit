package de.mm.lrrmod.flh;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.mm.lrrmod.LRRModTool;

public class FLHModTool extends LRRModTool {
	
	private static final long serialVersionUID = -7537319353930785086L;
	
	private FLHPreviewer makePreview, filePreview;
	private FLHFile readFile;
	private ArrayList<ImageFile> mFileArray;
	
	public FLHModTool() {
		name = "FLH Tool";
		makePreview = new FLHPreviewer(null);
		makePreview.setVisible(false);
		filePreview = new FLHPreviewer(null);
		filePreview.setVisible(false);
		
		mFileArray = new ArrayList<>();
		
		// TODO: Write GUI objects
		
		JTabbedPane panes = new JTabbedPane();
		
		JPanel previewPanel = new JPanel(new BorderLayout());
		
		JPanel pLeftPanel = new JPanel();
		pLeftPanel.setLayout(new BoxLayout(pLeftPanel, BoxLayout.Y_AXIS));
		previewPanel.add(pLeftPanel, BorderLayout.LINE_START);
		
		JSlider pFps = new JSlider(0, 60, 30);
		pFps.setAlignmentX(CENTER_ALIGNMENT);
		pFps.setOrientation(JSlider.VERTICAL);
		pFps.setPaintTicks(true);
		pFps.setPaintLabels(true);
		pFps.setMajorTickSpacing(10);
		pFps.setToolTipText("FPS");
		pFps.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("FPS"), 
				BorderFactory.createEmptyBorder(0, 10, 0, 10)));
		pFps.setAlignmentX(0.5f);
		pLeftPanel.add(pFps);
		
		JLabel pFpsLabel = new JLabel("FPS: 30");
		pFpsLabel.setAlignmentX(CENTER_ALIGNMENT);
		pLeftPanel.add(pFpsLabel);
		
		JLabel pFrameLabel = new JLabel("Frame: 0");
		pFrameLabel.setAlignmentX(CENTER_ALIGNMENT);
		pLeftPanel.add(pFrameLabel);
		
		JPanel pRightPanel = new JPanel(new GridBagLayout());
		previewPanel.add(pRightPanel, BorderLayout.CENTER);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		
		JButton pOpen = new JButton("Open");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.5;
		pRightPanel.add(pOpen, gridBagConstraints);
		
		JPanel dummy = new JPanel();
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 2;
		pRightPanel.add(dummy, gridBagConstraints);
		
		JButton pExtract = new JButton("Extract");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		pRightPanel.add(pExtract, gridBagConstraints);
		
		JPanel dummy2 = new JPanel();
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 3;
		pRightPanel.add(dummy2, gridBagConstraints);
		
		JButton pBack = new JButton("<-");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 0.333;
		pRightPanel.add(pBack, gridBagConstraints);
		
		JButton pPausePlay = new JButton(">");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		pRightPanel.add(pPausePlay, gridBagConstraints);
		
		JButton pNext = new JButton("->");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		pRightPanel.add(pNext, gridBagConstraints);
		
		JSlider pFrame = new JSlider();
		pFrame.setBorder(BorderFactory.createTitledBorder("Frame"));
		pFrame.setMinimum(0);
		pFrame.setMaximum(60);
		pFrame.setValue(0);
		pFrame.setPaintTicks(true);
		pFrame.setPaintLabels(true);
		pFrame.setMajorTickSpacing(10);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 6;
		gridBagConstraints.weightx = 1;
		pRightPanel.add(pFrame, gridBagConstraints);
		
		panes.add("Read", previewPanel);
		
		// Preview listensers
		pFps.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pFpsLabel.setText("FPS: " + pFps.getValue());
				filePreview.setFPS(pFps.getValue());
			}
		});
		pBack.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				filePreview.setVisible(true);
				filePreview.stop();
				pPausePlay.setText(">");
				int frames = filePreview.getFrameCount();
				if(frames != 0) {
					int frame = filePreview.getFrame();
					frame = (frame+(frames-1))%frames;
					filePreview.setFrame(frame);
					pFrameLabel.setText("Frame: " + frame);
				} else {
					filePreview.setFrame(0);
					pFrameLabel.setText("Frame: " + 0);
				}
			}
		});
		pNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filePreview.setVisible(true);
				filePreview.stop();
				pPausePlay.setText(">");
				int frames = filePreview.getFrameCount();
				if(frames != 0) {
					int frame = filePreview.getFrame();
					frame = (frame+1)%frames;
					filePreview.setFrame(frame);
					pFrameLabel.setText("Frame: " + frame);
				} else {
					filePreview.setFrame(0);
					pFrameLabel.setText("Frame: " + 0);
				}
			}
		});
		pPausePlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filePreview.setVisible(true);
				if(filePreview.isRunning()) {
					filePreview.stop();
					pPausePlay.setText(">");
				} else {
					filePreview.start();
					pPausePlay.setText("||");
				}
			}
		});
		pOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "FLH files";
					}
					
					@Override
					public boolean accept(File f) {
						String name = f.getName();
						int dotIndex = name.indexOf('.');
						if(dotIndex <= -1)
							return false;
						
						if(!name.substring(dotIndex).equalsIgnoreCase("FLH"))
							return false;
						
						return true;
					}
				});
				chooser.setMultiSelectionEnabled(false);
				int res = chooser.showOpenDialog(FLHModTool.this);
				if(res == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					FLHFile flh = null;
					FileInputStream in = null;
					try {
						in = new FileInputStream(chooser.getSelectedFile());
						flh = FLHFile.getFLHFile(in);
					} catch(Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(FLHModTool.this, "Couldnt load file: " + f , "Failed loading : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
					} finally {
						if(in != null)
							try {
								in.close();
							} catch (IOException e) {}
					}
					if(flh != null) {
						readFile = flh;
						filePreview.setImages(flh.frames);
						filePreview.setVisible(true);
						pFrame.setMaximum(flh.frames.size()-1);
						pFrame.setMajorTickSpacing(pFrame.getMaximum()/5);
					}
				}
			}
		});
		pExtract.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int res = chooser.showSaveDialog(FLHModTool.this);
				if(res == JFileChooser.APPROVE_OPTION) {
					File dir = chooser.getSelectedFile();
					for(int i = 0; i < readFile.frames.size(); i++) {
						try {
							ImageIO.write(readFile.frames.get(i), "png", new File(dir, i + ".png"));
						} catch(Exception e) {
							JOptionPane.showMessageDialog(FLHModTool.this, "Couldnt save frame " + i, "Failed saving frame", JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					
				}
			}
		});
		ChangeListener pFrameListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				filePreview.stop();
				pPausePlay.setText(">");
				filePreview.setFrame(pFrame.getValue());
				pFrameLabel.setText("Frame: " + pFrame.getValue());
			}
		};
		filePreview.setOnRedrawListener(new FLHPreviewer.OnRedrawListener() {
			@Override
			public void onRedraw(int frameNum) {
				pFrameLabel.setText("Frame: " + frameNum);
				pFrame.removeChangeListener(pFrameListener);
				pFrame.setValue(frameNum);
				pFrame.addChangeListener(pFrameListener);
			}
		});
		pFrame.addChangeListener(pFrameListener);
		
		
		JPanel makePanel = new JPanel(new BorderLayout());
		JSplitPane mPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		JPanel mL = new JPanel(new BorderLayout());
		mPanes.add(mL);
		
		JPanel mFilesPanel = new JPanel(new BorderLayout());
		DefaultListModel<ImageFile> mFilesList = new DefaultListModel<>();
		JList<ImageFile> mFiles = new JList<>(mFilesList);
		mFiles.setLayoutOrientation(JList.VERTICAL);
		JScrollPane mFilesScroll = new JScrollPane(mFiles);
		mFilesScroll.setBorder(BorderFactory.createTitledBorder("Frames"));
		mFilesPanel.add(mFilesScroll, BorderLayout.CENTER);
		
		
		JButton mSortFiles = new JButton("Sort");
		mFilesPanel.add(mSortFiles, BorderLayout.PAGE_END);
		
		mL.add(mFilesPanel, BorderLayout.CENTER);
		
		JPanel mFilesButtonPanel = new JPanel(new GridBagLayout());
		
		JButton mAdd = new JButton("Add");
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		mFilesButtonPanel.add(mAdd, gridBagConstraints);
		
		JButton mRemoveSelected = new JButton("Remove selected");
		gridBagConstraints.gridy = 1;
		mFilesButtonPanel.add(mRemoveSelected, gridBagConstraints);
		
		JButton mRemoveAll = new JButton("Remove all");
		gridBagConstraints.gridy = 2;
		mFilesButtonPanel.add(mRemoveAll, gridBagConstraints);
		
		
		mL.add(mFilesButtonPanel, BorderLayout.LINE_END);
		
		JPanel mR = new JPanel(new BorderLayout());
		mPanes.add(mR);
		
		JPanel mPLeftPanel = new JPanel();
		mPLeftPanel.setLayout(new BoxLayout(mPLeftPanel, BoxLayout.Y_AXIS));
		mR.add(mPLeftPanel, BorderLayout.LINE_START);
		
		JSlider mFps = new JSlider(0, 60, 30);
		mFps.setAlignmentX(CENTER_ALIGNMENT);
		mFps.setOrientation(JSlider.VERTICAL);
		mFps.setPaintTicks(true);
		mFps.setPaintLabels(true);
		mFps.setMajorTickSpacing(10);
		mFps.setToolTipText("FPS");
		mFps.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("FPS"), 
				BorderFactory.createEmptyBorder(0, 10, 0, 10)));
		mFps.setAlignmentX(0.5f);
		mPLeftPanel.add(mFps);
		
		JLabel mFpsLabel = new JLabel("FPS: 30");
		mFpsLabel.setAlignmentX(CENTER_ALIGNMENT);
		mPLeftPanel.add(mFpsLabel);
		
		JLabel mFrameLabel = new JLabel("Frame: 0");
		mFrameLabel.setAlignmentX(CENTER_ALIGNMENT);
		mPLeftPanel.add(mFrameLabel);
		
		JPanel mPRightPanel = new JPanel(new GridBagLayout());
		mR.add(mPRightPanel, BorderLayout.CENTER);
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		
		JButton mSave = new JButton("Save");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		mPRightPanel.add(mSave, gridBagConstraints);
		
		JButton mBack = new JButton("<-");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 0.333;
		mPRightPanel.add(mBack, gridBagConstraints);
		
		JButton mPausePlay = new JButton(">");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		mPRightPanel.add(mPausePlay, gridBagConstraints);
		
		JButton mNext = new JButton("->");
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		mPRightPanel.add(mNext, gridBagConstraints);
		
		JSlider mFrame = new JSlider();
		mFrame.setBorder(BorderFactory.createTitledBorder("Frame"));
		mFrame.setMinimum(0);
		mFrame.setMaximum(60);
		mFrame.setValue(0);
		mFrame.setPaintTicks(true);
		mFrame.setPaintLabels(true);
		mFrame.setMajorTickSpacing(10);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.weightx = 1;
		mPRightPanel.add(mFrame, gridBagConstraints);
		
		makePanel.add(mPanes);
		
		panes.add("Write", makePanel);
		
		mSortFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mFileArray.sort(new Comparator<ImageFile>() {
					@Override
					public int compare(ImageFile o1, ImageFile o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				mFilesList.removeAllElements();
				for(int i = 0; i < mFileArray.size(); i++)
					mFilesList.add(i, mFileArray.get(i));
				
				updateMakeImages();
			}
		});
		mAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int res = chooser.showOpenDialog(FLHModTool.this);
				if(res == JFileChooser.APPROVE_OPTION) {
					for(File f : chooser.getSelectedFiles()) {
						
						if(f.isFile()) {
							ImageFile imgFile = new ImageFile(f);
							mFileArray.add(imgFile);
							mFilesList.addElement(imgFile);
						} else {
							for(File subf : f.listFiles()) {
								if(subf.isFile()) {
									ImageFile imgFile = new ImageFile(subf);
									mFileArray.add(imgFile);
									System.out.println(mFileArray.size());
									mFilesList.addElement(imgFile);
								}
							}
						}
						
					}
				}
				
				mFrame.setMaximum(mFileArray.size()-1);
				mFrame.setPaintLabels(false);
				mFrame.setMajorTickSpacing(mFrame.getMaximum()/5);
				mFrame.setPaintLabels(true);
				updateMakeImages();
			}
		});
		mRemoveAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				mFileArray.clear();
				mFilesList.clear();
				
				mFrame.setMaximum(mFileArray.size()-1);
				updateMakeImages();
			}
		});
		mRemoveSelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				int[] selected = mFiles.getSelectedIndices();
				Object[] selectedObjs = new Object[selected.length];
				for(int i = 0; i < selected.length; i++)
					selectedObjs[i] = mFilesList.get(selected[i]);
				
				for(int i = 0; i < selectedObjs.length; i++) {
					mFileArray.remove(selectedObjs[i]);
					mFilesList.removeElement(selectedObjs[i]);
				}
				
				mFrame.setMaximum(mFileArray.size()-1);
				updateMakeImages();
			}
		});
		mFps.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mFpsLabel.setText("FPS: " + mFps.getValue());
				makePreview.setFPS(mFps.getValue());
			}
		});
		mBack.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				makePreview.setVisible(true);
				makePreview.stop();
				mPausePlay.setText(">");
				int frames = makePreview.getFrameCount();
				if(frames != 0) {
					int frame = makePreview.getFrame();
					frame = (frame+(frames-1))%frames;
					makePreview.setFrame(frame);
					mFrameLabel.setText("Frame: " + frame);
				} else {
					makePreview.setFrame(0);
					mFrameLabel.setText("Frame: " + 0);
				}
			}
		});
		mNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				makePreview.setVisible(true);
				makePreview.stop();
				mPausePlay.setText(">");
				int frames = makePreview.getFrameCount();
				if(frames != 0) {
					int frame = makePreview.getFrame();
					frame = (frame+1)%frames;
					makePreview.setFrame(frame);
					mFrameLabel.setText("Frame: " + frame);
				} else {
					makePreview.setFrame(0);
					mFrameLabel.setText("Frame: " + 0);
				}
			}
		});
		mPausePlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				makePreview.setVisible(true);
				if(makePreview.isRunning()) {
					makePreview.stop();
					mPausePlay.setText(">");
				} else {
					makePreview.start();
					mPausePlay.setText("||");
				}
			}
		});
		mSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int res = chooser.showSaveDialog(mFrame);
				if(res != JFileChooser.APPROVE_OPTION)
					return;
				
				File saveFile = chooser.getSelectedFile();
				if(!saveFile.getName().toUpperCase().endsWith(".FLH"))
					saveFile = new File(saveFile.getAbsolutePath() + ".flh");
				
				FLHFile toSave = new FLHFile();
				ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
				for(int i = 0; i < mFileArray.size(); i++)
					images.add(i, mFileArray.get(i).image);
				
				toSave.frames = images;
				toSave.width = images.get(0).getWidth();
				toSave.height = images.get(0).getHeight();
				
				try {
					
					FileOutputStream out = new FileOutputStream(saveFile);
					FLHFile.writeFlhFile(toSave, out);
					out.close();
					
				} catch(Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		ChangeListener mFrameListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				makePreview.stop();
				mPausePlay.setText(">");
				makePreview.setFrame(mFrame.getValue());
				mFrameLabel.setText("Frame: " + mFrame.getValue());
			}
		};
		makePreview.setOnRedrawListener(new FLHPreviewer.OnRedrawListener() {
			@Override
			public void onRedraw(int frameNum) {
				mFrameLabel.setText("Frame: " + frameNum);
				mFrame.removeChangeListener(mFrameListener);
				mFrame.setValue(frameNum);
				mFrame.addChangeListener(mFrameListener);
			}
		});
		mFrame.addChangeListener(mFrameListener);
		
		// TODO: Implement event listeners
		
		setLayout(new BorderLayout());
		add(panes, BorderLayout.CENTER);
	}
	
	@Override
	public void destroy() {
		makePreview.destroy();
		filePreview.destroy();
	}
	
	private void updateMakeImages() {
		ArrayList<BufferedImage> images = new ArrayList<>(mFileArray.size());
		for(int i = 0; i < mFileArray.size(); i++)
			images.add(i, mFileArray.get(i).image);
		makePreview.setImages(images);
	}
	
	public static class ImageFile {
		
		File file;
		BufferedImage image;
		
		public ImageFile(File f) {
			
			file = f;
			if(f == null)
				return;
			
			try {
				image = ImageIO.read(f);
			} catch(Exception e) {}
			
		}
		
		@Override
		public String toString() {
			if(file != null)
				return file.toString();
			return "nullFile";
		}
		
		public String getName() {
			if(file != null)
				return file.getName();
			return "null";
		}
		
	}
	
}
