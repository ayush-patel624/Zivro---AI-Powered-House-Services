# Zivro — online deployment helper (Vercel + Render + MySQL)
# Run from repo root: powershell -ExecutionPolicy Bypass -File scripts/deploy-online.ps1

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path (Split-Path $MyInvocation.MyCommand.Path -Parent) -Parent
Set-Location $repoRoot

Write-Host ""
Write-Host "=== ZIVRO ONLINE DEPLOY ===" -ForegroundColor Cyan
Write-Host "Repo: $repoRoot"
Write-Host ""

function Require-Command($name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    throw "Missing '$name'. Install it first (see README section 8)."
  }
}

Require-Command git

Write-Host "Step 1 — GitHub" -ForegroundColor Yellow
Write-Host "  Push this repo to GitHub if you have not already:"
Write-Host "    gh auth login"
Write-Host "    gh repo create zivro --public --source=. --remote=origin --push"
Write-Host ""

Write-Host "Step 2 — Cloud MySQL (pick one provider)" -ForegroundColor Yellow
Write-Host "  Railway, Aiven, PlanetScale, or Amazon RDS"
Write-Host "  Create database named 'zivro' and copy JDBC URL, user, password."
Write-Host "  Example URL:"
Write-Host "    jdbc:mysql://HOST:3306/zivro?useSSL=true&serverTimezone=UTC"
Write-Host ""

Write-Host "Step 3 — Render API (backend)" -ForegroundColor Yellow
Write-Host "  1. https://render.com → New → Blueprint → connect GitHub repo"
Write-Host "  2. Or New → Web Service → root: backend"
Write-Host "  3. Build:  mvn -B package -DskipTests"
Write-Host "  4. Start:  java -jar target/zivro-api-0.1.0-SNAPSHOT.jar"
Write-Host "  5. Set env vars:"
Write-Host "       ZIVRO_DB_URL, ZIVRO_DB_USER, ZIVRO_DB_PASSWORD"
Write-Host "       ZIVRO_JWT_SECRET (32+ random chars)"
Write-Host "       ZIVRO_BOOTSTRAP_ADMIN_EMAIL=admin@zivro.com"
Write-Host "       ZIVRO_BOOTSTRAP_ADMIN_PASSWORD=<strong password>"
Write-Host "       ZIVRO_CORS_ORIGINS=https://YOUR-APP.vercel.app  (update after Vercel)"
Write-Host "  6. Health check: /api/health"
Write-Host ""

Write-Host "Step 4 — Vercel frontend" -ForegroundColor Yellow
Write-Host "  1. https://vercel.com → Add Project → import GitHub repo"
Write-Host "  2. Root Directory: frontend"
Write-Host "  3. Framework: Vite | Build: npm run build | Output: dist"
Write-Host "  4. Environment: VITE_API_URL=https://YOUR-API.onrender.com"
Write-Host "  5. Deploy → copy site URL"
Write-Host ""

Write-Host "Step 5 — Connect CORS" -ForegroundColor Yellow
Write-Host "  On Render, set ZIVRO_CORS_ORIGINS to your Vercel URL and redeploy API."
Write-Host ""

Write-Host "Step 6 — Verify" -ForegroundColor Yellow
Write-Host "  curl https://YOUR-API.onrender.com/api/health"
Write-Host "  Open https://YOUR-APP.vercel.app → Services → Login → Book"
Write-Host ""

if (Get-Command gh -ErrorAction SilentlyContinue) {
  Write-Host "GitHub CLI: installed" -ForegroundColor Green
  gh auth status 2>$null
} else {
  Write-Host "GitHub CLI: not installed — winget install GitHub.cli" -ForegroundColor DarkYellow
}

if (Get-Command vercel -ErrorAction SilentlyContinue) {
  Write-Host "Vercel CLI: installed" -ForegroundColor Green
} else {
  Write-Host "Vercel CLI: not installed — npm install -g vercel" -ForegroundColor DarkYellow
}

Write-Host ""
Write-Host "Optional: Cloudinary for persistent images on Render (local disk is ephemeral)." -ForegroundColor DarkGray
Write-Host ""
