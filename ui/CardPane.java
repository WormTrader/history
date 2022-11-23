package com.wormtrader.history.ui;
/********************************************************************
* @(#)CardPane.java 1.00 20120503
* Copyright (c) 2009 by Richard T. Salamone, Jr. All rights reserved.
*
* CardPane: A simple replacement for JTabbedPane that allows for
* additional controls in the tab area.
*
* @author Rick Salamone
* @version 1.00
* 20120503 rts created
* 20120508 rts supports tabs placed at top or bottom
* 20120605 rts added get/setSelectedIndex, setEnabledAt, getComponentAt
* 20121001 rts added isEnabledAt
*******************************************************/
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CardPane
	extends JPanel
	implements SwingConstants
	{
	private final JPanel fControls;
	private final JPanel fTabs = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
	private final CardLayout fCardLayout = new CardLayout();
	private final JPanel fCardPanel = new JPanel(fCardLayout);
	private final int fTabPlacement;

	private Tab fSelected;
	private Color highlight = new Color(200,200,200);
	private Color darkShadow = Color.DARK_GRAY; //new Color(150,150,150);
	private Color unselectedBackground;
	protected Color tabAreaBackground;
	protected Color selectColor;
	protected Color selectHighlight;

	public Component getComponentAt(int aIndex) { return fCardPanel.getComponent(aIndex); }
	public CardPane() { this(BOTTOM); }

	public CardPane(int aTabPlacement)
		{
		super(new BorderLayout());

		fTabPlacement = aTabPlacement;

		fControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 4))
			{
			@Override protected void paintChildren(Graphics g)
				{
				super.paintChildren(g);
				Dimension size = getSize();
				int y = (fTabPlacement == TOP)? size.height - 1 : 0;
				int right = size.width - 1;
				g.drawLine( 0, y, right, y );
				}
			};

		JPanel tabsAndControls = new JPanel(new BorderLayout())
			{
			@Override protected void paintChildren(Graphics g)
				{
				super.paintChildren(g);
				Dimension size = getSize();
				int y = (fTabPlacement == TOP)? size.height - 1 : 0;
				int right = size.width - 1;
				g.drawLine( 0, y, right, y );
				if (fSelected == null) return;
				Rectangle selected = fSelected.getBounds();
				g.setColor(selectColor);
				g.drawLine( selected.x, y, selected.x+selected.width, y );
				}
			};
		tabsAndControls.add(fTabs, BorderLayout.WEST);
		tabsAndControls.add(fControls, BorderLayout.CENTER);

		add(tabsAndControls, (fTabPlacement == TOP)? BorderLayout.NORTH
		                                           : BorderLayout.SOUTH);
		add(fCardPanel, BorderLayout.CENTER);
		}

	@Override public void updateUI()
		{
		super.updateUI();
		tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
		selectColor = UIManager.getColor("TabbedPane.selected");
		selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
		unselectedBackground = UIManager.getColor(
		                                    "TabbedPane.unselectedBackground");
		if ( fTabs != null )
			fTabs.setBackground(tabAreaBackground);
		}

	public void addTabBarComponents(Component... aCompList)
		{
		for ( Component comp : aCompList )
			fControls.add(comp);
		}

	public void addTab(String aTitle, Object icon, JComponent aComponent)
		{
		addTab(aTitle, icon, aComponent, null);
		}

	public void addTab(String aTitle, Object icon, JComponent aComponent, String aTooltip)
		{
		Tab tab = new Tab(aTitle, icon, aTooltip);
		fTabs.add(tab);
		fCardPanel.add(aTitle, aComponent);
		if (fSelected == null)
			select(tab);
		}

	final public int indexOfTab(String aTitle)
		{
		int it = 0;
		for ( Component tab : fTabs.getComponents())
			if ( ((Tab)tab).getText().equals(aTitle)) return it;
			else ++it;
		return -1;
		}

	private void select(Tab aTab) { setSelectedIndex(indexOfTab(aTab.getText())); }

	public int getSelectedIndex() { return indexOfTab(fSelected.getText()); }

	public final void setEnabledAt(int aIndex, boolean aEnabled)
		{
		((Tab)fTabs.getComponent(aIndex)).setEnabled(aEnabled);
		}

	public final boolean isEnabledAt(int aIndex)
		{
		return ((Tab)fTabs.getComponent(aIndex)).isEnabled();
		}

	public void setSelectedIndex(int aIndex)
		{
		Component[] tabs = fTabs.getComponents();
		fSelected = (Tab)tabs[aIndex];
		fCardLayout.show(fCardPanel, fSelected.getText());
		for ( Component tab : tabs)
			tab.repaint();
		}

	class Tab
		extends JLabel
		{
		public Tab(String aTitle, Object icon, String aTooltip)
			{
			super(aTitle, CENTER);
			setOpaque(true);
			addMouseListener(new MouseAdapter()
				{
				public void mousePressed(MouseEvent e) { selectMe(); }
				});
			setToolTipText(aTooltip);
			}
		private void selectMe() { if (isEnabled()) select(this); }
		public Dimension getPreferredSize() { return new Dimension(60,21); }
		@Override public void paint(Graphics g)
			{
			Dimension size = getSize();
int x = 0;
int y = 0;
int w = size.width;
int h = size.height;
			boolean isSelected = fSelected == this;
			g.setColor( tabAreaBackground );
			g.fillRect( 0, 0, size.width,size.height);
			g.setColor( isSelected? selectColor : unselectedBackground);
			if ( fTabPlacement == TOP )
				{
				g.fillRect( x + 5, y + 1, w - 5, h - 1);
				g.fillRect( x + 2, y + 4, 3, h - 4 );
				paintTopTabBorder( g, x, y, w, h, isSelected );
				}
			else
				{
				g.fillRect( x + 2, y, w - 2, h - 4 );
				g.fillRect( x + 5, y + (h - 1) - 3, w - 5, 3 );
				paintBottomTabBorder( g, x, y, w, h, isSelected );
				}
			g.setColor(isEnabled()? Color.BLACK : Color.GRAY);
g.setFont(getFont());
			g.drawString( getText(), 11, h - 6);
			}

		protected void paintTopTabBorder( Graphics g, int x, int y, int w, int h,
		                                  boolean isSelected )
			{
			int bottom = h - 1;
			int right = w - 1;

			// Paint Highlight
			g.setColor( isSelected ? selectHighlight : highlight );
			g.drawLine( 1, 6, 6, 1 ); // Paint slant
			g.drawLine( 1, 6, 1, bottom ); // Paint left
			g.drawLine( 6, 1, right, 1 ); // Paint top

			// Paint Border
			g.setColor( darkShadow );
			g.drawLine( 1, 5, 6, 0 ); // Paint slant
			g.drawLine( 0, 5, 0, bottom ); // Paint left
			g.drawLine( 6, 0, right, 0 ); // Paint top
			g.drawLine( right, 0, right, bottom ); // Paint right

			if (!isSelected)
				g.drawLine( 0, bottom, right, bottom ); // Paint bottom
			}

		protected void paintBottomTabBorder( Graphics g, int x, int y, int w, int h,
		                                     boolean isSelected )
			{
			int bottom = h - 1;
			int right = w - 1;

			// Paint Highlight
			g.setColor( isSelected ? selectHighlight : highlight );
			g.drawLine( 1, bottom - 6, 6, bottom - 1 ); // Paint slant
			g.drawLine( 1, 0, 1, bottom - 6 ); // Paint left

			// Paint Border
			g.setColor( darkShadow );
			g.drawLine( 1, bottom - 5, 6, bottom ); // Paint slant
			g.drawLine( 6, bottom, right, bottom ); // Paint bottom
		//	if ( tabIndex == lastIndex )
				g.drawLine( right, 0, right, bottom ); // Paint right
			g.drawLine( 0, 0, 0, bottom-6 ); // Paint left

			if (!isSelected)
				g.drawLine( 0, 0, right, 0 ); // Paint top
			}
		}
	}
