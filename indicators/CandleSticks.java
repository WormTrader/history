package com.wormtrader.history.indicators;
/********************************************************************
* @(#)CandleSticks.java 1.00 2007
* Copyright © 2007 - 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* CandleSticks: Draws the candlesticks on the chart. Formerly this code
* resided directly in the chart model. But there is considerably more flexiblity
* in treating it as an indicator: The user may choose to draw them or not, and
* if so, may draw them at the desired z-index.
*
* @author Rick Salamone
* @version 1.00
* 2007???? rts created plot code in CandleGraphMOdel
* 20140612 rts refactored as an indicator
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import java.awt.Color;

public final class CandleSticks
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="CandleSticks";
	public String getName() { return STUDY_NAME; }

	protected BarList m_bars;
	protected int     m_minValue = Integer.MAX_VALUE;
	protected int     m_maxValue = Integer.MIN_VALUE;

	public CandleSticks(BarList bars) {m_bars = bars;}
	public CandleSticks(BarList bars, String params) {m_bars = bars;}

	public final String getParams() {return "";}
	public void setParams( String params ) {}
	private void reset() { dataChanged(0); }
	@Override public final void setColor(Color c) {}
	public final int getRangeMaximum()  { return m_maxValue; }
	public final int getRangeMinimum()  { return m_minValue; }
	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			{
			Bar bar = m_bars.get(i);
			int lo = bar.getLow();
			int hi = bar.getHigh();
			if (i == 0) {
				m_minValue = lo;
				m_maxValue = hi;
				}
			else {
				if (lo < m_minValue) m_minValue = lo;
				if (hi > m_maxValue) m_maxValue = hi;
				}
			}
		}

	public final String getRangeString(int value)
		{
		return "$" + SBFormat.toDollarString(value);
		}
	public final String getClipLabel() { return toString(); }
	@Override public final String toString() { return getName(); }

	public final String getToolTipText(int x) { return m_bars.get(x).toString();}
	public final String getMetrics(int x) { return m_bars.get(x).toString();}
	public final void plot ( TGGraph graph, byte clip, int x )
		{
		if ( x < 0 || x >= m_bars.size()) return;
		Bar bar = m_bars.get(x);
		graph.drawCandle ( bar.getOpen(), bar.getHigh(),
										bar.getLow(), bar.getClose());
		}
	}
