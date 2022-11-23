package com.wormtrader.history.ui;
import com.wormtrader.history.Tape;
/*
* BarEditor.java
*
*  Required by TapeView in order to allow editing the bars in a tape.
*  Only one method must be implemented:
*    editBar(Tape tape, int barIndex)
*
*  To record changes, call tape.barModified( barIndex, commit )
*  where commit should be set to true to write changes to disk.
*  This call will generate a TapeEvent to notify TapeListeners
*  of the modifications. Note that no consistency checking is done
*  in the barModified method - it is up to the bar editor to
*  validate the changes (e.g. high >= low).
*/
public interface BarEditor
	{
	public void editBar( Tape tape, int barIndex );
	}
