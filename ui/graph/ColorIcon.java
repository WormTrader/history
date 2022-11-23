package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)ColorIcon.java 1.00 20140612
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* ColorIcon: Creates square colored icon.
*
* @author Rick Salamone
* @version 1.00
* 20140612 rts created
*******************************************************/
import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import com.shanebow.util.Args;

/**
* Represents a square icon having no graphical content.
*
* <P>Intended for use with <tt>Action</tt> and <tt>JMenuItem</tt>. 
* Alignment of text is poor when the same menu mixes menu items without an icon with 
* menu items having an icon. In such cases, items without an icon can use 
* an <tt>ColorIcon</tt> to take up the proper amount of space, and allow 
* for alignment of all text in the menu.
*/
final class ColorIcon implements Icon {

	/**
	* Convenience object for small icons, whose size matches the size of 
	* small icons in Sun's graphics repository.
	*/
	static final ColorIcon SIZE_16 = new ColorIcon(16);

	/**
	* Convenience object for large icons, whose size matches the size of 
	* large icons in Sun's graphics repository.
	*/
	static final ColorIcon SIZE_24 = new ColorIcon(24);

	/**
	* ColorIcon objects are always square, having identical height and width.
	*
	* @param aSize length of any side of the icon in pixels, must 
	* be in the range 1..100 (inclusive).
	*/
	ColorIcon(int aSize) {fSize = aSize;}

	/**
	* Return the icon size (width is same as height).
	*/
	public int getIconWidth() {return fSize;}

	/**
	* Return the icon size (width is same as height).
	*/
	public int getIconHeight() {return fSize;}

	/**
	* This implementation is empty, and paints nothing.
	*/
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.GREEN);
		g.fillRect(x, y, fSize-1, fSize-1);

		g.setColor(Color.BLACK);
		g.drawRect(x, y, fSize-1, fSize-1);
		}

	// PRIVATE //
	private int fSize;
	}
