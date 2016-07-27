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

import com.me3tweaks.modmanager.mapmesh.MapMeshGenerator.PathNode;

public class MapMeshViewer extends JPanel {

	protected static JFrame fparent;
	private ArrayList<PathNode> nodes;

	public static JFrame getFparent() {
		return fparent;
	}

	public static void setFparent(JFrame fparent) {
		MapMeshViewer.fparent = fparent;
	}

	private int xoffset = 1000;
	private int yoffset = 0;
	private double scale = 1;
	private int delta = -2000;
	private double pointdiameter = 10;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.scale(scale, scale);
		g2d.setColor(Color.red);
		boolean first = true;
		if (nodes != null) {
			for (PathNode node : nodes) {
				//System.out.println(node);
				Ellipse2D.Double circle = new Ellipse2D.Double(node.getX() - (pointdiameter / 2) + getXoffset(), node.getY() - (pointdiameter / 2) + getYoffset(), pointdiameter,
						pointdiameter);
				//System.out.println(circle.getCenterX()+", "+circle.getCenterY());
				switch (node.getNodeType()) {
				case PathNode.NODE_STANDARD:
					g2d.setColor(Color.RED);
					break;
				case PathNode.NODE_ENEMYSPAWN:
					g2d.setColor(Color.GREEN);
					break;
				case PathNode.NODE_DOORMARKER:
					g2d.setColor(Color.YELLOW);
					break;
				case PathNode.NODE_COVERLINK:
					g2d.setColor(Color.ORANGE);
					break;
				case PathNode.NODE_BOOST:
					g2d.setColor(Color.CYAN);
				}
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

	public MapMeshViewer() {
		setFocusable(true);
		requestFocusInWindow();
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
					System.out.println("Key pressed: " + e.getKeyCode());
					MapMeshViewer.this.repaint();
					break;
				case KeyEvent.VK_RIGHT:
					setXoffset(getXoffset() + delta);
					MapMeshViewer.this.repaint();
					break;
				case KeyEvent.VK_UP:
					setYoffset(getYoffset() + -delta);
					MapMeshViewer.this.repaint();
					break;
				case KeyEvent.VK_DOWN:
					setYoffset(getYoffset() + delta);
					MapMeshViewer.this.repaint();
					break;
				case KeyEvent.VK_PLUS:
				case KeyEvent.VK_ADD:
					setRelativeScale(0.1);
					break;
				case KeyEvent.VK_MINUS:
				case KeyEvent.VK_SUBTRACT:
					setRelativeScale(-0.1);
					break;
				default:
					System.out.println("Key not handled: " + e.getKeyCode());
				}
				MapMeshViewer.fparent.setTitle("Current Position: " + xoffset + ", " + yoffset + ", scale: " + scale);
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
	}

	public int getYoffset() {
		return yoffset;
	}

	public void setYoffset(int yoffset) {
		this.yoffset = yoffset;
	}

	public int getXoffset() {
		return xoffset;
	}

	public void setXoffset(int xoffset) {
		this.xoffset = xoffset;
	}
}
