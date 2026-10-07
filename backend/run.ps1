Get-Content ..\.env | ForEach-Object {
    if ($_ -match '^(.*?)=(.*)$') {
        if ($matches[1] -eq 'DB_URL') {
            Set-Item -Path "env:\$($matches[1])" -Value "jdbc:mysql://localhost:3306/smartspace?useUnicode=true&characterEncoding=utf8&connectionTimeZone=UTC"
        } else {
            Set-Item -Path "env:\$($matches[1])" -Value $matches[2]
        }
    }
}
cmd /c "mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
