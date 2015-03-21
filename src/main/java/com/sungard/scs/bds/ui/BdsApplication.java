package com.sungard.scs.bds.ui;

import static com.sungard.scs.bds.ui.Util.createActionMapButton;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import com.sungard.scs.bds.FDSAPIWrapper.HashAlgorithm;

public class BdsApplication extends SingleFrameApplication {
	private static final Logger LOGGER = Logger.getLogger(BdsApplication.class.getName());

	private static final int DELETE_CHAR = 127;
	private List<File> files = new ArrayList<>();
	private List<String> tos = new ArrayList<>();
	private String username;
	private String subject;

	public static void main(String[] args) throws IOException {
		launch(BdsApplication.class, args);
	}

	protected void ready() {
		getComponentByName("password").requestFocus();

		JCheckBox useProxy = (JCheckBox) getComponentByName("useProxy");
		getComponentByName("proxySettings").setVisible(useProxy.isSelected());
	}

	protected void initialize(String[] args) {
		getContext().getSessionStorage().putProperty(JTextField.class, new JTextFieldPropertySupport());
		getContext().getSessionStorage().putProperty(JCheckBox.class, new JCheckBoxPropertySupport());

		this.username = System.getProperty("user.name");

		for (String arg : args) {
			File f = new File(arg);
			if ((f.exists()) && (f.canRead())) {
				this.files.add(f);
			}
		}

		if (this.files.size() > 0)
			this.subject = ((File) this.files.get(0)).getName();
		else
			this.subject = ("BDS-UI uploaded " + DateFormat.getDateTimeInstance().format(new Date()));
	}

	protected void shutdown() {
		try {
			getContext().getSessionStorage().save(getMainFrame(), "session.xml");
		} catch (IOException e) {
			String msg = getContext().getResourceMap().getString("sessionSaveError", new Object[] { e.getMessage() });
			JOptionPane.showMessageDialog(getMainFrame(), msg, "Shutdown Error", 2);
		}
	}

	protected void startup() {
		FrameView mainView = getMainView();
		mainView.setComponent(createComponent());
		try {
			getContext().getSessionStorage().restore(getMainFrame(), "session.xml");
		} catch (IOException e) {
			String msg = getContext().getResourceMap().getString("sessionRestoreError", new Object[] { e.getMessage() });
			JOptionPane.showMessageDialog(getMainFrame(), msg, "Startup Error", 2);
		}

		((JTextField) getComponentByName("subject")).setText(this.subject);

		JTextField txtUrl = (JTextField) getComponentByName("url");
		if ("".equals(txtUrl.getText())) {
			String url = getContext().getResourceMap().getString("url", new Object[0]);
			txtUrl.setText(url);
		}
		
		JTextField txtContactUrl = (JTextField) getComponentByName("contactUrl");
		if ("".equals(txtContactUrl.getText())) {
			String url = getContext().getResourceMap().getString("contactUrl", new Object[0]);
			txtContactUrl.setText(url);
		}

		JTextField txtProxyHost = (JTextField) getComponentByName("proxyHost");
		if ("".equals(txtProxyHost.getText())) {
			String proxyHost = getContext().getResourceMap().getString("proxyHost", new Object[0]);
			txtProxyHost.setText(proxyHost);
		}

		JTextField txtProxyPort = (JTextField) getComponentByName("proxyPort");
		if ("".equals(txtProxyPort.getText())) {
			String proxyPort = getContext().getResourceMap().getString("proxyPort", new Object[0]);
			txtProxyPort.setText(proxyPort);
		}

		show(mainView);
	}

	private JPanel createComponent() {
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = 2;
		gbc.weightx = 1.0D;
		gbc.gridx = 0;
		gbc.gridy = 0;

		JPanel connectionGroup = createConnectionGroup();
		connectionGroup.setBorder(BorderFactory.createTitledBorder("Connection Details"));
		main.add(connectionGroup, gbc);

		gbc.gridy = 1;
		gbc.fill = 1;
		gbc.weighty = 1.0D;
		JPanel deliveryGroup = createDeliveryGroup();
		main.add(deliveryGroup, gbc);

		gbc.gridy = 2;
		gbc.fill = 2;
		gbc.weighty = 0.0D;
		JPanel commandGroup = createCommandGroup();
		commandGroup.setBorder(BorderFactory.createTitledBorder("Commands"));
		main.add(commandGroup, gbc);

		return main;
	}

