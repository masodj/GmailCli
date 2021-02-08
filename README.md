# GmailCli
Simple CLI utility which can send emails via GMail using oAuth authentication.

## Prerequisites
- JRE 1.8+ installed
- GMail oAuth credentials filled in gmailcli.properties - file have to be placed in the same directory as jar file

## Usage
- compiled binary is located in /bin directory. 
- gmailcli.properties is filled with credentials to working test account testicek69@gmail.com
```
java -jar GmailCli-1.0.0.jar -r recipient@recipient.com -s Subject -b "1st_line_of_body%n2nd_line_of_body" -a path_to_attachment (not mandatory)
```
