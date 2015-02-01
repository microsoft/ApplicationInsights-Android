#!/bin/bash                                                                                                         

# generate docs
gradle docs

# save local.properties
git add local.properties -f
git stash

# add docs to version control
git add applicationinsights-android/build/docs/javadoc -f
git stash

# clear old docs
git checkout gh-pages
git clean -xdf
git rm -rf .

# add updated docs
git stash pop
mv applicationinsights-android/build/docs/javadoc/* ./
rm -rf applicationinsights-android
git add -A
git commit -m "updating docs via update-docs.sh"
git push origin gh-pages

# restore local.properties
git checkout master
git stash pop
git reset HEAD local.properties 
