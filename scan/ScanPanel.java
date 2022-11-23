package com.wormtrader.history.scan;
/********************************************************************
* @(#)ScanPanel.java 1.00 20090719
* Copyright © 2007-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ScanPanel: Prompts user for date and symbols lists to supply to
* the history grabber. 1 1 2 3 5 8 13 21 34 
*
* @author Rick Salamone
* @version 1.00
* 20090719 rts created
* 20120710 rts reorganized layout
*******************************************************/
import com.wormtrader.bars.symbols.*;
import com.wormtrader.dao.HitListener;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SplitPane;
import com.shanebow.ui.calendar.MonthCalendar;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBProperties;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public final class ScanPanel
	extends JPanel
	{
	private static final String KEY_PREFIX="usr.scan.";

	private ScanWorker m_scanWorker;

	private final SymbolListSelectionPanel symbolListSelector
					= new SymbolListSelectionPanel(KEY_PREFIX);
	private final MonthCalendar fCalendar = new MonthCalendar();

	private JTree tree;
	private ScanList m_root = ScanList.getInstance();
	private SymbolListPanel hitsPane = new SymbolListPanel();
//	private QuickTestTable hitsPane = new QuickTestTable();

	/** Filter inputs */
	private final JCheckBox  chkM5Exists = new JCheckBox("M5 Exists");
	private final JTextField tfMinPrice  = new JTextField("");
	private final JTextField tfMaxPrice  = new JTextField("");

	/** Controls */
	private JButton btnStart, btnStop;
	private JProgressBar progressBar;

	public ScanPanel(final HitListener pcl)
		{
		this();
		hitsPane.addPropertyChangeListener(new PropertyChangeListener()
			{
			public void propertyChange(PropertyChangeEvent evt)
				{
				String property = evt.getPropertyName();
				if (property.equals(SymbolListPanel.DOUBLE_CLICK_PROPERTY))
					{
					String symbol = (String) evt.getNewValue();
					String yyyymmdd = SBDate.yyyymmdd(fCalendar.getDate());
					pcl.hitSelected(symbol, yyyymmdd, "16:00");
					}
				}
			});
		}

	public ScanPanel()
		{
		super(new BorderLayout());
		//Create a tree that allows one selection at a time.
		tree = new JTree((TreeNode)m_root);
		tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener()
			{
			public void valueChanged(TreeSelectionEvent e)
				{
				TreeNode node = (TreeNode)tree.getLastSelectedPathComponent();
				if (node == null) return;

				if (node.equals(m_root))
					displayRoot();
				else if ( node instanceof ScanNode )
					displayHits((ScanNode)node);
				}
			});

		//Add the scroll panes to a split pane.
		SplitPane splitPane = new SplitPane(SplitPane.VSPLIT,
                      new JScrollPane(tree), new JScrollPane(hitsPane),
		                  KEY_PREFIX+"vsplit", 180);

		add(topPane(), BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(controlPanel(), BorderLayout.SOUTH);
		setBorder(LAF.bevel(5,5));
		}

	private JPanel topPane()
		{
		JPanel it = new JPanel(new BorderLayout());
		JPanel dateContainer = new JPanel();
		dateContainer.add(fCalendar);
		it.add(LAF.titled("Scan Date", dateContainer), BorderLayout.EAST );
		JPanel filterPanel = new JPanel(new GridLayout(0,1));
		filterPanel.add(symbolListSelector);
		filterPanel.add(chkM5Exists);
		JPanel pricePanel = new JPanel(new GridLayout(2,2));
		pricePanel.add(new JLabel("Min price"));
		pricePanel.add(tfMinPrice);
		pricePanel.add(new JLabel("Max price"));
		pricePanel.add(tfMaxPrice);
		filterPanel.add(pricePanel);
		it.add(LAF.titled(filterPanel, "Filters"), BorderLayout.CENTER);
		restoreSettings();
		return it;
		}

	private void restoreSettings()
		{
		SBProperties props = SBProperties.getInstance();
		tfMinPrice.setText(props.getProperty(KEY_PREFIX+"price.min", "0.00"));
		tfMaxPrice.setText(props.getProperty(KEY_PREFIX+"price.max", "1000.00"));
		chkM5Exists.setSelected(props.getBoolean(KEY_PREFIX+"m5exists", true));
		fCalendar.setDate(props.getProperty(KEY_PREFIX+"date", SBDate.yyyymmdd()));
		}

	private void saveSettings()
		{
		SBProperties props = SBProperties.getInstance();
		props.setProperty(KEY_PREFIX+"price.min", tfMinPrice.getText());
		props.setProperty(KEY_PREFIX+"price.max", tfMaxPrice.getText());
		props.setProperty(KEY_PREFIX+"m5exists", chkM5Exists.isSelected());
		props.setProperty(KEY_PREFIX+"date", fCalendar.yyyymmdd());
		}

	long getTime() { return SBDate.open(fCalendar.getDate()); }
	SymbolList getSymbolList() { return symbolListSelector.	getSelected(); }

	private void displayRoot()
		{
		tree.setSelectionInterval(0,0);
		displayHits(null);
		}

	private void displayHits(ScanNode node)
		{
		hitsPane.clear();
		if (node == null)
			{
			hitsPane.setTitle( "Results..." );
			return;
			}
		int nHits = node.numHits();
		hitsPane.setTitle( node.toString() + ": " + nHits + " hits"
											+ " on " + SBDate.yyyymmdd(ScanNode.getTime()));
		for ( int i = 0; i < nHits; i++ )
			{
			ScanHit hit = node.get(i);
			hitsPane.append( hit.m_symbol + "\t" );
			}
		hitsPane.showTop();
		}

	public JPanel controlPanel()
		{
		JPanel it = new JPanel();
		it.add(btnStart = new SBAction("Run", 'R', "", null)
			{
			public void action() { onStart(); }
			}.makeButton());
		it.add(btnStop  = new SBAction("Cancel", 'C', "", null)
			{
			public void action() { onStop(); }
			}.makeButton());
		btnStop.setVisible(false);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		it.add(progressBar);
		return it;
		}

	private void onStart()
		{
		int minPrice = 0;
		int maxPrice = 0;
		try { minPrice = SBFormat.parseDollarString(tfMinPrice.getText()); }
		catch (Exception e) { SBDialog.inputError("Bad min price"); return; }
		try { maxPrice = SBFormat.parseDollarString(tfMaxPrice.getText()); }
		catch (Exception e) { SBDialog.inputError("Bad max price"); return; }
		long time = getTime();
		if ( time == 0 )
			return;
		ScanNode.setTime( time );
		btnStart.setVisible(false);
		btnStop.setVisible(true);
		saveSettings();
		displayRoot();
		ScanList nodes = ScanList.getInstance();
		nodes.clearHits();
		m_scanWorker = new ScanWorker( nodes, getSymbolList(), minPrice, maxPrice,
			chkM5Exists.isSelected())
			{
			@Override protected void process(java.util.List<String> chunks)
				{
				progressBar.setValue(getProgress());
				}

			@Override public void done()
				{
				progressBar.setValue(getProgress());
				java.awt.Toolkit.getDefaultToolkit().beep();
				btnStart.setVisible(true);
				btnStop.setVisible(false);
				}
			};
		m_scanWorker.execute();
		}

	private void onStop()
		{
		if (m_scanWorker != null)
			m_scanWorker.cancel(true);
		m_scanWorker = null;
		}
	}
