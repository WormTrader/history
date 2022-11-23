package com.wormtrader.history.ui;
/********************************************************************
* @(#)TapeView.java 1.00 20090722
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* TapeView: A split screen with a graph of the tape on top and a table
* of the bars below. The one touch resizable split initially gives all
* the space to the graph.
*
* @author Rick Salamone
* @version 1.00
* 20090722 rts created
* 20120310 rts added graph() method
* 20120607 rts added backfill combo box
* 20121006 rts added the Yahoo Info menu item here temporarily
* 20121012 rts added getTape()
*******************************************************/
import com.wormtrader.dao.SBExecution;
import com.wormtrader.history.Tape;
import com.wormtrader.history.ui.BarEditor;
import com.wormtrader.history.ui.graph.*;
import com.wormtrader.history.ui.table.BarsTable;
import com.wormtrader.history.yahoo.ActYahooInfo;
import com.shanebow.ui.graph.SBGraph;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class TapeView // @TODO: make final after BTTapeView cleaned up
	extends JSplitPane
	{
	private static final ActYahooInfo _actYahooInfo = new ActYahooInfo();
	private final TapeChartModel m_graphModel;
	private final BarsTable        tblBars;
	private final JComboBox        cbBackfill;

	public TapeView(Tape tape)
		{
		this(new CandleGraphModel( tape ));
		}

	public TapeView( Tape tape, BarEditor editor )
		{
		this(tape);
		registerBarEditor(editor);
		}

	public void registerBarEditor( BarEditor editor )
		{
		tblBars.setBarEditor( editor );
		}

	public TapeView(TapeChartModel aModel)
		{
		super ( JSplitPane.VERTICAL_SPLIT, true );

		m_graphModel = aModel;
		Tape tape = m_graphModel.getTape();
		SBGraph graph = new SBGraph( m_graphModel );
graph.addActions(_actYahooInfo);
		setTopComponent(graph);

		tblBars = new BarsTable(tape);
		setBottomComponent( new JScrollPane(tblBars));
		setPreferredSize(new Dimension(500,600));
		setDividerLocation(600);
		setResizeWeight(1);
		setOneTouchExpandable(true);
		setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
		                             new BevelBorder(BevelBorder.LOWERED)));

		cbBackfill = new JComboBox(tape.getBarSize().backfills());
		cbBackfill.addActionListener(new ActionListener()
			{
			@Override public void actionPerformed( ActionEvent e)
				{
				JComboBox cb = (JComboBox)e.getSource();
				Tape tape = m_graphModel.getTape();
				tape.adjGoBack((String)cb.getSelectedItem());
				}
			});
		selectBackfill(tape);
		graph.addTools(cbBackfill);
		}

	private void selectBackfill(Tape tape)
		{
		String goBack = tape.getGoBack();
		if ( goBack != null )
			cbBackfill.setSelectedItem(goBack);
		else
			cbBackfill.setSelectedIndex(2);
		}

	public SBGraph graph() { return (SBGraph)getTopComponent(); }
	public final Tape getTape() { return m_graphModel.getTape(); }
	public void setTape( Tape tape )
		{
		tblBars.setTape(tape);
		selectBackfill(tape);
		m_graphModel.setTape(tape);
_actYahooInfo.setSymbol(tape.getSymbol());
		}

	public void configure()
		{
		m_graphModel.configure();
		}

	public void setExecs( Iterable<SBExecution> execs )
		{
		m_graphModel.setExecs( execs );
		}
	}
