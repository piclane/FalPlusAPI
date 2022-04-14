#!/bin/bash -ex

# stop fapi service
service fapi stop || true

# unregister httpd
\rm -f /etc/httpd/conf.d/fapi.conf

# unregister service
chkconfig fapi off
\rm -f /etc/init.d/fapi

# uninstall app jar
\rm -rf /usr/local/fapi

# uninstall java 11
yum remove -y java-11-amazon-corretto-devel
