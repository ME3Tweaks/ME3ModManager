package com.me3tweaks.modmanager.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.me3tweaks.modmanager.SelectiveRestoreWindow;

public class SelectiveRestoreTableCellRenderer extends DefaultTableCellRenderer {
	public static Color badColor = new Color(255, 140, 140);
	private static Color naColor = Color.gray;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value == null)
			return c;
		setHorizontalAlignment(JLabel.CENTER);
		switch (column) {
		case SelectiveRestoreWindow.COL_MODIFIED:
			if (value.toString().startsWith("MODIFIED")) {
				c.setBackground(badColor);
				c.setFont(c.getFont().deriveFont(Font.BOLD));
				c.setToolTipText("SFAR DLC size does not match known original");
			} else {
				c.setBackground(null);
			}
			break;
		case SelectiveRestoreWindow.COL_BACKEDUP:
			if (value.toString().equalsIgnoreCase("NO")) {
				c.setFont(c.getFont().deriveFont(Font.BOLD));
				c.setToolTipText("DLC SFAR is not backed up. You should do so now via the Backup menu.");
				c.setBackground(badColor);
			} else {
				c.setBackground(null);
			}
			break;
		default:
			c.setBackground(null);
			c.setToolTipText(null);
			break;
		}
		return this;
	}
}
