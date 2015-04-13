#!/bin/sh

# OS specific support
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac


CLASSPATH=../conf

for f in `ls ../lib/*.jar`
do
  CLASSPATH=$CLASSPATH:$f
done

# For Cygwin, convert path to windows format
if $cygwin; then
  CLASSPATH=`cygpath --path -w "$CLASSPATH"`
fi
