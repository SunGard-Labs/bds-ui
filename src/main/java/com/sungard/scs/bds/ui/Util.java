package com.sungard.scs.bds.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jdesktop.application.ApplicationContext;

public final class Util {
	private Util() {}

	public static JButton createActionMapButton(ActionMap actionMap, String actionName, String iconResource) {
		JButton button = new JButton(actionMap.get(actionName));
		try {
			BufferedImage resource = ImageIO.read(Util.class.getResource(iconResource));
			button.setIcon(new ImageIcon(resource));
		} catch (IOException e) {
		}
		return button;
	}
	
	public static JButton createActionMapButton(ApplicationContext context, String actionName, String iconResource) {
		return createActionMapButton(context.getActionMap(), actionName, iconResource);
	}
	
	public static JButton createImageButton(String iconResource) {
		JButton button = new JButton();
		try {
			BufferedImage resource = ImageIO.read(Util.class.getResource(iconResource));
			button.setIcon(new ImageIcon(resource));
		} catch (IOException e) {
		}
		return button;
	}
}
