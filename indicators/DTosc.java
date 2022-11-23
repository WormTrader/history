package com.wormtrader.history.indicators;
/********************************************************************
* @(#)DTosc.java 1.00 20130206
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* DTosc: A Stochastic of the RSI - proprietary to DT Trading
* Described in Robert Miner's book
*
* @author Rick Salamone
* @version 1.00
* 20130206 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.history.Tape;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class DTosc
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="DTosc";
	public String getName() { return STUDY_NAME; }
	public static final int OVERBOUGHT = 75;
	public static final int OVERSOLD = 25;

	// Parameters...
	private int fPeriodRSI = 13; // Param("PeriodRSI", 13, 1, 250, 1);
	private int fPeriodStoch = 8; // Param("PeriodStoch", 8, 1, 250, 1);
	private int fMAType = 2; // Param("MAType", 1, 1, 2, 1); // 1: SMA, 2: EMA
	private int fPeriodSK = 5; // Param("PeriodSK", 5, 1, 250, 1);
	private int fPeriodSD = 5; // Param("PeriodSD", 5, 1, 250, 1);
//	private int fUpper=Param("Upper", 75, 50, 100, 1);
//	private int fLower=Param("Lower", 25, 0, 50, 1);
//	private int fZero=Param("ZeroLine", 50, 0, 50, 1);
	private Color[]     m_color = { new Color(255,0,255), new Color(153,153,153) };

	private final Tape fTape;
	private final RSI fRSI;

	private int[] m_fastK; // could call this StoRSI
	private int[]       m_slowK; // %K slow stochastic == fast %D
	private int[]       m_slowD; // %D slow stochastic
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public DTosc(Tape aTape, String params)
		{
		fTape = aTape;
		fRSI = (RSI)aTape.addStudy(RSI.STUDY_NAME, "");
		int length = aTape.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_fastK = new int[length];
		m_slowK = new int[length];
		m_slowD = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_fastK = SBMisc.arrayExpand ( m_fastK, _capacityIncrement );
		m_slowK = SBMisc.arrayExpand ( m_slowK, _capacityIncrement );
		m_slowD = SBMisc.arrayExpand ( m_slowD, _capacityIncrement );
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
	public String getToolTipText(int x)
		{
		return "sk: " + m_slowK[x] + " sd: " + m_slowD[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum()  { return 100; }
	public int getRangeMinimum()  { return   0; }
	public void dataChanged(int index)
		{
		fRSI.dataChanged(index);

		int numBars = fTape.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	/**
	* StoRSI is the Stochastic of RSI - called m_fastK in the stoch calculation
	* StoRSI = 100*(( RSI( PeriodRSI) - LLV( RSI( PeriodRSI ) , PeriodStoch ) )
	*     / ( (HHV( RSI( PeriodRSI) , PeriodStoch ) ) - LLV(RSI( PeriodRSI ), PeriodStoch ) ));
	* SK=MA(StoRSI,PeriodSK);
	* SD=MA(SK,PeriodSD);
	*/
	private void compute(int index)
		{
		if ( m_fastK.length <= index )
			enlargeArrays();
		m_index = index;
		int i = index - fPeriodStoch;
		if ( i < 0 ) i = 0;
		int lowestLo, highestHi;
		int rsi = lowestLo = highestHi = fRSI.getValue(index);
		while ( i < index )
			{
			int value = fRSI.getValue(i++);
			if ( value < lowestLo  ) lowestLo = value;
			if ( value > highestHi ) highestHi = value;
			}
		int denomenator = highestHi - lowestLo;
		if ( denomenator == 0 ) denomenator = 1;
		m_fastK[m_index] = 100* (rsi - lowestLo) / denomenator;
		m_slowK[m_index] = SBMath.sma( m_fastK, m_index, fPeriodSK); // m_slowPeriod );
		m_slowD[m_index] = SBMath.sma( m_slowK, m_index, fPeriodSD); // m_sigPeriod );
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		graph.setColor( Color.DARK_GRAY ); // plot overbought
		graph.connect ( clip, OVERSOLD, OVERSOLD );
		graph.connect ( clip, OVERBOUGHT, OVERBOUGHT );
		if ( index < 4 ) return;
		graph.setColor( m_color[0] ); // plot slow %K stochastic
		graph.connect ( clip, m_slowK[index-1], m_slowK[index] );
		graph.setColor( m_color[1] ); // plot slow %D
		graph.connect ( clip, m_slowD[index-1], m_slowD[index] );
		}

	public int fastK ( int index ) { return m_fastK[index]; }
	public int fastD ( int index ) { return m_slowK[index]; }
	public int slowK ( int index ) { return m_slowK[index]; }
	public int slowD ( int index ) { return m_slowD[index]; }

	public String getMetrics ( int index )
		{
		return "";
//		return String.format("%3d) MACD.histo=%5d EMA(%s)=%4d",
//				index, fMACD.getHistogram(index), MA_PARAMS, fEMA.getValue(index));
		}
	}
