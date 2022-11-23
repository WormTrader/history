package com.wormtrader.history.indicators;
/********************************************************************
* @(#)TDCamouflage.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TDCamouflage - Developed by DeMark & DeMark
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

public final class TDCamouflage
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="TDCamouflage";
	public static final int SELL = -1;
	public static final int NEUTRAL = 0;
	public static final int BUY = 1;
	public String getName() { return STUDY_NAME; }

	private BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = 0;
	private int         m_offset = 30; // how far above or below bar to plot the dot
	private Color[]     m_color = { Color.YELLOW };

	public TDCamouflage( BarList bars )
		{
		this ( bars, "" );
		}
	public TDCamouflage( BarList bars, String params )
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

	public String getRangeString(int value) { return STUDY_NAME + ": " + value; }
	public String getClipLabel() { return STUDY_NAME; }
	public String getToolTipText(int x) { return ""; }
	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
	public void dataChanged(int index)
		{
		compute(index);
		}
	private void compute(int index)
		{
		if ( index == 1 )
			{
			Bar bar = m_bars.get( 0 );
			m_minValue = bar.getLow();
			m_maxValue = bar.getHigh();
			return;
			}
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		int bias = getBias( index );
		if ( bias == NEUTRAL ) return;
		Bar bar = m_bars.get( index );
		int placement = ( bias == BUY ) ? bar.getLow() - m_offset
														  : bar.getHigh() + m_offset;
graph.drawExecution( bias, placement );
//		graph.setColor( m_color[0] );
//		graph.drawDot ( clip, placement );
		}

	public String getMetrics ( int index ) { return "" + getBias(index); }

	public int getBias ( int index )
		{
		if ( index < 1 ) return NEUTRAL;
		Bar bar = m_bars.get( index );
		int open = bar.getOpen();
		int close = bar.getClose();
		Bar prev = m_bars.get( index - 1 );
		int prevClose = prev.getClose();
		
		if ((close < prevClose) && (close > open) && (bar.getLow() < prev.getLow()))
			return BUY;	// bullish at a market low
		if ((close > prevClose) && (close < open) && (bar.getHigh() > prev.getHigh()))
			return SELL; // bearish at a market high
		return NEUTRAL;
		}
	}
