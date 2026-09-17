$url = "http://127.0.0.1:8000/api/v1/analyze/file"
$file = "d:\ai-startup-pilot\test_data\real_saas_startup_kpis.csv"

# Send POST request with multipart/form-data
$response = Invoke-RestMethod -Uri $url -Method Post -Form @{
    file = Get-Item -Path $file
}

# Convert response object to JSON string
$jsonString = $response | ConvertTo-Json -Depth 10

# Create the markdown output
$output = @"
# 📊 AI Analysis Results: Real SaaS Startup

**Health Score**: $($response.healthScore)/100
**Health Status**: `$($response.healthStatus)`

### AI Prediction
> $($response.prediction)

### Actionable Insights
"@

foreach ($insight in $response.insights) {
    $output += "`n- $insight"
}

$output += "`n`n### KPI Trends"
foreach ($trend in $response.kpiTrends) {
    $output += "`n- **$($trend.metric)**: $($trend.direction) ($($trend.change_pct)%)"
}

# Save to artifact file
$output | Out-File -FilePath "d:\ai-startup-pilot\analysis_results.md" -Encoding utf8
Write-Host "Success! Results written to d:\ai-startup-pilot\analysis_results.md"
