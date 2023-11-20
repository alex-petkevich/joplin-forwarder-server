#!/bin/bash

chown jforwarder.jforwarder ./target/jforwarder-1.0-beta.jar
chmod u+x ./target/jforwarder-1.0-beta.jar
systemctl stop jforwarder
sleep 3
cp ./target/jforwarder-1.0-beta.jar /mnt/vol1/www/jforwarder/jforwarder.jar
systemctl start jforwarder