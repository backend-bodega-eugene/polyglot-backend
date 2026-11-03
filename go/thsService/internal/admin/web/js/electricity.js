'use strict';
const $=id=>document.getElementById(id);
const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const fields=['displayName','address','userIdNumber','supplier','meterReader','meterNo','postalCode'];
const root='/electricity/profiles';
const modal=new bootstrap.Modal($('profile-modal'));
let page=1,total=0,rows=[],current=null,busy=false,dirty=false;
function message(id,text,type='danger'){const el=$(id);el.textContent=text;el.className=`alert alert-${type}${text?'':' d-none'}`;}
async function api(path,options={}){const res=await fetch('/api/admin'+path,{credentials:'same-origin',cache:'no-store',...options,headers:{'Content-Type':'application/json',...options.headers}});if(res.status===401){location.replace('login.html');throw new Error('登录已失效');}const json=await res.json();if(!res.ok||json.code!==0){const error=new Error(json.message||'请求失败');error.status=res.status;throw error;}return json.data;}
async function load(){
 message('page-message','');$('rows').innerHTML='<tr><td colspan="7" class="text-center p-4">加载中...</td></tr>';
 try{const query=new URLSearchParams({q:$('query').value.trim(),page,pageSize:20});if($('user-filter').value)query.set('userId',$('user-filter').value);const data=await api(root+'?'+query);rows=data.items;total=data.total;
  $('rows').innerHTML=rows.map((p,i)=>`<tr><td><strong>${esc(p.realName)}</strong><small class="d-block text-body-secondary">ID ${p.userId} · ${esc(p.username)}</small></td><td>${esc(p.displayName)}</td><td><span>${esc(p.userIdNumber)}</span><small class="d-block text-body-secondary">表号 ${esc(p.meterNo)}</small></td><td>${esc(p.supplier)}</td><td class="address">${esc(p.address)}</td><td>${esc(new Date(p.updatedAt).toLocaleString())}</td><td class="text-end text-nowrap"><button class="btn btn-sm btn-outline-primary me-1" data-edit="${i}">查看 / 编辑</button><button class="btn btn-sm btn-outline-danger" data-delete="${i}">删除</button></td></tr>`).join('')||'<tr><td colspan="7" class="p-5 text-center text-body-secondary">暂无用电资料。可在后台新增，或由App首次查询时自动生成。</td></tr>';
  $('paging').textContent=`共 ${total} 条 · 第 ${page} / ${Math.max(1,Math.ceil(total/20))} 页`;$('previous').disabled=page<=1;$('next').disabled=page*20>=total;
 }catch(e){$('rows').innerHTML='';message('page-message',e.message);}
}
function fill(data){
 current=data;dirty=false;$('profile-title').textContent=data?'编辑用电资料':'新增用电资料';$('member-picker').classList.toggle('d-none',!!data);
 $('member-summary').textContent=data?`会员 ID ${data.userId} · ${data.username} · ${data.realName}`:'';
 fields.forEach(field=>$(field).value=data?.[field]??'');$('generate').textContent=data?'重新生成':'自动生成';
}
async function findMembers(){
 message('form-message','');const data=await api('/members?'+new URLSearchParams({q:$('member-search').value.trim(),pageSize:20}));
 $('member').innerHTML=data.items.map(u=>`<option value="${u.id}">ID ${u.id} · ${esc(u.realName)} · ${esc(u.phone||u.email||'')}</option>`).join('');if(!data.items.length)message('form-message','未找到会员');
}
function lock(value){busy=value;$('profile-form').querySelectorAll('input,select,button').forEach(el=>el.disabled=value);$('save').textContent=value?'处理中...':'保存资料';}
function close(){if(busy)return;if(dirty&&!confirm('关闭将丢弃未保存修改，确定继续？'))return;modal.hide();dirty=false;}
$('new').onclick=async()=>{fill(null);message('form-message','');$('member-search').value='';modal.show();try{await findMembers();}catch(e){message('form-message',e.message);}};
$('find-member').onclick=async()=>{try{await findMembers();}catch(e){message('form-message',e.message);}};
$('member-search').onkeydown=e=>{if(e.key==='Enter'){e.preventDefault();$('find-member').click();}};
$('profile-form').addEventListener('input',()=>dirty=true);
$('cancel').onclick=close;$('close').onclick=close;
async function persist(generate){
 if(busy)return;const uid=current?.userId||$('member').value;if(!uid){message('form-message','请先选择会员');return;}
 if(generate&&(current||dirty)&&!confirm('自动生成将直接替换并保存资料，未保存修改将丢弃，确定继续？'))return;
 const body={expectedVersion:current?.version||''};if(!generate)fields.forEach(field=>body[field]=$(field).value);
 lock(true);message('form-message','');
 try{const data=await api(`${root}/${uid}${generate?'/generate':''}`,{method:generate||!current?'POST':'PUT',body:JSON.stringify(body)});fill(data);message('form-message',generate?'已生成并保存模拟资料':'资料已保存，App下次读取即可看到','success');await load();}catch(e){message('form-message',e.message);}finally{lock(false);}
}
$('profile-form').onsubmit=e=>{e.preventDefault();persist(false);};$('generate').onclick=()=>persist(true);
$('rows').onclick=async e=>{if(busy)return;const edit=e.target.closest('[data-edit]'),del=e.target.closest('[data-delete]');if(!edit&&!del)return;const profile=rows[Number(edit?.dataset.edit??del.dataset.delete)];
 try{if(edit){const data=await api(`${root}/${profile.userId}`);fill(data);message('form-message','');modal.show();}else{if(!confirm(`删除「${profile.displayName}」的用电资料？\n不会删除会员账号。App再次查询时会自动生成新资料。`))return;busy=true;await api(`${root}/${profile.userId}`,{method:'DELETE',body:JSON.stringify({expectedVersion:profile.version})});if(rows.length===1&&page>1)page--;await load();message('page-message','资料已删除，会员账号保留','success');}}catch(error){message('page-message',error.message);}finally{busy=false;}
};
$('filters').onsubmit=e=>{e.preventDefault();page=1;load();};$('reset').onclick=()=>{$('query').value='';$('user-filter').value='';page=1;load();};$('previous').onclick=()=>{if(page>1){page--;load();}};$('next').onclick=()=>{if(page*20<total){page++;load();}};
$('logout').onclick=async()=>{try{await api('/auth/logout',{method:'POST'});location.replace('login.html');}catch(e){message('page-message',e.message);}};
window.addEventListener('beforeunload',e=>{if(dirty){e.preventDefault();e.returnValue='';}});
(async()=>{try{const admin=await api('/auth/me');$('admin-name').textContent=admin.username;document.body.style.visibility='visible';await load();}catch(e){document.body.style.visibility='visible';message('page-message',e.message);}})();
