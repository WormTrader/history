package com.wormtrader.history;
/********************************************************************
* @(#)TapeStudies.java 1.00 2009
* Copyright © 2009-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* TapeStudies - Maintains the indicators for a tape including
* moving averages, overlays, and clip indicators.
*
* @author Rick Salamone
* 20110526 rts added Elder's AutoEnvelope
* 20110531 rts addStudy() now takes a Tape object as the 1st param
* 20110531 rts added Elder's Impulse System Bar coloring
* 20120416 rts added Perry Kaufman's Efficiency
* 20120420 rts added Opening Range
* 20120625 rts added Van Tharp's ATR%
* 20120626 rts added Van Tharp's SQN
* 20120710 rts added Williams %R
* 20120711 rts add() method now parses params if present between parens
* 20120721 rts added smoothed & weighted moving averages
* 20120722 rts added Bill Williams' Awesome Oscillator: AO
* 20120723 rts added Bill Williams' Fractals
* 20120725 rts added Bill Williams' MFI
* 20130113 rts added Joe Ross' TLOC (The Law of Charts)
* 20130206 rts added Robert Miner's DTosc
* 20130206 rts fixed findStudy problem with null params
* 20130510 rts added Money Flow Index
* 20140319 rts added Regression Indicator
* 20140612 rts added CandleSticks Indicator
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize; // opening range only works for 5 min
import com.wormtrader.history.TapeStudy;
import com.wormtrader.history.indicators.*;
import com.shanebow.util.SBArray;
import com.shanebow.util.SBLog;

public final class TapeStudies // extends SBArray<TapeStudy>
	{
	public static final String[] CLIP_INDICATORS =
		{
		ADX.STUDY_NAME,
		AO.STUDY_NAME,
		ATR.STUDY_NAME,
		ATRPercent.STUDY_NAME,
		Candle.STUDY_NAME,
		DTosc.STUDY_NAME,
		IntrabarIntensity.STUDY_NAME,
		IINormalized.STUDY_NAME,
		Efficiency.STUDY_NAME,
		ForceIndex.STUDY_NAME+"(2)",
		MACD.STUDY_NAME,
		MACD.STUDY_NAME+"(5,34,5)",
		Temperature.STUDY_NAME,
		MFI.STUDY_NAME,
		MFI.STUDY_NAME+"(5)",
		BWMFI.STUDY_NAME,
		Momentum.STUDY_NAME,
		REI.STUDY_NAME,
		RSf.STUDY_NAME,
		RSI.STUDY_NAME,
		RSI.STUDY_NAME+"(3)",
		SQN.STUDY_NAME+"(20)",
		SQN.STUDY_NAME+"(100)",
		Stochastic.STUDY_NAME,
		TDREI.STUDY_NAME,
		TDSetup.STUDY_NAME,
		Volume.STUDY_NAME,
		VolMFI.STUDY_NAME,
		WilliamsR.STUDY_NAME+"(10)",
		WilliamsR.STUDY_NAME+"(260)",
		};
	public static final String[] OVERLAYS =
		{
		CandleSticks.STUDY_NAME,
		AutoEnvelope.STUDY_NAME,
		BollingerBands.STUDY_NAME,
		Dragon.STUDY_NAME,
		River.STUDY_NAME,
		DonchianBands.STUDY_NAME,
		ElderImpulse.STUDY_NAME,
		Fractals.STUDY_NAME,
		OpeningRange.STUDY_NAME,
		SAR.STUDY_NAME,
		SafeZone.STUDY_NAME,
		TDCamouflage.STUDY_NAME,
		TDRangeProjection.STUDY_NAME,
		"Price Channels",  // TODO: Not implemented yet
		TLOC.STUDY_NAME, // The Law of Charts: under construction
		};

	private final SBArray<TapeStudy> m_studies = new SBArray<TapeStudy>(10);

	public void dump()
		{
		SBLog.format ( " Studies(%d):", m_studies.size());
		for ( TapeStudy ts : m_studies )
			SBLog.format ( "   %s(%s)", ts.getName(), ts.getParams());
		}

	void dump(StringBuffer b)
		{
		b.append("<br>Studies(").append(m_studies.size()).append("):<ol>");
		for ( TapeStudy ts : m_studies )
			b.append( "<li>").append(ts.getName()).append('(').append(ts.getParams()).append(")</li>");
		b.append("</ol>");
		}

	public final TapeStudy add ( Tape aTape, String name, String params )
		{
		if ((params == null || params.isEmpty())
		&&  name.endsWith(")"))
			{
			int parenAt = name.lastIndexOf('(');
			params = name.substring(parenAt+1, name.length()-1);
			name = name.substring(0, parenAt);
			}

		if (params == null) params = "";
		TapeStudy bs = findStudy ( name, params );
		if ( bs != null ) // this study already exists!
			return bs;

		BarList bars = aTape.getBars();
		// Moving Averages
		if ( name.equals(EMA.STUDY_NAME))
			bs = new EMA( bars, params );
		else if ( name.equals(SMA.STUDY_NAME))
			bs = new SMA( bars, params );
		else if ( name.equals(SmMA.STUDY_NAME)) // smoothed MA
			bs = new SmMA( bars, params );
		else if ( name.equals(WMA.STUDY_NAME)) // weighted MA
			bs = new WMA( bars, params );
		else if ( name.equals(RegressionIndicator.STUDY_NAME))
			bs = new RegressionIndicator( bars, params );

		// Clip studies
		else if ( name.equals(ADX.STUDY_NAME))
			bs = new ADX( bars, params );
		else if ( name.equals(AO.STUDY_NAME))
			bs = new AO( bars, params );
		else if ( name.equals(ATRPercent.STUDY_NAME))
			bs = new ATRPercent( bars, params );
		else if ( name.equals(ATR.STUDY_NAME))
			bs = new ATR( bars, params );
		else if ( name.equals(Candle.STUDY_NAME))
			bs = new Candle( bars, params );
		else if ( name.equals(DTosc.STUDY_NAME))
			bs = new DTosc( aTape, params );
		else if ( name.equals(IntrabarIntensity.STUDY_NAME))
			bs = new IntrabarIntensity( bars, params );
		else if ( name.equals(IINormalized.STUDY_NAME))
			bs = new IINormalized( bars, params );
		else if ( name.equals(Efficiency.STUDY_NAME))
			bs = new Efficiency( bars, params );
		else if ( name.equals(ForceIndex.STUDY_NAME))
			bs = new ForceIndex( bars, params );
		else if ( name.equals(MACD.STUDY_NAME))
			bs = new MACD( bars, params );
		else if ( name.equals(MFI.STUDY_NAME))
			bs = new MFI( bars, params );
		else if ( name.equals(BWMFI.STUDY_NAME))
			bs = new BWMFI( bars, params );
		else if ( name.equals(Momentum.STUDY_NAME))
			bs = new Momentum( bars, params );
		else if ( name.equals(REI.STUDY_NAME))
			bs = new REI( bars, params );
		else if ( name.equals(SQN.STUDY_NAME))
			bs = new SQN( bars, params );
		else if ( name.equals(TDREI.STUDY_NAME))
			bs = new TDREI( bars, params );
		else if ( name.equals(RSf.STUDY_NAME))
			bs = aTape.getBarSize().equals(BarSize.ONE_DAY)?
				   new RSf( bars, params ) : null;
		else if ( name.equals(RSI.STUDY_NAME))
			bs = new RSI( bars, params );
		else if ( name.equals(Stochastic.STUDY_NAME))
			bs = new Stochastic( bars, params );
		else if ( name.equals(TDSetup.STUDY_NAME))
			bs = new TDSetup( bars, params );
		else if ( name.equals(Temperature.STUDY_NAME))
			bs = new Temperature( bars, params );
		else if ( name.equals(VolMFI.STUDY_NAME))
			bs = new VolMFI( bars, params );
		else if ( name.equals(Volume.STUDY_NAME))
			bs = new Volume( bars, params );
		else if ( name.equals(WilliamsR.STUDY_NAME))
			bs = new WilliamsR( bars, params );

		// Price Overlays
		else if ( name.equals(CandleSticks.STUDY_NAME))
			bs = new CandleSticks( bars, params );
		else if ( name.equals(AutoEnvelope.STUDY_NAME))
			bs = new AutoEnvelope( bars, params );
		else if ( name.equals(ElderImpulse.STUDY_NAME))
			bs = new ElderImpulse( aTape, params );
		else if ( name.equals(OpeningRange.STUDY_NAME))
			bs = aTape.getBarSize().equals(BarSize.FIVE_MIN)?
			     new OpeningRange( bars, params ) : null;
		else if ( name.equals(SAR.STUDY_NAME))
			bs = new SAR( bars, params );
		else if ( name.equals(BollingerBands.STUDY_NAME))
			bs = new BollingerBands( bars, params );
		else if ( name.equals(Dragon.STUDY_NAME))
			bs = new Dragon( bars, params );
		else if ( name.equals(River.STUDY_NAME))
			bs = new River( bars, params );
		else if ( name.equals(Fractals.STUDY_NAME))
			bs = new Fractals( bars, params );
		else if ( name.equals(DonchianBands.STUDY_NAME))
			bs = new DonchianBands( bars, params );
		else if ( name.equals(SafeZone.STUDY_NAME))
			bs = new SafeZone( bars, params );
		else if ( name.equals(TDCamouflage.STUDY_NAME))
			bs = new TDCamouflage ( bars, params );
		else if ( name.equals("Price Channels"))
			bs = null; // new PriceChannels( bars, params );
		else if ( name.equals(TDRangeProjection.STUDY_NAME))
			bs = new TDRangeProjection ( bars, params );
		else if ( name.equals(TLOC.STUDY_NAME))
			bs = new TLOC ( bars, params );
		else
			{
			SBLog.error(toString(), "Unrecognized study: " + name );
			return null;
			}
		if ( bs != null )
			m_studies.add(bs);
		return bs;
		}

	public final void update( int index )
		{
		for ( TapeStudy study : m_studies )
			study.dataChanged( index );
		}

	private final TapeStudy findStudy ( String name, String params )
		{
		for ( TapeStudy bs : m_studies )
			if ( bs.getName().equals(name)
			&&   bs.getParams().equals(params))
				return bs;
		return null;
		}
	}
