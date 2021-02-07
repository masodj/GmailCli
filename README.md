# GmailCli
Simple CLI utility which can send emails via GMail using oAuth authentication.

## Prerequisites
- JRE 1.8+ installed
- GMail oAuth credentials filled in gmailcli.properties file, which have to be placed in the same directory as jar file

## Usage
- compiled binary is located in /bin directory. 
- gmailcli.properties is filled witch credentials to working test account testicek69@gmail.com
```
java -jar GmailCli-1.0.0.jar -r recipient@recipient.com -s Subject -b body_text -a path_to_attachment (not mandatory)
```
