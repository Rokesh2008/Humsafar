const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');

const PORT = Number(process.env.PORT || 4173);
const ROOT = __dirname;
const DATA_DIR = path.join(ROOT, 'data');
const DATA_FILE = path.join(DATA_DIR, 'humsafar-room.json');
const mime = {'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.json':'application/json; charset=utf-8','.webmanifest':'application/manifest+json; charset=utf-8'};

fs.mkdirSync(DATA_DIR, {recursive:true});
const defaultRoom = () => ({roomId:'city-fest-main-grounds', roomName:'City Fest / Main Grounds', createdAt:new Date().toISOString(), reports:[]});
let room;
function loadRoom() { try { return {...defaultRoom(), ...JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'))}; } catch { const room=defaultRoom(); saveRoom(room); return room; } }
room = loadRoom();
function saveRoom(next=room) { room=next; fs.writeFileSync(DATA_FILE, JSON.stringify(room, null, 2)); }
function send(res, status, body, type='application/json; charset=utf-8') { res.writeHead(status, {'Content-Type':type,'Cache-Control':'no-store','Access-Control-Allow-Origin':'*','Access-Control-Allow-Methods':'GET,POST,DELETE,OPTIONS','Access-Control-Allow-Headers':'Content-Type'}); res.end(typeof body === 'string' ? body : JSON.stringify(body)); }
function readBody(req) { return new Promise((resolve,reject)=>{let body='';req.on('data',chunk=>{body+=chunk;if(body.length>100000) req.destroy();});req.on('end',()=>resolve(body));req.on('error',reject)}); }
function normalizeReport(report) { return {...report, timestamp:report.timestamp || new Date().toISOString(), language:report.language || 'en', reporter:report.reporter || 'Anonymous'}; }
function csvEscape(value) { const string=String(value ?? ''); return /[",\n]/.test(string) ? `"${string.replace(/"/g,'""')}"` : string; }
function analytics() {
  const reports=room.reports, byCategory={}, byLanguage={}, byReporter={};
  reports.forEach(r=>{byCategory[r.category||'other']=(byCategory[r.category||'other']||0)+1;byLanguage[r.language||'en']=(byLanguage[r.language||'en']||0)+1;byReporter[r.reporter||'Anonymous']=(byReporter[r.reporter||'Anonymous']||0)+1});
  const timestamps=reports.map(r=>Date.parse(r.timestamp)).filter(Number.isFinite);
  return {roomId:room.roomId,roomName:room.roomName,createdAt:room.createdAt,totalReports:reports.length,uniqueReporters:new Set(reports.map(r=>r.reporter)).size,languageCount:new Set(reports.map(r=>r.language)).size,safetyReports:reports.filter(r=>r.category==='safety'||r.urgency).length,contradictions:reports.filter(r=>r.contradiction).length,firstReport:timestamps.length?new Date(Math.min(...timestamps)).toISOString():null,lastReport:timestamps.length?new Date(Math.max(...timestamps)).toISOString():null,byCategory,byLanguage,byReporter};
}
function serveFile(req,res) { const requested=req.url==='/'?'/index.html':req.url.split('?')[0];const file=path.normalize(path.join(ROOT,requested));if(!file.startsWith(ROOT))return send(res,403,{error:'forbidden'});fs.readFile(file,(err,data)=>{if(err)return send(res,404,{error:'not found'});send(res,200,data,mime[path.extname(file)]||'application/octet-stream')}); }
const server=http.createServer(async(req,res)=>{
  if(req.method==='OPTIONS'){res.writeHead(204,{'Access-Control-Allow-Origin':'*','Access-Control-Allow-Methods':'GET,POST,DELETE,OPTIONS','Access-Control-Allow-Headers':'Content-Type'});return res.end()}
  if(req.url==='/api/state'&&req.method==='GET')return send(res,200,{room:{roomId:room.roomId,roomName:room.roomName,createdAt:room.createdAt},reports:room.reports});
  if(req.url==='/api/analytics'&&req.method==='GET')return send(res,200,analytics());
  if(req.url==='/api/export.json'&&req.method==='GET')return send(res,200,{room,analytics:analytics()});
  if(req.url==='/api/export.csv'&&req.method==='GET'){const columns=['id','timestamp','reporter','language','category','canonical','text','key','confidence','contradiction','urgency'];const lines=[columns.join(','),...room.reports.map(r=>columns.map(c=>csvEscape(r[c])).join(','))];return send(res,200,lines.join('\n'),'text/csv; charset=utf-8');}
  if(req.url==='/api/reports'&&req.method==='POST'){try{const report=normalizeReport(JSON.parse(await readBody(req)));if(!report.text||!report.id)return send(res,400,{error:'report id and text are required'});if(!room.reports.some(existing=>existing.id===report.id)){room.reports.push(report);saveRoom()}return send(res,201,report)}catch{return send(res,400,{error:'invalid JSON'})}}
  if(req.url==='/api/state'&&req.method==='DELETE'){saveRoom(defaultRoom());return send(res,200,{ok:true})}
  if(req.method==='GET')return serveFile(req,res);
  return send(res,405,{error:'method not allowed'});
});
server.listen(PORT,'0.0.0.0',()=>{const interfaces=Object.values(os.networkInterfaces()).flat().filter(Boolean).filter(info=>info.family==='IPv4'&&!info.internal);console.log(`Humsafar room running at http://localhost:${PORT}`);console.log(`Persistent room file: ${DATA_FILE}`);interfaces.forEach(info=>console.log(`Share with nearby devices: http://${info.address}:${PORT}`))});
