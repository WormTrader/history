package com.wormtrader.history.indicators;
/********************************************************************
* @(#)ATR.java 1.00 ????
* Copyright © 2007-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ATR - Average True Range
*
* @author Rick Salamone
* @version 1.00
* ???? rts created
* 20100622 rts fixed min/max to be that of atr, not individual true ranges
* 20100625 rts modified to allow to be extended
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

public class ATR
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="ATR";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 14;
	protected BarList m_bars;
	protected int     m_minValue = Integer.MAX_VALUE;
	protected int     m_maxValue = 0;
	protected int[]   m_atr;
	private Color     m_color = Color.YELLOW;
	private int       m_period = DEFAULT_PERIOD;
	private int       m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public ATR( BarList bars ) { this( bars, "" ); }
	public ATR( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_atr = new int[length];
		setParams(params);
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		if ((params == null) || params.isEmpty())
			setPeriod(DEFAULT_PERIOD);
		else System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public final void setPeriod(int p)  { m_period = p; dataChanged(0); }
	public final String getRangeString(int value)
		{
		return SBFormat.toDollarString(value);
		}
	public final String getClipLabel() { return getName() + "(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "$" + SBFormat.toDollarString(m_atr[x]);
		}

	public final void setColor(Color c) { m_color = c; }
	public final int getRangeMaximum() { return m_maxValue + 1; }
	public final int getRangeMinimum() { return m_minValue - 1; }
	public final void dataChanged(int index)
		{
		if ( index == 0 )
			{
			m_minValue = Integer.MAX_VALUE;
			m_maxValue = 0;
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	protected void checkMinMax(int atr, Bar aBar)
		{
		if ( atr > m_maxValue ) m_maxValue = atr;
		else if ( atr < m_minValue ) m_minValue = atr;
		}

	private void compute(int index)
		{
		m_index = index;
		Bar bar = m_bars.get(index);
		if ( index == 0 ) // prime the pump
			{
			m_atr[0] = bar.trueRange( bar.getOpen());
			m_minValue = Integer.MAX_VALUE;
			m_maxValue = 0;
			checkMinMax(m_atr[0], bar);
			checkMinMax(m_atr[0], bar);
			return;
			}
		if ( m_atr.length <= index )
			m_atr = SBMisc.arrayExpand ( m_atr, _capacityIncrement );
		int prevClose = m_bars.get(index-1).getClose();
		int trueRange = bar.trueRange( prevClose );
		int atr = SBMath.ema( trueRange, m_atr[index-1], m_period );
		m_atr[index] = atr;
		checkMinMax(atr, bar);
		}

	public final void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( m_color );
		graph.connect ( clip, getValue(index-1), getValue(index));
		}

	public String getMetrics( int index )
		{
		return SBFormat.toDollarString(m_atr[index]);
		}
	public final int getATR ( int index ) { return m_atr[index]; }
	public int getValue (int index) { return m_atr[index]; }
	public int getValue () { return (m_index < 0) ? 0 : m_atr[m_index]; }
	protected final int getClose() { return (m_index < 0) ? 0 : m_bars.get(m_index).getClose(); }
	}
