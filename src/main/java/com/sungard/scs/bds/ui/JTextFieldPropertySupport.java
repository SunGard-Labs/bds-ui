package com.sungard.scs.bds.ui;

import java.awt.Component;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdesktop.application.session.PropertySupport;

class JTextFieldPropertySupport implements PropertySupport {
	public void setSessionState(Component c, Object state) {
		((JTextField) c).setText(state.toString());
	}

	public Object getSessionState(Component c) {
		if ((c instanceof JPasswordField)) {
			return "";
		}
		return ((JTextField) c).getText();
	}
}
