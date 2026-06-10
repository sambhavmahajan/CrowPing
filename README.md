# CrowPing
This application prevents your webapp from going into cold start restart. Asks for your email and some password you very your email, then you can insert your uri, which will be pinged(a head request) each time every EXECUTOR_FIXDELAY(a cron expression). You can have atmost PING_LIMIT active URIs. If the ping fails, CrowPing automatically notifies the user, and when it gets up again, user is notified as well.
## Requirements
* Java 21
* Springboot 4.0.3
* Maven 3.6.3 (can also use maven wrapper already complied)
* Docker (Optional)

## Configuration
### .env file
```
BASEURL=<This is your deployed url, will be used for verification links>
CONNECTION_STRING=<jdbc:postgresql://>
DB_PASSWORD=<DB_PASSWORD>
DB_USERNAME=<DB_USERNAME>
DDL=<create(first time), validate(in prod)>
EXECUTOR_COREPOOL=10
EXECUTOR_FIXDELAY=<delay for Schedules, uses a fixedDelayString, eg: 10m>
EXECUTOR_MAXPOOL=<ExcecutorService maxpool>
EXECUTOR_NTHREADS=<I dont remember where i used it, seems i was doing something, just leave it as 2 for now>
EXECUTOR_QUEUECAP=<Excecutor service queue cap>
HIKARI_POOLSIZE=<Max hikari pool size>
MAIL=<SMTP email that will be used to email stuff>
MAIL_PASSWORD=<smtp email password for MAIL>
MINIDLE=<Hikari pool minimum idle connections>
MY_EMAIL=<This will be emailed when your application starts/restarts>
PING_LIMIT=<How many active uris per user>
PORT=<port number>
TIMEOUT=<SMTP timeout in miliseconds>
```

## Running
```
mvnw spring-boot:run
```
OR
```
mvn spring-boot:run
```
OR
```
docker build -t crowping:latest .
docker run -it -p <PORT>:<PORT> simpleforums
```
## Contributing
Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss your proposed changes.
