package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Stochastic.java 1.00
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Stochastic - Developed by Dr. George Lane
*
* @author Rick Salamone
* @version 1.00
* ???????? rts created
* 20130206 rts uses BarList instead of vector
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class Stochastic
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Stochastics";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_FAST_PERIOD = 5;
	private static final int DEFAULT_SLOW_PERIOD = 3;
	private static final int DEFAULT_SIGNAL_PERIOD = 3;
	private BarList m_bars;
	private Color[]     m_color = { new Color(255,0,255), new Color(153,153,153) };
	private int         m_fastPeriod = DEFAULT_FAST_PERIOD;
	private int         m_slowPeriod = DEFAULT_SLOW_PERIOD;
	private int         m_sigPeriod = DEFAULT_SIGNAL_PERIOD;
	private int[]       m_fastK; // %K fast stochastic
	private int[]       m_slowK; // %K slow stochastic == fast %D
	private int[]       m_slowD; // %D slow stochastic
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public Stochastic( BarList bars )
		{
		this ( bars, "" );
		}
	public Stochastic( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_fastK = new int[length];
		m_slowK = new int[length];
		m_slowD = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_fastK = SBMisc.arrayExpand ( m_fastK, _capacityIncrement );
		m_slowK = SBMisc.arrayExpand ( m_slowK, _capacityIncrement );
		m_slowD = SBMisc.arrayExpand ( m_slowD, _capacityIncrement );
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
		return "Stochastic: " + value;
		}
	public String getClipLabel() { return "Stoch"; }
	public String getToolTipText(int x)
		{
		return "%k: " + m_slowK[x] + " %d: " + m_slowD[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum()  { return 100; }
	public int getRangeMinimum()  { return   0; }
	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		if ( m_fastK.length <= index )
			enlargeArrays();
		m_index = index;
		Bar bar = m_bars.get(index);
		int lowestLow = bar.getLow();
		int highestHi = bar.getHigh();
		int close = bar.getClose();
		for ( int i = 1; i < m_fastPeriod; i++ )
			{
			if ( --index < 0 ) break;
			bar = m_bars.get(index);
			int lo = bar.getLow();
			int hi = bar.getHigh();
			if ( lo < lowestLow ) lowestLow = lo;
			if ( hi > highestHi ) highestHi = hi;
			}
		int denomenator = highestHi - lowestLow;
		if ( denomenator == 0 ) denomenator = 1;
		m_fastK[m_index] = 100 * (close - lowestLow) / denomenator;
		m_slowK[m_index] = SBMath.sma( m_fastK, m_index, m_slowPeriod );
		m_slowD[m_index] = SBMath.sma( m_slowK, m_index, m_sigPeriod );
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 4 ) return;
		graph.setColor( m_color[0] ); // plot slow %K stochastic
		graph.connect ( clip, m_slowK[index-1], m_slowK[index] );
		graph.setColor( m_color[1] ); // plot slow %D
		graph.connect ( clip, m_slowD[index-1], m_slowD[index] );
		}
	public String getMetrics( int index )
		{
		return "" + m_slowD[index] + ", " + m_slowK[index]
+ ", " + ((m_slowD[index] + m_slowK[index]) / 2);
		}

	public int fastK ( int index ) { return m_fastK[index]; }
	public int fastD ( int index ) { return m_slowK[index]; }
	public int slowK ( int index ) { return m_slowK[index]; }
	public int slowD ( int index ) { return m_slowD[index]; }
	public boolean isOversold( int index )
		{
		return (m_slowK[index] < 20) && (m_slowD[index] < 20);
		}
	public boolean isOverbought( int index )
		{
		return (m_slowK[index] > 80) && (m_slowD[index] > 80);
		}
	}

