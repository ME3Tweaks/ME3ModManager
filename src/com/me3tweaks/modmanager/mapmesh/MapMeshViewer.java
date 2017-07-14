package com.me3tweaks.modmanager.mapmesh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.objects.PCCDumpOptions;
import com.me3tweaks.modmanager.objects.ProcessResult;

import net.iharder.dnd.FileDrop;

/**
 * Generates a map mesh from a ScriptDump4 file.
 * 
 * @author Mgamerz
 *
 */
public class MapMeshViewer extends JFrame implements ActionListener {
	static String endentry = "=======================================================================";
	private ArrayList<ReachSpec> reachspecs = new ArrayList<>();
	private ArrayList<PathNode> pathnodes = new ArrayList<>();
	private JPanel mainPanel;
	private JLabel viewinFileLabel;
	MapMeshPanel mmp;
	JCheckBox spawnCheck;
	JCheckBox boostCheck;
	JCheckBox coverCheck;
	JCheckBox doorCheck;
	JCheckBox standardCheck;
	String currentFile;
	private JButton setCoordinatesButton;

	public MapMeshViewer() {
		ModManager.debugLogger.writeMessage("Opening pathfinding viewer");
		setupWindow();
		setVisible(true);
	}

	/**
	 * Open pathfinding viewer with the listed file
	 * 
	 * @param openfile
	 *            file to open and view on start
	 */
	public MapMeshViewer(File openfile) {
		ModManager.debugLogger.writeMessage("Opening pathfinding viewer with specified file: " + openfile);
		setupWindow();
		takeActionOnFile(openfile);
		setVisible(true);
	}

