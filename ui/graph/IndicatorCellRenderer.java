package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)IndicatorCellRenderer.java 1.00 20140612
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* IndicatorCellRenderer: Displays a color icon along with the name
* and params of an indicator in the choosen indicators JList of the
* chart configuration dialog.
*
* @author Rick Salamone
* @version 1.00
* 20140612 rts created
*******************************************************/
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class IndicatorCellRenderer
	extends JLabel
	implements ListCellRenderer
	{
	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called
 	public Component getListCellRendererComponent(
		JList list,              // the list
		Object value,            // value to display
		int index,               // cell index
		boolean isSelected,      // is the cell selected
		boolean cellHasFocus)    // does the cell have focus
		{
// System.out.println("IndicatorCellRenderer value " + value.getClass().getSimpleName());
		setFont(list.getFont());
		setOpaque(true);
		setText(value.toString());

		try { setIcon(new ColorIcon(16)); }
		catch (Exception e) {System.out.println("setIcon failed: " + e);}

		Color fg = Color.BLUE;
		setForeground(fg);
		setBackground(isSelected ? list.getSelectionBackground()
		                         : list.getBackground());
//		setEnabled(list.isEnabled());
		return this;
		}
	}
