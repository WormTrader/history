package com.wormtrader.history.query;
/********************************************************************
* @(#)DlgQResults.java 1.00 20120806
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgQResults: Monitors a Query in progress. Displays the QueryHit
* objects in a table as they are published by the QueryWorker
* background process. Shows a progress bar and has a cancel button.
*
* Double click on a hit to pull up its five minute chart.
*
* @author Rick Salamone
* @version 1.00
* 20120806 rts created
* 20130403 rts generalized to send hit to listener
* 20130404 rts decoupled to results panel
*******************************************************/
import com.wormtrader.bars.symbols.SymbolList;
import com.wormtrader.dao.PNL;
import com.wormtrader.dao.PNLRenderer;
import com.wormtrader.dao.USD;
import com.wormtrader.dao.USDRenderer;
import com.wormtrader.dao.HitListener;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.ToggleOnTop;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public final class DlgQResults
	extends JDialog
	{
	private static final String CMD_CANCEL="Cancel";
	private static final String CMD_DETAILS="Details";

	private final JButton       fButton = new JButton(CMD_CANCEL);
	private final JProgressBar  fProgress = new JProgressBar(0,100);
	private final JLabel        fStatus = new JLabel("running query");
//	private final HitsModel     fHitsModel = new HitsModel();
	private final ResultsPanel  fResults;
	private       QueryWorker   fQueryWorker;

	public DlgQResults(HitListener aHitListener)
		{
		super((Frame)null, LAF.getDialogTitle("Query Results"), false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		fResults = new ResultsPanel(aHitListener);
		JPanel content = new JPanel(new BorderLayout());
		content.add(fResults, BorderLayout.CENTER);
		content.add(controlPanel(),BorderLayout.SOUTH);
		content.setBorder(new EmptyBorder(5, 10, 2, 10));
		setContentPane(content);

		setPreferredSize(new Dimension(475,400));
		pack();
		setLocationByPlatform(true);
		try { setAlwaysOnTop(true); }
		catch (Exception e) {} // guess we can't always be on top!
		LAF.addUISwitchListener(this);
		}

	private JPanel controlPanel()
		{
		JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		fButton.addActionListener( new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				JButton src = (JButton)e.getSource();
				String cmd = src.getText();
				if ( cmd.equals(CMD_CANCEL))
					{
					if (fQueryWorker != null)
						fQueryWorker.cancel(true);
//					fButton.setText(CMD_DETAILS);
					fButton.setVisible(false);
					}
		/********
				else if ( cmd.equals(CMD_DETAILS))
					SBDialog.inform(fButton, "Query Details", fDetails);
//					setVisible(false);
		********/
				}
			});
		p.add( fButton );
		ToggleOnTop tot = new ToggleOnTop();
		p.add( tot );
		tot.setSelected(true);

		JPanel cp = new JPanel(new BorderLayout());
		cp.add(fProgress, BorderLayout.CENTER);
fProgress.setVisible(false);
fProgress.setMaximumSize(new Dimension(200,14));
		cp.add(fStatus, BorderLayout.WEST);
		cp.add(p, BorderLayout.EAST);
		return cp;
		}

int count = 0;
	public void doStart(SymbolList aSymbolList, long[] aDateRange,
	   int[] aTimes, boolean aSkipRuns, HitProcessor aHitProcessor, Query aQuery)
		{
		fStatus.setVisible(false);
		fProgress.setValue(0);
		fProgress.setIndeterminate(true);
		fProgress.setVisible(true);
	//	fHitsModel.clear();
		fResults.reset(aHitProcessor);
		fButton.setText(CMD_CANCEL);
		fButton.setVisible(true); // setText(CMD_DETAILS);
		setVisible(true);
		fButton.requestFocus();
		fQueryWorker = new QueryWorker(aSymbolList, aDateRange, aTimes,
		                               aQuery, aHitProcessor)
			{
			@Override protected void process(java.util.List<QueryHit> hits)
				{
				for (QueryHit hit : hits)
					{
					fResults.append(hit);
		//			fHitsModel.append(hit);
//if (fHitsModel.getRowCount() > 100) cancel(true);
					}
				fProgress.setIndeterminate(false);
				fProgress.setValue(fNumSymbolsDone * 100 / fNumSymbolsTotal);
				}

			@Override public void done()
				{
				fButton.setVisible(false); // setText(CMD_DETAILS);
				fQueryWorker = null;
				fProgress.setVisible(false);
				Toolkit.getDefaultToolkit().beep();
				String summary;
				if (isCancelled()) summary = "Cancelled";
				else
					{
					try { summary = get(); }
					catch (Exception e) { e.printStackTrace(); summary = "<html>" + e.toString(); }
					fResults.showSummary(details());
					}
				fStatus.setText(summary);
				fStatus.setVisible(true);
				}
			};
		fQueryWorker.fSkipRuns = aSkipRuns;
		fQueryWorker.execute();
		}
	}
