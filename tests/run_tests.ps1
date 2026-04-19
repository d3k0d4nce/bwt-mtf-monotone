$files = @("bib", "book1", "book2", "geo", "news", "obj1", "obj2", "paper1", "paper2", "pic", "progc", "progl", "progp", "trans")

New-Item -ItemType Directory -Force -Path "compressed" | Out-Null
New-Item -ItemType Directory -Force -Path "decoded" | Out-Null

Write-Host "=== TESTING CALGARY CORPUS ===" -ForegroundColor Cyan
Write-Host ""

$totalOriginal = 0
$totalCompressed = 0
$results = @()

foreach ($file in $files) {
    Write-Host "Processing: $file" -ForegroundColor Yellow

    java -cp "..\target\classes" ru.kishko.Main encoder $file "compressed\$file.compressed"

    $origSize = (Get-Item $file).Length
    $compSize = (Get-Item "compressed\$file.compressed").Length
    $bitsPerChar = [math]::Round(($compSize * 8) / $origSize, 2)

    java -cp "..\target\classes" ru.kishko.Main decoder "compressed\$file.compressed" "decoded\$file.decoded"

    $originalContent = Get-Content $file -Raw
    $decodedContent = Get-Content "decoded\$file.decoded" -Raw

    if ($originalContent -eq $decodedContent) {
        Write-Host "  [OK] Original: $origSize bytes | Compressed: $compSize bytes | Bits/char: $bitsPerChar" -ForegroundColor Green
        $results += [PSCustomObject]@{
            File = $file
            Original = $origSize
            Compressed = $compSize
            BitsPerChar = $bitsPerChar
            Status = "OK"
        }
    } else {
        Write-Host "  [FAIL] $file mismatch!" -ForegroundColor Red
        $results += [PSCustomObject]@{
            File = $file
            Original = $origSize
            Compressed = $compSize
            BitsPerChar = $bitsPerChar
            Status = "FAIL"
        }
    }

    $totalOriginal += $origSize
    $totalCompressed += $compSize
}

Write-Host ""
Write-Host "=== SUMMARY ===" -ForegroundColor Cyan
$results | Format-Table File, Original, Compressed, BitsPerChar, Status -AutoSize

$totalBits = [math]::Round(($totalCompressed * 8) / $totalOriginal, 2)
Write-Host "Total original size: $totalOriginal bytes"
Write-Host "Total compressed size: $totalCompressed bytes"
Write-Host "Average bits/char: $totalBits"
Write-Host ""
Write-Host "Testing completed." -ForegroundColor Green