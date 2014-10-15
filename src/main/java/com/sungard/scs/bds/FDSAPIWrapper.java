package com.sungard.scs.bds;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import com.biscom.fds.api.fdsapi.FDSAPIBINPortType;
import com.biscom.fds.api.fdsapi.FDSAPIPortType;
import com.biscom.fds.api.type.xsd.XSDataFileVO;
import com.biscom.fds.api.type.xsd.XSDeliveryVO;
import com.biscom.fds.api.type.xsd.XSEmailList;
import com.biscom.fds.api.type.xsd.XSEmails;
import com.biscom.fds.api.type.xsd.XSInactiveUsers;
import com.biscom.fds.api.type.xsd.XSInvalidEmails;
import com.biscom.fds.api.type.xsd.XSNewDocumentVO;
import com.biscom.fds.api.type.xsd.XSNewDocumentVOList;
import com.biscom.fds.api.type.xsd.XSNonRecipients;
import com.biscom.fds.api.type.xsd.XSNonUserEmails;
import com.biscom.fds.api.type.xsd.XSNotifyOnEventList;
import com.biscom.fds.api.type.xsd.XSRecipientEmails;
import com.biscom.fds.api.type.xsd.XSRejectedEmails;
import com.biscom.fds.api.type.xsd.XSTextMessage;
import com.biscom.fds.api.type.xsd.XSUserVO;

public class FDSAPIWrapper {
	public enum HashAlgorithm {
		None(null), MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256");
	
		protected final String digest;
		
		HashAlgorithm(String digest) {
			this.digest = digest;
		}
		
		protected MessageDigest getDigest() throws NoSuchAlgorithmException {
			if (digest != null) {
				return MessageDigest.getInstance(digest);
			} else {
				return new MessageDigest(digest){

					@Override
					protected void engineUpdate(byte input) {
						//NOOP
					}

					@Override
					protected void engineUpdate(byte[] input, int offset, int len) {
						//NOOP
					}

					@Override
					protected byte[] engineDigest() {
						return new byte[0];
					}

					@Override
					protected void engineReset() {
						//NOOP
					}};
			}
		}
	}
	
	private final Logger LOGGER = Logger.getLogger(FDSAPIWrapper.class.getName());
	private final FDSAPIBINPortType binaryService;
	private final FDSAPIPortType service;
	private UploadStateListener uploadStateListener = new UploadStateListener() {
		public void login(String username) {
		}

		public void logout() {
		}

		public void uploadedBytes(File uploadingFile, long alreadySentBytes) {
		}

		public boolean isCancelled() {
			return false;
		}
	};

	public FDSAPIWrapper(String endpoint) throws Exception {
		if (!endpoint.endsWith("/")) {
			throw new IllegalArgumentException("endpoint must have a trailing slash!");
		}

		this.service = initalizeAPIService(endpoint + "FDSAPI");
		this.binaryService = initalizeAPIBinaryService(endpoint + "FDSAPIBIN");
	}

	public LoginInfo login(String username, String password) throws RemoteException {
		Holder<String> retValue = new Holder<>();
		Holder<XSUserVO> userInfo = new Holder<>();
		this.service.signIn(username, "PASSWORD", null, "INTERNAL", null, password, null, null, null, retValue, userInfo);

		String sessionId = retValue.value;
		if (sessionId == null) {
			LOGGER.warning("Login failed as " + username);
			return null;
		}
		String emailAddress = (userInfo.value).getEmailAddress();
		this.LOGGER.info("Login as " + username + " succeeds: " + sessionId + " as " + emailAddress);
		this.uploadStateListener.login(username);
		LoginInfo ui = new LoginInfo(sessionId, emailAddress);
		return ui;
	}

	public void logout(String sessionId) throws RemoteException {
		this.service.signOut(sessionId);
		this.uploadStateListener.logout();
		this.LOGGER.info("Logout session " + sessionId);
	}

