package ru.rexchange.trading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import ru.rexchange.tools.FileUtils;

public class TradeMailNotifier extends AbstractTrader {
	private static Logger LOGGER = Logger.getLogger(TradeMailNotifier.class);
	private static final String SETTING_SENDER = "rexchange-info@yandex.ru";
	//private static final String SETTING_SENDER = "info@rexchange.ru";
	//private static final String SETTING_LOGIN = "WhyNotRecord@gmail.com";
	//private static final String SETTING_PASS = "5I2n8E8u";
	private static final String SETTING_LOGIN = "rexchange-info";
	private static final String SETTING_PASS = "otgryplewjtzpogy";
	private static final String MESSAGE_WRAP = "<html><body>%s<br><img src=\"cid:%s\"/></body></html>";
	private final List<String> receivers = new ArrayList<>();
	// String receiver = null;
	String sender = null;
	String host = null;
	String port = null;
	Authenticator auth = null;
	protected boolean signalsOnly = false;

	public TradeMailNotifier() {
		super(AbstractTrader.DEFAULT_TRADER, AmountDeterminant.FIXED, 0);
	}

	public TradeMailNotifier(String name) {
		super(name, AmountDeterminant.FIXED, 0);
	}

	public TradeMailNotifier(String name, boolean signalsOnly) {
		super(name, AmountDeterminant.FIXED, 0);
		this.signalsOnly = signalsOnly;
	}

	public void configureDefault() {
		setHost("smtp.yandex.ru");
		setPort("465");//465
		setSender(SETTING_SENDER);
		readReceiversFile();
		if (receivers.isEmpty())
			addReceiver("Conquistador-@mail.ru");
		setAuthInfo(SETTING_LOGIN, SETTING_PASS);
	}
	
	/*public void configureDefault() {
		setHost("mail.rexchange.ru");
		setPort("587");
		setSender(SETTING_SENDER);
		addReceiver("Conquistador-@mail.ru");
		setAuthInfo(SETTING_SENDER, SETTING_PASS);
	}*/

  /*public void configureAlternative() {
    setHost("smtp.yandex.ru");
    setPort("587");
    setSender("info@whynotrecord.ru");
    addReceiver("Conquistador-@mail.ru");
    setAuthInfo(sender, "q1W@e3R$_whynotrecord");
  }*/

