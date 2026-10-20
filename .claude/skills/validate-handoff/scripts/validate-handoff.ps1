[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Path
)

$ErrorActionPreference = 'Stop'

function Write-Failure {
    param([string[]]$Messages)

    foreach ($message in $Messages) {
        [Console]::Out.WriteLine("FAIL: $message")
    }
    exit 1
}

try {
    $workspaceRoot = (Get-Location).ProviderPath
    $targetPath = [System.IO.Path]::GetFullPath($Path)
    $handoffRoot = [System.IO.Path]::GetFullPath((Join-Path $workspaceRoot '.sdlc\claude')).TrimEnd('\') + '\'
} catch {
    Write-Failure @("Invalid handoff path '$Path': $($_.Exception.Message)")
}

if (-not $targetPath.StartsWith($handoffRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    exit 0
}

$routes = @{
    'requirements.json' = @{ Stage = 'requirements'; Next = 'architecture' }
    'architecture.json' = @{ Stage = 'architecture'; Next = 'design-review' }
    'design-review.json' = @{ Stage = 'design-review'; Next = 'planning' }
    'implementation-plan.json' = @{ Stage = 'planning'; Next = 'developer' }
    'implementation.json' = @{ Stage = 'developer'; Next = 'code-review' }
    'code-review.json' = @{ Stage = 'code-review'; Next = 'verification' }
    'verification.json' = @{ Stage = 'verification'; Next = 'pr' }
    'pr.json' = @{ Stage = 'pr'; Next = $null }
}

$filename = [System.IO.Path]::GetFileName($targetPath)
$route = $null
foreach ($candidate in $routes.Keys) {
    if ([string]::Equals($filename, $candidate, [System.StringComparison]::Ordinal)) {
        $route = $routes[$candidate]
        break
    }
}
if ($null -eq $route) {
    exit 0
}

if (-not [System.IO.File]::Exists($targetPath)) {
    Write-Failure @("Target handoff does not exist: $targetPath")
}

try {
    $jsonText = [System.IO.File]::ReadAllText($targetPath)
} catch {
    Write-Failure @("Could not read handoff '$targetPath': $($_.Exception.Message)")
}

try {
    $handoff = ConvertFrom-Json -InputObject $jsonText -ErrorAction Stop
} catch {
    Write-Failure @("Invalid JSON in '$targetPath': $($_.Exception.Message)")
}

$errors = New-Object 'System.Collections.Generic.List[string]'
if ($null -eq $handoff -or $handoff -isnot [System.Management.Automation.PSCustomObject]) {
    $errors.Add('Top-level JSON value must be an object.')
    Write-Failure $errors.ToArray()
}

$expectedKeys = @('stage', 'status', 'summary', 'artifacts', 'decisions', 'blockers', 'next_agent')
$actualKeys = @($handoff.PSObject.Properties | ForEach-Object { $_.Name })
if ($actualKeys.Count -ne $expectedKeys.Count) {
    $errors.Add("Expected exactly seven top-level keys; found $($actualKeys.Count).")
}
foreach ($key in $expectedKeys) {
    if ($actualKeys -cnotcontains $key) {
        $errors.Add("Missing required case-sensitive key '$key'.")
    }
}
foreach ($key in $actualKeys) {
    if ($expectedKeys -cnotcontains $key) {
        $errors.Add("Unexpected top-level key '$key'.")
    }
}

foreach ($key in @('stage', 'status', 'summary')) {
    $property = $handoff.PSObject.Properties[$key]
    if ($null -ne $property -and $property.Value -isnot [string]) {
        $errors.Add("Field '$key' must be a string.")
    }
}
if ($null -ne $handoff.PSObject.Properties['status'] -and
    $handoff.status -is [string] -and
    $handoff.status -cnotin @('completed', 'blocked')) {
    $errors.Add("Field 'status' must be 'completed' or 'blocked'.")
}
foreach ($key in @('artifacts', 'decisions', 'blockers')) {
    $property = $handoff.PSObject.Properties[$key]
    if ($null -ne $property -and $property.Value -isnot [System.Array]) {
        $errors.Add("Field '$key' must be an array.")
    }
}

$stageProperty = $handoff.PSObject.Properties['stage']
if ($null -ne $stageProperty -and $stageProperty.Value -is [string] -and $stageProperty.Value -cne $route.Stage) {
    $errors.Add("Expected stage '$($route.Stage)' for '$filename'; found '$($stageProperty.Value)'.")
}
$nextProperty = $handoff.PSObject.Properties['next_agent']
if ($null -eq $nextProperty) {
    $errors.Add("Missing required case-sensitive key 'next_agent'.")
} elseif ($null -eq $route.Next) {
    if ($null -ne $nextProperty.Value) {
        $errors.Add("Expected next_agent to be JSON null for '$filename'.")
    }
} elseif ($nextProperty.Value -isnot [string] -or $nextProperty.Value -cne $route.Next) {
    $errors.Add("Expected next_agent '$($route.Next)' for '$filename'.")
}

if ($errors.Count -gt 0) {
    Write-Failure $errors.ToArray()
}

if ($handoff.status -ceq 'blocked') {
    [Console]::Out.WriteLine('PASS: valid blocked handoff; orchestrator must stop.')
} else {
    [Console]::Out.WriteLine('PASS: valid handoff.')
}
exit 0