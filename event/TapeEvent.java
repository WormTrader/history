package com.wormtrader.history.event;
/********************************************************************
* @(#)TapeEvent.java	1.00 2008
* Copyright © 2007 - 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* TapeEvent:
*
* @version 1.00 2007
* @author Rick Salamone
* 2008???? rts created
* 20120627 rts added BACKFILLED and SYMBOL_CHANGING events
*******************************************************/
import  com.wormtrader.bars.Bar;
import  com.wormtrader.history.Tape;

public class TapeEvent extends java.util.EventObject
	{
	// action ids
	public static final int BAR_ERROR = 0;
	public static final int REALTIME_BAR = 1;
	public static final int BAR_ADDED = 2; // history - not realtime
	public static final int BAR_INSERTED = 3;
	public static final int BARS_MODIFIED = 4;
	public static final int BARS_CLEARED = 5;
	public static final int HISTORY_DONE = 6;
	public static final int BACKFILLED = 7;
	public static final int SYMBOL_CHANGING = 8;

	private static final String[] _idStrings =
		{ "BAR_ERROR", "REALTIME_BAR", "BAR_ADDED", "BAR_INSERTED", "BARS_MODIFIED",
		  "BARS_CLEARED", "HISTORY_DONE", "BACKFILLED", "SYMBOL_CHANGING" };
	private int m_eventID;
	private int m_index;
	private Bar m_bar;

	public TapeEvent(Tape src, int id, int index, Bar bar)
		{
		super(src);
		m_eventID = id;
		m_index = index;
		m_bar = bar;
		}
	public int  getActionID() { return m_eventID; }
	public int  getIndex()    { return m_index; }
	public Bar  getBar()      { return m_bar; }
	public Tape getTape()     { return (Tape)getSource(); }
//	public String toString()  { return "Tape event #" + m_eventID + " bar: "
//	                                + ((m_bar == null) ? "null" : m_bar.toString()); }
	public String toString()  { return _idStrings[m_eventID] + " bar: "
	                                + ((m_bar == null) ? "null" : m_bar.toString()); }
	}
