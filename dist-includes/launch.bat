REM UTF-8�Ή�
chcp 65001 >nul

SET JVM_OPT=-XX:MaxMetaspaceSize=256M

REM update/logbook-kai.jar �����݂��邩�m�F
IF EXIST update\logbook-kai.jar (
    echo update/logbook-kai.jar �� ./logbook-kai.jar �ɃR�s�[���Ă��܂�...
    COPY /Y update\logbook-kai.jar logbook-kai.jar
    echo �R�s�[���������܂����B
) ELSE (
    echo update/logbook-kai.jar ��������܂���ł����B�R�s�[���X�L�b�v���܂��B
)

REM �A�v���P�[�V�������N��
START javaw %JVM_OPT% -jar logbook-kai.jar