	private JPanel createToTable() {
		JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		//gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0D;
		gbc.weighty = 1.0D;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		
		
		JTable fileList = new JTable(new ListTableModel<String>(this.tos));
		fileList.setName("toTable");
		fileList.setTableHeader(null);
		//fileList.getColumn(fileList.getColumnName(1)).setCellRenderer(NumberRenderer.getIntegerRenderer());
		/*fileList.setTransferHandler(new DropTransferHandler(this.files, fileList) {
			private static final long serialVersionUID = -2216535566819906701L;

			protected void handleDirectoryImport(File d) {
				BdsApplication.this.addDirectoryToList(d);
			}
		});*/
		fileList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (DELETE_CHAR == e.getKeyCode()) {
					delEmail();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(fileList);
		fileList.setFillsViewportHeight(true);
		p.add(scrollPane, gbc);
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0D;
		gbc.weighty = 1.0D;
		gbc.gridx = 1;
		gbc.gridy = 0;
		JButton btnAddEmail = createActionMapButton(getContext(), "addEmail", "/icon/userplus32.png");
		p.add(btnAddEmail, gbc);

		gbc.gridy = 1;
		JButton btnDelEmail = createActionMapButton(getContext(), "delEmail", "/icon/userminus32.png");
		p.add(btnDelEmail, gbc);
		
		return p;
	}
	
	private JPanel createDeliveryGroup() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Delivery"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.0D;
		gbc.weighty = 0.0D;
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(new JLabel("To:"), gbc);

		gbc.weightx = 1.0D;
		gbc.weighty = 0.25D;
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.BOTH;
		p.add(createToTable(), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.0D;
		gbc.weighty = 0.0D;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		p.add(new JLabel("Subject:"), gbc);

		gbc.weightx = 1.0D;
		gbc.gridx = 1;
		JTextField txtSubject = new JTextField(this.subject);
		txtSubject.setName("subject");
		p.add(txtSubject, gbc);
		
		gbc.weightx = 0.0D;
		gbc.gridx = 2;
		JLabel hashAlgorithmLabel = new JLabel("Add Hashes for Files:");
		p.add(hashAlgorithmLabel, gbc);

		gbc.gridx = 3;
		JComboBox<HashAlgorithm> cbHashAlgorithm = new JComboBox<>(HashAlgorithm.values());
		//JCheckBox cbMd5sum = new JSelectB("Calculate MD5 for every file");
		cbHashAlgorithm.setName("hashAlgorithm");
		p.add(cbHashAlgorithm, gbc);
		
		gbc.weightx = 0.0D;
		gbc.gridx = 0;
		gbc.gridy = 2;
		p.add(new JLabel("<html>Secure<br>Message:</html>"), gbc);

		gbc.weightx = 1.0D;
		gbc.weighty = 0.5D;
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;

		JTextArea txtSecureMessage = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(txtSecureMessage); 
		txtSecureMessage.setName("secureMessage");
		txtSecureMessage.setFont(txtSubject.getFont());
		p.add(scrollPane, gbc);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0D;
		gbc.weighty = 1.0D;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 4;
		JPanel fileGroup = createFileGroup();
		fileGroup.setBorder(BorderFactory.createTitledBorder("Files"));
		p.add(fileGroup, gbc);

		return p;
	}

	private JPanel createCommandGroup() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints bgc = new GridBagConstraints();
		bgc.insets = new Insets(5, 5, 5, 5);
		
		bgc.weightx = 1.0D;
		bgc.weighty = 0.0D;
		bgc.gridx = 0;
		bgc.gridy = 1;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		p.add(createActionMapButton(getContext(), "quit", "/icon/block32.png"), bgc);

		bgc.gridx = 1;
		p.add(createActionMapButton(getContext(), "startUpload", "/icon/upload32.png"), bgc);

		bgc.gridx = 2;
		JButton btnOpenBds = createActionMapButton(getContext(), "openBds", "/icon/home32.png");
		btnOpenBds.setEnabled(isBrowserSupported());
		p.add(btnOpenBds, bgc);
		
		return p;
	}
	
	private boolean verifyListNotEmpty(List<?> list, String msgKey, Object[] arguments) {
		if ((list == null) || (list.isEmpty())) {
			String msg = getContext().getResourceMap().getString(msgKey, arguments);
			JOptionPane.showMessageDialog(getMainFrame(), msg, "Invalid Parameter", 0);
			return false;
		}
		return true;
	}

