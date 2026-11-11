import process from 'node:process';
const [apiRaw, originRaw] = process.argv.slice(2);
if (!apiRaw || !originRaw) {
  console.error('Uso: node scripts/comprobar-despliegue.mjs https://api.up.railway.app https://web.vercel.app');
  process.exit(2);
}
const api = apiRaw.replace(/\/+$/, '');
const origin = originRaw.replace(/\/+$/, '');
if (!/^https:\/\//.test(api) || !/^https:\/\//.test(origin)) {
  console.error('Las dos URL deben usar HTTPS.'); process.exit(2);
}
const cases = [
  { title: 'API health y PostgreSQL', url: `${api}/api/health`, method: 'GET', expected: 200 },
  { title: 'Catálogo, requiere sesión', url: `${api}/api/products`, method: 'GET', expected: 401 },
  { title: 'Preflight registro desde Vercel', url: `${api}/api/auth/register`, method: 'OPTIONS', expected: 200,
    headers: { Origin: origin, 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers':'content-type' } }
];
let errors=0;
for (const test of cases) {
  try {
    const res=await fetch(test.url,{method:test.method,headers:test.headers||{},signal:AbortSignal.timeout(12000)});
    const corsOk=test.method!=='OPTIONS'||res.headers.get('access-control-allow-origin')===origin;
    const statusOk=res.status===test.expected || (test.title.includes('Catálogo') && res.status===200);
    const pass=statusOk && corsOk;
    console.log(`${pass?'OK':'ERROR'} ${test.title}: HTTP ${res.status}; CORS: ${corsOk?'OK':'FALLA'}`);
    if (!pass) errors++;
  } catch(err) { errors++;console.error(`ERROR ${test.title}: ${err.message}`); }
}
process.exitCode=errors?1:0;
