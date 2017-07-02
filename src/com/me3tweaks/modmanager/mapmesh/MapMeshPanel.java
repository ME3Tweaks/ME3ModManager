package com.me3tweaks.modmanager.mapmesh;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.me3tweaks.modmanager.mapmesh.MapMeshViewer.PathNode;

public class MapMeshPanel extends JPanel {

	protected MapMeshViewer fparent;
	private ArrayList<PathNode> nodes;

	public JFrame getFparent() {
		return fparent;
	}

	private int xoffset = 1000;
	private int yoffset = 0;
	private double scale = 0.005;
	private int delta = -2000;
	private double pointdiameter = 10;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		boolean standard = fparent.standardCheck.isSelected();
		boolean boost = fparent.boostCheck.isSelected();
		boolean cover = fparent.coverCheck.isSelected();
		boolean door = fparent.doorCheck.isSelected();
		boolean spawn = fparent.spawnCheck.isSelected();

		Graphics2D g2d = (Graphics2D) g;
		g2d.scale(scale, scale);
		g2d.setColor(Color.red);
		boolean first = true;
		if (nodes != null) {
			for (PathNode node : nodes) {
				switch (node.getNodeType()) {
				case PathNode.NODE_STANDARD:
					if (!standard) {
						continue;
					}
					g2d.setColor(Color.RED);
					break;
				case PathNode.NODE_ENEMYSPAWN:
					if (!spawn) {
						continue;
					}
					g2d.setColor(Color.GREEN);
					break;
				case PathNode.NODE_DOORMARKER:
					if (!door) {
						continue;
					}
					g2d.setColor(Color.YELLOW);
					break;
				case PathNode.NODE_COVERLINK:
					if (!cover) {
						continue;
					}
					g2d.setColor(Color.ORANGE);
					break;
				case PathNode.NODE_BOOST:
					if (!boost) {
						continue;
					}
					g2d.setColor(Color.CYAN);
				}
				//System.out.println(node);
				Ellipse2D.Double circle = new Ellipse2D.Double(node.getX() - (pointdiameter / 2) + getXoffset(), node.getY() - (pointdiameter / 2) + getYoffset(), pointdiameter,
						pointdiameter);
				//System.out.println(circle.getCenterX()+", "+circle.getCenterY());
				
				g2d.fill(circle);
				g2d.setColor(Color.MAGENTA);
				g2d.setFont(new Font("TimesRoman", Font.PLAIN, (int) (12 / scale)));
				g2d.drawString(Integer.toString(node.getIndex()), (int) (node.getX() - (pointdiameter / 2) + getXoffset() + 15 / scale),
						(int) (node.getY() - (pointdiameter / 2) + getYoffset() + 20 / scale));

				if (first) {
					for (PathNode connectingnode : node.getConnectingNodes()) {
						//System.out.println(connectingnode);
						drawArrow(g2d, (int) node.getX() + getXoffset(), (int) node.getY() + getYoffset(), (int) connectingnode.getX() + getXoffset(),
								(int) connectingnode.getY() + getYoffset());
					}
					//	first = false;
				}
			}
		}
	}

	private final int ARR_SIZE = 9;

	void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
		Graphics2D g = (Graphics2D) g1.create();

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		// Draw horizontal arrow starting in (0, 0)
		g.drawLine(0, 0, len, 0);
		g.fillPolygon(new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len }, new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 }, 4);
	}

	public void setNodes(ArrayList<PathNode> pathnodes) {
		this.nodes = pathnodes;
	}

	private static final int BASE_DELTA = -1000;
	private static final double BASE_DIAMETER = 10;

	public MapMeshPanel(MapMeshViewer fParent, long centerx, long centery) {
		this.fparent = fParent;
		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					setXoffset(getXoffset() + -delta);
					MapMeshPanel.this.repaint();
					break;
				case KeyEvent.VK_RIGHT:
					setXoffset(getXoffset() + delta);
					MapMeshPanel.this.repaint();
					break;
				case KeyEvent.VK_UP:
					setYoffset(getYoffset() + -delta);
					MapMeshPanel.this.repaint();
					break;
				case KeyEvent.VK_DOWN:
					setYoffset(getYoffset() + delta);
					MapMeshPanel.this.repaint();
					break;
				case KeyEvent.VK_Z:
				case KeyEvent.VK_PLUS:
				case KeyEvent.VK_ADD:
					setRelativeScale(0.05);
					break;
				case KeyEvent.VK_O:
				case KeyEvent.VK_MINUS:
				case KeyEvent.VK_SUBTRACT:
					setRelativeScale(-0.05);
					break;
				default:
					break;
				}
			}
		});
	}

	protected void setRelativeScale(double d) {
		if (scale + d > 0.05) {
			scale += d;
			System.out.println("Scale updated to " + scale);
			pointdiameter = BASE_DIAMETER / scale;
			delta = (int) (BASE_DELTA * (scale));
			repaint();
		} else if (scale > 0.01) {
			scale *= .5;
			pointdiameter = BASE_DIAMETER / scale;
			delta = Math.min((int) (BASE_DELTA * (scale)), -400);
			repaint();
		}
		fparent.setPositionText("Top Left Coordinate: " + xoffset + ", " + yoffset + " | Scale: " + scale);
	}

	public int getYoffset() {
		return yoffset;
	}

	public void setYoffset(int yoffset) {
		this.yoffset = yoffset;
		fparent.setPositionText("Top Left Coordinate: " + xoffset + ", " + yoffset + " | Scale: " + scale);
	}

	public int getXoffset() {
		return xoffset;
	}

	public void setXoffset(int xoffset) {
		this.xoffset = xoffset;
		fparent.setPositionText("Top Left Coordinate: " + xoffset + ", " + yoffset + " | Scale: " + scale);
	}
}
