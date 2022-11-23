package com.wormtrader.history.query.hitprocessors;
/********************************************************************
* @(#)ListHits.java 1.00 20130405
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* ListHits: The most basic HitProcessor that does no
* processing on each query hit. Always available even if no custom
* hit processors have been defined.
*
* @author Rick Salamone
* @version 1.00
* 20130405 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.dao.USD;
import com.wormtrader.history.query.*;
import com.shanebow.util.SBFormat;

public final class ListHits
	implements HitProcessor
	{
	public static final String[] HEADERS =
		{
		"Symbol", "When", "Base $", "Run Bars"
		};
	public static final Class[] CLASSES =
		{
		String.class, String.class, USD.class, Integer.class
		};

	public final Class[]  classes() { return CLASSES; }
	public final String[] headers() { return HEADERS; }


	/**
	* @param BarList aM5Bars - this list will be refilled every day with
	* with the bars the query is going to feed to the Tape for that day
	*/
	public void startRun(BarList aM5Bars) {}

	/**
	* @param BarList aM5Bars - this list will be refilled every day with
	* with the bars the query is going to feed to the Tape for that day
	*/
	public void startDay(String symbol, String yyyymmdd) {}

	/**
	* @return a HTML formatted String summarizing all the hits
	*/
	public String summary(int aNumHits, int aNumDays)
		{
		int hitsPerDayX100 = (aNumDays == 0)? 0 : ((100 * aNumHits)/aNumDays);
		return "<html><code><b>" + aNumHits + "</b> hits in <b>"
		     + aNumDays + "</b> days = <b>" + SBFormat.toDollarString(hitsPerDayX100)
				+ "</b> hits/day/symbol";
		}

	/**
	* @return the index int bars after skipping the run
	*/
	public QueryHit createHit(String aSymbol, Bar aBar, int indexM5)
		{
		return new QueryHit(aSymbol, aBar.getTime(), aBar.getClose());
		}
	}
