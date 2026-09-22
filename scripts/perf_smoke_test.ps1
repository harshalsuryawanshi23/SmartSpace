$apiUrl = "http://localhost:8080/api"

# Configuration
$iterations = 50

Write-Host "Starting Performance Smoke Tests..."
Write-Host "-------------------------------------"

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body
    )
    
    $times = @()
    for ($i = 0; $i -lt $iterations; $i++) {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            if ($Method -eq "GET") {
                $response = Invoke-WebRequest -Uri $Url -Method GET -UseBasicParsing -ErrorAction SilentlyContinue
            } else {
                $response = Invoke-WebRequest -Uri $Url -Method POST -Body $Body -ContentType "application/json" -UseBasicParsing -ErrorAction SilentlyContinue
            }
        } catch {
            # Ignore errors for latency testing purposes if it's 401/403/404, we just want roundtrip time
        }
        $stopwatch.Stop()
        $times += $stopwatch.ElapsedMilliseconds
    }
    
    $times = $times | Sort-Object
    $p95Index = [Math]::Floor($times.Length * 0.95)
    $p95 = $times[$p95Index]
    
    Write-Host "$Name - p95 Latency: $p95 ms (over $iterations iterations)"
}

# Run tests
Test-Endpoint -Name "Search Halls (GET)" -Method "GET" -Url "$apiUrl/halls/search?lat=18.5204&lng=73.8567&radius=10"
Test-Endpoint -Name "Entry Scan (POST)" -Method "POST" -Url "$apiUrl/entry/scan" -Body '{"qrPayload":"mock-payload"}'

Write-Host "-------------------------------------"
Write-Host "Performance Smoke Tests Completed."
