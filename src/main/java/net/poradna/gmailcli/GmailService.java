package net.poradna.gmailcli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;

public final class GmailService {

    private static final String APPLICATION_NAME = "GmailCli";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private HttpTransport httpTransport;
    private String userEmail;
    private String accessToken;
    private String refreshToken;
    private String clientId;
    private String clientSecret;

    public GmailService() {
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            loadConfig();
        } catch (ConfigurationException e) {
            System.err.println("Error loading configuration file.");
        } catch (GeneralSecurityException e) {
            System.err.println("Error creating google api transport.");
        } catch (IOException e) {
            System.err.println("Error creating google api transport.");
        }
    }

    public boolean sendMessage(String recipientAddress, String subject, String body, File attachment) {
        try {
            Message message = createEmail(recipientAddress, userEmail, subject, body, attachment);
            return createGmail().users()
                    .messages()
                    .send(userEmail, message)
                    .execute()
                    .getLabelIds().contains("SENT");
        } catch (MessagingException e) {
            System.err.println("Error sending email.");
        } catch (IOException e) {
            System.err.println("Error sending email.");
        }
        return false;
    }

    private Gmail createGmail() {
        Credential credential = buildCredentials();
        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Message createEmail(String to, String from, String subject, String bodyText, File attachment) throws MessagingException, IOException {
        MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart textBodyPart = new MimeBodyPart();
        multipart.addBodyPart(textBodyPart);
        textBodyPart.setText(bodyText);

        if (attachment != null) {
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(FilenameUtils.getName(attachment.getName()));
            multipart.addBodyPart(attachmentBodyPart);
        }

        email.setContent(multipart);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        return new Message().setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }

    private Credential buildCredentials() {
        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    private void loadConfig() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFileName("gmailcli.properties"));
        Configuration config = builder.getConfiguration();
        accessToken = config.getString("accessToken");
        clientId = config.getString("clientId");
        refreshToken = config.getString("refreshToken");
        userEmail = config.getString("userEmail");
        clientSecret = config.getString("clientSecret");
    }
}