	private boolean verifyStringNotEmpty(String toCheck, String msgKey, Object[] arguments) {
		if (StringUtils.isEmpty(toCheck)) {
			String msg = getContext().getResourceMap().getString(msgKey, arguments);
			JOptionPane.showMessageDialog(getMainFrame(), msg, "Invalid Parameter", 0);
			return false;
		}
		return true;
	}

	@Action
	public void addEmail() {

		AddEmailDialog d = new AddEmailDialog(getContext(), getMainView().getFrame());
		d.setLocationRelativeTo(getMainView().getFrame());
		d.setVisible(true);
		String s = d.getEmail();
		
		if (s != null && s.length() > 0) {
			for(String to : s.split(";")) {
				tos.add(to.trim());
			}
			
			JTable toTable = getComponentByName("toTable");
			((AbstractTableModel) toTable.getModel()).fireTableDataChanged();
		}
	}
	
	protected void removeSelection(JTable toTable, List<?> model) {
		int[] selections = toTable.getSelectedRows();

		if (selections.length > 0) {
			String msg = getContext().getResourceMap().getString("removeEntryConfirmation", selections.length);
			int answer = JOptionPane.showConfirmDialog(getMainFrame(), msg, "Remove Entry", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	
			if (answer == 0) {
	
				List<Integer> selectionsList = new ArrayList<>();
				for (int i : selections) {
					selectionsList.add(Integer.valueOf(i));
				}
	
				Collections.reverse(selectionsList);
				for (Integer selection : selectionsList) {
					model.remove(selection.intValue());
				}
				((AbstractTableModel) toTable.getModel()).fireTableDataChanged();
			}
		}
		
	}
	
	@Action
	public void delEmail() {
		JTable toTable = getComponentByName("toTable");
		removeSelection(toTable, this.tos);
	}
	
	@Action
	public void addFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(0);
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(getMainFrame());
		if (returnVal == 0) {
			File[] selectedFiles = chooser.getSelectedFiles();
			for (File f : selectedFiles) {
				if (f.isFile()) {
					this.files.add(f);
				}
			}

			JTable fileTable = getComponentByName("fileTable");
			((AbstractTableModel) fileTable.getModel()).fireTableDataChanged();
		}
	}

