
Trabalho realizado no âmbito da cadeira de Sistemas Distribuidos (2013/14)

ANTES DE LANCAR QUALQUER UM DOS JAVA EXECUTAR:
admitindo execução a partir do interior da pasta bin.

(UNIX)
export CLASSPATH=.:../aux_libs/scribe-1.3.2.jar:../aux_libs/json-simple-1.1.1.jar:../aux_libs/commons-codec-1.7.jar

(WINDOWS)
set classpath=%classpath;.;../aux_libs/scribe-1.3.2.jar;../aux_libs/json-simple-1.1.1.jar;../aux_libs/commons-codec-1.7.jar

JAVA A EXECUTAR

CONTACT SERVER

java contactserver.ContactServer

CLIENTE

java client.FileClient url_contactServer utilizador

SERVIDOR
java fileserver.FileServer nome url_contactServer proprietario
java rest.GoogleDriveProxy nome url_contactServer proprietario
java rest.DropboxProxy nome url_contactServer proprietario

Eexemplos:

java client.FileClient localhost user1

SERVIDOR
java fileserver.FileServer server1 localhost user1
java rest.GoogleDriveProxy server2 localhost user1
java rest.DropboxProxy server3 localhost user2