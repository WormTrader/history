package com.wormtrader.history.yahoo;
// YahooGrabber.java
// Dispatches stock info requests to Yahoo Finance
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public final class YahooGrabber
	{
	private static String URL_PREFIX = "http://download.finance.yahoo.com/d/quotes.csv?s=";
	private static String CONNECTOR = "&f=%2520";
	private static String SPACE_CHAR = "%20";

	public static final String request( final String symbol, final String parameters )
		throws Exception
		{
		String response = null;
		try
			{
			URL yahoo = new URL(URL_PREFIX + symbol + CONNECTOR + parameters);
			BufferedReader in = new BufferedReader( new InputStreamReader( yahoo.openStream()));
			response = in.readLine();
			in.close();
			// System.out.format( "REQUEST(%s,%s)\nRESPONSE: %s\n", symbol, parameters, response );
			}
		catch ( Exception ex ) { throw ( ex ); }
		return response;
		}
	}
