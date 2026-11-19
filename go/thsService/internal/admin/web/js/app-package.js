'use strict';
const $=id=>document.getElementById(id);let busy=false;
function lock(v){busy=v;document.querySelectorAll('.apk-card form input,.apk-card form button').forEach(e=>e.disabled=v);}
function message(text,success=false){$('page-message').textContent=text;$('page-message').className=`alert alert-${success?'success':'danger'}${text?'':' d-none'}`;}
async function api(path,options={}){const r=await fetch('/api/admin'+path,{credentials:'same-origin',cache:'no-store',...options,headers:{'Content-Type':'application/json',...options.headers}});if(r.status===401){location.replace('login.html');throw Error('请重新登录');}const b=await r.json();if(!r.ok||b.code!==0)throw Error(b.message||'请求失败');return b.data;}
function bindPackage(app){
 const $=id=>document.getElementById(app+'-'+id);let info=null;
 const selected=()=>encodeURIComponent(app);
 function message(text,success=false){$('page-message').textContent=text;$('page-message').className=`alert alert-${success?'success':'danger'}${text?'':' d-none'}`;}
 function render(d){info=d;$('current-version').textContent=d.releaseKey||'尚未上传';$('current-size').textContent=d.sizeBytes?`${(d.sizeBytes/1048576).toFixed(2)} MB`:'—';$('current-time').textContent=d.uploadedAt?new Date(d.uploadedAt).toLocaleString():'—';$('current-hash').textContent=d.sha256||'—';$('passcode-state').textContent=d.passcodeConfigured?'已设置，可在下方修改':'尚未设置，请先设置口令';}
$('upload-form').onsubmit=async e=>{
 e.preventDefault();if(busy)return;const file=$('apk-file').files[0];if(!file||!file.name.toLowerCase().endsWith('.apk')||file.size>=100000000){message('请选择100MB以下的APK文件');return;}if(!info?.passcodeConfigured){message('请先在下方设置下载口令，再上传APK');return;}
 if(info?.available&&!confirm('上传成功后会替换当前下载版本，是否继续？'))return;
 lock(true);message('');$('upload-progress').textContent='正在上传…';
 const data=new FormData();data.append('file',file);
 const xhr=new XMLHttpRequest();xhr.open('POST','/api/admin/app-package/upload?app='+selected());xhr.withCredentials=true;xhr.timeout=600000;
 xhr.upload.onprogress=e=>{if(e.lengthComputable)$('upload-progress').textContent=`上传 ${Math.round(e.loaded/e.total*100)}%${e.loaded===e.total?'，正在校验并发布…':''}`;};
 xhr.onload=()=>{try{if(xhr.status===401){location.replace('login.html');return;}if(xhr.status===413)throw Error('上传被服务器或代理拒绝（413）：请确认Nginx已加载新的上传大小配置');let b;try{b=JSON.parse(xhr.responseText);}catch{throw Error(`上传失败（HTTP ${xhr.status}），请检查服务器或代理日志`);}if(xhr.status<200||xhr.status>=300||b.code!==0)throw Error(b.message||`上传失败（HTTP ${xhr.status}）`);render(b.data);$('upload-form').reset();message('APK已上传并切换为当前版本',true);}catch(error){message(error.message);}finally{lock(false);$('upload-progress').textContent='';}};
 xhr.onerror=()=>{lock(false);$('upload-progress').textContent='';message('上传连接中断，请刷新查看当前版本后重试');};xhr.ontimeout=()=>{lock(false);$('upload-progress').textContent='';message('上传超过10分钟未完成，请检查网络或代理，并刷新确认当前版本后再重试');};xhr.onabort=()=>{lock(false);$('upload-progress').textContent='';message('上传已取消，请刷新确认当前版本');};xhr.send(data);
};
$('passcode-form').onsubmit=async e=>{
 e.preventDefault();if(busy)return;const passcode=$('new-passcode').value;if([...passcode].length<5||[...passcode].length>64||!passcode.trim()){message('口令须为5至64个字符，支持中文，不能全为空格');return;}if(passcode!==$('confirm-passcode').value){message('两次输入的口令不一致');return;}
 lock(true);message('');try{render(await api('/app-package/passcode?app='+selected(),{method:'PUT',body:JSON.stringify({passcode})}));$('passcode-form').reset();message('下载口令已更新，新下载请求立即生效',true);}catch(error){message(error.message);}finally{lock(false);}
};
 return render;
}
const renderers=Object.fromEntries(['nanwang','alipay','tax','ths'].map(app=>[app,bindPackage(app)]));
$('download-link').href=location.origin+'/app-download';
$('logout').onclick=async()=>{try{await api('/auth/logout',{method:'POST'});location.replace('login.html');}catch(e){message(e.message);}};
window.addEventListener('beforeunload',e=>{if(busy){e.preventDefault();e.returnValue='';}});
(async()=>{try{const admin=await api('/auth/me');$('admin-name').textContent=admin.username;const items=await api('/app-package');if(!Array.isArray(items)||items.length!==4)throw Error('安装包信息不完整，请刷新重试');for(const item of items){if(!renderers[item.app])throw Error('安装包类型无效');renderers[item.app](item);}lock(false);}catch(e){message(e.message);}finally{document.body.style.visibility='visible';}})();
