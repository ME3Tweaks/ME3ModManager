package com.me3tweaks.modmanager.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.me3tweaks.modmanager.CustomDLCWindow;
import com.me3tweaks.modmanager.SelectiveRestoreWindow;

public class CustomDLCManagerTableCellRenderer extends DefaultTableCellRenderer {
	public static Color badColor = new Color(255, 140, 140);
	private static Color naColor = Color.gray;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value == null)
			return c;
		switch (column) {
		case CustomDLCWindow.COL_MOUNT_PRIORITY:
			setHorizontalAlignment(JLabel.CENTER);
			break;
		default:
			c.setBackground(null);
			c.setToolTipText(null);
			break;
		}
		return this;
	}
}