	private void setupWindow() {
		// TODO Auto-generated method stub
		setIconImages(ModManager.ICONS);
		JButton browse = new JButton("Open File");
		browse.setFocusable(false);
		mainPanel = new JPanel(new BorderLayout());
		viewinFileLabel = new JLabel("Open or drop a pcc/dump file to view");
		viewinFileLabel.setBorder(new EmptyBorder(3, 3, 3, 3));

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(viewinFileLabel, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout());
		setCoordinatesButton = new JButton("Set coordinates");
		setCoordinatesButton.addActionListener(this);
		setCoordinatesButton.setEnabled(false);
		setCoordinatesButton.setFocusable(false);
		buttons.add(setCoordinatesButton);
		buttons.add(browse);

		topPanel.add(buttons, BorderLayout.EAST);

		JPanel itemPanel = new JPanel(new FlowLayout());
		standardCheck = new JCheckBox("Pathing");
		spawnCheck = new JCheckBox("Spawns");
		doorCheck = new JCheckBox("Doors");
		coverCheck = new JCheckBox("Cover");
		boostCheck = new JCheckBox("Boost");

		standardCheck.setSelected(true);
		spawnCheck.setSelected(true);
		doorCheck.setSelected(true);
		coverCheck.setSelected(true);
		boostCheck.setSelected(true);

		standardCheck.addActionListener(this);
		spawnCheck.addActionListener(this);
		doorCheck.addActionListener(this);
		coverCheck.addActionListener(this);
		boostCheck.addActionListener(this);

		standardCheck.setFocusable(false);
		spawnCheck.setFocusable(false);
		doorCheck.setFocusable(false);
		coverCheck.setFocusable(false);
		boostCheck.setFocusable(false);

		itemPanel.add(standardCheck);
		itemPanel.add(spawnCheck);
		itemPanel.add(doorCheck);
		itemPanel.add(coverCheck);
		itemPanel.add(boostCheck);

		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(itemPanel, BorderLayout.SOUTH);
		add(mainPanel);

		new FileDrop(mainPanel, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				if (files.length > 0) {
					File f = files[0];
					if (f.exists() && f.isFile()) {
						ModManager.debugLogger.writeMessage("File dropped onto Map Mesh Viewer: " + f);
						takeActionOnFile(f);
					}
				}
			}

		});

		setTitle("Mod Manager Map Pathfinding Viewer");
		setMinimumSize(new Dimension(800, 500));
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		browse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				//In response to a button click:
				int returnVal = fc.showOpenDialog(MapMeshViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					openFile(file);
				}
			}
		});
	}

	/**
	 * Shared action on file
	 * 
	 * @param f
	 *            file to take action on
	 */
	private void takeActionOnFile(File f) {
		String extension = FilenameUtils.getExtension(f.getAbsolutePath());
		ModManager.debugLogger.writeMessage("File dropped onto Map Mesh Viewer: " + f);
		ModManager.debugLogger.writeMessage("Extension: " + extension);
		switch (extension) {
		case "txt":
			openFile(f);
			break;
		case "pcc":
			String transplanter = ModManager.getGUITransplanterCLI(true);
			if (transplanter != null) {
				viewinFileLabel.setText("Parsing file - this will take a few seconds...");
				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						String biogamedir = ModManagerWindow.GetBioGameDir();
						String directory = new File(biogamedir).getParent();
						PCCDumpOptions options = new PCCDumpOptions();
						options.coalesced = false;
						options.exports = true;
						options.gamePath = directory;
						options.names = true;
						options.properties = true;
						options.scripts = false;
						options.outputFolder = ModManager.getPCCDumpFolder();
						ProcessResult pr = ModManager.dumpPCC(f.getAbsolutePath(), options);
						if (pr.getReturnCode() == 0) {
							File outfile = new File(ModManager.getPCCDumpFolder() + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".txt");
							if (outfile.exists()) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										openFile(outfile);
									}
								});
							} else {
								ModManager.debugLogger.writeError("Output file does not exist: " + outfile);
							}
						}
					}
				});
			}
			break;
		}
	}

	protected void openFile(File file) {
		String oldfile = currentFile;
		currentFile = file.getName();
		MapMeshPanel panel = loadFile(file.getAbsolutePath());
		if (panel != null) {
			mmp = panel;
			mainPanel.add(panel, BorderLayout.CENTER);
			panel.requestFocusInWindow();
			//viewinFileLabel.setText("Viewing " + file.getName());
			revalidate();
			setCoordinatesButton.setEnabled(true);
		} else {
			currentFile = oldfile;
		}
	}

	private MapMeshPanel loadFile(String infile) {
		ModManager.debugLogger.writeMessage("Loading pathfinding file: " + infile);
		PathNode findingXYZ = null;
		if (infile != null) {
			//Parse PathNodes.
			try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
				for (String line; (line = br.readLine()) != null;) {
					boolean newline = false;
					if (findingXYZ != null) {
						if (line.contains("ReachSpec [EXPORT")) {
							String exportindex = line.substring(line.indexOf('[') + 8, line.indexOf(']'));
							findingXYZ.addConnectingReachSpec(Integer.parseInt(exportindex));
						}

						if (line.contains(" X : ")) {
							String endindex = line.substring(line.indexOf(':') + 1);
							while (endindex.contains(":")) {
								endindex = endindex.substring(endindex.indexOf(':') + 1).trim();
							}
							//System.out.print("X: " + endindex);
							newline = true;
							findingXYZ.setX(Double.parseDouble(endindex));
						}

						if (line.contains(" Y : ")) {
							String endindex = line.substring(line.indexOf(':') + 1);
							while (endindex.contains(":")) {
								endindex = endindex.substring(endindex.indexOf(':') + 1).trim();
							}
							//System.out.print(" Y: " + endindex);
							newline = true;

							findingXYZ.setY(Double.parseDouble(endindex));
						}

						if (line.contains(" Z : ")) {
							String endindex = line.substring(line.indexOf(':') + 1);
							while (endindex.contains(":")) {
								endindex = endindex.substring(endindex.indexOf(':') + 1).trim();
							}
							findingXYZ.setZ(Double.parseDouble(endindex));
							newline = true;

							//System.out.print(" Z: " + endindex);
						}
						//if (newline) {
						//	System.out.println();
						//}
					}

					if (line.contains("TheWorld.PersistentLevel.PathNode(PathNode)") || line.contains("TheWorld.PersistentLevel.SFXEnemySpawnPoint(SFXEnemySpawnPoint)")
							|| line.contains("TheWorld.PersistentLevel.SFXDoorMarker(SFXDoorMarker)") || line.contains("TheWorld.PersistentLevel.CoverLink")
							|| line.contains("TheWorld.PersistentLevel.CoverSlotMarker") || line.contains("TheWorld.PersistentLevel.SFXNav_BoostNode(SFXNav_BoostNode)")) {
						String index = line.trim().substring(line.indexOf('#') + 1, line.indexOf(' ')).trim();
						findingXYZ = new PathNode(Integer.parseInt(index));
						if (line.contains("TheWorld.PersistentLevel.PathNode(PathNode)")) {
							findingXYZ.setNodeType(PathNode.NODE_STANDARD);
						} else if (line.contains("TheWorld.PersistentLevel.SFXEnemySpawnPoint(SFXEnemySpawnPoint)")) {
							findingXYZ.setNodeType(PathNode.NODE_ENEMYSPAWN);
						} else if (line.contains("TheWorld.PersistentLevel.SFXDoorMarker(SFXDoorMarker)")) {
							findingXYZ.setNodeType(PathNode.NODE_DOORMARKER);
						} else if (line.contains("TheWorld.PersistentLevel.CoverLink")) {
							findingXYZ.setNodeType(PathNode.NODE_COVERLINK);
						} else if (line.contains("TheWorld.PersistentLevel.SFXNav_BoostNode(SFXNav_BoostNode)")) {
							findingXYZ.setNodeType(PathNode.NODE_BOOST);
						}

					}

					if (line.equals(endentry)) {
						//commit
						if (findingXYZ != null) {
							pathnodes.add(findingXYZ);
						}
						findingXYZ = null;
						continue;
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ReachSpec findingreachspec = null;

			//Parse ReachSpecs.
			try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
				for (String line; (line = br.readLine()) != null;) {
					if (findingreachspec != null) {
						if (line.contains("Actor : ")) {
							//end
							String endindex = line.substring(line.indexOf(':') + 1);
							while (endindex.contains(":")) {
								endindex = endindex.substring(endindex.indexOf(':') + 1).trim();
							}
							endindex = endindex.substring(0, endindex.indexOf('(')).trim();
							findingreachspec.setEndindex(Integer.parseInt(endindex));
							//System.out.println("END INDEX: " + endindex);
						}
						if (line.contains("Name: \"Start\" Type: \"ObjectProperty\" Size: 4 Value:")) {
							//start
							String startindex = line.substring(line.indexOf(':') + 1);
							while (startindex.contains(":")) {
								startindex = startindex.substring(startindex.indexOf(':') + 1).trim();
							}
							startindex = startindex.substring(0, startindex.indexOf('(')).trim();
							findingreachspec.setStartindex(Integer.parseInt(startindex));
							//System.out.println("START INDEX: " + startindex);
						}
					}

					if (line.contains("TheWorld.PersistentLevel.ReachSpec(ReachSpec)") || line.contains("TheWorld.PersistentLevel.SlotToSlotReachSpec(SlotToSlotReachSpec)")) {
						//System.out.println("ReachSpec: " + line);
						findingreachspec = new MapMeshViewer.ReachSpec();
						String index = line.trim().substring(line.indexOf('#') + 1, line.indexOf(' ')).trim();
						findingreachspec.setIndex(Integer.parseInt(index));
						if (line.contains("TheWorld.PersistentLevel.ReachSpec(ReachSpec)")) {
							findingreachspec.setReachSpecType(ReachSpec.SPEC_STANDARD);
						} else if (line.contains("TheWorld.PersistentLevel.SlotToSlotReachSpec(SlotToSlotReachSpec)")) {
							findingreachspec.setReachSpecType(ReachSpec.SPEC_SLOTTOSLOT);

						}
					}

					if (line.equals(endentry)) {
						//commit
						if (findingreachspec != null) {
							reachspecs.add(findingreachspec);
						}
						findingreachspec = null;
						continue;
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		int leftmost = 1000000;
		int topmost = 1000000;
		for (PathNode node : pathnodes) {
			node.resolveNodes(this);
			if (node.getX() < leftmost && node.getX() != 0 && node.getY() < topmost && node.getY() != 0) {
				leftmost = (int) node.getX();
				topmost = (int) node.getY();
				//System.out.println("New topmost: " + topmost);
				//System.out.println("New leftmost: " + leftmost);
			}
			//if (ModManager.IS_DEBUG) {
			//	System.out.println(node);
			//}
		}

		MapMeshPanel points = new MapMeshPanel(this, leftmost, topmost);

		points.setNodes(pathnodes);
		if (leftmost == 1000000) {
			leftmost = 1200;
		}
		if (topmost == 1000000) {
			topmost = 1200;
		}
		//System.out.println("Coordinates: " + leftmost + ", " + topmost);
		points.setXoffset(-leftmost);
		points.setYoffset(-topmost);
		return points;
	}

	static class PathNode {
		public static final int NODE_BOOST = 4;
		public static final int NODE_DOORMARKER = 2;
		public static final int NODE_STANDARD = 0;
		public static final int NODE_ENEMYSPAWN = 1;
		public static final int NODE_COVERLINK = 3;

		public int getNodeType() {
			return nodeType;
		}

		public void setNodeType(int nodeType) {
			this.nodeType = nodeType;
		}

		private int index;
		private double z;
		private double y;
		private double x;
		private int nodeType = NODE_STANDARD;
		private ArrayList<Integer> reachSpecIndexes = new ArrayList<>();
		private ArrayList<PathNode> connectingNodes = new ArrayList<>();

		@Override
		public String toString() {
			return "Pathnode " + x + "," + y + "," + z;
		}

		public ArrayList<PathNode> getConnectingNodes() {
			return connectingNodes;
		}

		public void setConnectingNodes(ArrayList<PathNode> connectingNodes) {
			this.connectingNodes = connectingNodes;
		}

		public PathNode(int index) {
			this.setIndex(index);
		}

		public double getZ() {
			return z;
		}

		public void setZ(double z) {
			this.z = z;
		}

		public double getY() {
			return y;
		}

		public void setY(double y) {
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public void addConnectingReachSpec(int index) {
			reachSpecIndexes.add(index);
		}

		public boolean resolveNodes(MapMeshViewer map) {
			boolean first = true;
			for (int rindex : reachSpecIndexes) {
				if (!first)
					continue;
				ReachSpec rs = getReachSpecByIndex(rindex, map);
				if (rs == null) {
					//System.err.println("No reachspec found for " + rindex);
					continue;
				}
				PathNode node = getPathNodeByIndex(rs.endindex, map);
				if (node == null) {
					continue;
				}
				connectingNodes.add(node);
			}
			return true;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

	}

	static class ReachSpec {
		public static final int SPEC_SLOTTOSLOT = 1;
		public static final int SPEC_STANDARD = 0;
		private int spectype;

		@Override
		public String toString() {
			return "ReachSpec [startindex=" + startindex + ", endindex=" + endindex + ", reachflags=" + reachflags + ", index=" + index + "]";
		}

		public int getSpectype() {
			return spectype;
		}

		public void setSpectype(int spectype) {
			this.spectype = spectype;
		}

		public void setReachSpecType(int spectype) {
			this.spectype = spectype;

		}

		public int startindex, endindex;
		public int reachflags;
		public int index;

		public ReachSpec() {

		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getStartindex() {
			return startindex;
		}

		public void setStartindex(int startindex) {
			this.startindex = startindex;
		}

		public int getEndindex() {
			return endindex;
		}

		public void setEndindex(int endindex) {
			this.endindex = endindex;
		}

		public int getReachflags() {
			return reachflags;
		}

		public void setReachflags(int reachflags) {
			this.reachflags = reachflags;
		}
	}

	public static PathNode getPathNodeByIndex(int endindex, MapMeshViewer map) {
		for (PathNode p : map.pathnodes) {
			if (p.getIndex() == endindex) {
				return p;
			}
		}
		return null;
	}

	public static ReachSpec getReachSpecByIndex(int rindex, MapMeshViewer map) {
		for (ReachSpec r : map.reachspecs) {
			if (r.index == rindex) {
				return r;
			}
		}
		return null;
	}

	public void setPositionText(String string) {
		viewinFileLabel.setText("Viewing " + currentFile + " | " + string);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			if (mmp != null) {
				mmp.repaint();
			}
		} else if (e.getSource() == setCoordinatesButton) {
			String result = JOptionPane.showInputDialog(this, "Enter the top left coordinates to view in the format X,Y. (e.g. 200,300)", "Enter coordinates",
					JOptionPane.QUESTION_MESSAGE);

			int indexOfComma = result.indexOf(',');
			if (indexOfComma > 0) {
				String X = result.substring(0, indexOfComma);
				String Y = result.substring(indexOfComma + 1, result.length());
				try {
					int xint = Integer.parseInt(X);
					int yint = Integer.parseInt(Y);
					if (mmp != null) {
						mmp.setXoffset(xint);
						mmp.setYoffset(yint);
						mmp.repaint();
					}
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Invalid X,Y coordinates: " + result);
				}
			} else {
				JOptionPane.showMessageDialog(this, "Invalid X,Y coordinates, missing comma: " + result);

			}
		}
	}
}
