package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Momentum.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Momentum
*   M = current close / n-day close * 100
*   n is usually 10
*   Bearish: M < 100 (95)
*   Bullish: M > 100 (105)
*   Neutral: M around 100
*
* @author Rick Salamone
* @version 1.00
* 2007     rts created
* 20130302 rts uses List
*******************************************************/
import com.wormtrader.bars.Close;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;
import java.util.List;

public final class Momentum implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Momentum";
	public String getName() { return STUDY_NAME; }
	private static final int BASE=100;

	private List<? extends Close>   m_closes;
	private int        m_minValue = Integer.MAX_VALUE;
	private int        m_maxValue = Integer.MIN_VALUE;
	private Color      m_color = Color.GREEN;
	private int        m_period = 12;

	public Momentum( List<? extends Close> closes )
		{
		this ( closes, "" );
		}
	public Momentum( List<? extends Close> closes, String params )
		{
		m_closes = closes;
		setParams(params);
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return "Momentum(" + m_period + "): " + value;
		}
	public String getClipLabel() { return "Mom(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		if ( x < m_period ) return "";
		return "" + getValue(x) + "%";
		}

	public void   setColor(Color c) { m_color = c; }
	public int    getRangeMaximum() { return (int)(1.02 * m_maxValue); }
	public int    getRangeMinimum() { return (int)(0.98 * m_minValue); }
	public void   dataChanged(int index)
		{
		int numCloses = m_closes.size();
		for ( int i = index; i < numCloses; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		if ( index == 0 )
			{
			m_minValue = m_maxValue = 100;
			return;
			}
		int mo = getValue(index);
		if ( mo > m_maxValue ) m_maxValue = mo;
		if ( mo < m_minValue ) m_minValue = mo;
		}
	public void   plot ( TGGraph graph, byte clip, int index )
		{
		if ((index-1) < m_period )
			return;
		graph.setColor( m_color );
		graph.mountain ( clip, BASE, getValue(index-1), getValue(index));
		graph.setColor( Color.GRAY ); // plot baseline
		graph.connect ( clip, BASE, BASE );
		}

	public final String getMetrics(int index) { return "" + getValue(index); }
	public final int getValue(int index)
		{
		if ( index < m_period )
			return BASE;
		Close close = m_closes.get(index);
		int priceNow = close.getPrice();
		close = m_closes.get(index - m_period);
		int priceThen = close.getPrice();
		return 100 * priceNow / priceThen;
		}
	}
