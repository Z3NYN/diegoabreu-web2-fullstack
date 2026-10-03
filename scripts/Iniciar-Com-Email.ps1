param([switch]$Configurar, [switch]$SomenteConfigurar)

# A chave permanece protegida pelo Windows e nunca entra no Git.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$configDirectory = Join-Path $projectRoot '.nexus'
$configFile = Join-Path $configDirectory 'smtp.clixml'
if ([Environment]::OSVersion.Platform -ne [PlatformID]::Win32NT) {
    throw 'Este script protege a chave com o Windows. Em outros sistemas, configure as variaveis de ambiente descritas no README.'
}
if ($Configurar -or !(Test-Path -LiteralPath $configFile)) {
    $loginSmtp = (Read-Host 'Login SMTP da Brevo (SMTP & API)').Trim()
    $remetenteNexus = (Read-Host 'Endereco de remetente verificado na Brevo').Trim()
    if ([string]::IsNullOrWhiteSpace($loginSmtp) -or $remetenteNexus -notmatch '^[^\s@]+@[^\s@]+\.[^\s@]{2,}$') {
        throw 'Informe o login SMTP e um endereco valido de remetente verificado.'
    }
    $chaveSmtp = Read-Host 'Chave SMTP da Brevo (entrada oculta)' -AsSecureString
    try {
        if ($chaveSmtp.Length -eq 0) { throw 'A chave SMTP nao pode estar vazia.' }
        $configNexus = [pscustomobject]@{
            Remetente = $remetenteNexus
            Credencial = [pscredential]::new($loginSmtp, $chaveSmtp)
        }
        New-Item -ItemType Directory -Path $configDirectory -Force | Out-Null
        $configNexus | Export-Clixml -LiteralPath $configFile
        Write-Host 'Configuracao salva localmente. A chave esta criptografada para seu usuario Windows.'
    } finally { $chaveSmtp.Dispose() }
}
if ($SomenteConfigurar) { return }
$configNexus = Import-Clixml -LiteralPath $configFile
$envNames = @('SMTP_PASSWORD','NEXUS_EMAIL_ENABLED','NEXUS_EMAIL_FROM','SMTP_HOST','SMTP_PORT','SMTP_USERNAME','SMTP_AUTH','SMTP_STARTTLS','NEXUS_FRONTEND_URL')
$previousEnv = @{}
foreach ($envName in $envNames) { $previousEnv[$envName] = [Environment]::GetEnvironmentVariable($envName, 'Process') }
$chavePointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($configNexus.Credencial.Password)
try {
    $env:SMTP_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($chavePointer)
    $env:NEXUS_EMAIL_ENABLED = 'true'
    $env:NEXUS_EMAIL_FROM = $configNexus.Remetente
    $env:SMTP_HOST = 'smtp-relay.brevo.com'
    $env:SMTP_PORT = '587'
    $env:SMTP_USERNAME = $configNexus.Credencial.UserName
    $env:SMTP_AUTH = 'true'
    $env:SMTP_STARTTLS = 'true'
    if ([string]::IsNullOrWhiteSpace($env:NEXUS_FRONTEND_URL)) { $env:NEXUS_FRONTEND_URL = 'http://localhost:5173' }
    Push-Location $projectRoot
    try { & .\mvnw.cmd spring-boot:run; if ($LASTEXITCODE -ne 0) { throw 'O backend nao iniciou corretamente. Confira a porta 8080 e a configuracao SMTP.' } }
    finally { Pop-Location }
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($chavePointer)
    foreach ($envName in $envNames) { [Environment]::SetEnvironmentVariable($envName, $previousEnv[$envName], 'Process') }
    $configNexus.Credencial.Password.Dispose()
}
