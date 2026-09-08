const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');

const PORT = Number(process.env.PORT || 4173);
const ROOT = __dirname;
const reports = [];
const mime = {'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.json':'application/json; charset=utf-8'};

function send(res, status, body, type='application/json; charset=utf-8') {
  res.writeHead(status, {'Content-Type': type, 'Cache-Control': 'no-store', 'Access-Control-Allow-Origin': '*'});
  res.end(typeof body === 'string' ? body : JSON.stringify(body));
}
function readBody(req) { return new Promise((resolve, reject) => { let body=''; req.on('data', chunk => { body += chunk; if (body.length > 100000) req.destroy(); }); req.on('end', () => resolve(body)); req.on('error', reject); }); }
function serveFile(req, res) {
  const requested = req.url === '/' ? '/index.html' : req.url.split('?')[0];
  const file = path.normalize(path.join(ROOT, requested));
  if (!file.startsWith(ROOT)) return send(res, 403, {error:'forbidden'});
  fs.readFile(file, (err, data) => { if (err) return send(res, 404, {error:'not found'}); send(res, 200, data, mime[path.extname(file)] || 'application/octet-stream'); });
}
const server = http.createServer(async (req, res) => {
  if (req.method === 'OPTIONS') { res.writeHead(204, {'Access-Control-Allow-Origin':'*','Access-Control-Allow-Methods':'GET,POST,OPTIONS','Access-Control-Allow-Headers':'Content-Type'}); return res.end(); }
  if (req.url === '/api/state' && req.method === 'GET') return send(res, 200, {reports});
  if (req.url === '/api/reports' && req.method === 'POST') {
    try {
      const report = JSON.parse(await readBody(req));
      if (!report || !report.text || !report.id) return send(res, 400, {error:'report id and text are required'});
      if (!reports.some(existing => existing.id === report.id)) reports.push(report);
      return send(res, 201, report);
    } catch { return send(res, 400, {error:'invalid JSON'}); }
  }
  if (req.method === 'GET') return serveFile(req, res);
  return send(res, 405, {error:'method not allowed'});
});
server.listen(PORT, '0.0.0.0', () => {
  const interfaces = Object.values(os.networkInterfaces()).flat().filter(Boolean).filter(info => info.family === 'IPv4' && !info.internal);
  console.log(`Humsafar room running at http://localhost:${PORT}`);
  interfaces.forEach(info => console.log(`Share with nearby devices: http://${info.address}:${PORT}`));
});
