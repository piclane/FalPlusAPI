#!/bin/bash -ex

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE:-$0}")"; pwd)

# stop fapi service
service fapi stop || true

# install java 11
if ! java -version 2>&1 | grep -q -F '"11.' ; then
  curl -L -# -o '/tmp/amazon-corretto-11-x64-linux-jdk.rpm' 'https://corretto.aws/downloads/latest/amazon-corretto-11-x64-linux-jdk.rpm'
  yum localinstall -y '/tmp/amazon-corretto-11-x64-linux-jdk.rpm'
  rm -f '/tmp/amazon-corretto-11-x64-linux-jdk.rpm'
fi

# install app jar
mkdir -p /usr/local/fapi
\rm -f /usr/local/fapi/*.jar
cp "${SCRIPT_DIR}"/*.jar /usr/local/fapi/
mv /usr/local/fapi/*.jar /usr/local/fapi/fapi.jar
chown foltia:foltia /usr/local/fapi/fapi.jar
chmod 500 /usr/local/fapi/fapi.jar

# register service
if [ ! -L /etc/init.d/fapi ] ; then
  ln -s /usr/local/fapi/fapi.jar /etc/init.d/fapi
  chkconfig fapi on
fi

# register httpd
echo 'ProxyPass /api/ http://localhost:8080/api/' > /etc/httpd/conf.d/fapi.conf

# start fapi service
service fapi start

# reload httpd service
service httpd reload
