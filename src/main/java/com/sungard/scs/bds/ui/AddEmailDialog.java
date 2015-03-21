package com.sungard.scs.bds.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import com.sungard.scs.bds.contact.Contact;
import com.sungard.scs.bds.contact.ContactQueryTask;
import com.sungard.scs.bds.ui.BdsApplication.ConnectionType;

import static com.sungard.scs.bds.ui.Util.createImageButton;
import static com.sungard.scs.bds.ui.Util.createActionMapButton;

public class AddEmailDialog extends JDialog {
	private static final long serialVersionUID = 407781519975915681L;
	private static final Logger LOGGER = Logger.getLogger(AddEmailDialog.class.getName());
	private final ApplicationContext context;
	private final ActionMap actionMap;
	private List<Contact> searchEmail = new ArrayList<>();
	private JTextField txtSearchEmail;
	private JTextField txtEmail;
	private ListTableModel<Contact> model;
	
	public AddEmailDialog(ApplicationContext context, Frame owner) {
		super(owner, "Add E-Mail", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.context = context;
		this.actionMap = context.getActionMap(this);
		
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
		add(new JLabel("Search by E-Mail/Name:"), gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 1.0D;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		txtSearchEmail = new JTextField();
		
		txtSearchEmail.setName("txtSearchEmail");
		txtSearchEmail.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Action x = actionMap.get("searchEmail");
				e.setSource(txtSearchEmail);
				
				x.actionPerformed(e);
			}
		});
		add(txtSearchEmail, gbc);
		
		gbc.gridx = 2;
		gbc.weightx = 0.0D;
		gbc.fill = GridBagConstraints.NONE;
		JButton btnSearch = createImageButton("/icon/search32.png");
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Action x = actionMap.get("searchEmail");
				e.setSource(txtSearchEmail);
				
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
		model = new ListTableModel<Contact>(this.searchEmail);
		JTable contactList = new JTable(model);
		contactList.setName("searchEmail");
		contactList.setTableHeader(null);
		contactList.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1){
					JTable table =(JTable) e.getSource();
					Point p = e.getPoint();
					int row = table.rowAtPoint(p);
					
					Contact c = searchEmail.get(row);
					
					StringBuilder sb = new StringBuilder(txtEmail.getText());
					if (sb.length() > 0) {
						sb.append("; ");
					}
					sb.append(c.toString());
					txtEmail.setText(sb.toString());
				}
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(contactList);
		contactList.setFillsViewportHeight(true);
		add(scrollPane, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0D;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		txtEmail = new JTextField();
		txtEmail.setName("txtEmail");
		add(txtEmail, gbc);
		
		gbc.weighty = 0.0D;
		gbc.weightx = 0.0D;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		gbc.gridx = 1;
		gbc.gridy = 3;	
		gbc.anchor = GridBagConstraints.EAST;
		JButton btnUse = createActionMapButton(actionMap, "useSearchEmail", "/icon/userplus32.png");
		btnUse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(btnUse, gbc);

		gbc.gridx = 2;
		JButton btnCancel = createActionMapButton(actionMap, "cancelSearchEmail", "/icon/leftturnarrow32.png");
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				txtSearchEmail.setText("");
				txtEmail.setText("");
			}
		});
		addWindowListener(new WindowAdapter() {
	         @Override
	         public void windowClosing(WindowEvent e) {
	        	 txtSearchEmail.setText("");
	        	 txtEmail.setText("");
	         }
	      });
		
		add(btnCancel, gbc);
	}
	
	public String getEmail() {
		return txtEmail.getText();
	}
	

	@org.jdesktop.application.Action(block = Task.BlockingScope.APPLICATION)
	public Task<List<Contact>, Void> searchEmail(final ActionEvent e) {
		return ((BdsApplication)context.getApplication()).executeNetworkOperation(new NetworkOperation<Task<List<Contact>, Void>>() {
			@Override
			public Task<List<Contact>, Void> execute(String url, String username, String password) {
				String contactToFind = ((JTextField)e.getSource()).getText();
				if (contactToFind == null || contactToFind.length() == 0) {
					//TODO translation
					JOptionPane.showMessageDialog(getOwner(), "Please input a name", "Please input a name", JOptionPane.ERROR_MESSAGE);
					return null;
				} else {
					ContactQueryTask task = new ContactQueryTask(context.getApplication(), url, username, password, contactToFind);
					task.addTaskListener(new TaskListener.Adapter<List<Contact>, Void>() {

						@Override
						public void succeeded(TaskEvent<List<Contact>> event) {
							List<Contact> contacts = event.getValue();

							searchEmail.clear();
							searchEmail.addAll(contacts);
							Collections.sort(searchEmail);
							
							model.fireTableDataChanged();
						}

						@Override
						public void failed(TaskEvent<Throwable> event) {
							Throwable t = event.getValue();
							LOGGER.log(Level.WARNING, "Fetching contacts failed: " + t.getMessage(), t);
							JOptionPane.showMessageDialog(getOwner(), t.getMessage(), "Fetching contacts failed", JOptionPane.WARNING_MESSAGE);
						}
					});
					return task;
				}
			}

		}, ConnectionType.Exchange);

	}
	
}
