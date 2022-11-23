package com.wormtrader.history.event;

public interface TapeListener extends java.util.EventListener
	{
	public abstract void tapeChanged(TapeEvent e);
	}
