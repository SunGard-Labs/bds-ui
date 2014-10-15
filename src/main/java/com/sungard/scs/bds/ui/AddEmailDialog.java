package com.sungard.scs.bds.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.jdesktop.application.ApplicationContext;

import static com.sungard.scs.bds.ui.Util.createImageButton;
import static com.sungard.scs.bds.ui.Util.createActionMapButton;

public class AddEmailDialog extends JDialog {
	private static final long serialVersionUID = 407781519975915681L;
	private final ApplicationContext context;
	private List<String> searchEmail = new ArrayList<>();
	private JTextField txtEmail;
	private String selectedEmail;
	
	public AddEmailDialog(ApplicationContext context, Frame owner) {
		super(owner, "Add E-Mail", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.context = context;
		
		initComponent();
		pack();
	}

	private void initComponent() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weighty = 0.0D;
		gbc.weightx = 0.0D;
		gbc.fill = GridBagConstraints.NONE;
		add(new JLabel("E-Mail/Name:"), gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 1.0D;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		txtEmail = new JTextField();
		
		txtEmail.setName("txtEmail");
		add(txtEmail, gbc);
		
		gbc.gridx = 2;
		gbc.weightx = 0.0D;
		gbc.fill = GridBagConstraints.NONE;
		JButton btnSearch = createImageButton("/icon/search32.png");
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Action x = context.getActionMap().get("searchEmail");
				e.setSource(txtEmail);
				x.actionPerformed(e);
			}
		});
		add(btnSearch, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.weighty = 1.0D;
		gbc.weightx = 1.0D;
		gbc.fill = GridBagConstraints.BOTH;
		JTable fileList = new JTable(new ListTableModel<String>(this.searchEmail));
		fileList.setName("searchEmail");
		fileList.setTableHeader(null);
		
		JScrollPane scrollPane = new JScrollPane(fileList);
		fileList.setFillsViewportHeight(true);
		add(scrollPane, gbc);

		gbc.weighty = 0.0D;
		gbc.weightx = 0.0D;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		gbc.gridx = 1;
		gbc.gridy = 2;	
		gbc.anchor = GridBagConstraints.EAST;
		JButton btnUse = createActionMapButton(context, "useSearchEmail", "/icon/userplus32.png");
		btnUse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedEmail = txtEmail.getText();
				setVisible(false);
			}
		});
		add(btnUse, gbc);

		gbc.gridx = 2;
		JButton btnCancel = createActionMapButton(context, "cancelSearchEmail", "/icon/leftturnarrow32.png");
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				txtEmail.setText("");
				selectedEmail = null;
			}
		});
		addWindowListener(new WindowAdapter() {
	         @Override
	         public void windowClosing(WindowEvent e) {
	        	 txtEmail.setText("");
	        	 selectedEmail = null;
	         }
	      });
		
		add(btnCancel, gbc);
	}
	
	public String getEmail() {
		return selectedEmail;
	}
	
}
