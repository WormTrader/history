package com.wormtrader.history.query;
/********************************************************************
* @(#)Query.java 1.00 20120806
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* Query: Runs a query on historical data in the background.
*
* @author Rick Salamone
* @version 1.00
* 20120806 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.Tape;
import com.wormtrader.history.indicators.*;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;

public class Query
	{
	public static boolean m_verbose = true;

	protected final static void log ( String msg )
		{
		SBLog.write("Query: " + msg);
		if ( m_verbose ) System.out.println ( "Query: " + msg );
		}

	/** M5 alligator */
	protected BWChaos2nd gator5;

	/** M30 alligator */
	protected BWChaos2nd gator30;

	/** D1 alligator */
	protected BWChaos2nd gatorD1;

	/** M5 MFI */
	protected MFI mfiM5;

	/** M30 MFI */
	protected MFI mfiM30;

	/** D1 MFI */
	protected MFI mfiD1;

	/** M5 Candles */
	protected Candle candleM5;

	/** M30 Candles */
	protected Candle candleM30;

	/** D1 Candles */
	protected Candle candleD1;

	/** The tapes - this should be a TripleTape instead */
	protected Tape tD1;
	protected Tape tM30;
	protected Tape tM5;
	protected void init(Tape tD1, Tape tM30, Tape tM5)
		{
		this.tD1 = tD1;
		this.tM30 = tM30;
		this.tM5 = tM5;
		gator5  = new BWChaos2nd(tM5);
		gator30 = new BWChaos2nd(tM30);
		gatorD1 = new BWChaos2nd(tD1);
		mfiM5  = (MFI)tM5.addStudy(MFI.STUDY_NAME);
		mfiM30 = (MFI)tM30.addStudy(MFI.STUDY_NAME);
		mfiD1  = (MFI)tD1.addStudy(MFI.STUDY_NAME);
		candleM5  = (Candle)tM5.addStudy(Candle.STUDY_NAME);
		candleM30 = (Candle)tM30.addStudy(Candle.STUDY_NAME);
		candleD1  = (Candle)tD1.addStudy(Candle.STUDY_NAME);
		}

	/**
	* Adds the offset to the last index of M5 tape and returns its Bar
	* @param aOffset - the offset (zero or negative) from the end of tM5
	* @return the Bar back offset periods from the end
	*/
	protected Bar M5(int aOffset)
		{
		int index = tM5.size() -1 + ((aOffset > 0)? -aOffset : aOffset);
		return tM5.get(index);
		}

	/**
	* Adds the offset to the last index of M30 tape and returns its Bar
	* @param aOffset - the offset (zero or negative) from the end of tM30
	* @return the Bar back offset periods from the end
	*/
	protected Bar M30(int aOffset)
		{
		int index = tM30.size() -1 + ((aOffset > 0)? -aOffset : aOffset);
		return tM30.get(index);
		}

	/**
	* Adds the offset to the last index of D1 tape and returns its Bar
	* @param aOffset - the offset (zero or negative) from the end of tD1
	* @return the Bar back offset periods from the end
	*/
	protected Bar D1(int aOffset)
		{
		int index = tD1.size() -1 + ((aOffset > 0)? -aOffset : aOffset);
		return tD1.get(index);
		}

	/**
	* Subclasses override this method to determine if the current bar is
	* a hit.
	* @param bM5 - the current five minute bar
	* @param iM5 - the index in tM5 of bM5
	* @return true if this bar meets the query criteria, false if not
	*/
	protected boolean isHit(Bar bM5, int iM5) { return false; }
	}
