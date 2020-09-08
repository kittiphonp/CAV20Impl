. ./env.sh

for code in DFL DFL_m WP genGraph; do
  cd $myRoot/mycode/$code
  make
done