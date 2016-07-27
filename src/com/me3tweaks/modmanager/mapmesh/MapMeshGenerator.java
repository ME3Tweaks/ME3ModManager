package com.me3tweaks.modmanager.mapmesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

/**
 * Generates a map mesh from a ScriptDump4 file.
 * 
 * @author Mgamerz
 *
 */
public class MapMeshGenerator {
	static String endentry = "=======================================================================";
	private static ArrayList<ReachSpec> reachspecs = new ArrayList<>();
	private static ArrayList<PathNode> pathnodes = new ArrayList<>();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File infile = new File(args[0]);
		PathNode findingXYZ = null;
		if (infile != null) {
			//Parse PathNodes.
			try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
				for (String line; (line = br.readLine()) != null;) {
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
							findingXYZ.setX(Double.parseDouble(endindex));
						}

						if (line.contains(" Y : ")) {
							String endindex = line.substring(line.indexOf(':') + 1);
							while (endindex.contains(":")) {
								endindex = endindex.substring(endindex.indexOf(':') + 1).trim();
							}
							findingXYZ.setY(Double.parseDouble(endindex));
						}

						if (line.contains(" Z : ")) {
							String endindex = line.substring(line.indexOf(':') + 1);
							while (endindex.contains(":")) {
								endindex = endindex.substring(endindex.indexOf(':') + 1).trim();
							}
							findingXYZ.setZ(Double.parseDouble(endindex));
						}
					}

					if (line.contains("TheWorld.PersistentLevel.PathNode(PathNode)") || line.contains("TheWorld.PersistentLevel.SFXEnemySpawnPoint(SFXEnemySpawnPoint)")
							|| line.contains("TheWorld.PersistentLevel.SFXDoorMarker(SFXDoorMarker)") || line.contains("TheWorld.PersistentLevel.CoverLink") || line.contains("TheWorld.PersistentLevel.CoverSlotMarker") || line.contains("TheWorld.PersistentLevel.SFXNav_BoostNode(SFXNav_BoostNode)") ) {
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
						} else if (line.contains("TheWorld.PersistentLevel.SFXNav_BoostNode(SFXNav_BoostNode)")){
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
							System.out.println("END INDEX: " + endindex);
						}
						if (line.contains("Name: \"Start\" Type: \"ObjectProperty\" Size: 4 Value:")) {
							//start
							String startindex = line.substring(line.indexOf(':') + 1);
							while (startindex.contains(":")) {
								startindex = startindex.substring(startindex.indexOf(':') + 1).trim();
							}
							startindex = startindex.substring(0, startindex.indexOf('(')).trim();
							findingreachspec.setStartindex(Integer.parseInt(startindex));
							System.out.println("START INDEX: " + startindex);
						}
					}

					if (line.contains("TheWorld.PersistentLevel.ReachSpec(ReachSpec)") || line.contains("TheWorld.PersistentLevel.SlotToSlotReachSpec(SlotToSlotReachSpec)")) {
						System.out.println("ReachSpec: " + line);
						findingreachspec = new MapMeshGenerator.ReachSpec();
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
			}
		}

		for (ReachSpec spec : reachspecs) {
			//	System.out.println(spec);
		}

		int leftmost = 0;
		int topmost = 0;
		for (PathNode node : pathnodes) {
			node.resolveNodes();
			if (node.getX() == 0) {
				leftmost = (int) node.getX();
			}
			if (node.getY() == 0) {
				topmost = (int) node.getY();
			}
			System.out.println(node);
		}

		MapMeshViewer points = new MapMeshViewer();
		points.setNodes(pathnodes);
		System.out.println("Coordinates: " + leftmost + ", " + topmost);
		points.setXoffset(leftmost);
		points.setYoffset(topmost);
		JFrame frame = new JFrame("Points");
		points.setFparent(frame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(points);
		frame.setSize(250, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
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
			return "Pathnode...";
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

		public boolean resolveNodes() {
			boolean first = true;
			for (int rindex : reachSpecIndexes) {
				if (!first)
					continue;
				ReachSpec rs = getReachSpecByIndex(rindex);
				if (rs == null) {
					System.err.println("No reachspec found for " + rindex);
					continue;
				}
				PathNode node = getPathNodeByIndex(rs.endindex);
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

	public static PathNode getPathNodeByIndex(int endindex) {
		for (PathNode p : pathnodes) {
			if (p.getIndex() == endindex) {
				return p;
			}
		}
		return null;
	}

	public static ReachSpec getReachSpecByIndex(int rindex) {
		for (ReachSpec r : reachspecs) {
			if (r.index == rindex) {
				return r;
			}
		}
		return null;
	}
}
