param(
    [string[]]$MavenArguments = @('--batch-mode', 'javafx:run')
)

$ErrorActionPreference = 'Stop'
try {
    # Search existing installations without changing the user's system settings.
    $javaCandidates = @()
    if ($env:JAVA_HOME) { $javaCandidates += $env:JAVA_HOME }
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaCandidates += Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
    }
    $javaRoots = @(
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles\Microsoft",
        "$env:USERPROFILE\.jdks"
    )
    foreach ($javaRoot in $javaRoots) {
        if (Test-Path -LiteralPath $javaRoot) {
            $javaCandidates += Get-ChildItem -LiteralPath $javaRoot -Directory |
                Select-Object -ExpandProperty FullName
        }
    }

    $selectedJdk = $null
    foreach ($candidate in ($javaCandidates | Select-Object -Unique)) {
        $javaExe = Join-Path $candidate 'bin\java.exe'
        $javacExe = Join-Path $candidate 'bin\javac.exe'
        if (!(Test-Path -LiteralPath $javaExe) -or !(Test-Path -LiteralPath $javacExe)) { continue }
        # JavaFX 25 requires Java 23 or newer, even though sources target Java 21.
        # Windows PowerShell treats native stderr as an error stream; Java prints
        # its version there even on success.
        $ErrorActionPreference = 'Continue'
        try {
            $versionInfo = & $javaExe -XshowSettings:properties -version 2>&1 | Out-String
        } finally {
            $ErrorActionPreference = 'Stop'
        }
        if ($LASTEXITCODE -eq 0 -and $versionInfo -match 'java.specification.version\s*=\s*(\d+)') {
            if ([int]$Matches[1] -ge 23) {
                $selectedJdk = $candidate
                break
            }
        }
    }
    if (!$selectedJdk) {
        throw 'Java JDK 23 or newer is required. Install a compatible JDK, then double-click Run Game.cmd again. If installed elsewhere, set JAVA_HOME to its folder.'
    }

    $env:JAVA_HOME = $selectedJdk
    Write-Host "Using Java: $selectedJdk"
    Write-Host 'Starting Snake Game. The first launch downloads Maven and dependencies; internet access is required.'
    Push-Location -LiteralPath $PSScriptRoot
    try {
        & "$PSScriptRoot\mvnw.cmd" @MavenArguments
        $result = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    exit $result
} catch {
    Write-Host "Unable to start Snake Game: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
