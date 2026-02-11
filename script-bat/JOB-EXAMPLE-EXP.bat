@echo off
rem #
rem # データエクスポートバッチ実行
rem #
call %~dp0sub\java-exec.bat %~n0 com.example.app.bat.exmodule.ExampleExport "output=C:\tmp\example_export.txt"