	protected void readReceiversFile() {
		File recsFile = new File("receivers.txt");
		if (recsFile.exists()) {
			try (InputStream is = new FileInputStream(recsFile)) {
				String content = FileUtils.readToString(is, "UTF-8");
				receivers.clear();
				String[] recs = content.split("\\n");
				for (String rec : recs) {
					receivers.add(rec.trim());
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	/*public void configureSafe() {
		setHost("imap.gmail.com");
		setPort("587");
		setSender("n.malygin@id-sys.ru");
		setReceiver("Conquistador-@mail.ru");
		setAuthInfo("info@rexchange.ru", "6M3w7Q8z");
	}*/

	@Override
	public void requestCurrenciesAmount() {
		// not used
	}

	//TODO упростить четыре метода, выделив общее
	@Override
	public boolean openBuy(float desiredRate, DealInfo info) {
		try {
			Integer trend = (info instanceof TrendInfo) ? ((TrendInfo) info).getTrend() : null;
			String file = (info instanceof TrendAndFileInfo)
					? ((TrendAndFileInfo) info).getFilePath()
					: null;
			sendMail(String.format("BUY %s", getName()), formMessageBody(desiredRate, trend),
					file);
			dealRate = desiredRate;
		} catch (MessagingException e) {
			LOGGER.error(e);
			return false;
		}
		return true && !signalsOnly;
	}
	
	@Override
	public boolean preOpenBuy(float desiredRate, DealInfo info) {
		try {
			Integer trend = (info instanceof TrendInfo) ? ((TrendInfo) info).getTrend() : null;
			String file = (info instanceof TrendAndFileInfo)
					? ((TrendAndFileInfo) info).getFilePath()
					: null;
			sendMail(String.format("LOW %s", getName()), formMessageBody(desiredRate, trend),
					file);
			dealRate = desiredRate;
		} catch (MessagingException e) {
			LOGGER.error(e);
			return false;
		}
		return true && !signalsOnly;
	}

	@Override
	public boolean openSell(float desiredRate, DealInfo info) {
		try {
			Integer trend = (info instanceof TrendInfo) ? ((TrendInfo) info).getTrend() : null;
			String file = (info instanceof TrendAndFileInfo)
					? ((TrendAndFileInfo) info).getFilePath()
					: null;
			sendMail(String.format("SELL %s", getName()), formMessageBody(desiredRate, trend),
					file);
			dealRate = desiredRate;
		} catch (MessagingException e) {
			LOGGER.error(e);
			return false;
		}
		return true && !signalsOnly;
	}
	
	@Override
	public boolean preOpenSell(float desiredRate, DealInfo info) {
		try {
			Integer trend = (info instanceof TrendInfo) ? ((TrendInfo) info).getTrend() : null;
			String file = (info instanceof TrendAndFileInfo)
					? ((TrendAndFileInfo) info).getFilePath()
					: null;
			sendMail(String.format("HIGH %s", getName()), formMessageBody(desiredRate, trend),
					file);
			dealRate = desiredRate;
		} catch (MessagingException e) {
			LOGGER.error(e);
			return false;
		}
		return true && !signalsOnly;
	}

	@Override
	public boolean closeBuy(float desiredRate, DealInfo info) {
		try {
			Integer trend = (info instanceof TrendInfo) ? ((TrendInfo) info).getTrend() : null;
			String file = (info instanceof TrendAndFileInfo)
					? ((TrendAndFileInfo) info).getFilePath()
					: null;
			sendMail(String.format("%s: close buy", getName()), formMessageBody(desiredRate, trend),
					file);
			dealRate = 0f;
		} catch (MessagingException e) {
			LOGGER.error(e);
			return false;
		}
		return true && !signalsOnly;
	}

	@Override
	public boolean closeSell(float desiredRate, DealInfo info) {
		try {
			Integer trend = (info instanceof TrendInfo) ? ((TrendInfo) info).getTrend() : null;
			String file = (info instanceof TrendAndFileInfo)
					? ((TrendAndFileInfo) info).getFilePath()
					: null;
			sendMail(String.format("%s: close sell", getName()),
					formMessageBody(desiredRate, trend), file);
			dealRate = 0f;
		} catch (MessagingException e) {
			LOGGER.error(e);
			return false;
		}
		return true && !signalsOnly;
	}

	private String formMessageBody(float desiredRate, Integer trend) {
		String messageBody = String.format("Rate: %s.", desiredRate);
		if (trend != null) {
			messageBody += String.format("%nTrend: %s.",
					trend > 0 ? "up" : trend == 0 ? "none" : "down");
		}
		return messageBody;
	}

	public void sendMail(String subjectKey, String text, String filePath)
			throws MessagingException {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		properties.setProperty("mail.smtp.port", port);
		properties.setProperty("mail.smtp.auth", "true");
		//properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.socketFactory.port", port);
		//properties.setProperty("mail.smtp.ssl.checkserveridentity", "false");
		//properties.setProperty("mail.smtp.ssl.trust", "*");

		Session session = null;
		if (auth == null) {
			session = Session.getDefaultInstance(properties);
		} else {
			properties.setProperty("mail.smtp.auth", "true");
			session = Session.getDefaultInstance(properties, auth);
		}
		//session.setDebug(true);

		MimeMessage message = new MimeMessage(session); // email message
		message.setFrom(sender);
		message.setSubject(subjectKey);
		//String fullText = String.format("%s%n%s", subjectKey, text);
		String fullText = text;
		if (filePath != null) {
			addAttachment(message, filePath, fullText);
		} else {
			message.setText(fullText);
		}

		// Send message
		for (String receiver : receivers) {
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
			Transport.send(message);
		}
	}

	protected void addAttachment(MimeMessage message, String filePath, String text)
			throws MessagingException {
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		try {
			String cid = UUID.randomUUID().toString();
			messageBodyPart.attachFile(filePath);
			messageBodyPart.setContentID(String.format("<%s>", cid));
			messageBodyPart.setDisposition(MimeBodyPart.INLINE);
			multipart.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();
			text = text.replaceAll("\\n", "<br>");
			messageBodyPart.setText(String.format(MESSAGE_WRAP, text, cid), "US-ASCII", "html");
		} catch (IOException e) {
			LOGGER.error(e);
			DataSource source = new FileDataSource(filePath);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName("graphics.png");
			multipart.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(text);
		}
		multipart.addBodyPart(messageBodyPart);
		message.setContent(multipart);
	}

	public void addReceiver(String receiver) {
		this.receivers.add(receiver);
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setAuthInfo(final String user, final String password) {
		auth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		};
	}

	@Override
	public String getBalance() {
		return null;
	}

	public void setSignalsOnly(boolean signalsOnly) {
		this.signalsOnly = signalsOnly;
	}

	@Override
	public void customRuntimeReconfig() {
		readReceiversFile();
	}

  /*public static void main(String[] args) {
  	TradeMailNotifier mailer = new TradeMailNotifier();
    mailer.configureDefault();
  	try {
  		mailer.sendMail("Untitled", "test mail", "graphics.png");
  	} catch (MessagingException e) {
  		e.printStackTrace();
  	}
  }*/
	/*if (tls) {
		props.put("mail.smtp.starttls.enable", "true");
	} else if(ssl) {
		props.put("mail.smtp.socketFactory.port", smtpPort);
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	}*/

}
