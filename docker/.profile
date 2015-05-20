#!/bin/dash

test -z "${ETH0_IP_ADDRESS}" && export ETH0_IP_ADDRESS=`ip -f inet -o addr show dev eth0 | awk '{sub(/\/.*$/,"",$4); print $4}'`
test -z "${PROXY_IP_ADDRESS}" && export PROXY_IP_ADDRESS=`ip route show 0.0.0.0/0 | awk '{print $3}'`
test -z "${ELASTICSEARCH_URL}" && export ELASTICSEARCH_URL="http://${PROXY_IP_ADDRESS}:9200"
test -z "${MAX_MEMORY}" && export MAX_MEMORY="1g"
test -z "${SYSLOG_TAG}" && export SYSLOG_TAG="sparky-elephants"

test -z "${ZOOKEEPER_CONNECT}" && export ZOOKEEPER_CONNECT="localhost:2181"
test -z "${METADATA_BROKER_LIST}" && export METADATA_BROKER_LIST="localhost:9092"

>&2 echo ETH0_IP_ADDRESS: ${ETH0_IP_ADDRESS}
>&2 echo PROXY_IP_ADDRESS: ${PROXY_IP_ADDRESS}
>&2 echo PORT: ${PORT}
>&2 echo MAX_MEMORY: ${MAX_MEMORY}
>&2 echo ELASTICSEARCH_URL: ${ELASTICSEARCH_URL}
>&2 echo SYSLOG_TAG: ${SYSLOG_TAG}
>&2 echo ZOOKEEPER_CONNECT: ${ZOOKEEPER_CONNECT}
>&2 echo METADATA_BROKER_LIST: ${METADATA_BROKER_LIST}

ifconfig 1>&2
netstat -nr 1>&2
