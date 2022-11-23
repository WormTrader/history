package com.wormtrader.history.indicators;
/********************************************************************
* @(#)ADX.java 1.00 20121113
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ADX: Developed by Welles Wilder
*
* @author Rick Salamone
* @version 1.00
* 20121113 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class ADX
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="ADX";
	public String getName() { return STUDY_NAME; }

	public static final int OVERBOUGHT = 70;
	public static final int OVERSOLD = 30;
	public static final int DEFAULT_PERIOD = 14;

	private final BarList m_bars;
	private Color[]       m_color = { Color.ORANGE };
	int fADXPeriod;
	int fDMIPeriod;

	private double[] fAdmPlus = new double[100];
	private double[] fAdmMinus = new double[100];
	private int[] fDiPlus;
	private int[] fDiMinus;
	private int[] fDx;
	private int[] fADX;
	private int[] fATR;

	private int           m_index = -1; // index of last valid data
	private static int    _capacityIncrement = 80;


	public ADX( BarList bars ) { this ( bars, "" ); }
	public ADX( BarList bars, String params )
		{
		m_bars = bars;
		int length = m_bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;

//		fAdmPlus = new int[length];
//		fAdmMinus = new int[length];
		fDiPlus = new int[length];
		fDiMinus = new int[length];
		fDx = new int[length];
		fADX = new int[length];
		fATR = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
//		fAdmPlus  = SBMisc.arrayExpand ( fAdmPlus,  _capacityIncrement);
//		fAdmMinus = SBMisc.arrayExpand ( fAdmMinus, _capacityIncrement);
		fDiPlus   = SBMisc.arrayExpand ( fDiPlus,   _capacityIncrement);
		fDiMinus  = SBMisc.arrayExpand ( fDiMinus,  _capacityIncrement);
		fDx       = SBMisc.arrayExpand ( fDx,       _capacityIncrement);
		fADX      = SBMisc.arrayExpand ( fADX,      _capacityIncrement);
		fATR      = SBMisc.arrayExpand ( fATR,      _capacityIncrement);
		}

	public String getParams() { return "" + fADXPeriod + "," + fDMIPeriod; }

	public void setParams( String params )
		{
		int adxPeriod = DEFAULT_PERIOD;
		int dmiPeriod = DEFAULT_PERIOD; // 8
		try
			{
			String[] pieces = params.split(",");
			adxPeriod = Integer.parseInt(pieces[0].trim());
			dmiPeriod = Integer.parseInt(pieces[1].trim());
			}
		catch (Exception e) {System.out.println(STUDY_NAME + " params: '" + params + "' " + e);}
		if (fADXPeriod == adxPeriod
		&&  fDMIPeriod == dmiPeriod)
			return; // unchanged
		fADXPeriod = adxPeriod;
		fDMIPeriod = dmiPeriod;
double a = (double) 2/(double)(fDMIPeriod + 1);
fDMIMult = (int)(SCALE * a);
		dataChanged(0);
		}
private static final int SCALE = 100000;
int fDMIMult;

	public String getRangeString(int value)
		{
		return "ADX: " + value;
		}
	public String getClipLabel() { return "ADX(" + getParams() + ")"; }
	public String getToolTipText(int x)
		{
		return "ADX(" + getParams() + "): " + fADX[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return 100; }
	public int getRangeMinimum() { return   0; }

	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
		if ( fADX.length <= index )
			enlargeArrays();
		m_index = index;
		doIt(index);
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( Color.GRAY ); // plot overbought
		graph.connect ( clip, OVERSOLD, OVERSOLD );
		graph.connect ( clip, OVERBOUGHT, OVERBOUGHT );
		graph.setColor( Color.LIGHT_GRAY ); // plot center line
		graph.connect ( clip, 50, 50 );
		graph.setColor( m_color[0] ); // plot smoothed value
		graph.connect ( clip, fADX[index-1], fADX[index] );
		}

	public String getMetrics ( int index ) { return "" + fADX[index]; }
	public int getValue ( int index ) { return fADX[index]; }

	//The Average Directional Indicator
	private void doIt(int index)
		{
		int dmPlus = 0;
		int dmMinus = 0;
		Bar cBar = m_bars.get(index);
		Bar pBar = cBar; // previous bar will be reset below
		if(index >= 1)
			{
			pBar = m_bars.get(index-1);

			//Directional Movement Indicator
			int deltaHigh = cBar.getHigh() - pBar.getHigh();
			int deltaLow = pBar.getLow() - cBar.getLow();

			if((deltaHigh < 0 && deltaLow < 0)
			|| (deltaHigh == deltaLow))
				{
				dmPlus = 0;
				dmMinus = 0;
				}
			else if(deltaHigh > deltaLow)
				{
				dmPlus = deltaHigh;
				dmMinus = 0;
				}
			else if (deltaHigh < deltaLow)
				{
				dmPlus = 0;
				dmMinus = deltaLow;
				}
			}

		//Average Directional Movement Indicator
		if (index <= 1)
			{
			fAdmPlus[index] = dmPlus;
			fAdmMinus[index] = dmMinus;
			}
		else
			{
			double a = (double) 2/(double)(fDMIPeriod + 1);
int prevAdmPlus = (int)(fAdmPlus[index-1] * SCALE);

int admPlus  = dmPlus  * fDMIMult + prevAdmPlus * (SCALE - fDMIMult);
admPlus /= SCALE;
double admPlusPrev = (double)(prevAdmPlus/SCALE);
double dtest  = dmPlus  * a + admPlusPrev * (1 - a);
int itest = (int)(dtest * SCALE);
System.out.println("\nxAdmPlus[" + index + "] " + itest + " int " + itest);
			fAdmPlus[index]  = dmPlus  * a + fAdmPlus[index-1] * (1 - a);
			fAdmMinus[index] = dmMinus * a + fAdmMinus[index-1] * (1 - a);
System.out.println("fAdmPlus[" + index + "] " + fAdmPlus[index] + " a " + a);
System.out.println("iAdmPlus[" + index + "] " + admPlus + " a " + fDMIMult);
//System.out.println("fAdmMinus[" + index + "] " + fAdmMinus[index]);
			}
fADX[index] = 30; /**********************************

		//True Range Indicator
		int trueRange = cBar.trueRange(pBar.getClose());

		//Average True Range Indicator
		if(index <= 1)
			{
			fATR[index] = trueRange;
			}
		else
			{
			double a = (double)2 / (double)(fADXPeriod + 1);
			fATR[index] = trueRange * a + fATR[index-1] * (1 - a);
			}

		//Directional index Indicator
		if (trueRange != 0)
			{
			fDiPlus[index] = 100 * fAdmPlus[index]/trueRange;
			fDiMinus[index] = 100 * fAdmMinus[index]/trueRange;
			}
		else
			{
			fDiPlus[index] = fDiPlus[index-1];
			fDiMinus[index] = fDiMinus[index-1];
			}

		//Directional Movement Index Indicator
		if ((fDiPlus[index] + fDiMinus[index]) != 0)
			fDx[index] = Math.abs((fDiPlus[index] - fDiMinus[index]))
		                      / (fDiPlus[index] + fDiMinus[index]) * 100;
		else
			fDx[index] = fDx[index-1];

		//Average Directional Movement Indicator
		if (index <= 1)
			{
			fADX[index] = fDx[index];
			}
		else
			{
			double a = (double)2 / (double)(fDMIPeriod + 1);
			fADX[index] = fDx[index] * a + fADX[index-1] * (1 - a);
			}
***********/
		}
	}
