$ErrorActionPreference = 'Stop'
$cityExe = 'D:\Tool\weather-city-app\WeatherCity\WeatherCity.exe'
$cityJar = Join-Path $PSScriptRoot 'desktop\build\libs\desktop-1.0.jar'
$javaExe = 'C:\Users\a\.jdks\corretto-11.0.27\bin\javaw.exe'
if (Test-Path -LiteralPath $cityExe) {
    Start-Process -FilePath $cityExe -WorkingDirectory $PSScriptRoot -WindowStyle Normal
} elseif ((Test-Path -LiteralPath $cityJar) -and (Test-Path -LiteralPath $javaExe)) {
    Start-Process -FilePath $javaExe -ArgumentList @('-Djava.net.preferIPv4Stack=true','-cp',('"'+$cityJar+'"'),
        'wk.com.test.WeatherCityLauncher') -WorkingDirectory (Join-Path $PSScriptRoot 'assets') -WindowStyle Normal
} else {
    throw 'Build the application first with gradlew.bat :desktop:dist.'
}
