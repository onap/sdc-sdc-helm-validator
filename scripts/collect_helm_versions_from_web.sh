#!/bin/bash

set -e

HELM_TMP_DIR=helm_tmp
HELM_VERSIONS_DIR=helm_versions
IS_LOCAL=false
if [ "$1" == "local" ]; then
  IS_LOCAL=true
fi

readarray -d , -t SUPPORTED_VERSION_ARRAY <<<"$HELM_SUPPORTED_VERSIONS"

mkdir -p $HELM_VERSIONS_DIR
mkdir -p $HELM_TMP_DIR

getHelm() {
  VERSION=$1
  echo "Attempt to collect HELM ${VERSION}"

  if [ -a "$HELM_VERSIONS_DIR/helm-v${VERSION}" ]; then
    echo "HELM ${VERSION} already exists"
  else
    mkdir -p $HELM_TMP_DIR/v${VERSION}
    echo "Downloading..."
    wget -q https://get.helm.sh/helm-v${VERSION}-linux-amd64.tar.gz -O $HELM_TMP_DIR/helm-v${VERSION}.tar.gz
    echo "Archive Extracting..."
    tar -zxvf $HELM_TMP_DIR/helm-v${VERSION}.tar.gz -C $HELM_TMP_DIR/v${VERSION} linux-amd64/helm
    mv $HELM_TMP_DIR/v${VERSION}/linux-amd64/helm $HELM_VERSIONS_DIR/helm-v${VERSION}
  fi

  echo "DONE"
}

for i in "${SUPPORTED_VERSION_ARRAY[@]}"; do
  getHelm $i
done

if [ $IS_LOCAL == false ]; then
  mv $HELM_VERSIONS_DIR/* /usr/local/bin/
  rm -r $HELM_VERSIONS_DIR
  rm -r $HELM_TMP_DIR
fi
