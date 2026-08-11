$destDir = "C:\Users\Guest2\.gradle\wrapper\dists\gradle-8.5-bin\5t9huq95ubn472n8rpzujfbqh"
$zipPath = Join-Path $destDir "gradle-8.5-bin.zip"

Write-Host "Creating destination directory: $destDir"
New-Item -ItemType Directory -Path $destDir -Force | Out-Null

Write-Host "Downloading Gradle 8.5 distribution from GitHub releases..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$url = "https://github.com/gradle/gradle-distributions/releases/download/v8.5.0/gradle-8.5-bin.zip"

$webClient = New-Object System.Net.WebClient
$webClient.DownloadFile($url, $zipPath)

Write-Host "Extracting Gradle 8.5..."
Expand-Archive -Path $zipPath -DestinationPath $destDir -Force

$okFile = Join-Path $destDir "gradle-8.5-bin.zip.ok"
New-Item -ItemType File -Path $okFile -Force | Out-Null

$partFile = Join-Path $destDir "gradle-8.5-bin.zip.part"
Remove-Item $partFile -ErrorAction SilentlyContinue

Write-Host "Gradle 8.5 installation successfully completed!"
