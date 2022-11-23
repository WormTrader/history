package com.wormtrader.history.indicators;
/********************************************************************
* @(#)TDSetup.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TDSetup - Developed by DeMark & DeMark
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

public final class TDSetup
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="TDSetup";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 4;
	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = Integer.MIN_VALUE;
	private Color[]     m_color = { Color.BLUE };
	private int         m_period = DEFAULT_PERIOD;
	private int[]       m_value;   // smoothed force index
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public TDSetup( BarList bars )
		{
		this ( bars, "" );
		}
	public TDSetup( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_value = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_value = SBMisc.arrayExpand ( m_value, _capacityIncrement );
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
	public String getToolTipText(int x) { return STUDY_NAME + ": " + m_value[x]; }
	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
	public void dataChanged(int index)
		{
		if ( index == 0 )
			m_minValue = m_maxValue = 0;
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		if ( m_value.length <= index )
			enlargeArrays();
		m_index = index;
		if ( index == 0 ) // prime the pump
			{
			m_value[0] = m_minValue = m_maxValue = 0;
			return;
			}
		int comp = getChange( index );
		if ( comp == 0 )
			{
			m_value[index] = 0;
			return;
			}
		int value = m_value[index - 1];
		if ( value < 0 )
			value = ( comp < 0 )? value - 1 : 0;
		else if ( value > 0 )
			value = ( comp > 0 )? value + 1 : 0;
		else
			value = ( comp > 0 )? 1 : -1;
		m_value[index] = value;
		if ( value > m_maxValue ) m_maxValue = value;
		if ( value < m_minValue ) m_minValue = value;
		}
	public int getChange ( int index )
		{
		if ( index < m_period ) return 0;
		return m_bars.get(index).getClose()
					- m_bars.get(index - m_period).getClose();
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( Color.GRAY ); // plot zero line
		graph.connect ( clip, 0, 0 );
		graph.setColor( m_color[0] ); // plot smoothed Force Index
		graph.connect ( clip, m_value[index-1], m_value[index] );
		}
	public String getMetrics( int index ) { return "" + m_value[index]; }
	public int getValue ( int index ) { return m_value[index]; }
	}
