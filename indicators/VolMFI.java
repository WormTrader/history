package com.wormtrader.history.indicators;
/********************************************************************
* @(#)VolMFI.java 1.00 20120725
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* VolMFI: Bill Williams
*
* @author Rick Salamone
* @version 1.00
* 2007     rts created Volume indicator
* 20120725 rts created MFI indicator
* 20120914 rts added isFade, isFake, isSquat, isGreen methods
* 20130326 rts merged Volume and MFI code for a "combo" indicator
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

public final class VolMFI
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Vol/MFI";
	private static final String[] MFI_TYPES =
		{
		"Fade", "Fake", "Squat", "Green"
		};
	public String getName() { return STUDY_NAME; }

	private final BarList m_bars;
	private int       m_minMFI = Integer.MAX_VALUE;
	private int       m_maxMFI = Integer.MIN_VALUE;

	public VolMFI( BarList bars ) { this ( bars, "" ); }
	public VolMFI( BarList bars, String params )
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

	public String getClipLabel() { return STUDY_NAME; }

	public String getRangeString(int value)
		{
		return "" + value + ", " + (int)(value * m_maxMFI / m_maxVol);
		}

	private final int getVolMFIType(int x)
		{
		if (x < 1) return 0;
		return m_bars.get(x).mfiType(m_bars.get(x-1));
		}

	public final boolean isFade(int x) { return getVolMFIType(x) == 0; }
	public final boolean isFade()
		{
		int x = m_bars.size() -1;
		return (x < 1)? false : isFade(x);
		}

	public final boolean isFake(int x) { return getVolMFIType(x) == 1; }
	public final boolean isFake()
		{
		int x = m_bars.size() -1;
		return (x < 1)? false : isFake(x);
		}

	public final boolean isSquat(int x) { return getVolMFIType(x) == 2; }
	public final boolean isSquat()
		{
		int x = m_bars.size() -1;
		return (x < 1)? false : isSquat(x);
		}

	public final boolean isGreen(int x) { return getVolMFIType(x) == 3; }
	public final boolean isGreen()
		{
		int x = m_bars.size() -1;
		return (x < 1)? false : isGreen(x);
		}

	public String getToolTipText(int x)
		{
		Bar bar = m_bars.get(x);
		String it = "" + bar.getVolume();
		if (x >= 1)
			it += " " + MFI_TYPES[getVolMFIType(x)];
		return it;
		}
	public void   setColor(Color c) { m_colors[2] = c; }

	private long    m_maxVol = Long.MIN_VALUE;
	private Color[] m_colors = {Color.GREEN, Color.RED, Color.WHITE, Color.GRAY};
	public int    getRangeMaximum() { return (int)m_maxVol; }
	public int    getRangeMinimum() { return 0; }
	public void   dataChanged(int index)
		{
		if (index == 0)
			{
			m_minMFI = Integer.MAX_VALUE;
			m_maxMFI = Integer.MIN_VALUE;
			m_maxVol = 0;
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			{
			Bar bar = m_bars.get(i);
			long v = bar.getVolume();
			if (v > m_maxVol) m_maxVol = v;
			int mfi = bar.mfi();
			if ( mfi > m_maxMFI ) m_maxMFI = mfi;
			if ( mfi < m_minMFI ) m_minMFI = mfi;
			}
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		Bar bar = m_bars.get(index);
		int change = bar.change();
		graph.setColor( (change > 0)? m_colors[0]
		              : (change < 0)? m_colors[1]
		              :               m_colors[2]);
		graph.vLine ( clip, 0, (int)bar.getVolume());

		if ( index < 1 ) return;
		graph.setColor(m_colors[3]);
		Bar prev = m_bars.get(index-1);
//System.out.format("mfi.plot(%d) connect %d .. %d range %d .. %d\n", index, was, is, m_minMFI, m_maxMFI);
		graph.connect(clip, scaledMFI(prev.mfi()), scaledMFI(bar.mfi()));
		}

	private int scaledMFI(int aMFI)
		{
		return (int)(aMFI * m_maxVol / m_maxMFI);
		}

	public String getMetrics(int index) { return "" + m_bars.get(index).mfi(); }
	}
