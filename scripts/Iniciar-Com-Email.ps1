# Executar na raiz do projeto; não salva a chave em arquivo.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$loginSmtp = Read-Host 'Login SMTP da Brevo (SMTP & API)'
$remetenteNexus = Read-Host 'Endereco de remetente verificado na Brevo'
$chaveSmtp = Read-Host 'Chave SMTP da Brevo (entrada oculta)' -AsSecureString
$envNames = @('SMTP_PASSWORD','NEXUS_EMAIL_ENABLED','NEXUS_EMAIL_FROM','SMTP_HOST','SMTP_PORT','SMTP_USERNAME','SMTP_AUTH','SMTP_STARTTLS','NEXUS_FRONTEND_URL')
$previousEnv = @{}
foreach ($envName in $envNames) { $previousEnv[$envName] = [Environment]::GetEnvironmentVariable($envName, 'Process') }
$chavePointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($chaveSmtp)
try {
    $env:SMTP_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($chavePointer)
    $env:NEXUS_EMAIL_ENABLED = 'true'
    $env:NEXUS_EMAIL_FROM = $remetenteNexus
    $env:SMTP_HOST = 'smtp-relay.brevo.com'
    $env:SMTP_PORT = '587'
    $env:SMTP_USERNAME = $loginSmtp
    $env:SMTP_AUTH = 'true'
    $env:SMTP_STARTTLS = 'true'
    $env:NEXUS_FRONTEND_URL = 'http://localhost:5173'
    Push-Location $projectRoot
    try { & .\mvnw.cmd spring-boot:run }
    finally { Pop-Location }
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($chavePointer)
    foreach ($envName in $envNames) { [Environment]::SetEnvironmentVariable($envName, $previousEnv[$envName], 'Process') }
    $chaveSmtp.Dispose()
}
