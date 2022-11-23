package com.wormtrader.history.indicators;
/********************************************************************
* @(#)IINormalized.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* IINormalized: Normalized Intraday Intensity by Dr. Alexander
* Elder.
*
* @author Rick Salamone
* @version 1.00
* 2007     rts created
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMath;

import java.awt.Color;
import com.wormtrader.bars.BarList;

final public class IINormalized
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="II Normalized";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 21;
	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = Integer.MIN_VALUE;
	private Color       m_color = Color.ORANGE;
	private int				 m_period = DEFAULT_PERIOD;

	public IINormalized( BarList bars )
		{
		this ( bars, "" );
		}
	public IINormalized( BarList bars, String params )
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

	public String getRangeString(int value)
		{
		return "Normalized II(" + m_period + "): " + value;
		}
	public String getClipLabel() { return "II Norm"; }
	public String getToolTipText(int x)
		{
		return "" + getValue(x);
		}

	public void setColor(Color c) { m_color = c; }
	public int  getRangeMaximum() { return SBMath.maxAbsValue(m_maxValue, m_minValue); }
	public int  getRangeMinimum() { return -SBMath.maxAbsValue(m_maxValue, m_minValue); }
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
		if ( index < m_period - 1 ) return;
		int iiNorm = getValue(index);
		if ( iiNorm > m_maxValue ) m_maxValue = iiNorm;
		if ( iiNorm < m_minValue ) m_minValue = iiNorm;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < m_period - 1 ) return;
		graph.setColor ( m_color );
		graph.mountain ( clip, getValue(index));
		}

	public String getMetrics( int index ) { return "" + getValue(index); }
	public int getValue( int index )
		{
		// if ( index < m_period - 1 ) return;
		int iiSum = 0;
		long volSum = 0;
		for ( int i = 1 + index - m_period; i <= index; i++ )
			{
			Bar bar = m_bars.get(i);
			iiSum += bar.getII();
			volSum += bar.getVolume();
			}
		return (int)(100 * (long)iiSum / volSum);
		}
	}
