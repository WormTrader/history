package com.wormtrader.history.indicators;
/********************************************************************
* @(#)DonchianBands.java 1.00 2009
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* DonchianBands: Extends TapyStudy to implement Donchian Bands,
* developed in the 1960's by Richard Donchian.
*
* @author Rick Salamone
* @version 1.00
* 2009     rts created
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

final public class DonchianBands
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Donchian Bands";
	public String getName() { return STUDY_NAME; }
	private static final int DEFAULT_HI_PERIOD = 20;
	private static final int DEFAULT_LO_PERIOD = 20;

	private BarList   m_bars;
	private int       m_minValue = Integer.MAX_VALUE;
	private int       m_maxValue = Integer.MIN_VALUE;
	private Color[]   m_color = { Color.PINK };
	private int       m_hiPeriod = DEFAULT_HI_PERIOD;
	private int       m_loPeriod = DEFAULT_LO_PERIOD;
	private int[]     m_hiBand;
	private int[]     m_loBand;
	private int       m_index = -1; // index of last valid data
	private static int    _capacityIncrement = 80;

	public DonchianBands( BarList bars )
		{
		this ( bars, "" );
		}
	public DonchianBands( BarList bars, String params )
		{
		m_bars = bars;
/***************
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_hiBand = new int[length];
		m_loBand = new int[length];
***************/
		setParams(params);
		}

/***************
	private void enlargeArrays()
		{
		m_hiBand = SBMisc.arrayExpand ( m_hiBand, _capacityIncrement );
		m_loBand = SBMisc.arrayExpand ( m_loBand, _capacityIncrement );
		}
***************/

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getMetrics(int index)
		{
		return bruteDonHi(index) + " - " + bruteDonHi(index);
		}

	public String getRangeString(int value)
		{
		return STUDY_NAME + ": " + value;
		}
	public String getClipLabel() { return ""; }
	public String getToolTipText(int x)
		{
		return "Don";
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
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
	/*
		if ( m_bandOffset.length <= index )
			enlargeArrays();
	*/
		m_index = index;
		Bar bar = m_bars.get(index);
		if ( bar.getHigh() > m_maxValue ) m_maxValue = bar.getHigh();
		if ( bar.getLow() < m_minValue ) m_minValue = bar.getLow();
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( m_color[0] );
		graph.connect ( clip, bruteDonHi(index-1), bruteDonHi(index));
		graph.connect ( clip, bruteDonLo(index-1), bruteDonLo(index));
		}

	private final int bruteDonLo( int index )
		{
		int size = m_bars.size();
		if ( index >= size ) return -1;
		int lo = Integer.MAX_VALUE;
		int count = ( ++index < m_loPeriod ) ? index : m_loPeriod;
		for ( int i = 0; i < count; i++ )
			{
			int barLo = m_bars.get( --index ).getLow();
			if ( barLo < lo ) lo = barLo;
			}
//		if ( lo < m_minValue ) m_minValue = lo;
		return lo;
		}

	private final int bruteDonHi( int index )
		{
		int size = m_bars.size();
		if ( index >= size ) return -1;
		int hi = Integer.MIN_VALUE;
		int count = ( ++index < m_loPeriod ) ? index : m_loPeriod;
//log("index %d size %d count %d\n", index, size, count );
// System.exit(1);
		for ( int i = 0; i < count; i++ )
			{
			int barHi = m_bars.get( --index ).getHigh();
			if ( barHi > hi ) hi = barHi;
			}
//		if ( hi > m_maxValue ) m_maxValue = hi;
		return hi;
		}

	public int getLowerBand ( int index ) { return bruteDonLo( index ); }
	public int getUpperBand ( int index ) { return bruteDonHi( index ); }
	}

/******************************
	public int[] loDonchian ( int periods )
		{
		Bar bar;
		int i, price;
		int sum = 0;
		int size = m_bars.size();
		int[] results = new int[size];
		for ( i = 0; i < periods && i < size; i++ )
			{
			bar = (Bar)m_bars.get( i );
			price = bar.getLow();
			if ( i == 0 )
				{
				results[0] = price;
				continue;
				}
			results[i] = min ( price, results[i-1] );
			}
		for ( ; i < size; i++ )
			{
			bar = (Bar)m_bars.get( i );
			price = bar.getLow();
			if ( price > results[i-1] )
				for ( int j = 1; j < periods; j++ )
					{
					bar = (Bar)m_bars.get( i - j );
					int p = bar.getLow();
					if ( p < price )
						price = p;
					}
			results[i] = price;
			}
		return results;
		}

	public int[] hiDonchian ( int periods )
		{
		Bar bar;
		int i, price;
		int sum = 0;
		int size = m_bars.size();
		int[] results = new int[size];
		for ( i = 0; i < periods && i < size; i++ )
			{
			bar = (Bar)m_bars.get( i );
			price = bar.getHigh();
			if ( i == 0 )
				{
				results[0] = price;
				continue;
				}
			results[i] = max ( price, results[i-1] );
			}
		for ( ; i < size; i++ )
			{
			bar = (Bar)m_bars.get( i );
			price = bar.getHigh();
			if ( price < results[i-1] )
				for ( int j = 1; j < periods; j++ )
					{
					bar = (Bar)m_bars.get( i - j );
					int p = bar.getHigh();
					if ( p > price )
						price = p;
					}
			results[i] = price;
			}
		return results;
		}
******************************/
