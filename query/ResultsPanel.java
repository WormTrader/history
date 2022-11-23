package com.wormtrader.history.query;
/********************************************************************
* @(#)ResultsPanel.java 1.00 20130404
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* ResultsPanel: Monitors a Query in progress. Displays the QueryHit
* objects in a table as they are published by the QueryWorker
* background process. Shows a progress bar and has a cancel button.
*
* Double click on a hit to pull up its five minute chart.
*
* @author Rick Salamone
* @version 1.00
* 20120806 rts created
* 20130404 rts decoupled from the dialog that holds the results
*******************************************************/
import com.wormtrader.dao.PNL;
import com.wormtrader.dao.PNLRenderer;
import com.wormtrader.dao.USD;
import com.wormtrader.dao.USDRenderer;
import com.wormtrader.dao.HitListener;
import com.shanebow.ui.LAF;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class ResultsPanel
	extends JPanel
	{
	private final HitListener   fHitListener;
	private final HitsModel     fHitsModel = new HitsModel();
	private final JLabel        lblSummary = new JLabel();

	public ResultsPanel(HitListener aHitListener)
		{
		super(new BorderLayout());
		fHitListener = aHitListener;

		JTable table = new JTable(fHitsModel)
			{
			// ctor code
				{
				setFillsViewportHeight(true);
				setDefaultRenderer(PNL.class, new PNLRenderer());
				setDefaultRenderer(USD.class, new USDRenderer());
				addMouseListener( new MouseAdapter ()
					{
					public void mouseReleased (MouseEvent e)
						{
						if (e.getClickCount() <= 1) return; // doubleClick ();
						int row = rowAtPoint(e.getPoint());
						if ( row < 0 )	return;
						QueryHit hit = fHitsModel.get(row);
						fHitListener.hitSelected(hit.symbol(), hit.yyyymmdd(), hit.hhmm());
						}
					});
				}

			public void tableChanged(javax.swing.event.TableModelEvent e)
				{
				super.tableChanged(e);
				int lastRow = getRowCount() - 1;
				if ( lastRow > 5)
					scrollRectToVisible( getCellRect(lastRow, 0, true));
				}
			};

		add(LAF.titled("Results", new JScrollPane(table)), BorderLayout.CENTER);
		add(LAF.titled("Summary", lblSummary), BorderLayout.SOUTH);
		lblSummary.setVisible(false);
		}

	void showSummary(String aHtmlSummary)
		{
		lblSummary.setText(aHtmlSummary);
		lblSummary.setVisible(true);
		}

	void reset(HitProcessor aHitProcessor)
		{
		fHitsModel.reset(aHitProcessor.headers(), aHitProcessor.classes());
		lblSummary.setVisible(false);
		}

	void append(QueryHit aHit)
		{
		fHitsModel.append(aHit);
		}
	}
