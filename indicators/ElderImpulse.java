package com.wormtrader.history.indicators;
/********************************************************************
* @(#)ElderImpulse.java 1.00 20110601
* Copyright © 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* ElderImpulse - The Elder Impulse System was designed by
* Alexander Elder and featured in his book, <I>Come Into My
* Trading Room</I>. According to Elder, "the system identifies
* inflection points where a trend speeds up or slows down".
* The Impulse System is based on two indicators, a 13-day EMA
* and the MACD-Histogram. The moving average identifies the
* trend, while the MACD-Histogram measures momentum. As a result,
* the Impulse System combines trend following and momentum to
* identify tradable impulses.
*
* Elder codes a color as the forground of standard (western) price
* bars. This implementation extends this idea to candlesticks,
* coloring the price range "wick" of the candle.
*
* Calculation
* Green: EMA > previous EMA and MACD-Histogram > previous period's MACD-Histogram
*  Red: EMA < previous EMA and MACD-Histogram < previous period's MACD-Histogram
* Blue: when conditions for a Red or Green are not met.
* The MACD-Histogram is based on MACD(12,26,9), and the EMA is EMA("13,C")
*
* @author Rick Salamone
* 20110601 rts first iteration
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.history.Tape;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

public final class ElderImpulse
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Elder Impulse";
	public String getName() { return STUDY_NAME; }
	static final String MA_PARAMS="13,C";

	private final Tape  fTape;
	private final MACD  fMACD;
	private final EMA   fEMA;
	private Color[]     m_color = { Color.RED, Color.BLUE, Color.GREEN };

	public ElderImpulse( Tape aTape )
		{
		this ( aTape, "" );
		}
	public ElderImpulse( Tape aTape, String params )
		{
		fTape = aTape;
		fEMA = (EMA)fTape.addStudy(EMA.STUDY_NAME, MA_PARAMS );
		fMACD = (MACD)fTape.addStudy(MACD.STUDY_NAME, "12,26,9" );
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

	public String getRangeString(int value) { return "ElderImpulse: " + value; }
	public String getClipLabel() { return ""; }
	public String getToolTipText(int x){ return ""; }

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return fTape.getHighestHi(); }
	public int getRangeMinimum() { return fTape.getLowestLo(); }
	public void dataChanged(int index)
		{
		fEMA.dataChanged(index);
		fMACD.dataChanged(index);
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		Bar bar = fTape.getBars().get(index);
		graph.setColor( m_color[1 + getValue(index)] );
		graph.vLine(clip, bar.getHigh(), bar.getLow());
//		graph.fill( bar.getHigh() + 1, bar.getLow() - 1, m_color[index % 3] );
		}

	public String getMetrics ( int index )
		{
		return String.format("%3d) MACD.histo=%5d EMA(%s)=%4d",
				index, fMACD.getHistogram(index), MA_PARAMS, fEMA.getValue(index));
		}

	public int getValue ( int index )
		{
		int momentum = fMACD.getHistogram(index) - fMACD.getHistogram(index - 1);
		int inertia = fEMA.getValue(index) - fEMA.getValue(index-1);
		return (momentum > 0 && inertia > 0)? 1
		     : (momentum < 0 && inertia < 0)? -1
		     : 0;
		}
	}
