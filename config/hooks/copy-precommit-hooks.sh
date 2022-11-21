#!/bin/bash
echo "Copies pre-commit hooks from config/hooks to .git folder"
file='.git/hooks/pre-commit'

if [ ! -f "$file" ]
then
    echo 'No hooks available. Setting up the hook now..'
    echo 'Copying pre-commits hook to your git hooks'
    cp config/hooks/pre-commit .git/hooks
    chmod +x .git/hooks/pre-commit
    echo 'Copying Talisman bin to your git hooks'
    mkdir -p .git/hooks/bin
    cp config/talisman/talisman .git/hooks/bin/

else
    echo 'A pre-commit hook already exists. Ensure Talisman check and gradle checks are also part of your pre-commit hook'
fi