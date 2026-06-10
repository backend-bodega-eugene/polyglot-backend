const http = require('node:http');
const path = require('node:path');
const fs = require('node:fs');
const { exec } = require('node:child_process');
const os = require('node:os');

const root = __dirname;
const port = Number(process.env.PORT) || 5173;
const host = process.env.HOST || '0.0.0.0';
const startPage = process.argv[2] || 'login.html';
const mimeTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.jpg': 'image/jpeg',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.webp': 'image/webp',
};

function send(res, status, content, type = 'text/plain; charset=utf-8') {
  res.writeHead(status, { 'Content-Type': type });
  res.end(content);
}

const server = http.createServer((req, res) => {
  const requestPath = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  const relativePath = requestPath === '/' ? startPage : requestPath.replace(/^\/+/, '');
  const filePath = path.resolve(root, relativePath);

  if (filePath !== root && !filePath.startsWith(`${root}${path.sep}`)) {
    send(res, 403, 'Forbidden');
    return;
  }

  fs.stat(filePath, (error, stat) => {
    if (error || !stat.isFile()) {
      send(res, 404, 'Not found');
      return;
    }

    res.writeHead(200, {
      'Content-Type': mimeTypes[path.extname(filePath).toLowerCase()] || 'application/octet-stream',
    });
    fs.createReadStream(filePath).pipe(res);
  });
});

function getLanAddresses() {
  return Object.values(os.networkInterfaces())
    .flat()
    .filter((item) => item && item.family === 'IPv4' && !item.internal)
    .map((item) => item.address);
}

server.listen(port, host, () => {
  const localUrl = `http://localhost:${port}/${startPage}`;
  const lanUrls = getLanAddresses().map((address) => `http://${address}:${port}/${startPage}`);
  console.log(`Static site is running at ${localUrl}`);
  lanUrls.forEach((url) => console.log(`LAN: ${url}`));
  console.log('Press Ctrl+C to stop.');

  if (process.env.OPEN_BROWSER === '1') {
    exec(`start "" "${localUrl}"`);
  }
});
