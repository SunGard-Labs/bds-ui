bds-ui
======
This is a "fat client" for the binary distribution system provided by Biscom: http://www.biscom.com/secure-file-transfer/sft/

It uses the SOAP webservices usually deployed with every installation.
![Screenshot](https://raw.githubusercontent.com/SunGard-Labs/bds-ui/master/doc/screenshot.png)

### Reasons and Features
Biscom's binary distribution system provides a secure way to upload & download files. Usually access is provided via browser applets for the major browser brands.
Access to applets might be prevented by customer policies and sometimes the applet just get stuck by a new policy update, browser update etc.

Also the applet lacks some usuability features which come in handy on a day2day usage:

* Easy creation of express deliveries
* Uploading all files and subfolders of a given folder 
* Calculation of hash for every uploaded file during upload
	* [SHA-256](http://en.wikipedia.org/wiki/Sha-256)
	* [SHA-1](http://en.wikipedia.org/wiki/Sha-1)
	* [MD5](http://en.wikipedia.org/wiki/MD5)
* Drag&Drop support for files and folders
* No installation required
	* [Java Runtime Environment](http://java.com/) required
	* Also to be used as [jPortable](http://portableapps.com/apps/utilities/java_portable_launcher)
* Including HTTP Proxy support
* Integration of command line arguments
	* Every argument is treated as file path

The upload rates are a little bit slower - in the end it is a [SOAP](http://en.wikipedia.org/wiki/SOAP)-[base64](http://en.wikipedia.org/wiki/Base64) operation which is by-design not the most efficent way to transmit binary data :/

## How to run
Download from https://github.com/SunGard-Labs/bds-ui/releases 

Execute the `bds-ui-<version>.one-jar.jar` as JAR.

* Windows
	* Double click while having a Java Runtime [installed](http://www.java.com/en/download/help/index_installing.xml)

	* Run via Portal Apps/jPortable: http://portableapps.com/apps/utilities/java_portable_launcher

	* Add a link to `java -jar bds-ui-<version>.one-jar.jar` in `%APPDATA%\Microsoft\Windows\SendTo`. 
		* All arguments to the JAR are treated as file paths and are prepared for upload.
		![](https://raw.githubusercontent.com/SunGard-Labs/bds-ui/master/doc/sendToScreenshot.png)
		* Details can be found here: http://www.howtogeek.com/howto/windows-vista/customize-the-windows-vista-send-to-menu/
* Command line (Windows, Linux, Mac): `java -jar bds-ui-<version>.one-jar.jar`

## How to build

You have to use JDK7/JRE7 due to https://java.net/jira/browse/JAX_WS_COMMONS-129

As EWS is not available via a public repository. Follow https://github.com/OfficeDev/ews-java-api/issues/1 for details.
For the time being, ``clone`` https://github.com/OfficeDev/ews-java-api and ``mvn install`` it.

### Pure
Maven is the primary build platform - therefore the following should always work.
```
mvn clean package
```
The release file is `target/bds-ui-<version>.one-jar.jar`
It does not require any additional files and can be copied and executed standalone.
### Eclipse
It should work in Eclipse with no m2e lifecycle hack. `jaxws-maven-plugin` is M2E compatible. 

## How to release
Nothing fancy
```
mvn release:prepare release:perform
```
