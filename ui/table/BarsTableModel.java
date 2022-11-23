package com.wormtrader.history.ui.table;
/********************************************************************
* @(#)BarsTableModel.java 1.00 2010
* Copyright © 2010-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* BarsTableModel
*
* @author Rick Salamone
* @version 1.00
* 2010     rts created
* 20111119 rts removed MACD, EMA, and Stochastics
* 20130308 rts documentation
*******************************************************/
import com.wormtrader.history.Tape;
import com.wormtrader.history.TapeStudy;
import com.wormtrader.history.event.TapeEvent;
import com.wormtrader.history.event.TapeListener;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBDate;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTable;
import javax.swing.table.*;

public class BarsTableModel
	extends AbstractTableModel
	implements TapeListener
	{
	public static final int COL_DATE = 0;
	public static final int COL_OPEN = 1;
	public static final int COL_HIGH = 2;
	public static final int COL_LOW = 3;
	public static final int COL_CLOSE = 4;
	public static final int COL_VOLUME = 5;
	public static final int COL_STUDY0 = 6;

	static final String[] columnNames =
		{ "Time", "Open", "High", "Low", "Close", "Volume" };

	private Tape m_tape = null;
	private int dateTimeFormat = SBDate.MMsDDsYY_HHcMM;
	private final Vector<TapeStudy> m_studies = new Vector<TapeStudy>(2);

	void setTape( Tape tape, JTable table )
		{
		if ( m_tape != null )
			m_tape.removeTapeListener(this);
		m_studies.clear();
		m_tape = tape;
		if ( m_tape != null )
			{
//			m_studies.add( tape.addStudy ( "MACD", "" ));
//			m_studies.add( tape.addStudy ( "Stochastics", "" ));
			m_studies.add( tape.addStudy ( "ATR", "" ));
	//		m_studies.add( tape.addStudy ( "EMA", "21,C" ));
			m_tape.addTapeListener(this);
			}
		fireTableStructureChanged();
		initColumns( table );
		}

	private static final int[] colWidths =
		{ 70,    25,    25,    25,   25,     35, 50, 50,  15 };
	private void initColumns( JTable table )
		{
		for ( int c = getColumnCount(); c-- > 0; )
			{
			TableColumn column = table.getColumnModel().getColumn(c);
			int newWidth = (c < colWidths.length) ? colWidths[c] : 18;
			column.setPreferredWidth( newWidth );
			}
		}

	public void tapeChanged(TapeEvent e)
		{
		fireTableDataChanged();
		}

	public int getRowCount()
		{
		return ( m_tape == null ) ? 0 : m_tape.size();
		}

	public int getColumnCount()
		{
		return columnNames.length + m_studies.size();
		}

	public Object getValueAt( int r, int c )
		{
		if (( m_tape == null ) || (m_tape.size() == 0))
			return "";
		switch ( c )
			{
			case COL_DATE:   return SBDate.format( m_tape.get(r).getTime(), dateTimeFormat );
			case COL_OPEN:   return SBFormat.toDollarString( m_tape.get(r).getOpen());
			case COL_HIGH:   return SBFormat.toDollarString( m_tape.get(r).getHigh());
			case COL_LOW:    return SBFormat.toDollarString( m_tape.get(r).getLow());
			case COL_CLOSE:  return SBFormat.toDollarString( m_tape.get(r).getClose());
			case COL_VOLUME: return "" + m_tape.get(r).getVolume();
			default:         return m_studies.get(c - COL_STUDY0).getMetrics(r);
			}
		}

	public boolean isCellEditable(int r, int c) { return false; }
	public String  getColumnName(int c)
		{
		return (c < columnNames.length)? columnNames[c]
		       : m_studies.get(c - COL_STUDY0).getClipLabel();
		}
	public Tape getTape() { return m_tape; }
	public String toString() { return getClass().getSimpleName() + ": " + m_tape; }
	}
