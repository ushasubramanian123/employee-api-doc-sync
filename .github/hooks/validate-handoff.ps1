$workspaceRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$handoffDirectory = Join-Path $workspaceRoot '.sdlc'

if (-not (Test-Path -LiteralPath $handoffDirectory -PathType Container)) {
    exit 0
}

$handoffFiles = @(Get-ChildItem -LiteralPath $handoffDirectory -Filter '*.json' -File | Sort-Object Name)
if ($handoffFiles.Count -eq 0) {
    exit 0
}

$requiredStringFields = @('stage', 'status', 'summary', 'next_agent')
$requiredArrayFields = @('artifacts', 'decisions', 'blockers')
$failures = @()

foreach ($file in $handoffFiles) {
    try {
        $handoff = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        $failures += "$($file.Name): invalid JSON: $($_.Exception.Message)"
        continue
    }

    if ($null -eq $handoff -or $handoff -isnot [System.Management.Automation.PSCustomObject]) {
        $failures += "$($file.Name): root value must be a JSON object."
        continue
    }

    foreach ($field in $requiredStringFields) {
        $property = $handoff.PSObject.Properties[$field]
        if ($null -eq $property) {
            $failures += "$($file.Name): missing required field '$field'."
        }
        elseif ($property.Value -isnot [string] -or [string]::IsNullOrWhiteSpace($property.Value)) {
            $failures += "$($file.Name): field '$field' must be a non-empty string."
        }
    }

    $statusProperty = $handoff.PSObject.Properties['status']
    if ($null -ne $statusProperty -and $statusProperty.Value -is [string] -and
        $statusProperty.Value -notin @('completed', 'blocked', 'failed')) {
        $failures += "$($file.Name): field 'status' must be completed, blocked, or failed."
    }

    foreach ($field in $requiredArrayFields) {
        $property = $handoff.PSObject.Properties[$field]
        if ($null -eq $property) {
            $failures += "$($file.Name): missing required field '$field'."
        }
        elseif ($property.Value -isnot [System.Array]) {
            $failures += "$($file.Name): field '$field' must be a JSON array."
        }
    }
}

if ($failures.Count -gt 0) {
    $message = 'SDLC handoff validation failed: ' + ($failures -join ' ')
    $output = @{
        decision = 'block'
        reason = $message
        hookSpecificOutput = @{
            hookEventName = 'PostToolUse'
            additionalContext = $message
        }
    }
    [Console]::Out.WriteLine((ConvertTo-Json -InputObject $output -Compress -Depth 5))
}
