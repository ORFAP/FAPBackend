# FAPBackend

[![Build Status](https://travis-ci.org/ORFAP/FAPBackend.svg?branch=master)](https://travis-ci.org/ORFAP/FAPBackend)


##Starten

###IDE
1. Mit Intellij das Repo importieren

2. Lombok Plugin installieren und als Annotation Processors für das Projekt aktivieren (https://github.com/mplushnikov/lombok-intellij-plugin)

3. FapBackendApplication.main als Spring Application starten

###Docker
#### Local debug profile:
```docker run -d -p 8081:8080 darenegade/fapbackend```
#### Production configuration:
```docker run -d -e TZ=GMT+2 -e "SPRING_PROFILES_ACTIVE=production" -p 8081:8080 darenegade/fapbackend```



##RESTful
Erlaubt sind alle CRUD Operationen und es ist eine Suche implementiert. Hateos ist integriert.
Übermittlung der Daten im JSON Format.
Schnittstellen:
* /airlines
* /markets
* /routes
* /routes/filter
* /settings
