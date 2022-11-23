package com.wormtrader.history.indicators;
/********************************************************************
* @(#)IntrabarIntensity.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* IntrabarIntensity: by Dr. Alexander Elder.
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
import com.shanebow.util.SBMath;
import java.awt.Color;

final public class IntrabarIntensity
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Intrabar Intensity";
	public String getName() { return STUDY_NAME; }

	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = Integer.MIN_VALUE;
	private Color       m_color = Color.WHITE;

	public IntrabarIntensity( BarList bars )
		{
		this( bars, "" );
		}
	public IntrabarIntensity( BarList bars, String params )
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
		return "II: " + value;
		}
	public String getClipLabel() { return "II"; }
	public String getToolTipText(int x)
		{
		return "" + m_bars.get(x).getII();
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
		Bar bar = m_bars.get(index);
		int ii = bar.getII();
		if ( ii > m_maxValue ) m_maxValue = ii;
		if ( ii < m_minValue ) m_minValue = ii;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		Bar bar = m_bars.get(index);
		graph.setColor(m_color);
		graph.mountain ( clip, bar.getII());
		}

	public String getMetrics( int index ) { return "" + m_bars.get(index).getII(); }
	public int getValue( int index ) { return m_bars.get(index).getII(); }
	}
