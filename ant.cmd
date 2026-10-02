@echo off
setlocal
if not defined JAVA_HOME (
    if exist "C:\Program Files\NetBeans-13\jdk1.8.0_172" (
        set "JAVA_HOME=C:\Program Files\NetBeans-13\jdk1.8.0_172"
    )
)
if exist "C:\Program Files\NetBeans-13\netbeans\extide\ant\bin\ant.bat" (
    call "C:\Program Files\NetBeans-13\netbeans\extide\ant\bin\ant.bat" %*
) else (
    call ant %*
)
endlocal
