package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)MASettings.java 1.00 20080518
* Copyright © 2007 - 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* MASettings: Moving Average settings include the type of average (simple or
* exponential), the period, the price (OHLC) used, and the display color.
*
* @author Rick Salamone
* @version 1.00
* 20080518 rts created
* 20120719 rts added an offset setting and code to freeze/thaw self
* 20120721 rts added smoothed & weighted moving averages
* 20140319 rts added Regression Indicator
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.history.indicators.*;
import java.awt.Color;

public final class MASettings
	{
	// Which method does this average use?
	public static final int	USE_NONE = 0;	// No Average Used
	public static final int	USE_SMA = 1;	// Simple Moving Average
	public static final int	USE_EMA = 2;	// Exponential Lag
/**
	public static final int	USE_SmMA = 3;	// Smoothed Moving Average
	public static final int	USE_WMA = 4;	// Weighted Moving Average
	public static final int	USE_RL = 5;	// Regression Line
**/
	public static final String[] AVG_METHODS =
		{
		"None",          // USE_NONE = 0
		"SMA",           // USE_SMA = 1
		"EMA",	          // USE_EXP = 2
		"SmMA",	          // USE_SmMA = 3
		"WMA",	          // USE_WMA = 4
		RegressionIndicator.STUDY_NAME,	          // USE_RL = 5
		};

	private boolean m_isActive = true;
	private Color   m_color;
	private int     m_period;
	private int     m_offset = 0;
	private int     m_avgType = USE_SMA;
	private char    m_whichPrice = Bar.PRICE_CLOSE;

	public MASettings( int type, int period, char whichPrice, Color c )
		{
		m_color = c;
		m_period = period;
		m_avgType = type;
		m_whichPrice = whichPrice;
		}

	public boolean isActive()      { return m_isActive; }
	public int     getAvgType()    { return m_avgType; }
	public String  getAvgName()    { return AVG_METHODS[m_avgType]; }
	public Color   getColor()      { return m_color; }
	public int     getPeriod()     { return m_period; }
	public int     getOffset()     { return m_offset; }
	public char    getWhichPrice() { return m_whichPrice; }

	public void    setActive(boolean a)  { m_isActive = a; }
	public void    setAvgType(int t)     { m_avgType = t; }
	public void    setName(String n)
		{
		for (int i = 1; i < AVG_METHODS.length; i++ )
			if ( n.equals(AVG_METHODS[i]))
				{ m_avgType = i; return; }
		m_avgType = 0;
		}
	public void    setColor(Color c)     { m_color = c; }
	public void    setColor(int rgb)     { m_color = new Color(rgb); }
	public void    setPeriod(int p)      { m_period = p; }
	public void    setOffset(int o)      { m_offset = o; }
	public void    setWhichPrice(char p) { m_whichPrice = p; }
	public String  toCSV()
		{
		return (isActive() ? "x" : "_")
		   + ": " + getAvgName()
		   + ": " + getPeriod()
		   + ": " + getWhichPrice()
		   + ": " + getOffset()
		   + ": " + getColor().getRGB();
		}

	public boolean set(String[] pieces)
		{
		try
			{
			setActive(pieces[0].charAt(0) == 'x');
			setName(pieces[1]);
			setPeriod(Integer.parseInt(pieces[2]));
			setWhichPrice(pieces[3].charAt(0));
			setOffset(Integer.parseInt(pieces[4]));
			setColor(Integer.parseInt(pieces[5]));
			return true;
			}
		catch (Exception e) { return false; }
		}
	}
