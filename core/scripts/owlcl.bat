echo off

if defined OWLCL_OPTS goto skipOpts
set OWLCL_OPTS=-Xmx1024m
:skipOpts

java %OWLCL_OPTS% -jar "%~dp0\owlcl.jar" %*
