package com.wormtrader.history.query;
/********************************************************************
* @(#)QueryPanel.java 1.00 2009
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* QueryPanel.java: GAMBARU - Never Give Up (Japanese)
*
* @author Rick Salamone
* 2009 rts initial demo
* 20110521 rts modified to load scans from the custom jar file
* 20120905 rts added time panel with check to skip runs
* 20120907 rts added the time of day inputs
*******************************************************/
import com.wormtrader.bars.symbols.DateRangePanel;
import com.wormtrader.bars.symbols.SymbolList;
import com.wormtrader.bars.symbols.SymbolListSelectionPanel;
import com.wormtrader.dao.HitListener;
import com.wormtrader.history.query.hitprocessors.*;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.table.SideCellRenderer;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableCellRenderer;

public final class QueryPanel
	extends JPanel
	{
	private static final String KEY_PREFIX="usr.query.";
	private static final String[] START_TIMES =
		{
		"09:30", "09:35", "09:40", "09:45", "09:50", "09:55", "10:00"
		};

	private static final String[] SKIP_EOD =
		{
		"5 min", "10 min", "15 min", "20 min", "25 min", "30 min"
		};

	private static final String[] HIT_PROCESSORS =
		{
		"--", "TallyRuns", "TallyLongs"
		};

	Border border = BorderFactory.createLoweredBevelBorder();
	private final SymbolListSelectionPanel symbolListSelector
		= new SymbolListSelectionPanel(KEY_PREFIX);
	private final JTextField tfMoreSymbols = new JTextField();
	private final JComboBox cbStartTime = new JComboBox(START_TIMES);
	private final JComboBox cbSkipAtEnd = new JComboBox(SKIP_EOD);
	private final JComboBox cbHitProc = new JComboBox(HIT_PROCESSORS);
	private final JCheckBox  chkSkipRuns = new JCheckBox("Skip Runs?", true);
	private final DateRangePanel datePanel = new DateRangePanel();
	private CodeEditor taCode = new CodeEditor();
	private final DlgQResults dlgQResults;
	private final DlgQBuilder dlgQBuilder = new DlgQBuilder(taCode);

	public QueryPanel (HitListener aHitListener)
		{
		super(new BorderLayout());
		add(north(), BorderLayout.NORTH);
		add(LAF.titled(new JScrollPane(taCode), "Query Code"), BorderLayout.CENTER);

		QManager controls = new QManager(taCode);
		controls.add(new SBAction("Build", 'B', "Run the Query builder", null)
			{
			@Override public void actionPerformed(ActionEvent e) { dlgQBuilder.setVisible(true); }
			}.makeButton());
		controls.add(new SBAction("Run", 'R', "Compile and run the query", null)
			{
			@Override public void actionPerformed(ActionEvent e) { onCompile(); }
			}.makeButton());
		add(controls, BorderLayout.SOUTH);
		setBorder(LAF.bevel(5,5));
		dlgQResults = new DlgQResults(aHitListener);
		restore();
		}

	private JPanel north()
		{
		JPanel sym = new JPanel(new BorderLayout());
		sym.add( new JLabel(SBAction.getIcon("plus")), BorderLayout.WEST);
		sym.add( tfMoreSymbols, BorderLayout.CENTER );

		JPanel timePanel = new JPanel(); // new GridLayout(2,4));
		timePanel.add(new JLabel("Start @"));
		timePanel.add(cbStartTime);
		timePanel.add(new JLabel("Skip last"));
		timePanel.add(cbSkipAtEnd);
		timePanel.add(chkSkipRuns);

		JPanel it = new JPanel();
		it.setLayout(new BoxLayout(it, BoxLayout.Y_AXIS));
		it.add(LAF.titled(symbolListSelector, "Symbols from List"));
		it.add(LAF.titled(sym, "Additional Symbols (Comma separated)"));
		it.add(LAF.titled(datePanel, "Dates"));
		it.add(LAF.titled(timePanel, "Time of day to Process"));
		it.add(LAF.titled(cbHitProc, "Hit Processor"));
		return it;
		}

	private void restore()
		{
		SBProperties props = SBProperties.getInstance();
		tfMoreSymbols.setText(props.getProperty(KEY_PREFIX+"symcsv",""));
		datePanel.set(props.getProperty(KEY_PREFIX+"date0",""),
		              props.getProperty(KEY_PREFIX+"date1",""));
		cbHitProc.setSelectedItem(props.getProperty(KEY_PREFIX+"hitproc", "--"));
		int[] timeIndices = props.getIntArray(KEY_PREFIX+"times",0,0);
		cbStartTime.setSelectedIndex(timeIndices[0]);
		cbSkipAtEnd.setSelectedIndex(timeIndices[1]);
		}

	private void onCompile()
		{
		Query query = (Query)taCode.compile();
		if (query == null) return;
		long[] dateRange = datePanel.get();
		if (dateRange == null) return;
		SymbolList symbols = symbolListSelector.getSelected();
		String moreSymbols = tfMoreSymbols.getText();
		symbols.addCSV(moreSymbols);
		boolean skipRuns = chkSkipRuns.isSelected();
		int[] times = { cbStartTime.getSelectedIndex(),
		                cbSkipAtEnd.getSelectedIndex() };
		String hitProcName = (String)cbHitProc.getSelectedItem();
		HitProcessor hitProcessor = hitProcName.equals("TallyRuns")? new TallyRuns()
		                          : hitProcName.equals("TallyLongs")? new TallyLongs()
		                          : new ListHits();

		SBProperties props = SBProperties.getInstance();
		props.setProperty(KEY_PREFIX+"hitproc", hitProcName);
		props.setProperty(KEY_PREFIX+"symcsv", moreSymbols);
		props.setProperty(KEY_PREFIX+"date0", datePanel.getStartDate());
		props.setProperty(KEY_PREFIX+"date1", datePanel.getEndDate());
		props.setProperty(KEY_PREFIX+"times", times[0] + "," + times[1]);

		++times[1]; // selected index ZERO entry means skip ONE bar, etc
		dlgQResults.doStart(symbols, dateRange, times, skipRuns, hitProcessor, query);
		}
	}
