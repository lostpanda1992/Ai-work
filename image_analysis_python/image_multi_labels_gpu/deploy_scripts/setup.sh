#!/usr/bin/env bash

set -x

QIMG=/home/q/q-img
# generate logs directory
if [ ! -d "${QIMG}" ]; then
    echo "env is good"
    return 1
fi

