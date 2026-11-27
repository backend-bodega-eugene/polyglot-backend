'use strict';
const $=id=>document.getElementById(id);let busy=false;
function message(text){$('page-message').textContent=text;$('page-message').className='alert alert-danger'+(text?'':' d-none');}
async function api(path,options={}){const r=await fetch('/api/admin'+path,{credentials:'same-origin',cache:'no-store',...options});if(r.status===401){location.replace('login.html');throw Error('请重新登录');}const b=await r.json();if(!r.ok||b.code!==0)throw Error(b.message||'请求失败');return b.data;}
$('export-backup').onclick=async()=>{
 if(busy||!confirm('导出当前数据库全部表结构和数据？文件包含会员资料，请妥善保存。'))return;
 busy=true;$('export-backup').disabled=true;message('');$('backup-status').textContent='正在生成并下载备份，请勿关闭页面…';
 try{
  const r=await fetch('/api/admin/data-backup/download',{method:'POST',credentials:'same-origin',cache:'no-store'});
  if(r.status===401){location.replace('login.html');throw Error('登录已失效，请重新登录');}
  if(!r.ok){const b=await r.json().catch(()=>null);throw Error(b?.message||`备份失败（HTTP ${r.status}），请稍后重试`);}
  if(!r.headers.get('Content-Type')?.startsWith('application/sql'))throw Error('返回的备份格式无效');
  const blob=await r.blob();if(!blob.size)throw Error('备份文件为空，请重试');
  const name=r.headers.get('Content-Disposition')?.match(/filename="([a-zA-Z0-9]+\.sql)"/)?.[1]||'thsbackup.sql';
  const url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download=name;document.body.appendChild(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),60000);
  $('backup-status').textContent='备份文件已交给浏览器下载，请确认保存完成。';
 }catch(e){message(e.message);$('backup-status').textContent='备份未完成，可重试。';}finally{busy=false;$('export-backup').disabled=false;}
};
$('logout').onclick=async()=>{if(busy)return;try{await api('/auth/logout',{method:'POST'});location.replace('login.html');}catch(e){message(e.message);}};
window.addEventListener('beforeunload',e=>{if(busy){e.preventDefault();e.returnValue='';}});
(async()=>{try{const a=await api('/auth/me');$('admin-name').textContent=a.username;const allowed=a.username==='eugene'&&a.isSuperAdmin;$('export-backup').disabled=!allowed;if(!allowed)message('仅eugene超级管理员可以导出数据备份');}catch(e){message(e.message);}finally{document.body.style.visibility='visible';}})();
