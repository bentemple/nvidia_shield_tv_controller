#!/bin/bash

if [[ "$1" == "-c" ]];then 
    rm -fr output/*
    for i in `ls switch/ic_app_icon/res/`;do mkdir output/$i;done
fi

for i in `gfind . -name "*.png" -o -name "*.webp" |grep -E "\/.*\/.*\/.*\/*.\/"`;do 
    output=$(echo $i |awk -F "/" '{print "output/"$5"/"$2"_"$6}';)
    cp $i $output
done
