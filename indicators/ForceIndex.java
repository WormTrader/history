package com.wormtrader.history.indicators;
/********************************************************************
* @(#)ForceIndex.java 1.00 2009????
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ForceIndex - Developed by Dr. Alexander Elder
*
* @author Rick Salamone
* @version 1.01
* 20120710 rts implemented setParams
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;
import java.util.List;

public final class ForceIndex
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Force Index";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 2;
	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = Integer.MIN_VALUE;
	private Color[]     m_color = { Color.BLUE };
	private int         m_period;
	private int[]       m_ema;   // smoothed force index
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public ForceIndex( BarList bars ) { this ( bars, "" ); }
	public ForceIndex( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_ema = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_ema = SBMisc.arrayExpand ( m_ema, _capacityIncrement );
		}

	public String getParams() { return "" + m_period; }

	public void setParams( String params )
		{
		int period = DEFAULT_PERIOD;
		try { period = Integer.parseInt(params.trim()); }
		catch (Exception e) {System.out.println(STUDY_NAME + " params: '" + params + "' " + e);}
		if (m_period == period) return; // unchanged
		m_period = period;
		dataChanged(0);
		}

	public String getRangeString(int value)
		{
		return "ForceIndex: " + value;
		}
	public String getClipLabel() { return "FI(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "FI(" + m_period + "): " + m_ema[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum()
		{
		return SBMath.maxAbsValue(m_maxValue, m_minValue);
		}
	public int getRangeMinimum()
		{
		return -SBMath.maxAbsValue(m_maxValue, m_minValue);
		}
	public void dataChanged(int index)
		{
		if ( index == 0 )
			{
			m_minValue = Integer.MAX_VALUE;
			m_maxValue = Integer.MIN_VALUE;
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		if ( m_ema.length <= index )
			enlargeArrays();
		m_index = index;
		int fi = getRawFI( index );
		if ( index == 0 ) // prime the pump
			{
			m_ema[0] = m_minValue = m_maxValue = fi;
			return;
			}
		m_ema[index] = SBMath.ema( fi, m_ema[index-1], m_period );
		if ( fi > m_maxValue ) m_maxValue = fi;
		if ( fi < m_minValue ) m_minValue = fi;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( Color.GRAY ); // plot zero line
		graph.connect ( clip, 0, 0 );
		graph.setColor( m_color[0] ); // plot smoothed Force Index
		graph.connect ( clip, m_ema[index-1], m_ema[index] );
		}

	public String getMetrics ( int index ) { return "" + m_ema[index]; }
	public int getValue ( int index ) { return m_ema[index]; }
	public int getRawFI ( int index )
		{
		Bar bar = m_bars.get(index);
		int close = bar.getClose();
		int vol = bar.getVolK();
		int prevClose = ( index == 0 ) ? bar.getOpen()
		                               : m_bars.get(index-1).getClose();
		return (close - prevClose) * vol;
		}
	}
