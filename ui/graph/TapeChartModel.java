package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)TapeChartModel.java 1.00 ?????????
* Copyright © 2007 - 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* TapeChartModel: Provides the glue between a graph and the tape it
* displays. It causes the correct templates to be loaded and sets the
* graph's clips accordingly. it also notifies the graph when data (bars)
* have been added, and controls the saving and restoring of anotations
* to the graph.
*
* IMPORTANT USAGE NOTE: Quite often a single graph object is used to
* display different symbols at different times. There are two ways to
* accomplish this (depending on the requirements of the app), but this
* object must work with either method:
* Method A) Call this object's setTape() method, which will swap all
*           references to the old tape with references to the new
*           tape. Then it calls the graph's dataChanged() to refresh.
* Method B) Call one of the tape's method's that change it's symbol,
*           such as setSymbol() or one of the reset() methods. In
*           this case, this model object must react to tape events
*           to keep everything copacetic.
* Method A is used in apps like the AutoTrader where every position
* has it's own tape (that is being processed by strategies in real
* time), but only one tape is visible as a graph at any given time.
* Method B is used by the HistoryManager for instance where there is
* one tape object (per time frame) that is reloaded from disk with
* whatever symbol the user selects.
* 
* @author Rick Salamone
* @version 1.00
* ???????? rts created
* 20080518 rts handles moving average configurations
* 20120501 rts added getTape()
* 20120504 rts added DailyDomainRenderer that works
* 20120608 rts save and restore zoom in properties
* 20120628 rts save and restore annotations
* 20120719 rts handles offset moving averages
* 20120921 rts execution plotting checks the symbol for match
* 20121006 rts modified to be base class for Candle & Mini models
* 20121126 rts added check for null graph to tapeChanged()
* 20130227 rts modified to handle weekly bar size
* 20130305 rts tooltip uses different time format than x-axis
* 20130305 rts added weekly domain renderer
* 20130411 rts modified to support exec price in USD
* 20140331 rts setupOlays & setupClips now apply params
* 20140612 rts candlesticks are now an overlay, no longer drawn here
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarSize;
import com.wormtrader.dao.SBExecution;
import com.wormtrader.history.Tape;
import com.wormtrader.history.event.TapeEvent;
import com.wormtrader.history.event.TapeListener;
import com.wormtrader.history.indicators.MovingAverage;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.ui.graph.ConfigurableGraphModel;
import com.shanebow.ui.graph.GraphModel;
import com.shanebow.ui.graph.DomainRenderer;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBProperties;

