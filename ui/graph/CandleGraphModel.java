package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)CandleGraphModel.java 1.00 ?????????
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* CandleGraphModel: Extends the TapeChartModel to handle scrollable
* charts. Essentially for scrollable charts, the actual graph clip's
* (i.e. clip 0) max and min values have to be calculated for all bars
* int the tape, and must take into account any overlays which will
* increase the range of the price axis.
*
* @author Rick Salamone
* @version 1.00
* ???????? rts created
* 20080518 rts handles moving average configurations
* 20120501 rts added getTape()
* 20120504 rts added DailyDomainRenderer that works
* 20120608 rts save and restore zoom in properties
* 20120628 rts save and restore annotations
* 20120719 rts handles offset moving averages
* 20120921 rts execution plotting checks the symbol for match
* 20121006 rts extends TapeChartModel to share code with mini charts
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.Tape;
import com.wormtrader.history.indicators.BWChaos2nd;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;

public final class CandleGraphModel
	extends TapeChartModel
	{
	private BWChaos2nd fBWChaos2nd;

	public CandleGraphModel(Tape aTape) { this(aTape, null); }
	public CandleGraphModel(Tape aTape, TemplateManager aTemplateMgr)
		{
		super(aTape, aTemplateMgr);
		KEY_PREFIX = "usr.tape.";
		fBWChaos2nd = new BWChaos2nd(aTape);
		}

	public BWChaos2nd getBWChaos2nd() { return fBWChaos2nd; }

	@Override protected String zoomPropertyKey()
		{
		return "usr.tape.zoom." + m_tape.getBarSize().paramValue();
		}

	@Override public int getRangeMaximum(byte clip)
		{
		if ( clip == 0 )
			{
			int max = m_tape.getHighestHi();
			for ( byte j = 0; j < m_olay.length; j++ )
				if (( m_olay[j] != null )
				&&  ( m_olay[j].getRangeMaximum() > max ))
					max = m_olay[j].getRangeMaximum();
			return max;
			}
		else return super.getRangeMaximum(clip);
		}

	@Override public int getRangeMinimum(byte clip)
		{
		if ( clip == 0 )
			{
			int min = m_tape.getLowestLo();
			for ( byte j = 0; j < m_olay.length; j++ )
				if (( m_olay[j] != null )
				&&  ( m_olay[j].getRangeMinimum() < min ))
					min = m_olay[j].getRangeMinimum();
			return min;
			}
		else return super.getRangeMinimum(clip);
		}

	@Override public String getToolTipText(byte clip, int x)
		{
		String it = super.getToolTipText(clip, x);
		return (it.isEmpty() || clip!= 0)? it
		:      (it + " " + fBWChaos2nd.getToolTipText(x,m_tape.get(x)));
		}

	@Override void applySettings()
		{
		GraphTemplate template = fTemplateMgr.getTemplate();
fBWChaos2nd = new BWChaos2nd(m_tape);
		if ( template != null ) applySettings(template);
		}
	}
