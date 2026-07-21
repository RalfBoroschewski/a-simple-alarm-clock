#!/bin/bash
MYAPPPATH=/usr/share/a-simple-alarm-clock
java -cp "$MYAPPPATH/lib/*:$MYAPPPATH/*" com.ralf.asac.Asac
