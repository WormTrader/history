package com.wormtrader.history.scan;
/*
* QuickTestTable.java
*
* 20090317 rts created
*
*/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.shanebow.ui.table.SideCellRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBDate;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;

public class QuickTestTable extends JTable
	implements MouseListener
	{
	public static final String DOUBLE_CLICK_PROPERTY="double click";

	public QuickTestTable()
		{
		super( new QuickTestTableModel());
		QuickTestTableModel qtModel = (QuickTestTableModel)getModel();
//		qtModel.reset();
		addMouseListener(this); // set up to handle double click
		}

	public void clear() { reset( null, null ); }
	public void reset( ScanNode scan, String yyyymmdd )
		{
		((QuickTestTableModel)getModel()).reset( scan, yyyymmdd );
		}

/***************
	@Override
	public void tableChanged(javax.swing.event.TableModelEvent e)
		{
		super.tableChanged(e);
		int lastRow = getRowCount() - 1;
		if ( lastRow > 5)
			scrollRectToVisible( getCellRect(lastRow, 0, true));
		}
***************/

	@Override
	public TableCellRenderer getCellRenderer(int row, int column)
		{
		return (column == 0) ? super.getCellRenderer(row, column)
//											: SideCellRenderer.getInstance();
											: com.shanebow.ui.table.DollarCellRenderer.getInstance();
		}

	// implement MouseListener
	public void mouseClicked(MouseEvent e)
		{ if ( e.getClickCount() > 1 ) onDoubleClick(); }
	public void mousePressed(MouseEvent e)
		{ if ( e.isPopupTrigger()) onRightClick(); }
	public void mouseReleased(MouseEvent e)
		{ if ( e.isPopupTrigger()) onRightClick(); }
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	private final void onRightClick() {}
	private final void onDoubleClick() // deterime which bar selected
		{												// & launch dialog to edit it
		int row = getSelectionModel().getLeadSelectionIndex();
		if ( row >= 0 )
			{
			String symbol = (String)((QuickTestTableModel)getModel()).getValueAt( row, 0 );
			firePropertyChange(DOUBLE_CLICK_PROPERTY, "", symbol);
			}
		else java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}

class QuickTestTableModel extends AbstractTableModel
	{
	public static final int COL_SYMBOL = 0;
	private Vector<QuickTestRow> m_rows = new Vector<QuickTestRow>();
	private long[] m_dates = new long[5];
	private ScanNode fNode;

	public void reset( ScanNode scan, String yyyymmdd )
		{
		m_rows.clear();
		fNode = scan;
		if ( scan == null )
			{
			fireTableStructureChanged();
			return;
			}
		m_dates[0] = SBDate.toTime("20090317");
		m_dates[1] = SBDate.toTime("20090318");
		m_dates[2] = SBDate.toTime("20090319");
		m_dates[3] = SBDate.toTime("20090320");
		m_dates[4] = SBDate.toTime("20090324");
		int nHits = scan.numHits();
		for ( int i = 0; i < nHits; i++ )
			{
			ScanHit hit = scan.get(i);
			m_rows.add( new QuickTestRow( hit.m_symbol, m_dates ));
			}
		fireTableStructureChanged();
		}

	public int getRowCount()
		{
		return m_rows.size();
		}

	public int getColumnCount()
		{
		return 1 + m_dates.length;
		}

	public Object getValueAt( int r, int c )
		{
		if ( m_rows.size() == 0 )
			return "";
		return (c == 0) ? fNode.get(r).m_symbol
									: m_rows.get(r).getValue( c - 1 );
		}

	public String  getColumnName(int c)
		{
		return (c == 0) ? "Symbol" : SBDate.yyyymmdd(m_dates[c-1]);
		}
	public boolean isCellEditable(int r, int c) { return false; }
	}

class QuickTestRow
	{
	int[] prices;
	public QuickTestRow( String symbol, long[] dates )
		{
		BarList bars = new BarList();
		long[] dateRange = { dates[0], dates[dates.length-1] };
		prices = new int[dates.length];
		bars.fetch(symbol, BarSize.ONE_DAY, dateRange);
		int i = 0;
		for ( Bar bar : bars )
			if ( bar.getTime() == dates[i] )
				prices[i++] = bar.getClose();
		bars.clear();
		}

	String getValue(int index)
		{
		return SBFormat.toDollarString(prices[index]);
		}
	}
