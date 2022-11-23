package com.wormtrader.history.indicators;
/********************************************************************
* @(#)SAR.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* SAR: Welles Wilder's Parabolic Stop And Reverse
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
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

final public class SAR
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Parabolic SAR";
	public String getName() { return STUDY_NAME; }

	private static final double DEFAULT_AF = 0.02;
	private static final double MAX_AF = 0.20;
	private static final double DEFAULT_STEP = 0.02;
	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = Integer.MIN_VALUE;
	private Color       m_color = Color.CYAN;
	private int         m_ep;               // extreme price
	private double      m_af = DEFAULT_AF;  // acceleration factor
	private double      m_step = DEFAULT_STEP;
	private int[]       m_sar;
	private int         m_index = -1; // index of last valid data
	boolean             m_isLong = true;
	private static int  _capacityIncrement = 80;

	public SAR( BarList bars )
		{
		this( bars, "" );
		}

	public SAR( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_sar = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_sar = SBMisc.arrayExpand ( m_sar, _capacityIncrement );
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
		return "SAR: " + SBFormat.toDollarString(value);
		}
	public String getClipLabel() { return "SAR"; }
	public String getToolTipText(int x)
		{
		return "$" + SBFormat.toDollarString(getSAR(x));
		}

	public void setColor(Color c) { m_color = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

public String debug = "";
	private void compute(int index)
		{
		m_index = index;
		Bar bar;
		int prevClose;
		int trueRange;
		if ( index == 0 ) // prime the pump
			{
			bar = m_bars.get(index);
			m_sar[0] = bar.getClose();
			m_ep = bar.getHigh();
			m_minValue = m_maxValue = m_sar[0];
			}
		else // do the actual calculations
			{  // SAR(i) = SAR(i-1)+ACCELERATION*(EPRICE(i-1)-SAR(i-1))
			if ( m_sar.length <= index )
				enlargeArrays();
			int prev = index - 1;
			bar = m_bars.get(prev);
			int hi = bar.getHigh();
			int lo = bar.getLow();
			if ( m_isLong && (hi > m_ep)) updateEP(hi); // m_ep = hi;
			else if ( !m_isLong && (lo < m_ep)) updateEP(lo); // m_ep = lo;
			m_sar[index] = m_sar[prev] + (int)(m_af * (m_ep - m_sar[prev]));
// debug = String.format ("sar[%d] = prev + (%f * (%d - prev))", index, m_af, m_ep );
			bar = m_bars.get(index);
			lo = bar.getLow();
			hi = bar.getHigh();
			if ( m_isLong && (lo <= m_sar[index]))
				reverse(index, hi );
			else if ( !m_isLong && (hi >= m_sar[index]))
				reverse(index, lo );
// debug += " " + m_sar[index];
// System.out.println( debug );
			}
		}
	private void updateEP(int value) // Updates the Extreme Price, which
		{                              // triggers Acceleration Factor update
		m_ep = value;                  // and can register a change in min/maxValue
		if ( m_af < MAX_AF )
			m_af += m_step;
		if ( m_ep > m_maxValue ) m_maxValue = m_ep;
		else if ( m_ep < m_maxValue ) m_minValue = m_ep;
		}
	private void reverse( int index, int newEP )
		{
		m_af = DEFAULT_AF;
		if ( m_isLong && (newEP > m_ep)) m_ep = newEP; // prevHi;
		else if ( !m_isLong && (newEP < m_ep)) m_ep = newEP; // prevLo;
		m_isLong = !m_isLong;
		m_sar[index] = m_ep;
debug += " REVERSE";
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( m_color );
		graph.drawDot ( clip, m_sar[index] );
		}

	public String getMetrics(int index) { return SBFormat.toDollarString(m_sar[index]); }
	public int getValue () { return m_sar[m_index]; }
	public int getValue ( int x ) { return m_sar[x]; }
	public int getSAR   ( int x ) { return m_sar[x]; }
	}
