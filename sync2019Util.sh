#!/bin/bash

project_git=$1
project_name=$2
branch_name=$3
commit_info=$4
project_workir=sync-$2
project_path=$(cd `dirname $0`; pwd)
echo "server 2019 git location: $project_git"
echo "server 2019 project name: $project_name"
echo "server 2019 branch name: $branch_name"
echo "server 2019 branch commit info: $commit_info"

cd ~
rm -rf $project_workir
mkdir $project_workir
cd $project_workir
git clone $project_git
cd $project_name
git pull origin $branch_name

rm -rf ./*
rm -rf ./.gitignore

cp -r $project_path/* ./
cp $project_path/.gitignore ./
rm -rf ./sync*.sh

git add ./*
echo "ready to commit with : $commit_info "
git commit -m "$commit_info"

git push origin $branch_name

cd $project_path
#rm -rf ~/$project_workir
