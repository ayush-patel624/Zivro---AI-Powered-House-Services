# Zivro deployment smoke test
$base = "http://localhost:8081"
$frontend = "http://127.0.0.1:5173"
$preview = "http://127.0.0.1:4173"
$pass = 0
$fail = 0
$results = @()

function Test-Feature($name, $scriptBlock) {
  try {
    $ok = & $scriptBlock
    if ($ok) {
      $script:pass++
      $script:results += [pscustomobject]@{ Feature = $name; Status = "PASS"; Detail = $ok }
    } else {
      $script:fail++
      $script:results += [pscustomobject]@{ Feature = $name; Status = "FAIL"; Detail = "Returned false" }
    }
  } catch {
    $script:fail++
    $script:results += [pscustomobject]@{ Feature = $name; Status = "FAIL"; Detail = $_.Exception.Message }
  }
}

Test-Feature "API health" {
  $r = Invoke-RestMethod "$base/api/health"
  $r.status -eq "UP"
}

Test-Feature "Service catalog (15+ with icons)" {
  $s = Invoke-RestMethod "$base/api/services"
  ($s.Count -ge 15) -and ($s[0].iconKey -ne $null)
  "$($s.Count) services"
}

Test-Feature "Service quote" {
  $q = Invoke-RestMethod "$base/api/services/1/quote?urgency=NORMAL"
  ($q.quotedPrice -gt 0) -and ($q.currency -eq "INR")
  "INR $($q.quotedPrice)"
}

Test-Feature "AI image analysis" {
  $img = "d:\zivro\d-zivro\frontend\public\icons\icon-512x512.png"
  $r = curl.exe -s -F "image=@$img" "$base/api/ai/analyze-image?serviceIconKey=utensils" | ConvertFrom-Json
  ($r.detectedType -eq "UTENSILS") -and ($r.estimatedMinutes -gt 0)
  "$($r.label) · $($r.quantity) $($r.quantityUnit) · $($r.estimatedMinutes) min"
}

Test-Feature "Payments public config" {
  $p = Invoke-RestMethod "$base/api/payments/public-config"
  $true
  "razorpayEnabled=$($p.razorpayEnabled)"
}

Test-Feature "User register + login" {
  $email = "smoke-$(Get-Date -Format 'HHmmss')@zivro.test"
  $reg = @{ name = "Smoke User"; email = $email; password = "TestPass123!"; role = "USER" } | ConvertTo-Json
  $regRes = Invoke-RestMethod -Method Post -Uri "$base/api/auth/register" -Body $reg -ContentType "application/json"
  $login = @{ email = $email; password = "TestPass123!" } | ConvertTo-Json
  $loginRes = Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -Body $login -ContentType "application/json"
  $script:token = $loginRes.accessToken
  ($null -ne $script:token -and $script:token.Length -gt 20)
  $email
}

Test-Feature "Create booking (location + image + AI)" {
  $headers = @{ Authorization = "Bearer $token" }
  $bookingJson = '{"serviceId":5,"urgencyLevel":"NORMAL","location":{"address":"MG Road Bengaluru","label":"Other location","latitude":12.9716,"longitude":77.5946}}'
  Set-Content -Path "$env:TEMP\zivro-booking.json" -Value $bookingJson -NoNewline
  $imgPath = "d:\zivro\d-zivro\frontend\public\icons\icon-192x192.png"
  $r = curl.exe -s -H "Authorization: Bearer $token" -F "booking=@$env:TEMP\zivro-booking.json;type=application/json" -F "referenceImage=@$imgPath;type=image/png" "$base/api/bookings" | ConvertFrom-Json
  $script:bookingId = $r.id
  Start-Sleep -Seconds 1
  $detail = Invoke-RestMethod -Uri "$base/api/bookings/$($r.id)" -Headers $headers
  ($r.id -gt 0) -and ($r.serviceAddress -ne $null) -and ($detail.mapsUrl -like "https://www.google.com/maps/*")
  "booking #$($r.id) · $($r.serviceAddress) · maps OK"
}

Test-Feature "Nearby workers (Rapido-style)" {
  $headers = @{ Authorization = "Bearer $token" }
  $w = Invoke-RestMethod -Uri "$base/api/bookings/$bookingId/nearby-workers" -Headers $headers
  ($w.Count -ge 1) -and ($w[0].distanceKm -gt 0)
  "$($w.Count) workers · nearest $($w[0].distanceKm) km"
}

Test-Feature "My bookings list" {
  $headers = @{ Authorization = "Bearer $token" }
  $b = Invoke-RestMethod -Uri "$base/api/bookings/my" -Headers $headers
  ($b | Where-Object { $_.id -eq $bookingId }).Count -eq 1
  "found booking #$bookingId"
}

Test-Feature "Google Maps URL on booking" {
  $headers = @{ Authorization = "Bearer $token" }
  $b = Invoke-RestMethod -Uri "$base/api/bookings/$bookingId" -Headers $headers
  $b.mapsUrl -like "https://www.google.com/maps/*"
  $b.mapsUrl
}

Test-Feature "Frontend dev server" {
  $r = Invoke-WebRequest -Uri $frontend -UseBasicParsing -TimeoutSec 5
  $r.StatusCode -eq 200
  $frontend
}

Test-Feature "Frontend production preview" {
  $r = Invoke-WebRequest -Uri $preview -UseBasicParsing -TimeoutSec 5
  $r.StatusCode -eq 200
  $preview
}

Test-Feature "PWA manifest" {
  $m = Invoke-RestMethod -Uri "$preview/manifest.webmanifest"
  ($m.display -eq "standalone") -and ($m.icons.Count -ge 1)
  "$($m.icons.Count) icons · standalone"
}

Write-Output ""
Write-Output "=== ZIVRO TEST RESULTS ==="
$results | Format-Table -AutoSize
Write-Output "PASSED: $pass | FAILED: $fail | TOTAL: $($pass + $fail)"
