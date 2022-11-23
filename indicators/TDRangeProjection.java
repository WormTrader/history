package com.wormtrader.history.indicators;
/********************************************************************
* @(#)TDRangeProjection.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TDRange Projection:
*
* @author Rick Salamone
* @version 1.00
* 2007     rts created
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import java.awt.Color;

public final class TDRangeProjection
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="TDRange Projection";
	public String getName() { return STUDY_NAME; }

	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = 0;
	private Color[]     m_color = { Color.YELLOW };

	public static int[] compute (Bar bar )
		{
		int open = bar.getOpen(); int close = bar.getClose();
		int high = bar.getHigh(); int low = bar.getLow();
		int x;
		if ( close > open )
			x = (2 * high) + low + close;
		else if ( close < open )
			x = high + (2 * low) + close;
		else // if ( close == open )
			x = high + low + (2 * close);
		int[] range = new int[2];
		range[0] = x/2 - high; // projectedLow
		range[1] = x/2 - low; // projectedHigh
		return range;
		}

	public TDRangeProjection( BarList bars )
		{
		this ( bars, "" );
		}
	public TDRangeProjection( BarList bars, String params )
		{
		m_bars = bars;
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

	public String getRangeString(int value) { return STUDY_NAME + ": " + value; }
	public String getClipLabel() { return STUDY_NAME; }
	public String getToolTipText(int x) { return ""; }
	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
	public void dataChanged(int index)
		{
		compute(index);
		}
	private void compute(int index)
		{
		if ( index < 1 )
			{
			m_minValue = Integer.MAX_VALUE;
			m_maxValue = Integer.MIN_VALUE;
			return;
			}
		int[] range = compute( m_bars.get( index - 1 ));
		if ( range[0] < m_minValue ) m_minValue = range[0];
		if ( range[1] > m_maxValue ) m_maxValue = range[1];
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		int[] range = compute( m_bars.get( index - 1 ));
		graph.setColor( m_color[0] );
		graph.dash( clip, range[0] );
		graph.dash( clip, range[1] );
		}
	public String getMetrics ( int index )
		{
		if ( index < 1 ) return "";
		int[] range = compute( m_bars.get( index - 1 ));
		return SBFormat.toDollarString(range[0])
			+ " - " + SBFormat.toDollarString(range[1]);
		}
	}
