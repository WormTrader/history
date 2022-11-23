package com.wormtrader.history.yahoo;
/********************************************************************
* @(#)YahooInfoPanel.java 1.00 2009????
* Copyright © 2007-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* YahooInfoPanel: Downloads summary information for a security from
* the Yahoo Finance web site.
*
* @author Rick Salamone
* @version 1.00
* 2009???? rts created
* 20120803 rts Uses CSV module to split return values (have quotes on some)
*******************************************************/
import com.shanebow.util.CSV;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public final class YahooInfoPanel
	extends JPanel
	{
	private String m_yahooParameters = "";
	private YahooField[] tField =
			{
			new YahooField( "Name", "n" ),
			new YahooField( "Dividend/Share", "d" ),
			new YahooField( "Dividend Yield", "y" ),
			new YahooField( "Ex-Dividend Date", "q" ),
			new YahooField( "Dividend Pay Date", "r1" ),
			new YahooField( "Exchange", "x" ),
			new YahooField( "Open", "o" ),
			new YahooField( "Prev Close", "p" ),
			new YahooField( "Last", "l1" ), // last trade price only
			new YahooField( "Time", "t1" ), // last trade time only
			new YahooField( "Last", "l" ),  // last trade price & time
			new YahooField( "Market Cap", "j1" ),
			new YahooField( "Book Value", "b4" ),
			new YahooField( "52-Week High", "k" ),
			new YahooField( "52-Week Low", "j" ),
			new YahooField( "Earnings/Share", "e" ),
			new YahooField( "EPS Est This Year", "e7" ),
			new YahooField( "EPS Next Year", "e8" ),
			new YahooField( "EBITDA", "j4" ),
			new YahooField( "Short Ratio", "s7" ),
			new YahooField( "50-Day MA", "m3" ),
			new YahooField( "200-Day MA", "m4" ),
			new YahooField( "Avg Daily Volume", "a2" ),
			new YahooField( "Float Shares", "f6" ),
			};

	public YahooInfoPanel()
		{
		super(new GridLayout(0,2));
		m_yahooParameters = "";
		for ( int i = 0; i < tField.length; i++ )
			{
			add( new JLabel( tField[i].getLabel()));
			add( tField[i] );
			m_yahooParameters += tField[i].getParameter();
			}
		setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		}

	public void reset ( String symbol )
		{
		if ((symbol == null) || symbol.isEmpty())
			return;
		try
			{
			String csv = YahooGrabber.request( symbol, m_yahooParameters );
//			String[] values = csv.split(",", tField.length + 3);
			String[] values = CSV.split(csv, tField.length + 3);
			for ( int i = 0; i < tField.length; i++ )
				tField[i].setText("<html>"+ values[i+3] );
			}
		catch ( Exception ex )
			{
			JOptionPane.showMessageDialog( this, "Error: " + ex,
									"Yahoo Data Error", JOptionPane.ERROR_MESSAGE );
			}
		}
	}

class YahooField extends JLabel // JTextField
	{
	private final String m_label;
	private final String m_parameter;

	public YahooField( String label, String parameter )
		{
		super();
		m_label = label;
		m_parameter = parameter;
//		setEditable( false );
		}

	public final String getLabel() { return m_label; }
	public final String getParameter() { return m_parameter; }
	}
