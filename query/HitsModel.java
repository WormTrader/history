package com.wormtrader.history.query;
/********************************************************************
* @(#)HitsModel.java 1.00 20120903
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* HitsModel: Simple non-editable table model wrapped around a Vector
* of QueryHit. Columns are essentially managed in the QueryHit class.
*
* @author Rick Salamone
* @version 1.00
* 20120903 rts created
*******************************************************/
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

public final class HitsModel
	extends AbstractTableModel
	{
	private String[] fHeaders = {};
	private Class[] fClasses = {};
	private final Vector<QueryHit> fHits = new Vector<QueryHit>();

	public final void reset(String[] aHeaders, Class[] aClasses)
		{
		fHeaders = aHeaders;
		fClasses = aClasses;
		fHits.clear();
		fireTableStructureChanged();
		}

	public final void append(QueryHit row)
		{
		int index = fHits.size();
		fHits.add(row);
		fireTableRowsInserted( index, index );
		}

	public final QueryHit get(int r) { return fHits.get(r); }

	@Override public Object getValueAt(int r, int c) { return fHits.get(r).field(c); }
	@Override public Class  getColumnClass(int c) { return fClasses[c]; }
	@Override public int    getColumnCount() { return fHeaders.length; }
	@Override public String getColumnName(int c) { return fHeaders[c]; }
	@Override public int    getRowCount() { return fHits.size(); }
	}
