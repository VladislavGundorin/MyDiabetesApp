# MyDiabetesApp

## Запуск на другом ПК

1. Клонируем репозиторий:
   \`\`\`bash
   git clone <URL>
   cd MyDiabetesApp
   \`\`\`

2. Копируем в корень проекта (не попадают в репо, перечислены в .gitignore):
   - client_secret.json  
   - google-services.json

3. Собираем:
   \`\`\`bash
   ./gradlew assembleDebug
   \`\`\`
