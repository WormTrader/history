package com.wormtrader.history.scan;
/********************************************************************
* @(#)ScanWorker.java 1.00 20090701
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* ScanWorker: Runs multiple scans on historical data in the background.
*
* @author Rick Salamone
* @version 1.00
* 20090701 rts created
* 20121108 rts log symbols that are missing data
* 20130325 rts cleaned up, save settings, and filters m5 exists & prices
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.wormtrader.bars.symbols.SymbolList;
import com.wormtrader.history.Tape;
import com.shanebow.util.SBLog;
import java.util.List;
import javax.swing.SwingWorker;

public class ScanWorker
	extends SwingWorker<Void, String>
	{
	public static boolean m_verbose = true;

	private ScanList    m_scanList;
	private SymbolList  m_symbolList;
	private final boolean fCheckM5Exists;
	private final int fMinPrice;
	private final int fMaxPrice;

	public static void log ( String msg )
		{
		if ( m_verbose ) System.out.println ( "ScanWorker: " + msg );
		}

	public ScanWorker(ScanList scans, SymbolList symbols,
		int aMinPrice, int aMaxPrice, boolean aCheckM5Exists )
		{
		m_scanList = scans;
		m_symbolList = symbols;
		fMinPrice = aMinPrice;
		fMaxPrice = aMaxPrice;
		fCheckM5Exists = aCheckM5Exists;
		}

	@Override protected Void doInBackground()
		{
		log ( "task priority: " + Thread.currentThread().getPriority());
		publish( "Initializing" );
		int nSymbols = m_symbolList.size();
		int symbolNum = 0;
		Tape tape = new Tape(BarSize.ONE_DAY);
		long dateRange[] = { 0, ScanNode.getTime() };
		for ( ScanNode scan : m_scanList )
			scan.initialize( tape );
		for (String symbol : m_symbolList)
			{
			if ( isCancelled()) break;
			publish(symbol);
			setProgress( symbolNum++ * 100 / nSymbols );

			if (( tape.reset( symbol, dateRange ) > 0 )
			&&  ( tape.get(tape.size()-1).getTime() == dateRange[1] ))
				{
				Bar lastBar = tape.lastBar();
				int closePrice = lastBar.getClose();
				if (fMinPrice != 0
				&&  closePrice < fMinPrice )
					{
					SBLog.write("Scan", symbol + " min price > " + closePrice);
					continue;
					}
				if (fMaxPrice != 0
				&&  closePrice > fMaxPrice )
					{
					SBLog.write("Scan", symbol + " max price < " + closePrice);
					continue;
					}
				if (fCheckM5Exists
				&& !BarList.m5Exists(symbol, lastBar.yyyymmdd()))
					{
					SBLog.write("Scan", "no M5 data for " + symbol + " on " + tape.lastBar().yyyymmdd());
					continue;
					}
				for ( ScanNode scan : m_scanList )
					try
						{
						if (scan.isHit(tape))
							scan.add(symbol, null);
						}
					catch( Exception ex )
						{
						System.err.format( "!%s(%s): %s\n", scan.toString(),
													symbol, ex.toString());
						}
				}
			else SBLog.write("Scan", "missing data for " + symbol);
			}
if (!isCancelled()) setProgress( 100 );
		return (Void)null;
		}
	}
