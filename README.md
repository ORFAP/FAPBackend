# FAPBackend

[![Build Status](https://travis-ci.org/ORFAP/FAPBackend.svg?branch=master)](https://travis-ci.org/ORFAP/FAPBackend)


##Starten
1. Mit Intellij das Repo importieren

2. Lombok Plugin installieren und als Annotation Processors für das Projekt aktivieren (https://github.com/mplushnikov/lombok-intellij-plugin)

3. FapBackendApplication.main als Spring Application starten

##RESTful
Erlaubt sind alle CRUD Operationen und es ist eine Suche implementiert. Hateos ist integriert.
Übermittlung der Daten im JSON Format.
Schnittstellen:
* /airlines
* /airports
* /routes
