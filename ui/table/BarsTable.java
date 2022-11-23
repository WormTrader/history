package com.wormtrader.history.ui.table;
/*
 * BarsTable.java
 *
 */
import com.wormtrader.bars.Bar;
import com.wormtrader.history.Tape;
import com.wormtrader.history.ui.BarEditor;
import com.shanebow.ui.table.DollarCellRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBDate;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public final class BarsTable extends JTable
	implements MouseListener
	{
	private boolean   m_editingBar = false;
	private BarEditor m_barEditor = null;

	public BarsTable( Tape tape )
		{
		this();
		setTape( tape );
		}
	public BarsTable()
		{
		super( new BarsTableModel());
		addMouseListener(this); // set up to handle double click
		}

	public void setTape( Tape tape )
		{
		((BarsTableModel)getModel()).setTape(tape, this);
		}

	@Override
	public void tableChanged(javax.swing.event.TableModelEvent e)
		{
		super.tableChanged(e);
		if ( m_editingBar )
			{
			m_editingBar = false;
			return;
			}
		int lastRow = getRowCount() - 1;
		if ( lastRow > 5)
			scrollRectToVisible( getCellRect(lastRow, 0, true));
		}

	@Override
	public TableCellRenderer getCellRenderer(int r, int c)
		{
		return ( c == BarsTableModel.COL_DATE ) ? super.getCellRenderer(r, c)
		        : DollarCellRenderer.getInstance();
		}

	public void setBarEditor( BarEditor editor )
		{
		m_barEditor = editor;
		}

	// implement MouseListener - double click a bar to edit it
	public void mousePressed(MouseEvent e)
		{ if (e.isPopupTrigger()) popup(); }
	public void mouseReleased(MouseEvent e)
		{ if (e.isPopupTrigger()) popup(); }
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e)
		{
		if (( m_barEditor != null )
		&&  ( e.getClickCount() > 1 ))
			onEditBar();
		} 

	private final void popup() { ((BarsTableModel)getModel()).getTape().dump(); }
	private final void onEditBar() // deterime which bar selected
		{												// & launch dialog to edit it
		int row = getSelectionModel().getLeadSelectionIndex();
		if ( row >= 0 )
			{
			Tape tape = ((BarsTableModel)getModel()).getTape();
			m_editingBar = true;
			m_barEditor.editBar(tape, row);
			}
		else java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}
