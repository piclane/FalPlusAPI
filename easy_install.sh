#!/bin/bash -e

title() {
  printf "\n\e[36m%s\e[0m\n\n" "$*"
}

echo '  _____     _ ____  _              _    ____ ___ '
echo ' |  ___|_ _| |  _ \| |_   _ ___   / \  |  _ \_ _|'
echo ' | |_ / _` | | |_) | | | | / __| / _ \ | |_) | | '
echo ' |  _| (_| | |  __/| | |_| \__ \/ ___ \|  __/| | '
echo ' |_|  \__,_|_|_|   |_|\__,_|___/_/   \_\_|  |___|'
echo

# FAL+ easy installer

pushd /tmp >/dev/null
curl -o fapi.json -sS -H 'Accept: application/vnd.github.v3+json' 'https://api.github.com/repos/piclane/FalPlusAPI/releases/latest'
DOWNLOAD_URL="$(grep '"browser_download_url"' fapi.json | sed -E -e 's/^.*"(https:[^"]+)"$/\1/')"
VERSION="$(grep '"tag_name"' fapi.json | sed -E -e 's/^.*"tag_name": "v([^"]+)".*$/\1/')"
\rm -f fapi.json

title 本体をダウンロードしています...
curl -L -# -o fapi.tar.gz "${DOWNLOAD_URL}"
tar zxf fapi.tar.gz
\rm -f fapi.tar.gz
cd "fal-plus-api-${VERSION}"

title インストールを開始します...
if [ "$(whoami)" != "root" ] ; then
  echo "root ユーザーのパスワードを入力してください:"
fi
if ! su -c "$(pwd)/install.sh" - ; then
  echo
  echo 自動インストールに失敗しました。
  echo 以下のコマンドを実行してインストールを続行してください。
  echo
  echo 'su -c "'"$(pwd)"'/install.sh"'
  echo
  popd >/dev/null
  exit 2
fi
popd >/dev/null
