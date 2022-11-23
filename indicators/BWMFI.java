package com.wormtrader.history.indicators;
/********************************************************************
* @(#)BWMFI.java 1.00 20120725
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* BWMFI: Bill Williams Market Facilitation Index
*
* @author Rick Salamone
* @version 1.00
* 20120725 rts created
* 20120914 rts added isFade, isFake, isSquat, isGreen methods
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

public final class BWMFI
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="BWMFI";
	private static final String[] _volMFI =
		{
		"-- Fade", "-+ Fake", "+- Squat", "++ Green"
		};
	public String getName() { return STUDY_NAME; }

	private final BarList m_bars;
	private int       m_minMFI = Integer.MAX_VALUE;
	private int       m_maxMFI = Integer.MIN_VALUE;
	private Color     m_color = Color.GREEN;

	public BWMFI( BarList bars ) { this ( bars, "" ); }
	public BWMFI( BarList bars, String params )
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
	public String getClipLabel() { return STUDY_NAME; }

	public final int getVolMFIType(int x)
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
		if (x < 1) return "";
		return _volMFI[getVolMFIType(x)];
		}
	public void   setColor(Color c) { m_color = c; }
	public int    getRangeMaximum() { return m_maxMFI; }
	public int    getRangeMinimum() { return m_minMFI; }
	public void   dataChanged(int index)
		{
		if ( index == 0 )
			{
			m_minMFI = Integer.MAX_VALUE;
			m_maxMFI = Integer.MIN_VALUE;
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			{
			int mfi = m_bars.get(i).mfi();
			if ( mfi > m_maxMFI ) m_maxMFI = mfi;
			if ( mfi < m_minMFI ) m_minMFI = mfi;
			}
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( m_color );
		int was = m_bars.get(index-1).mfi();
		int is = m_bars.get(index).mfi();
//System.out.format("mfi.plot(%d) connect %d .. %d range %d .. %d\n", index, was, is, m_minMFI, m_maxMFI);
		graph.connect(clip, was, is);
		}
	public String getMetrics(int index) { return "" + m_bars.get(index).mfi(); }
	}
