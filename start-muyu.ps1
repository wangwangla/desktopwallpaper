$ErrorActionPreference = 'Stop'
$muyuExe = 'D:\Tool\muyu-wallpaper\Muyu\Muyu.exe'
if (Test-Path -LiteralPath $muyuExe) {
    Start-Process -FilePath $muyuExe -ArgumentList '--wallpaper' -WindowStyle Normal
} else {
    $muyuJar = Join-Path $PSScriptRoot 'desktop\build\libs\desktop-1.0.jar'
    $muyuJava = 'C:\Users\a\.jdks\corretto-11.0.27\bin\javaw.exe'
    Start-Process -FilePath $muyuJava -ArgumentList @('-cp',('"'+$muyuJar+'"'),'wk.com.test.MuyuLauncher','--wallpaper') -WorkingDirectory (Join-Path $PSScriptRoot 'assets') -WindowStyle Normal
}
