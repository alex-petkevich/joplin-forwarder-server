#!/bin/bash

chown joplinforwarder:joplinforwarder ./target/joplinforwarder-1.0.0-SNAPSHOT.jar
chmod u+x ./target/joplinforwarder-1.0.0-SNAPSHOT.jar
systemctl stop joplinforwarder
sleep 3
cp ./target/joplinforwarder-1.0.0-SNAPSHOT.jar /mnt/vol1/www/joplin/joplinforwarder.jar
systemctl start joplinforwarder