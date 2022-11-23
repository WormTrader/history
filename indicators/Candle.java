package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Candle.java 1.00 ????
* Copyright © 2007-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* Candle - Average True Range
*
* @author Rick Salamone
* @version 1.00
* ???? rts created
* 20100622 rts fixed min/max to be that of atr, not individual true ranges
* 20100625 rts modified to allow to be extended
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public class Candle
	extends ATR
	{
	public static final String STUDY_NAME="Candle";
	public String getName() { return STUDY_NAME; }

	public static final byte SOLID        = (byte)0;
	public static final byte HAMMER       = (byte)1;
	public static final byte BALD         = (byte)2;
	public static final byte INV_HAMMER   = (byte)3;
	public static final byte INV_BALD     = (byte)4;
	public static final byte SPINNING_TOP = (byte)5;
	public static final byte LONG_TOP_WICK= (byte)6;
	public static final byte LONG_BOT_WICK= (byte)7;
	public static final byte OTHER        = (byte)8;
	public static final String[] SHAPE_NAMES =
		{
		"SOLID",
		"HAMMER",
		"BALD",
		"INV_HAMMER",
		"INV_BALD",
		"SPINNING_TOP",
		"LONG_TOP_WICK",
		"LONG_BOT_WICK",
		"OTHER"
		};

	public static final byte DOJI     = (byte)0;
	public static final byte SHORT    = (byte)1;
	public static final byte MEDIUM   = (byte)2;
	public static final byte TALL     = (byte)3;
	public static final byte MARUBOZO = (byte)4;

	static final char UP_ARROW='\u2191';
	static final char DN_ARROW='\u2193';

	static final String[] NAMES =
		{
		"DOJI",
		"SHORT",
		"MEDIUM",
		"TALL",
		"MARUBOZO",
		};

	public final static String nameFor(byte type)
		{
		String it = "";
		if (type < 0)
			{
			it += DN_ARROW;
			type *= -1;
			}
		else if (type > 0) it += UP_ARROW;
		return it + NAMES[type];
		}

	public static final byte UP_MARUBOZO = MARUBOZO;
	public static final byte UP_TALL     = TALL;
	public static final byte UP_MEDIUM   = MEDIUM;
	public static final byte UP_SHORT    = SHORT;
	public static final byte DN_SHORT    = -SHORT;
	public static final byte DN_MEDIUM   = -MEDIUM;
	public static final byte DN_TALL     = -TALL;
	public static final byte DN_MARUBOZO = -MARUBOZO;

	public Candle( BarList bars ) { this( bars, "" ); }
	public Candle( BarList bars, String params )
		{
		super(bars, params);
		}

//	public final String getClipLabel() { return getName() + "(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
Bar bar = m_bars.get(x);
		byte ht = height(x);
		return "$" + SBFormat.toDollarString(m_atr[x])
		     + " \u0394" + srange(x)
//		     + " " + body(x)
		     + " " + nameFor(ht) + "(" + ht + ")"
+ " " + travel(bar)
		     + " " + SHAPE_NAMES[shape(bar)];
//		     + " " + bar.topShadow() + " " + bar.bottomShadow();
		}

/*********
	public final void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( m_color );
		graph.connect ( clip, getValue(index-1), getValue(index));
		}
*********/

	public String getMetrics( int index )
		{
		return SBFormat.toDollarString(m_atr[index]);
		}

	public int srange(int index) { return 100 * m_bars.get(index).srange() / m_atr[index]; }
	public int body(int index) { return 100 * m_bars.get(index).change() / m_atr[index]; }

	public byte shape(Bar bar)
		{
		int open = bar.getOpen();
		int close = bar.getClose();
		int high = bar.getHigh();
		int low = bar.getLow();
		boolean isDown = (close < open);
		int bodyTop = isDown? open : close;
		int bodyBot = isDown? close : open;
		int topWick = high - bodyTop;
		int botWick = bodyBot - low;
		int threshold = (high - low)/20; // 5%
// System.out.println("range " + (high-low) + " thresh = " + threshold);
		if (threshold == 0) threshold = 1;
		if (topWick <= threshold && botWick <= threshold)
			return SOLID;
		int body = bodyTop - bodyBot;
		if (topWick <= threshold)
			return (botWick > body)? HAMMER : BALD;
		if (botWick <= threshold)
			return (topWick > body)? INV_HAMMER : INV_BALD;
// System.out.format("top %d body %d bot %d\n", topWick, body, botWick);
		int threeHalfsBody = 3*body/2;
		if ((topWick > threeHalfsBody) || (botWick > threeHalfsBody))
			{
			if (Math.abs(topWick-botWick) <= 2*threshold)
				return SPINNING_TOP;
			else return (topWick > botWick)? LONG_TOP_WICK : LONG_BOT_WICK;
			}
		if (topWick > body && botWick > body)
			return SPINNING_TOP;
		return OTHER;
		}

