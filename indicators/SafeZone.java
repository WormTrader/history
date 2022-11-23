package com.wormtrader.history.indicators;
/********************************************************************
* @(#)SafeZone.java 1.00 2008
* Copyright © 2008-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* SafeZone - Safe Zone Stop - This study is a STOP system indicator,
* derived from the book "Come Into My Trading Room" by Dr. Alexander Elder
*
* @author Rick Salamone
* @version 1.0
* 2008     rts created
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

final public class SafeZone
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="SafeZone";
	public String getName() { return STUDY_NAME; }

	public static final int DEFAULT_PERIOD = 13; // Lookback # of bars for up/down penetration
	public static final int DEFAULT_COEFF = 2; // coefficient applied to avg up/down penetration

	private BarList m_bars;
	private Color       m_color[] = { Color.CYAN, Color.GRAY };
	private int         m_period = DEFAULT_PERIOD;
	private int         m_coeff = DEFAULT_COEFF;
	private int[]       m_lszs;  // long position SafeZone stop for each bar
	private int[]       m_sszs;  // short "
	private int[]       m_upPen; // circular buffers for up and down penetration
	private int[]       m_dnPen; //   for previous m_period bars
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = Integer.MIN_VALUE;
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public SafeZone( BarList bars )
		{
		this( bars, "" );
		}
	public SafeZone( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_sszs = new int[length];
		m_lszs = new int[length];
		setParams(params);
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		setPeriod(DEFAULT_PERIOD);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public void setPeriod(int p)
		{
		m_period = p;
		m_upPen = new int[p];
		m_dnPen = new int[p];
		dataChanged(0);
		}

	public String getRangeString(int value)
		{
		return "SafeZone(" + m_period + "): " + value;
		}
	public String getClipLabel() { return "SafeZone(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "" + m_sszs[x] + " " + m_lszs[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }

	public void dataChanged(int index)
		{
		if ( index != m_index + 1 )
			{
			index = 0;  // TODO: complete recalc - should only
			m_index = -1; // have go back m_periods
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
		if ( ++m_index != index ) // not in order !
			SBDialog.fatalError("SafeZone.compute(" + index + ") expected index " + m_index );
		Bar bar = m_bars.get(index);
		int hi = bar.getHigh();
		int lo = bar.getLow();
		if ( index == 0 )
			{
			m_dnPen[0] = m_upPen[0] = 0;
			m_sszs[0] = m_maxValue = hi;
			m_lszs[0] = m_minValue = lo;
			return;
			}
		int slot = index % m_period; // index in circular buffers for this rei

		bar = m_bars.get(index - 1);
		int hi1 = bar.getHigh();
		int lo1 = bar.getLow();

		if ( m_sszs.length <= index ) // enough space?
			{
			m_sszs = SBMisc.arrayExpand ( m_sszs, _capacityIncrement );
			m_lszs = SBMisc.arrayExpand ( m_lszs, _capacityIncrement );
			}
		m_upPen[slot] = (hi > hi1) ? hi - hi1 : 0;
		m_dnPen[slot] = (lo < lo1) ? lo1 - lo : 0;
		int penAvg = average( m_upPen ); // for a down trend
		m_sszs[index] = hi + (penAvg * m_coeff);
		// finalSafety = Min(Min(safety, safety[1]), safety[2]);
		if ( m_sszs[index] > m_maxValue ) m_maxValue = m_sszs[index];

		penAvg = average( m_dnPen ); // for a down trend
		m_lszs[index] = lo - (penAvg * m_coeff);
		if ( m_lszs[index] < m_minValue ) m_minValue = m_lszs[index];
		}

	private int average( int[] a )
		{
		int count = 0;
		int sum = 0;
		for ( int slot = 0; slot < m_period; slot++ ) // tally previous m_period's
			if ( a[slot] != 0 )
				{
				++count;
				sum += a[slot];
				}
		return (count > 0) ? sum / count : 0;
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( --index < m_period ) return;
		graph.setColor( m_color[0] );
		graph.connect ( clip, m_sszs[index-1], m_sszs[index] );
		graph.connect ( clip, m_lszs[index-1], m_lszs[index] );
		}

	public String getMetrics( int index )
		{
		return SBFormat.toDollarString(m_sszs[index])
		       + ", " + SBFormat.toDollarString(m_lszs[index]);
		}
	public int shortStop ( int index ) { return m_sszs[index]; }
	public int longStop ( int index ) { return m_lszs[index]; }
	public int shortStop () { return m_sszs[m_index]; }
	public int longStop () { return m_lszs[m_index]; }
	}