public class TapeChartModel
	implements ConfigurableGraphModel, TapeListener, TemplateListener
	{
	protected String KEY_PREFIX = "usr.minis.";
	protected Tape                  m_tape;
	protected int                   m_offset; // max MA offset past end of tape
	protected Iterable<SBExecution> m_allExecs = null;
	protected TGGraph               m_graph;
	protected int                   m_axisTimeFormat = SBDate.MMsDDsYY;
	protected int                   m_tipTimeFormat;

	protected MovingAverage[]       m_average; // MA's displayed in main graph clip
	protected GraphClipRenderer[]   m_olay;    // indicators in main graph panel
	protected GraphClipRenderer[]   m_clip;    // indicators in their own lower clip
	protected byte                  m_numClips = 1;
	protected final TemplateManager fTemplateMgr;

	public TapeChartModel(Tape aTape, TemplateManager aTemplateMgr)
		{
		m_average = new MovingAverage[GraphTemplate.MAX_AVERAGES];
		m_olay = new GraphClipRenderer[0];
		m_clip = new GraphClipRenderer[TGGraph.MAX_CLIPS];
		m_tape = aTape;
		m_tape.addTapeListener(this);
		applyTimeFormat();
		fTemplateMgr = (aTemplateMgr != null)? aTemplateMgr
		             : new TemplateManager(this,
		                   "usr.tape.gt." + m_tape.getBarSize().paramValue());
		}

	public final TGGraph getGraph() { return m_graph; }
	public final Tape getTape() { return m_tape; }
	public final void setTape(Tape aTape)
		{
		if ( m_tape != null )
			m_tape.removeTapeListener(this);
		m_tape = aTape;
		m_tape.addTapeListener(this);
		applyTimeFormat();
		m_graph.setTitle( m_tape.getTitle());
		applySettings();
		m_graph.dataChanged();
		restoreAnnotations();
		}

	// implement TapeListener
	public void tapeChanged( TapeEvent e )
		{
if (m_graph == null) return;
		int action = e.getActionID();
		if (action == TapeEvent.BACKFILLED)
			restoreAnnotations();
		else if (action != TapeEvent.SYMBOL_CHANGING) // ignore symbol change event
			m_graph.dataChanged();
		}

	@Override public void usrAnnotated(TGGraph graph)
		{
		Annotations.save(m_graph, m_tape);
		}

	private void restoreAnnotations()
		{
		m_graph.clearAnnotations(); // out with the old...
		Annotations.restore(m_graph, m_tape);
		}

	private void applyTimeFormat()
		{
		boolean isIntraday = m_tape.getBarSize().isIntraday();
		m_tipTimeFormat = isIntraday? SBDate.MMsDDsYY_HHcMM : SBDate.MMsDDsYY;
		m_axisTimeFormat = isIntraday? SBDate.HHcMM
		                 : m_tape.getBarSize().equals(BarSize.ONE_DAY) ? SBDate.MMMYY
		                 : SBDate.MMsYY;
		}

	void applySettings()
		{
		GraphTemplate template = fTemplateMgr.getTemplate();
		if ( template != null ) applySettings(template);
		}

	// implement TemplateListener
	@Override public void applySettings(GraphTemplate aTemplate)
		{
		setupAverages(aTemplate);
		setupOlays(aTemplate);
		setupClips(aTemplate);
		}

	public void setExecs( Iterable<SBExecution> allExecs )
		{
		m_allExecs = allExecs;
		}

	protected String zoomPropertyKey()
		{
		return KEY_PREFIX + "zoom." + m_tape.getBarSize().paramValue();
		}

	// implement ConfigurableGraphModel
	@Override public void addNotify( TGGraph g )
		{
		m_graph = g;
		int zoom = 3;
		if (m_tape.getBarSize().equals(BarSize.ONE_DAY))
			{
			m_graph.setDomainRenderer(new DailyDomainRenderer());
			zoom = 2;
			}
		else if (!m_tape.getBarSize().isIntraday())
			m_graph.setDomainRenderer(new WeeklyDomainRenderer());
		m_graph.setZoom(SBProperties.getInstance().getInt(zoomPropertyKey(), zoom));
		applySettings();
		}

	public int getNumDataPoints()
		{
		return m_tape.size() + m_offset;
		}

	public int getRangeMaximum(byte clip)
		{
		if ( m_clip[clip] != null )
			return m_clip[clip].getRangeMaximum();
		return 0;
		}

	public int getRangeMinimum(byte clip)
		{
		if ( m_clip[clip] != null )
			return m_clip[clip].getRangeMinimum();
		return 0;
		}

	public final String getDomainString(int i)
		{
		int nbars = m_tape.size();
		if ( i >= nbars ) return "";
		Bar bar = m_tape.get(i);
		return SBDate.format( bar.getTime(), m_axisTimeFormat );
		}

	public final String getRangeString(byte clip, int value)
		{
		if (clip == 0)
			return SBFormat.toDollarString(value);
		else if ( m_clip[clip] != null )
			return m_clip[clip].getRangeString( value );
		else return "" + value;
		}

	public final String getClipLabel(byte clip)
		{
//System.out.println("clip: " + clip + " num clips: " + m_numClips);
		return (clip == 0)? "" : m_clip[clip].getClipLabel();
		}

	public String getToolTipText(byte clip, int x)
		{
		int nbars = m_tape.size();
		if ( x >= nbars )
			{
			return "";
			}
		Bar bar = m_tape.get(x);
		return SBDate.format(bar.getTime(), m_tipTimeFormat) + " "
		     + ((clip == 0)? bar.ohlcString() : m_clip[clip].getToolTipText(x));
		}

	public final void plot ( int i )
		{
		byte clip = 0;
		for ( byte j = 0; j < m_average.length; j++ )
			if ( m_average[j] != null )
				m_average[j].plot( m_graph, clip, i );

		if ( i >= m_tape.size()) return; // only MA's can be offset past end of tape

		for ( byte j = 0; j < m_olay.length; j++ )
			if ( m_olay[j] != null )
				m_olay[j].plot( m_graph, clip, i );

		if ( m_allExecs != null )
			{
			Bar bar = m_tape.get(i);
			long barStartTime = bar.getTime();
			long barEndTime = barStartTime + m_tape.getBarSize().duration();
			String symbol = m_tape.getSymbol();
			for ( SBExecution e : m_allExecs )
				{
				long time = e.getTime();
				if ( time >= barEndTime ) break;
				if ( time < barStartTime ) continue;
				if ( !e.getDesc().equals(symbol)) continue;
				m_graph.drawExecution( e.getQty(), e.getPrice().cents());
				}
			}
		while ( ++clip < m_numClips )
			if ( m_clip[clip] != null )
				m_clip[clip].plot( m_graph, clip, i );
		}

	@Override public final void usrZoomed(int value)
		{
		SBProperties.set(zoomPropertyKey(), "" + value);
		}

	@Override public final void configure(TGGraph g) { configure(); }
	public final void configure()
		{
		if ( fTemplateMgr != null )
			fTemplateMgr.launch(getTape().getBarSize().toString());
		}

	private void setupClips(GraphTemplate aTemplate)
		{
		m_numClips = 1; // cause clip 0 is main price clip
		if ( aTemplate != null )
			{
			String[] names = aTemplate.getClipNames();
			String[] params = aTemplate.getClipParams();
			if ( names != null )
				m_numClips += (byte)(names.length);
			if ( m_numClips > m_clip.length )
				{
				System.err.println ( "Too many clips!" );
				System.exit(0);
				}
			for ( int i = 1; i < m_numClips; i++ )
				m_clip[i] = (GraphClipRenderer)(m_tape.addStudy( names[i-1], params[i-1] ));
			}
		if ( m_graph != null )
			m_graph.setClipCount( m_numClips );
		}

	private void setupAverages(GraphTemplate aTemplate)
		{
		if (aTemplate == null) return;
		MASettings[] maSettings = aTemplate.getMASettings();
		int numMA = maSettings.length;
		m_offset = 0;
		for ( int i = 0; i < numMA; i++ )
			{
			int type = maSettings[i].getAvgType();
			if ( !maSettings[i].isActive()
			||   (type == MASettings.USE_NONE))
				{
				m_average[i] = null;
				continue;
				}
			String name = maSettings[i].getAvgName();
			int offset = maSettings[i].getOffset();
			if (offset > m_offset) m_offset = offset;
			String settings = "" + maSettings[i].getPeriod()
                + "," + maSettings[i].getWhichPrice() + "," + offset;
			m_average[i] = (MovingAverage)(m_tape.addStudy( name, settings ));
			m_average[i].setColor(maSettings[i].getColor());
			}
		if (m_offset > 0 && m_graph != null)
			m_graph.dataChanged();
		}

	private void setupOlays(GraphTemplate aTemplate)
		{
		if (aTemplate == null) return;
		String[] list = aTemplate.getOverlayNames();
		String[] params = aTemplate.getOverlayParams();
		int numOverlays = (list == null) ? 0 : list.length;
		m_olay = new GraphClipRenderer[numOverlays];
		for ( int i = 0; i < numOverlays; i++ )
			m_olay[i] = (GraphClipRenderer)(m_tape.addStudy(list[i], params[i] ));
		}

	public String toString()
		{
		return getClass().getSimpleName() + ": " + m_tape
		              + ", " + fTemplateMgr.getTemplate();
		}

	class DailyDomainRenderer
		implements DomainRenderer
		{
		public boolean draw(java.awt.Graphics g, GraphModel aModel, int aIndex,
			int x, int y, int ticksPerLabel)
			{
			if (aIndex >= m_tape.size()) // in "offset" area = the future
				{
g.setColor(java.awt.Color.YELLOW);
				g.drawLine(x, y, x, y + SHORT_TICK);
				return false; // wantGridline
				}
			Bar bar = m_tape.get(aIndex);
			long time = bar.getTime();
			int month = SBDate.getMonth(time);
			int day = SBDate.dayOfWeek(time);
			int prevMonth, prevDay;
			if ( aIndex == 0 )
				prevMonth = prevDay = 0;
			else
				{
				long prevTime = m_tape.get(aIndex-1).getTime();
				prevMonth = SBDate.getMonth(prevTime);
				prevDay = SBDate.dayOfWeek(prevTime);
				}
			boolean wantGridLine = day < prevDay;
			int tickLength = SHORT_TICK;
			if ( month != prevMonth )
				{
				tickLength = TALL_TICK;
				String str;
				if (ticksPerLabel > 12) // zoomed with no room for MMM
					{
					str = (month == 1 || aIndex == 0)? ("" + (SBDate.year(time) % 100))
					                                 : "";
					}
				else str = SBDate.getMonthString(month) + " " + (SBDate.year(time) % 100);
				if (!str.isEmpty())
					g.drawString( str, Math.max(x-3,0), y + LABEL_Y_OFFSET);
				}
			else if (wantGridLine) tickLength = MEDIUM_TICK;
			g.drawLine(x, y, x, y + tickLength);
			return wantGridLine;
			}
		}

	class WeeklyDomainRenderer
		implements DomainRenderer
		{
		public boolean draw(java.awt.Graphics g, GraphModel aModel, int aIndex,
			int x, int y, int ticksPerLabel)
			{
			if (aIndex >= m_tape.size()) // in "offset" area = the future
				{
g.setColor(java.awt.Color.YELLOW);
				g.drawLine(x, y, x, y + SHORT_TICK);
				return false; // wantGridline
				}
			long time = m_tape.get(aIndex).getTime();
			String str = SBDate.format(time, m_axisTimeFormat);
			String prev = (aIndex == 0)? "" : SBDate.format(m_tape.get(aIndex-1).getTime(), m_axisTimeFormat);
			boolean wantGridLine = false;
			int tickLength = SHORT_TICK;
			if ( !str.equals(prev))
				{
				wantGridLine = str.startsWith("01");
				tickLength = (wantGridLine)? TALL_TICK : MEDIUM_TICK;
				g.drawString( str, Math.max(x-3,0), y + LABEL_Y_OFFSET);
				}
			g.drawLine(x, y, x, y + tickLength);
			return wantGridLine;
			}
		}
	} // 264
