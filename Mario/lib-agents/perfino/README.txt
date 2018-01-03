In order to setup a JVM on a remote system to be monitored by perfino, copy
the appropriate agent file to the remote machine, extract it somewhere and
add the VM parameter

    -javaagent:[1]=server=[2],name=[3]

to the start command of the JVM replacing

[1] with the path to perfino.jar
[2] with the name or IP address of the perfino server
[3] with the name of the JVM as it should appear in perfino

If you have several groups of JVMs, you can also add ",group=[group name]"
to the above parameter. JVM groups are added on the fly, you can configure
them later on in perfino.