int xxx = 0;
	public boolean isMatch(int index, byte aHeight, int aTravel)
		{
		Bar bar = m_bars.get(index);
		byte ht = height(bar, m_atr[index]);
		if ((aHeight == DN_SHORT || aHeight == UP_SHORT)
		&&  (ht==DN_SHORT || ht==UP_SHORT || ht==DOJI))
			return true;
		if (aHeight != ht) return false;
		int trav = travel(bar);
		if ( aTravel == trav ) return true; // exact match in travel
		// here the heights are the same, and we allow some slop in the travel
		return approxEqual( aTravel/10, trav/10)  // opening part of bar approx =
		    && approxEqual( aTravel%10, trav%10); // closing part of bar approx =
/******
boolean it = approxEqual( aTravel/10, trav/10)  // opening part of bar approx =
		    && approxEqual( aTravel%10, trav%10); // closing part of bar approx =
if(xxx++ < 8)
System.out.format("isMatch(%d)? %02d %s " + it + "\n", index, travel(bar), Candle.nameFor(ht));
return it;
******/
		}

	private boolean approxEqual(int a, int b)
		{
		return (a == b)
		    || ((a>=3) && (b>=3))   // high of bar(4) ~= top 1/3 of bar(3)
		    || ((a<=1) && (b<=1));  // low of bar(0) ~= bottom 1/3 of bar(1)
		}

	public int travel(int index)
		{
		return travel(m_bars.get(index));
		}

	public int travel(Bar bar)
		{
		int it = 0;
		int open = bar.getOpen();
		int close = bar.getClose();
		int high = bar.getHigh();
		int low = bar.getLow();
		int range = high - low;
		if (range < 2) return 22; // "MM"; // DOJI;
		if (range%3 == 2) ++range;
		int range3 = range/3;
		int topThird = high - range3;
		int botThird = low + range3;
		int threshold = (high - low)/20; // 5%
//		if (threshold == 0) threshold = 1;
		high -= threshold;
		low  += threshold;
		it += (open >= high)? 40     // order of tests critical...
		   :  (open >= topThird)? 30
		   :  (open <= low)? 0
		   :  (open <= botThird)? 10
		   :   20;
		it += (close >= high)? 4
		   :  (close >= topThird)? 3
		   :  (close <= low)? 0
		   :  (close <= botThird)? 1
		   :   2;
		return it;
		}

	public byte height(int index)
		{
		return height(m_bars.get(index), m_atr[index]);
/**********
		Bar bar = m_bars.get(index);
		if (bar.isDoji()) return DOJI;
		int range = 100 * bar.range() / m_atr[index]; // always positive
		byte ht = (range > 150)? TALL
		        : (range < 60)? SHORT
		        : MEDIUM;
		if (bar.isDown()) ht *= -1;
		return ht; 
**********/
		}

	public byte height(Bar bar, int atr)
		{
		if (bar.isDoji()) return DOJI;
		int range = 100 * bar.range() / atr; // always positive
		byte ht = (range > 150)? TALL
		        : (range < 85)? SHORT
		        : MEDIUM;
		if (bar.isDown()) ht *= -1;
		return ht; 
		}

	public int atr(int index) { return m_atr[index]; }
//	protected final int getClose() { return (m_index < 0) ? 0 : m_bars.get(m_index).getClose(); }
	}
