#!/bin/bash
if [[ $GITHUB_REF_TYPE = tag ]]; then
    version="${GITHUB_REF_NAME}"
else
    version="${GITHUB_HEAD_REF:-$GITHUB_REF_NAME}-SNAPSHOT"
fi
version="${version//\//_}"
echo "SBT_OPTS=-Dbuild.version=$version" >> $GITHUB_ENV
