const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 80;
const HOSTNAME = 'localhost';

// 获取文件的 MIME 类型
function getMimeType(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    const mimeTypes = {
        '.html': 'text/html; charset=utf-8',
        '.css': 'text/css; charset=utf-8',
        '.js': 'text/javascript; charset=utf-8',
        '.json': 'application/json; charset=utf-8',
        '.png': 'image/png',
        '.jpg': 'image/jpeg',
        '.jpeg': 'image/jpeg',
        '.gif': 'image/gif',
        '.svg': 'image/svg+xml',
        '.ico': 'image/x-icon',
        '.woff': 'font/woff',
        '.woff2': 'font/woff2',
        '.ttf': 'font/ttf',
        '.eot': 'application/vnd.ms-fontobject'
    };
    return mimeTypes[ext] || 'application/octet-stream';
}

// 创建 HTTP 服务器
const server = http.createServer((req, res) => {
    // 设置 CORS 头
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    // 处理 OPTIONS 请求
    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }

    // 获取请求的文件路径
    let filePath;
    if (req.url === '/' || req.url === '') {
        filePath = path.join(__dirname, '/register.html');
    } else {
        filePath = path.join(__dirname, req.url);
    }

    // 防止目录遍历攻击
    const realPath = path.resolve(filePath);
    const baseDir = path.resolve(__dirname);
    if (!realPath.startsWith(baseDir)) {
        res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('403 Forbidden');
        return;
    }

    // 读取文件
    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                res.writeHead(404, { 'Content-Type': 'text/html; charset=utf-8' });
                res.end('<h1>404 Not Found</h1><p>文件未找到：' + req.url + '</p>');
            } else {
                res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
                res.end('500 Internal Server Error');
            }
        } else {
            const mimeType = getMimeType(filePath);
            res.writeHead(200, { 'Content-Type': mimeType });
            res.end(content);
        }
    });
});

// 启动服务器
server.listen(PORT, HOSTNAME, () => {
    console.log(`
╔════════════════════════════════════════╗
║      GoalHub - 用户注册界面服务器       ║
╚════════════════════════════════════════╝

📍 服务器地址: http://${HOSTNAME}:${PORT}
🌐 打开浏览器访问上述地址即可

按 Ctrl+C 停止服务器
    `);
});

// 处理服务器错误
server.on('error', (err) => {
    if (err.code === 'EACCES') {
        console.error(`❌ 错误: 端口 ${PORT} 需要管理员权限访问`);
        console.error('💡 请以管理员身份运行此脚本，或使用其他端口 (如 8080)');
        process.exit(1);
    } else if (err.code === 'EADDRINUSE') {
        console.error(`❌ 错误: 端口 ${PORT} 已被占用`);
        console.error('💡 请关闭占用该端口的程序，或使用其他端口');
        process.exit(1);
    } else {
        console.error('❌ 服务器错误:', err.message);
        process.exit(1);
    }
});
