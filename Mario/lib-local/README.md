install deadlockmonitor.jar to local maven using following command:

mvn install:install-file \
    -Dfile=/Path/to/deadlockmonitor.jar \
    -DgroupId=nhb.common \
    -DartifactId=deadlockmonitor \
    -Dversion=1.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true