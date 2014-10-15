package com.sungard.scs.bds.ui;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

import com.sungard.scs.bds.FDSAPIWrapper;
import com.sungard.scs.bds.FDSAPIWrapper.HashAlgorithm;

public class UploadAction extends Task<List<File>, File> {
	private static final Logger LOGGER = Logger.getLogger(UploadAction.class.getName());
	private final List<File> files;
	private final String url;
	private final List<String> emailAddress;
	private final String username;
	private final String message;
	private final String password;
	private final String subject;
	private final HashAlgorithm hash;
	private final ProxySettings proxySettings;
	private String oldProxyHost;
	private String oldProxyPort;

	public UploadAction(Application application, String url, String username, String password, 
			List<String> tos, String subject, List<File> files, String message,
			HashAlgorithm hash, ProxySettings settings) {
		super(application);
		this.files = Collections.unmodifiableList(files);
		this.url = url;
		this.username = username;
		this.password = password;
		this.emailAddress = Collections.unmodifiableList(tos);
		this.message = message;
		this.subject = subject;
		this.hash = hash;
		this.proxySettings = settings;
	}

	private void enableProxy() {
		LOGGER.fine("Enable proxy");
		if (this.proxySettings.isActive()) {
			String prefix;
			if (this.url.startsWith("https"))
				prefix = "https";
			else {
				prefix = "http";
			}
			this.oldProxyHost = System.getProperty(prefix + ".proxyHost");
			this.oldProxyPort = System.getProperty(prefix + ".proxyPort");
			System.setProperty(prefix + ".proxyHost", this.proxySettings.getHost());
			System.setProperty(prefix + ".proxyPort", this.proxySettings.getPort());
		}
	}

	private void disableProxy() {
		if (this.proxySettings.isActive()) {
			if (this.oldProxyHost == null)
				System.getProperties().remove("http_proxyHost");
			else {
				System.setProperty("http_proxyHost", this.oldProxyHost);
			}
			if (this.oldProxyPort == null)
				System.getProperties().remove("http_proxyPort");
			else
				System.setProperty("http_proxyPort", this.oldProxyPort);
		}
	}

	protected List<File> doInBackground() throws Exception {
		enableProxy();
		message("initalize", new Object[] { this.url });
		FDSAPIWrapper wrapper = new FDSAPIWrapper(this.url);
		wrapper.setUploadStateListener(new FDSAPIWrapper.UploadStateListener() {
			public void login(String username) {
				UploadAction.this.message("login", new Object[] { username });
			}

			public void logout() {
				UploadAction.this.message("logout", new Object[0]);
			}

			public void uploadedBytes(File uploadingFile, long alreadySentBytes) {
				UploadAction.this.message("uploading", new Object[] { uploadingFile.getName() });
				UploadAction.this.setProgress((float) alreadySentBytes, 0.0F, (float) uploadingFile.length());
			}

			public boolean isCancelled() {
				return UploadAction.this.isCancelled();
			}
		});
		if (isCancelled()) {
			return null;
		}
		FDSAPIWrapper.LoginInfo loginInfo = wrapper.login(this.username, this.password);
		if (isCancelled()) {
			return null;
		}
		if (loginInfo == null) {
			throw new IllegalStateException("Login failed!");
		}
		wrapper.createPackage(loginInfo.sessionId, this.emailAddress, this.subject, this.message, this.files, this.hash);
		if (isCancelled()) {
			return null;
		}
		wrapper.logout(loginInfo.sessionId);

		disableProxy();
		return this.files;
	}

	protected void failed(Throwable cause) {
		cause.printStackTrace();
		String msg = getApplication().getContext().getResourceMap().getString("uploadFailed", new Object[] { cause.getMessage() });
		JOptionPane.showMessageDialog(null, msg, "Upload failed",  JOptionPane.ERROR_MESSAGE);
	}

	protected void succeeded(List<File> result) {
		String msg = getApplication().getContext().getResourceMap().getString("uploadSucceeded", new Object[] { result });
		// LOGGER.info("Upload succeeded");
		JOptionPane.showMessageDialog(null, msg, "Upload succeeded", JOptionPane.INFORMATION_MESSAGE);
	}

	public static abstract interface ProxySettings {
		public abstract boolean isActive();

		public abstract String getHost();

		public abstract String getPort();
	}
}
