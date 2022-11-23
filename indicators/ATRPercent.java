package com.wormtrader.history.indicators;
/********************************************************************
* @(#)ATR.java 1.00 20100625
* Copyright © 2011-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ATRPercent: Average True Range as a percent of closing price. As
* suggested by Van Tharp, this measure of volatility allows apples
* to apples comparisons of ATR for a single asset over an extended
* time period or between different assets.
*
* @author Rick Salamone
* @version 1.00
* 20100625 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

final public class ATRPercent
	extends ATR
	{
	public static final String STUDY_NAME="ATR%";
	public String getName() { return STUDY_NAME; }

	public ATRPercent( BarList bars ) { this( bars, "" ); }
	public ATRPercent( BarList bars, String params ) { super(bars, params); }

	public String getToolTipText(int x)
		{
		return SBFormat.toDollarString(getValue(x)) + "%";
		}

	protected void checkMinMax(int atr, Bar aBar)
		{
		int value = atr * 10000 / aBar.getClose();
		if ( value > m_maxValue ) m_maxValue = value;
		else if ( value < m_minValue ) m_minValue = value;
		}

	public int getValue(int index)
		{
		return getATR(index) * 10000 / m_bars.get(index).getClose();
		}
	public int getValue ()
		{
		int close = getClose();
		return (close == 0)? 0 : (super.getValue() * 10000 / close);
		}
	}
