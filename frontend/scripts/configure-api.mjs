import fs from 'node:fs';
import path from 'node:path';
const raw = (process.env.API_BASE_URL ?? '').trim().replace(/\/+$/, '');
if (process.env.VERCEL && !raw) {
  console.error('[NovaStore] Falta API_BASE_URL en las variables de Vercel.');
  process.exit(1);
}
if (raw) {
  const url = new URL(raw);
  if (url.protocol !== 'https:' && !['localhost','127.0.0.1'].includes(url.hostname)) {
    console.error('[NovaStore] API_BASE_URL debe utilizar HTTPS en producción.');
    process.exit(1);
  }
}
const target = path.join(process.cwd(), 'src', 'app', 'core', 'config', 'api-url.ts');
fs.mkdirSync(path.dirname(target), { recursive: true });
fs.writeFileSync(target, `// Archivo generado; no guardar claves aquí.\nexport const API_BASE_URL = ${JSON.stringify(raw)};\n`);
console.log(`[NovaStore] API pública configurada: ${raw || '(proxy local /api)'}`);