	@Action
	public void addDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(1);
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser.showOpenDialog(getMainFrame());
		if (returnVal == 0) {
			File selectedDirectory = chooser.getSelectedFile();
			addDirectoryToList(selectedDirectory);

			JTable fileTable = getComponentByName("fileTable");
			((AbstractTableModel) fileTable.getModel()).fireTableDataChanged();
		}
	}

	private void addDirectoryToList(File selectedDirectory) {
		if (!selectedDirectory.isDirectory()) {
			return;
		}
		File[] subdirectories = selectedDirectory.listFiles(new DirectoryFileFilter());
		if ((subdirectories.length > 0)
				&& (0 == JOptionPane.showConfirmDialog(getMainFrame(), "Sub directories detected in " + selectedDirectory
						+ ".\n Include them?", "Include subdirectories", 0, 3))) {
			for (File subdirectory : subdirectories) {
				addDirectoryContentToFileList(subdirectory);
			}
		}

		this.files.addAll(Arrays.asList(selectedDirectory.listFiles(new FileFileFilter())));
	}

	private void addDirectoryContentToFileList(File directory) {
		File[] allFiles = directory.listFiles(new FileFileFilter());

		if (allFiles == null) {
			return;
		}
		this.files.addAll(Arrays.asList(allFiles));
		File[] subdirectories = directory.listFiles(new DirectoryFileFilter());
		for (File subdirectory : subdirectories)
			addDirectoryContentToFileList(subdirectory);
	}

	@Action
	public void removeFileEntry() {
		JTable fileTable = getComponentByName("fileTable");
		removeSelection(fileTable, this.files);
	}

	private boolean isBrowserSupported() {
		if (Desktop.isDesktopSupported()) {
			Desktop d = Desktop.getDesktop();
			if (d.isSupported(Desktop.Action.BROWSE)) {
				return true;
			}
		}
		return false;
	}
	
	@Action
	public void openBds() {
		if (isBrowserSupported()) {
			Pattern p = Pattern.compile("(.*)/axis2/services(.*)$");
			String url = ((JTextField) getComponentByName("url")).getText();
			Matcher m = p.matcher(url);
			if (m.matches() && m.groupCount() > 1) {
				String loginUrl = m.group(1);
				try {
					Desktop.getDesktop().browse(new URI(loginUrl));
				} catch (IOException | URISyntaxException e) {
					LOGGER.log(Level.WARNING, "Could not browser to " + loginUrl, e);
				}
			}
		}
	}
	
	public enum ConnectionType {
		Exchange,
		BDS
	}
	
	public <T> T executeNetworkOperation(NetworkOperation<T> op, ConnectionType type) {
		String url;
		if (type == ConnectionType.BDS) {
			url = ((JTextField) getComponentByName("url")).getText();
		} else {
			url = ((JTextField) getComponentByName("contactUrl")).getText();
		}
		boolean valid = true;
		valid &= verifyStringNotEmpty(url, "urlMissing", new Object[0]);
		String username = ((JTextField) getComponentByName("username")).getText();
		valid &= verifyStringNotEmpty(username, "usernameMissing", new Object[0]);
		String password = ((JTextField) getComponentByName("password")).getText();
		valid &= verifyStringNotEmpty(password, "passwordMissing", new Object[0]);
		if (valid) {
			return op.execute(url, username, password);
		} else {
			return null;
		}
	}
	

	@Action(block = Task.BlockingScope.APPLICATION)
	public Task<List<File>, File> startUpload() {
		return executeNetworkOperation(new NetworkOperation<Task<List<File>, File>>() {
			@Override
			public Task<List<File>, File> execute(String url, String username, String password) {
				boolean valid = true;
				String subject = ((JTextField) getComponentByName("subject")).getText();
				valid &= verifyStringNotEmpty(subject, "subjectMissing", new Object[0]);

				String message = ((JTextArea) getComponentByName("secureMessage")).getText();
				
				valid &= verifyListNotEmpty(files, "filesMissing", new Object[0]);

				JComboBox<HashAlgorithm> hash1 = getComponentByName("hashAlgorithm");
				HashAlgorithm hash = (HashAlgorithm) hash1.getSelectedItem();

				if (valid)
					return new UploadAction(getInstance(), url, username, password, 
							tos, subject, files, message, hash,
						new UploadAction.ProxySettings() {
							public boolean isActive() {
								JCheckBox useProxy = BdsApplication.this.getComponentByName("useProxy");
								return useProxy.isSelected();
							}

							public String getPort() {
								JTextField host = BdsApplication.this.getComponentByName("proxyPort");
								return host.getText();
							}

							public String getHost() {
								JTextField host = BdsApplication.this.getComponentByName("proxyHost");
								return host.getText();
							}
						});
				return null;
			}
		}, ConnectionType.BDS);
	}

	private <T extends Component> T getComponentByName(String name) {
		return getComponentByName(getMainFrame().getRootPane(), name);
	}

	@SuppressWarnings("unchecked")
	private <T extends Component> T getComponentByName(Container root, String name) {
		for (Component c : root.getComponents()) {
			if (name.equals(c.getName())) {
				return (T)c;
			}
			if ((c instanceof Container)) {
				Component result = getComponentByName((Container) c, name);
				if (result != null) {
					return (T)result;
				}
			}
		}
		return null;
	}

	private JPanel createFileGroup() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints bgc = new GridBagConstraints();
		bgc.insets = new Insets(5, 5, 5, 5);
		bgc.weightx = 1.0D;
		bgc.weighty = 1.0D;
		bgc.gridx = 0;
		bgc.gridy = 0;
		bgc.gridwidth = 4;
		bgc.fill = GridBagConstraints.BOTH;

		JTable fileList = new JTable(new FileListTableModel(this.files));
		fileList.setName("fileTable");
		fileList.getColumn(fileList.getColumnName(1)).setCellRenderer(NumberRenderer.getIntegerRenderer());
		fileList.setTransferHandler(new DropTransferHandler(this.files, fileList) {
			private static final long serialVersionUID = -2216535566819906701L;

			protected void handleDirectoryImport(File d) {
				BdsApplication.this.addDirectoryToList(d);
			}
		});
		fileList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (DELETE_CHAR == e.getKeyCode()) {
					removeFileEntry();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(fileList);
		fileList.setFillsViewportHeight(true);
		p.add(scrollPane, bgc);

		bgc.gridwidth = 1;
		bgc.weighty = 0.0D;
		bgc.weightx = 0.0D;
		bgc.gridy = 1;
		
		bgc.gridx = 1;
		bgc.weightx = 1.0D;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		p.add(createActionMapButton(getContext(), "addFile","/icon/paperplus32.png"), bgc);

		bgc.gridx = 2;
		p.add(createActionMapButton(getContext(), "addDirectory", "/icon/folderplus32.png"), bgc);

		bgc.gridx = 3;
		p.add(createActionMapButton(getContext(), "removeFileEntry", "/icon/minus32.png"), bgc);

		return p;
	}

	private JPanel createConnectionGroup() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints bgc = new GridBagConstraints();
		bgc.insets = new Insets(5, 5, 5, 5);
		bgc.weightx = 0.0D;
		bgc.weighty = 0.0D;
		bgc.gridx = 0;
		bgc.gridy = 0;

		JLabel lblUrl = new JLabel("URL:");
		p.add(lblUrl, bgc);
		JPanel urlPanel = new JPanel();
		urlPanel.setLayout(new GridBagLayout());

		bgc.gridx = 1;
		bgc.gridy = 0;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		bgc.weightx = 1.0D;
		bgc.gridwidth = 2;
		p.add(urlPanel, bgc);
		
		GridBagConstraints innerBgc = new GridBagConstraints();
		innerBgc.fill = GridBagConstraints.HORIZONTAL;
		innerBgc.weightx = 1.0D;
		JTextField txtUrl = new JTextField();
		txtUrl.setName("url");
		txtUrl.setToolTipText("BDS service like 'https://fileshare.domain.tld/axis2/services/'");
		urlPanel.add(txtUrl, innerBgc);
		
		JTextField txtContactUrl = new JTextField();
		txtContactUrl.setName("contactUrl");
		txtContactUrl.setToolTipText("Exchange OWA like 'https://owa.domain.tld/EWS/Exchange.asmx'");
		urlPanel.add(txtContactUrl, innerBgc);
		
		bgc.gridx = 3;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		bgc.weightx = 1.0D;
		bgc.gridwidth = 1;
		final JCheckBox cbUseProxy = new JCheckBox("Use Proxy");
		cbUseProxy.setName("useProxy");
		cbUseProxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Container c = BdsApplication.this.getComponentByName("proxySettings");
				c.setVisible(cbUseProxy.isSelected());
			}
		});
		p.add(cbUseProxy, bgc);

		bgc.gridx = 0;
		bgc.gridy = 1;
		bgc.fill = GridBagConstraints.NONE;
		bgc.weightx = 0.0D;
		bgc.gridwidth = 1;
		p.add(new JLabel("Username:"), bgc);

		bgc.gridx = 1;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		bgc.weightx = 1.0D;
		JTextField txtUsername = new JTextField(this.username);
		txtUsername.setName("username");
		p.add(txtUsername, bgc);

		bgc.gridx = 2;
		bgc.fill = GridBagConstraints.NONE;
		bgc.weightx = 0.0D;
		p.add(new JLabel("Password:"), bgc);

		bgc.gridx = 3;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		bgc.weightx = 1.0D;
		JPasswordField txtPassword = new JPasswordField();
		txtPassword.setName("password");
		p.add(txtPassword, bgc);

		bgc.fill = GridBagConstraints.HORIZONTAL;
		bgc.weightx = 1.0D;
		bgc.gridwidth = 4;
		bgc.gridx = 0;
		bgc.gridy = 2;
		p.add(getProxyPanel(), bgc);

		return p;
	}

	private JPanel getProxyPanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Proxy Details"));
		p.setName("proxySettings");
		p.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(new JLabel("Host:"), gbc);

		gbc.gridx = 2;
		p.add(new JLabel("Port:"), gbc);

		gbc.fill = 2;
		gbc.weightx = 0.75D;
		gbc.gridx = 1;
		JTextField proxyHost = new JTextField();
		proxyHost.setName("proxyHost");
		p.add(proxyHost, gbc);

		gbc.gridx = 3;
		gbc.weightx = 0.25D;
		JTextField proxyPort = new JTextField();
		proxyPort.setName("proxyPort");
		p.add(proxyPort, gbc);

		return p;
	}

	private final class DirectoryFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	private final class FileFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
	}
	

	@Action
	public void useSearchEmail() {
	}
	
	@Action
	public void cancelSearchEmail() {
	}

}