	private Service loadWsdl(String localResource, String remoteResource, QName qname) throws IOException {
		File tmpFile = File.createTempFile("FDSAPI", ".wsdl");
		Service service;
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream(localResource);
			OutputStream os = new FileOutputStream(tmpFile);
			int c;
			while ((c = is.read()) != -1) {
				os.write((char) c);
			}
			os.close();

			service = Service.create(tmpFile.toURI().toURL(), qname);
		} catch (WebServiceException wse) {
			this.LOGGER.info("Failed to load local WSDL: " + wse.getMessage() + " - Using HTTP");
			service = Service.create(new URL(remoteResource), qname);
		} finally {
			tmpFile.delete();
		}
		return service;
	}

	protected FDSAPIBINPortType initalizeAPIBinaryService(String endpoint) throws IOException {
		Service apiBinaryService = loadWsdl("wsdl/FDSAPIBIN.wsdl", endpoint + "/FDSAPIBIN?wsdl",
				new QName("http://FDSAPI.api.fds.biscom.com", "FDSAPIBIN"));

		FDSAPIBINPortType apiBinaryServicePortType = apiBinaryService.getPort(FDSAPIBINPortType.class);
		updateEndpoint((BindingProvider) apiBinaryServicePortType, endpoint);

		this.LOGGER.info("Initalize APIBinaryService with endpoint " + endpoint);

		return apiBinaryServicePortType;
	}

	protected FDSAPIPortType initalizeAPIService(String endpoint) throws IOException {
		Service apiService = loadWsdl("wsdl/FDSAPI.wsdl", endpoint + "/FDSAPI?wsdl", new QName(
				"http://FDSAPI.api.fds.biscom.com", "FDSAPI"));

		FDSAPIPortType apiServicePortType = apiService.getPort(FDSAPIPortType.class);
		updateEndpoint((BindingProvider) apiServicePortType, endpoint);

		this.LOGGER.info("Initalize APIService with endpoint " + endpoint);

		return apiServicePortType;
	}

	private void updateEndpoint(BindingProvider apiServiceBindingProvider, String endpoint) {
		Map<String, Object> context = apiServiceBindingProvider.getRequestContext();

		context.put("javax.xml.ws.service.endpoint.address", endpoint);

		if (Boolean.getBoolean("debug")) {
			@SuppressWarnings("rawtypes")
			List<Handler> chain = apiServiceBindingProvider.getBinding().getHandlerChain();
			chain.add(new SOAPLoggingHandler());
			apiServiceBindingProvider.getBinding().setHandlerChain(chain);
		}
	}

	public XSDataFileVO uploadFile(String sessionId, File f) throws IOException {
		Holder<Byte[]> unused = new Holder<>();
		return uploadFile(sessionId, f, HashAlgorithm.None, unused);
	}
	
	public XSDataFileVO uploadFile(String sessionId, File f, HashAlgorithm hash, Holder<Byte[]> hashHolder) throws IOException {
		long start = -System.nanoTime();
		Holder<Integer> returnCode = new Holder<>();
		Holder<Integer> returnDataFileId = new Holder<>();
		Holder<Integer> returnChunkSize = new Holder<>();

		long fileSize = f.length();
		this.LOGGER.info("Initalize file upload for file " + f.getAbsolutePath() + " (" + fileSize + "b)");
		this.service.initiateDataFileUpload(Long.valueOf(fileSize), sessionId, returnCode, returnDataFileId, returnChunkSize);
		Integer chunkSize = returnChunkSize.value;
		Integer dataFileId = returnDataFileId.value;

		if (returnCode.value.intValue() != 0) {
			throw new IllegalStateException("File upload initalization failed with return code " + returnCode.value + " of file "
					+ f.getAbsolutePath());
		}

		this.LOGGER.fine("Initalized file upload " + dataFileId + " for " + f.getAbsolutePath() + " with chunk size of " + chunkSize);

		byte[] b = new byte[chunkSize.intValue()];
		InputStream bis = null;
		try {
			InputStream fis = new FileInputStream(f);
			MessageDigest md = hash.getDigest();
			fis = new DigestInputStream(fis, md);

			bis = new BufferedInputStream(fis);

			long readOffset = 0L;
			int numberOfReadBytes;
			while ((numberOfReadBytes = bis.read(b)) != -1) {
				this.LOGGER.fine("Transmitting for file " + dataFileId + " " + numberOfReadBytes + " bytes");
				byte[] shrinkedArray = Arrays.copyOf(b, numberOfReadBytes);

				Integer returnValue = this.binaryService.updateDataFileUpload(dataFileId, Long.valueOf(readOffset),
						Integer.valueOf(numberOfReadBytes), shrinkedArray, sessionId);

				if (this.uploadStateListener.isCancelled()) {
					return null;
				}
				if (returnValue.intValue() != 0) {
					throw new IllegalStateException("Upload failed with return value: " + returnValue);
				}
				this.uploadStateListener.uploadedBytes(f, readOffset);
				readOffset += numberOfReadBytes;
			}

			this.LOGGER.info("Complete file upload for file " + dataFileId + ": " + f.getAbsolutePath());
			Holder<XSDataFileVO> dataFileVO = new Holder<>();
			this.service.completeDataFileUpload(dataFileId, sessionId, returnCode, dataFileVO);
			this.LOGGER.info(fileSize + " bytes uploaded in " + (start + System.nanoTime()) / 1000000000L + " seconds");

			if ((hash != HashAlgorithm.None) && (!this.uploadStateListener.isCancelled())) {
				byte[] digest = md.digest();
				hashHolder.value = new Byte[digest.length];
				for (int i = 0; i < digest.length; i++) {
					hashHolder.value[i] = Byte.valueOf(digest[i]);
				}
			}
			return dataFileVO.value;
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e.getMessage(), e);
		} finally {
			if (bis != null)
				bis.close();
		}
	}

	public void createPackage(String sessionId, List<String> toEmailAddress, String subject, String message, 
			List<File> filesToUpload, HashAlgorithm hash)
			throws IOException, DatatypeConfigurationException {
		Holder<Integer> returnCode = new Holder<>();

		XSRecipientEmails recipientEmails = new XSRecipientEmails();
		XSEmailList toList = new XSEmailList();
		for(String email : toEmailAddress) {
			XSEmails toAddress = new XSEmails();
			toAddress.setEmails(email);
			toList.getEmailEntryList().add(toAddress);
		}
		recipientEmails.setTo(toList);
		XSEmailList notificationEmails = null;
		XSTextMessage secureMessage = new XSTextMessage();
		secureMessage.setMessage(message);
		XSTextMessage notificationMessage = new XSTextMessage();
		notificationMessage.setMessage("");

		XMLGregorianCalendar dateAvailable = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MONTH, 2);
		XMLGregorianCalendar dateExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

		Boolean notifyRecipients = Boolean.valueOf(true);
		Boolean requireSignIn = Boolean.valueOf(true);
		Boolean notifyWhenRecipientDeletes = Boolean.valueOf(false);
		String encodedPassword = "";

		XSNotifyOnEventList notifyOnEvents = null;
		String notifyOnAccessSetting = "";

		XSNewDocumentVOList newDocumentVOs = new XSNewDocumentVOList();
		for (File fileToUpload : filesToUpload) {
			Holder<Byte[]> md5sumholder = new Holder<>();
			XSDataFileVO uploadedFile = uploadFile(sessionId, fileToUpload, hash, md5sumholder);
			if (this.uploadStateListener.isCancelled()) {
				return;
			}
			this.LOGGER.info("File " + fileToUpload + " craeted a " + hash.digest + " of " + md5sumholder.value);

			XSNewDocumentVO mainDoc = new XSNewDocumentVO();
			mainDoc.setName(fileToUpload.getName());
			mainDoc.setDescription("");

			mainDoc.setDirectoryId(Integer.valueOf(1));
			mainDoc.setDataFileId(uploadedFile.getDataFileId());

			newDocumentVOs.getNewDocumentVO().add(mainDoc);

			if (hash != HashAlgorithm.None) {
				StringBuilder sb = new StringBuilder();
				Byte[] md5sumResult = md5sumholder.value;
				for (int i = 0; i < md5sumResult.length; i++) {
					byte x = md5sumResult[i].byteValue();

					sb.append(Integer.toHexString(x & 0xFF | 0x100).toLowerCase().substring(1, 3));
				}

				File tmpFile = File.createTempFile(fileToUpload.getName(), "." + hash.digest.toLowerCase());
				FileWriter fw = new FileWriter(tmpFile);
				try {
					fw.write(sb.toString());
					fw.write("  ");
					fw.write(fileToUpload.getName());
					fw.close();
					this.LOGGER.fine("Created tmp file for hash at " + tmpFile);
					XSDataFileVO hashFile = uploadFile(sessionId, tmpFile);
					if (this.uploadStateListener.isCancelled()) {
						return;
					}
					XSNewDocumentVO hashDoc = new XSNewDocumentVO();
					hashDoc.setName(fileToUpload.getName() + "." + hash.digest.toLowerCase());
					hashDoc.setDescription(hash.digest + " hash of the file");

					hashDoc.setDirectoryId(Integer.valueOf(1));
					hashDoc.setDataFileId(hashFile.getDataFileId());

					newDocumentVOs.getNewDocumentVO().add(hashDoc);
				} finally {
					if (tmpFile.delete())
						this.LOGGER.fine("Deleted temp file " + tmpFile.getAbsolutePath());
					else {
						this.LOGGER.warning("Couldn't delete temp file " + tmpFile.getAbsolutePath());
					}
				}
			}
		}

		Holder<XSDeliveryVO> deliveryVO = new Holder<>();
		Holder<XSNonUserEmails> nonUserEmails = new Holder<>();
		Holder<XSInactiveUsers> inactiveUsers = new Holder<>();
		Holder<XSNonRecipients> nonRecipients = new Holder<>();
		Holder<XSInvalidEmails> invalidEmails = new Holder<>();
		Holder<XSRejectedEmails> rejectedEmails = new Holder<>();

		this.LOGGER.info("Create delivery " + subject + " with files " + filesToUpload);
		
		//UUID is to complex as ID and will destroy the notification email
		//System.nanoTimestamp behaves also strange sometimes - trying blank string...
		this.service.addExpressDelivery("", recipientEmails, subject, secureMessage, notificationMessage,
				dateAvailable, dateExpires, notifyRecipients, requireSignIn, notifyWhenRecipientDeletes, encodedPassword,
				notifyOnEvents, notifyOnAccessSetting, notificationEmails, newDocumentVOs, sessionId, returnCode, deliveryVO,
				nonUserEmails, inactiveUsers, nonRecipients, invalidEmails, rejectedEmails);
	
		if (returnCode.value.intValue() != 0)
			throw new IllegalStateException("Creating an express delivery failed with return code " + returnCode.value);
	}

	public void setUploadStateListener(UploadStateListener uploadStateListener) {
		this.uploadStateListener = uploadStateListener;
	}

	public static abstract interface UploadStateListener {
		public abstract void uploadedBytes(File paramFile, long paramLong);

		public abstract void login(String paramString);

		public abstract void logout();

		public abstract boolean isCancelled();
	}

	public static class LoginInfo {
		public final String sessionId;
		public final String emailAddress;

		public LoginInfo(String sessionId, String emailAddress) {
			this.sessionId = sessionId;
			this.emailAddress = emailAddress;
		}
	}
}
