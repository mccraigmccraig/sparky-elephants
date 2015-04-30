#!/bin/dash

test -z "${ETH0_IP_ADDRESS}" && export ETH0_IP_ADDRESS=`ip -f inet -o addr show dev eth0 | awk '{sub(/\/.*$/,"",$4); print $4}'`
test -z "${PROXY_IP_ADDRESS}" && export PROXY_IP_ADDRESS=`ip route show 0.0.0.0/0 | awk '{print $3}'`
test -z "${ELASTICSEARCH_URL}" && export ELASTICSEARCH_URL="http://${PROXY_IP_ADDRESS}:9200"
test -z "${MAX_MEMORY}" && export MAX_MEMORY="1g"
test -z "${SYSLOG_TAG}" && export SYSLOG_TAG="sparky-elephants"

>&2 echo ETH0_IP_ADDRESS: ${ETH0_IP_ADDRESS}
>&2 echo PROXY_IP_ADDRESS: ${PROXY_IP_ADDRESS}
>&2 echo PORT: ${PORT}
>&2 echo MAX_MEMORY: ${MAX_MEMORY}
>&2 echo ELASTICSEARCH_URL: ${ELASTICSEARCH_URL}
>&2 echo SYSLOG_TAG: ${SYSLOG_TAG}
ifconfig 1>&2
netstat -nr 1>&2
