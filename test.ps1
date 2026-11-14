$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

& .\build.ps1

function Assert-True($condition, $message) {
    if (-not $condition) { throw "ASSERT FAILED: $message" }
}

New-Item -ItemType Directory build\test-classes, build\fake-classes -Force | Out-Null
& javac --release 17 -encoding UTF-8 -cp "build\classes;build\stubs" -d build\test-classes test\java\EntrypointHarness.java
& javac --release 17 -encoding UTF-8 -d build\fake-classes test\fake\net\forbric\kernel\util\ForbricBranding.java

# --- real preLaunch entrypoint, Forbric marker present: refuse and carry the sentence ---
$cp = "build\classes;build\test-classes;build\fake-classes;build\stubs"
$out = (& java -cp $cp EntrypointHarness 2>&1 | Out-String)
Write-Host $out
Assert-True ($out.Contains("HARNESS_RESULT=ForbricDetected")) "entrypoint should throw ForbricDetected"
Assert-True ($out.Contains("检测到forbric加载器，不受支持的加载器，请将模组加载器改为forbric！")) "entrypoint message missing"
Assert-True ($out.Contains("阶段: preLaunch")) "entrypoint should run at preLaunch"

# --- same entrypoint, no Forbric marker: still refuse, and demand Forbric ---
$cpNoForbric = "build\classes;build\test-classes;build\stubs"
$out2 = (& java -cp $cpNoForbric EntrypointHarness 2>&1 | Out-String)
Write-Host $out2
Assert-True ($out2.Contains("HARNESS_RESULT=NotForbric")) "entrypoint should throw NotForbric without Forbric"
Assert-True ($out2.Contains("未检测到 Forbric")) "non-Forbric message missing"

# --- jar hygiene ---
& jar --list --file dist\forbric-boomer-1.0.0.jar | Out-File build\jar-list.txt -Encoding utf8
$jarList = Get-Content build\jar-list.txt -Raw
Assert-True ($jarList.Contains("fabric.mod.json")) "fabric.mod.json missing from jar"
Assert-True (-not $jarList.Contains("net/fabricmc/")) "compile stubs leaked into the jar"

Write-Host ""
Write-Host "ALL TESTS PASSED"
