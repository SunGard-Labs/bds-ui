package com.sungard.scs.bds.ui;

import java.awt.Component;

import javax.swing.JCheckBox;

import org.jdesktop.application.session.PropertySupport;

class JCheckBoxPropertySupport implements PropertySupport {
	public void setSessionState(Component c, Object state) {
		((JCheckBox) c).setSelected(((Boolean) state).booleanValue());
	}

	public Object getSessionState(Component c) {
		return Boolean.valueOf(((JCheckBox) c).isSelected());
	}
}
