'use strict';
const $=id=>document.getElementById(id);
const esc=value=>String(value??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const memberModal=new bootstrap.Modal($('member-modal')),passwordModal=new bootstrap.Modal($('password-modal'));
let page=1,total=0,rows=[],editing=null,passwordMember=null,busy=false;
function message(id,text,type='danger'){const e=$(id);e.textContent=text;e.className=`alert alert-${type}${text?'':' d-none'}`;}
async function api(path,options={}){const res=await fetch('/api/admin'+path,{credentials:'same-origin',cache:'no-store',...options,headers:{'Content-Type':'application/json',...options.headers}});if(res.status===401){location.replace('login.html');throw new Error('登录已失效');}const data=await res.json();if(!res.ok||data.code!==0)throw new Error(data.message||'请求失败');return data.data;}
function date(value){return value?new Date(value).toLocaleString():'—';}
function passwordError(value){if([...value].length<6)return '密码至少6个字符';if(new TextEncoder().encode(value).length>72)return '密码最多72字节';return '';}
async function load(){
 message('page-message','');$('rows').innerHTML='<tr><td colspan="8" class="p-4 text-center">加载中...</td></tr>';
 try{const query=new URLSearchParams({page,pageSize:20,q:$('query').value.trim(),status:$('filter-status').value});const data=await api('/members?'+query);rows=data.items;total=data.total;
  $('rows').innerHTML=rows.map((u,i)=>`<tr><td>${u.id}</td><td>${esc(u.realName)}</td><td>${esc(u.phone||'—')}</td><td>${esc(u.email||'—')}</td><td><span class="badge ${u.status===1?'text-bg-success':'text-bg-secondary'}">${u.status===1?'正常':'停用'}</span></td><td>${esc(date(u.createdAt))}</td><td>${esc(date(u.lastLoginAt))}</td><td class="text-end text-nowrap"><button class="btn btn-sm btn-outline-primary me-1" data-edit="${i}">编辑</button><button class="btn btn-sm btn-outline-secondary me-1" data-password="${i}">修改密码</button><a class="btn btn-sm btn-outline-secondary me-1" href="holdings.html?userId=${u.id}">持仓</a><button class="btn btn-sm btn-outline-danger" data-delete="${i}">删除</button></td></tr>`).join('')||'<tr><td colspan="8" class="p-5 text-center text-body-secondary">暂无会员</td></tr>';
  $('paging').textContent=`共 ${total} 条 · 第 ${page} / ${Math.max(1,Math.ceil(total/20))} 页`;$('previous').disabled=page<=1;$('next').disabled=page*20>=total;
 }catch(e){$('rows').innerHTML='';message('page-message',e.message);}
}
function showMember(user){
 editing=user?.id??null;$('member-form').reset();message('member-message','');$('member-title').textContent=editing?`编辑会员 · ID ${editing}`:'新增会员';
 $('real-name').value=user?.realName??'';$('phone').value=user?.phone??'';$('email').value=user?.email??'';$('status').value=user?.status??1;
 $('initial-password-wrap').classList.toggle('d-none',!!editing);$('initial-password').required=!editing;$('initial-password').disabled=!!editing;
 $('member-times').textContent=user?`注册：${date(user.createdAt)} · 更新：${date(user.updatedAt)}`:'';memberModal.show();
}
function lock(form,value){busy=value;$(form).querySelectorAll('button,input,select').forEach(el=>el.disabled=value);}
$('new').onclick=()=>showMember(null);
$('member-form').onsubmit=async e=>{
 e.preventDefault();if(busy)return;message('member-message','');
 const fields={realName:$('real-name').value.trim(),phone:$('phone').value.trim(),email:$('email').value.trim(),status:Number($('status').value)};
 if(!fields.phone&&!fields.email){message('member-message','手机号和邮箱至少填写一项');return;}
 if(!editing){const error=passwordError($('initial-password').value);if(error){message('member-message',error);return;}fields.password=$('initial-password').value;}
 lock('member-form',true);
 try{await api(editing?`/members/${editing}`:'/members',{method:editing?'PUT':'POST',body:JSON.stringify(fields)});memberModal.hide();$('initial-password').value='';await load();message('page-message',editing?'会员资料已更新':'会员已创建','success');}catch(error){message('member-message',error.message);}finally{lock('member-form',false);$('initial-password').disabled=!!editing;}
};
$('rows').onclick=async e=>{
 if(busy)return;const edit=e.target.closest('[data-edit]'),password=e.target.closest('[data-password]'),del=e.target.closest('[data-delete]');if(!edit&&!password&&!del)return;
 const user=rows[Number(edit?.dataset.edit??password?.dataset.password??del.dataset.delete)];
 try{
  if(edit){showMember(await api(`/members/${user.id}`));}
  else if(password){passwordMember=user;$('password-form').reset();message('password-message','');$('password-member').textContent=`${user.realName} · ID ${user.id}`;passwordModal.show();}
  else{if(!confirm(`确定删除会员「${user.realName}」（ID ${user.id}）？\n这是逻辑删除，保留会员、持仓和交易数据，删除后会员无法登录。`))return;busy=true;await api(`/members/${user.id}`,{method:'DELETE'});if(rows.length===1&&page>1)page--;await load();message('page-message','会员已逻辑删除，历史数据保留','success');}
 }catch(error){message('page-message',error.message);}finally{busy=false;}
};
$('password-form').onsubmit=async e=>{
 e.preventDefault();if(busy)return;const password=$('new-password').value;const error=passwordError(password);if(error){message('password-message',error);return;}if(password!==$('confirm-password').value){message('password-message','两次输入的密码不一致');return;}
 lock('password-form',true);try{await api(`/members/${passwordMember.id}/password`,{method:'PUT',body:JSON.stringify({password})});passwordModal.hide();$('password-form').reset();message('page-message','会员密码已修改','success');}catch(error){message('password-message',error.message);}finally{lock('password-form',false);}
};
$('member-modal').addEventListener('hidden.bs.modal',()=>{$('initial-password').value='';});$('password-modal').addEventListener('hidden.bs.modal',()=>{$('password-form').reset();});
$('filters').onsubmit=e=>{e.preventDefault();page=1;load();};$('reset').onclick=()=>{$('query').value='';$('filter-status').value='';page=1;load();};$('previous').onclick=()=>{if(page>1){page--;load();}};$('next').onclick=()=>{if(page*20<total){page++;load();}};
$('logout').onclick=async()=>{try{await api('/auth/logout',{method:'POST'});location.replace('login.html');}catch(e){message('page-message',e.message);}};
(async()=>{try{const a=await api('/auth/me');$('admin-name').textContent=a.username;document.body.style.visibility='visible';await load();}catch(e){document.body.style.visibility='visible';message('page-message',e.message);}})();
