import { createServer } from 'node:http';
import { createHash, randomBytes } from 'node:crypto';
import { readFile, mkdir, writeFile } from 'node:fs/promises';
import { homedir } from 'node:os';
import { join, resolve } from 'node:path';

// Ejecutar desde la PC del propietario, con el JSON DESDE FUERA de GitHub.
// No envía credenciales a ningún servidor salvo el endpoint OAuth oficial de Google.
const [jsonPath, senderEmail] = process.argv.slice(2);
if (!jsonPath || !senderEmail) {
  console.error('Uso: node tools/autorizar-gmail.mjs "C:\\ruta\\client_secret.json" "remitente@gmail.com"');
  process.exit(1);
}
if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(senderEmail)) {
  console.error('Dirección del remitente inválida.');
  process.exit(1);
}
const loaded = JSON.parse(await readFile(resolve(jsonPath), 'utf8'));
const client = loaded.installed;
if (!client?.client_id || !client?.client_secret) {
  throw new Error('El JSON no corresponde a un cliente OAuth de tipo Escritorio (installed).');
}

const verifier = randomBytes(48).toString('base64url');
const challenge = createHash('sha256').update(verifier).digest('base64url');
const state = randomBytes(24).toString('base64url');
const timeoutMs = 300_000;
let used = false;
let timeout;
let server;
const outcome = new Promise((resolveFlow, rejectFlow) => {
  server = createServer(async (req, res) => {
    const requestUrl = new URL(req.url ?? '/', 'http://127.0.0.1');
    if (requestUrl.pathname !== '/oauth2callback') {
      res.writeHead(404); res.end('No encontrado'); return;
    }
    if (used || requestUrl.searchParams.get('state') !== state) {
      res.writeHead(400); res.end('Estado de autorización inválido.'); return;
    }
    used = true;
    const code = requestUrl.searchParams.get('code');
    const error = requestUrl.searchParams.get('error');
    if (error || !code) {
      res.writeHead(400); res.end('Autorización cancelada.');
      rejectFlow(new Error('Google no autorizó la operación: ' + (error ?? 'sin código')));
      return;
    }
    try {
      const form = new URLSearchParams({
        code, client_id: client.client_id, client_secret: client.client_secret,
        redirect_uri: `http://127.0.0.1:${server.address().port}/oauth2callback`,
        grant_type: 'authorization_code', code_verifier: verifier
      });
      const response = await fetch('https://oauth2.googleapis.com/token', {
        method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: form,
        signal: AbortSignal.timeout(20000)
      });
      const data = await response.json();
      if (!response.ok || !data.refresh_token) {
        throw new Error(response.ok
          ? 'Google no devolvió refresh_token. Reautoriza con prompt=consent o revoca el permiso previo.'
          : `Google OAuth respondió HTTP ${response.status}: ${data.error ?? 'sin detalle'}`);
      }
      const location = join(homedir(), '.novastore-private');
      await mkdir(location, { recursive: true, mode: 0o700 });
      const secretsFile = join(location, 'railway-gmail.env');
      const env = [
        'GMAIL_CLIENT_ID=' + client.client_id,
        'GMAIL_CLIENT_SECRET=' + client.client_secret,
        'GMAIL_REFRESH_TOKEN=' + data.refresh_token,
        'MAIL_FROM=' + senderEmail,
        'EMAIL_PROVIDER=gmail-api',
        'AUTH_REQUIRE_EMAIL_VERIFICATION=true',
        ''
      ].join('\n');
      await writeFile(secretsFile, env, { mode: 0o600 });
      res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('NovaStore: Gmail autorizado. Regresa a Visual Studio Code. Puedes cerrar esta pestaña.');
      resolveFlow(secretsFile);
    } catch (e) {
      res.writeHead(500); res.end('No fue posible completar OAuth. Revisa la terminal.');
      rejectFlow(e);
    }
  });
  server.on('error', rejectFlow);
});
server.listen(0, '127.0.0.1');
await new Promise((resolveListen, rejectListen) => {
  if (server.listening) return resolveListen();
  server.once('listening', resolveListen);
  server.once('error', rejectListen);
});
const redirectUri = `http://127.0.0.1:${server.address().port}/oauth2callback`;
const parameters = new URLSearchParams({
  response_type: 'code', client_id: client.client_id, redirect_uri: redirectUri,
  scope: 'https://www.googleapis.com/auth/gmail.send', access_type: 'offline',
  prompt: 'consent', state, code_challenge: challenge, code_challenge_method: 'S256'
});
console.log('\nABRE ESTA DIRECCIÓN EN EL NAVEGADOR DE ESTA MISMA COMPUTADORA:\n');
console.log('https://accounts.google.com/o/oauth2/v2/auth?' + parameters.toString());
console.log('\nSelecciona el Gmail remitente, autoriza SOLO gmail.send y regresa aquí.');
timeout = setTimeout(() => { console.error('Se agotó el tiempo de autorización (5 minutos).'); server.close(); process.exitCode = 1; }, timeoutMs);
try {
  const location = await outcome;
  console.log('\nAutorización completada. El archivo PRIVADO está en:\n' + location);
  console.log('No lo subas a GitHub ni compartas su contenido. Pasa esas variables a Railway manualmente.');
} catch (e) {
  console.error('Error OAuth:', e.message);
  process.exitCode = 1;
} finally {
  clearTimeout(timeout);
  server.close();
}
