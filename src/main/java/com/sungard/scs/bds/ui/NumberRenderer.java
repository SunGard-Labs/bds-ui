package com.sungard.scs.bds.ui;

import java.text.NumberFormat;

public class NumberRenderer extends FormatRenderer {
	private static final long serialVersionUID = 5731701665971785486L;

	public NumberRenderer(NumberFormat formatter) {
		super(formatter);
		setHorizontalAlignment(4);
	}

	public static NumberRenderer getCurrencyRenderer() {
		return new NumberRenderer(NumberFormat.getCurrencyInstance());
	}

	public static NumberRenderer getIntegerRenderer() {
		return new NumberRenderer(NumberFormat.getIntegerInstance());
	}

	public static NumberRenderer getPercentRenderer() {
		return new NumberRenderer(NumberFormat.getPercentInstance());
	}
}
