'use strict';
const $=id=>document.getElementById(id);
$('download-form').addEventListener('submit',()=>{
 $('status').textContent='已提交下载请求，口令正确后浏览器将开始下载。';
 $('download').disabled=true;
 setTimeout(()=>{$('download').disabled=false;},2000);
});
$('download-result').addEventListener('load',()=>{
 try{const text=$('download-result').contentDocument.body.textContent;if(!text.trim())return;const data=JSON.parse(text);$('status').textContent=data.message||'下载失败，请稍后重试';$('download').disabled=false;}catch{$('status').textContent='下载失败，请刷新页面后重试';$('download').disabled=false;}
});
(async()=>{
 try{const r=await fetch('/api/app-download/info',{cache:'no-store'});const b=await r.json();if(!r.ok||b.code!==0)throw Error(b.message||'暂时无法获取安装包');const d=b.data;
 $('version').textContent=d.available?`Android 安装包 · ${(d.sizeBytes/1048576).toFixed(2)} MB`:'暂未开放下载';
 $('passcode').disabled=$('download').disabled=!d.available;
 }catch(e){$('version').textContent='暂不可用';$('status').textContent=e.message;}
})();
