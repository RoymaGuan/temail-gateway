#!/bin/bash
# 如果有额外(即除.gitignore, .git外)的隐藏文件或者文件夹的情况，需要自行在util.sh增加中同步这些文件的操作。
branch_name=$1
commit_info=$2
./sync2019Util.sh "git@172.31.68.105:temail-server2019/temail-gateway.git" "temail-gateway" "$branch_name" "$commit_info"