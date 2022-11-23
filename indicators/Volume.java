package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Volume.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Volume:
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
import java.awt.Color;

public final class Volume
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Volume";
	public String getName() { return STUDY_NAME; }

	private BarList m_bars;
	private long    m_minVol = Long.MAX_VALUE;
	private long    m_maxVol = Long.MIN_VALUE;
	private Color   m_colors[] = {Color.GREEN, Color.RED, Color.WHITE};

	public Volume(BarList bars) { this( bars, "" ); }

	public Volume( BarList bars, String params )
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

	public String getRangeString(int value) { return "" + value; }
	public String getClipLabel() { return "Vol"; }
	public String getToolTipText(int x)
		{
		Bar bar = m_bars.get(x);
		return "" + bar.getVolume();
		}

	public void   setColor(Color c) { m_colors[3] = c; }
	public int    getRangeMaximum() { return (int)m_maxVol; }
	public int    getRangeMinimum() { return 0; }

	public void   dataChanged(int index)
		{
		if ( index == 0 )
			{
			m_minVol = Long.MAX_VALUE;
			m_maxVol = Long.MIN_VALUE;
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		long v = m_bars.get(index).getVolume();
		if (v > m_maxVol) m_maxVol = v;
		if (v < m_minVol) m_minVol = v;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		Bar bar = m_bars.get(index);
		int change = bar.change();
		graph.setColor( (change > 0)? m_colors[0]
		              : (change < 0)? m_colors[1]
		              :               m_colors[2]);
		graph.vLine ( clip, 0, (int)bar.getVolume());
		}
	public String getMetrics(int index) { return "" + m_bars.get(index).getVolume(); }
	}
