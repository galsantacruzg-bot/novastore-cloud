import fs from 'node:fs';
const required=['frontend/package.json','frontend/angular.json','frontend/vercel.json','frontend/scripts/configure-api.mjs','backend/pom.xml','backend/Dockerfile','backend/railway.json','backend/src/main/resources/application.properties','docs/GUIA_DESPLIEGUE.md'];
let errors=0;
for (const file of required) { const exists=fs.existsSync(file); console.log(`${exists?'OK':'FALTA'} ${file}`); if (!exists) errors++; }
process.exitCode=errors?1:0;
