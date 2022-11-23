package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)MiniChartModel.java 1.00 ?????????
* Copyright © 2007 - 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* MiniChartModel: Extends the TapeChartModel to handle mini charts which
* only show the tail end of the tape that is visible in the window. In
* other words, mini charts are not scrollable.
* 
* @author Rick Salamone
* @version 1.00
* 20120802 rts created by cloning CandleGraphModel
* 20121006 rts extends TapeChartModel to share code with mini charts
* 20121126 rts added check for null graph to calcVisibleRange()
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.Tape;
import com.wormtrader.history.event.TapeEvent;
import com.wormtrader.history.event.TapeListener;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.util.SBProperties;

public final class MiniChartModel
	extends TapeChartModel
//	implements ConfigurableGraphModel, TapeListener, TemplateListener
	{
	private int fVisibleMax;
	private int fVisibleMin;

	public MiniChartModel(Tape aTape, TemplateManager aTemplateMgr)
		{
		super(aTape, aTemplateMgr);
KEY_PREFIX = "usr.minis.";
		}

	private void calcVisibleRange()
		{
if (m_graph == null) return; // haven't addNotify'ed yet
		// Calc max & min prices - do before call data changed Different CandleGraphModel
		fVisibleMax = m_tape.getLowestLo();
		fVisibleMin = m_tape.getHighestHi();
		int visibleBars = m_graph.numBarsVisible();
		int numBars = m_tape.size();
		int i = numBars - visibleBars;
		if ( i < 0 ) i = 0;
//System.out.format(toString() + " numBars: %d visible bars: %d i: %d\n",
//numBars, visibleBars, i); 
		while ( i < numBars )
			{
			Bar bar = m_tape.get(i++);
			int hi = bar.getHigh();
			int lo = bar.getLow();
			if ( hi > fVisibleMax ) fVisibleMax = hi;
			if ( lo < fVisibleMin ) fVisibleMin = lo;
			}
//System.out.format(toString() + " visible range: %d - %d\n", fVisibleMin, fVisibleMax); 
		}

	// implement TapeListener
	@Override public void tapeChanged( TapeEvent e )
		{
		calcVisibleRange();
		super.tapeChanged(e);
		}

	@Override public void applySettings(GraphTemplate aTemplate)
		{
		super.applySettings(aTemplate);
		calcVisibleRange();
		}

	@Override public void addNotify( TGGraph g )
		{
		m_graph = g;
		int zoom = 3;
		m_graph.setDomainRenderer(null);
		m_graph.setZoom(SBProperties.getInstance().getInt(zoomPropertyKey(), zoom));
		applySettings();
		}

	@Override public int getRangeMaximum(byte clip)
		{
		if ( clip == 0 )
			return fVisibleMax + 10;
		else return super.getRangeMinimum(clip);
		}

	@Override public int getRangeMinimum(byte clip)
		{
		if ( clip == 0 )
			return fVisibleMin - 10;
		else return super.getRangeMinimum(clip);
		}
	}
