'use strict';
const $ = id => document.getElementById(id);
const categories = {global:'全球',a_stock:'A股',hk_stock:'港股',us_stock:'美股',etf:'ETF',future:'期货',gold:'黄金',convertible_bond:'可转债',other:'其他'};
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const categoryOptions = Object.entries(categories).map(([v,n]) => `<option value="${v}">${n}</option>`).join('');
$('filter-category').innerHTML = '<option value="">全部分类</option>' + categoryOptions;
$('new-category').innerHTML = categoryOptions;
const initialFilters = new URLSearchParams(location.search);
if (/^[1-9]\d*$/.test(initialFilters.get('userId')||'')) $('filter-user').value=initialFilters.get('userId');
const editor = new bootstrap.Modal($('editor'));
const creator = new bootstrap.Modal($('create-modal'));
let page = 1, total = 0, rows = [], current = null, selected = -1, dirty = false, busy = false;
function message(id,text,type='danger') { const el=$(id);el.textContent=text;el.className=`alert alert-${type}${text?'':' d-none'}`; }
async function api(path, options={}) {
 const res=await fetch('/api/admin'+path,{credentials:'same-origin',cache:'no-store',...options,headers:{'Content-Type':'application/json',...options.headers}});
 if(res.status===401){location.replace('login.html');throw new Error('登录已失效');}
 const json=await res.json();if(!res.ok||json.code!==0)throw new Error(json.message||'请求失败');return json.data;
}
function money(v){return Number(v||0).toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2});}
function localTime(v){const d=new Date(v);if(!Number.isFinite(d.getTime()))return '';return new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,19);}
function iso(v){return v?new Date(v).toISOString():'';}
function changed(){dirty=true;$('dirty-state').textContent='有未保存修改';}
function path(){return `/holdings/${current.userId}/${current.category}`;}
function selectedStock(){return current?.items[selected];}
async function load(){
 message('page-message','');$('rows').innerHTML='<tr><td colspan="6" class="p-4 text-center">加载中...</td></tr>';
 try{
  const query=new URLSearchParams({page,pageSize:20});if($('filter-user').value)query.set('userId',$('filter-user').value);if($('filter-category').value)query.set('category',$('filter-category').value);
  const data=await api('/holdings?'+query);rows=data.items;total=data.total;
  $('rows').innerHTML=rows.map((r,i)=>`<tr><td><strong>${esc(r.realName)}</strong><small class="d-block text-body-secondary">ID ${esc(r.userId)}</small></td><td>${esc(categories[r.category]||r.category)}</td><td>${money(r.totalAssets)}</td><td>${esc(r.positionCount)}</td><td>${esc(new Date(r.updatedAt).toLocaleString())}</td><td class="text-end"><button class="btn btn-sm btn-outline-primary me-2" data-edit="${i}">查看 / 编辑</button><button class="btn btn-sm btn-outline-danger" data-delete="${i}">删除</button></td></tr>`).join('')||'<tr><td colspan="6" class="p-5 text-center text-body-secondary">暂无持仓，可为会员新增或自动生成</td></tr>';
  $('paging').textContent=`共 ${total} 条 · 第 ${page} / ${Math.max(1,Math.ceil(total/20))} 页`;$('previous').disabled=page<=1;$('next').disabled=page*20>=total;
 }catch(e){$('rows').innerHTML='';message('page-message',e.message);}
}
function openEditor(data,userId,category,isNew=false){
 current={...structuredClone(data),userId,category,isNew};selected=current.items.length?0:-1;dirty=false;
 $('editor-title').textContent=`${isNew?'新增':'编辑'}${categories[category]}持仓`;$('editor-subtitle').textContent=`会员 ID ${userId} · 股票及交易统一保存`;
 $('assets').value=data.account.totalAssets;summary();message('editor-message','');$('dirty-state').textContent='修改后请保存，关闭将丢弃未保存修改';renderStocks();editor.show();
}
function summary(){const a=current.account;$('summary-value').textContent=money(a.totalMarketValue);$('summary-cash').textContent=money(a.availableCash??a.totalAssets);$('summary-profit').textContent=money(a.floatingProfit);}
function renderStocks(){
 $('stock-count').textContent=`(${current.items.length})`;
 $('stocks').innerHTML=current.items.map((item,i)=>`<button class="list-group-item list-group-item-action ${i===selected?'active':''}" data-stock="${i}"><strong>${esc(item.position.name||'新股票')}</strong><small>${esc(item.position.symbol||'请填写代码')} · ${esc(item.position.quantity)} 股/单位</small></button>`).join('');renderStock();
}
function renderStock(){
 const item=selectedStock();$('stock-empty').classList.toggle('d-none',!!item);$('stock-detail').classList.toggle('d-none',!item);if(!item)return;
 for(const input of $('stock-form').querySelectorAll('[data-field]'))input.value=input.dataset.field==='quoteTime'?localTime(item.position.quoteTime):item.position[input.dataset.field]??'';
 $('cost').textContent=item.position.costPrice||'保存后计算';$('market-value').textContent=item.position.marketValue?money(item.position.marketValue):'—';$('profit').textContent=item.position.profit?money(item.position.profit):'—';renderTrades();
}
function renderTrades(){
 const item=selectedStock();if(!item)return;
 $('trade-count').textContent=`(${item.trades.length})`;
 $('trades').innerHTML=item.trades.map((t,i)=>`<tr data-index="${i}"><td><select data-trade="side" aria-label="方向" class="form-select form-select-sm"><option value="buy" ${t.side==='buy'?'selected':''}>买入</option><option value="sell" ${t.side==='sell'?'selected':''}>卖出</option></select></td><td><input data-trade="quantity" aria-label="数量" type="number" min="1" step="1" class="form-control form-control-sm" value="${esc(t.quantity)}"></td><td><input data-trade="price" aria-label="成交价" type="number" min="0.0001" step="0.0001" class="form-control form-control-sm" value="${esc(t.price)}"></td><td><input data-trade="fees" aria-label="手续费" type="number" min="0" step="0.01" class="form-control form-control-sm" value="${esc(t.fees)}"></td><td><input data-trade="tradedAt" aria-label="成交时间" type="datetime-local" step="1" class="form-control form-control-sm" value="${esc(localTime(t.tradedAt))}"></td><td><button class="btn btn-sm btn-outline-danger" data-remove-trade="${i}">删除</button></td></tr>`).join('')||'<tr><td colspan="6" class="text-center text-body-secondary p-4">暂无交易，请新增交易</td></tr>';
 netQuantity();
}
function netQuantity(){const item=selectedStock();const n=item.trades.reduce((n,t)=>n+(t.side==='buy'?1:-1)*Number(t.quantity||0),0);$('net-quantity').textContent=n;return n;}
function closeEditor(){if(busy)return;if(dirty&&!confirm('关闭将丢弃未保存的修改，确定关闭？'))return;editor.hide();current=null;}
function lock(value){busy=value;for(const id of ['save','generate','cancel','close-editor','add-stock','remove-stock','add-trade'])$(id).disabled=value;$('save').textContent=value?'处理中...':'保存全部修改';$('editor').querySelectorAll('input,select').forEach(el=>el.disabled=value);}
$('stock-form').addEventListener('input',event=>{
 const field=event.target.dataset.field;if(!field)return;try{selectedStock().position[field]=['quantity','availableQuantity'].includes(field)?Number(event.target.value):field==='quoteTime'?iso(event.target.value):event.target.value;changed();}catch(e){message('editor-message','请输入有效时间');}
});
$('trades').addEventListener('input',event=>{
 const field=event.target.dataset.trade;if(!field)return;const i=Number(event.target.closest('tr').dataset.index);
 try{selectedStock().trades[i][field]=field==='quantity'?Number(event.target.value):field==='tradedAt'?iso(event.target.value):event.target.value;changed();netQuantity();}catch(e){message('editor-message','请输入有效时间');}
});
$('trades').addEventListener('click',event=>{const button=event.target.closest('[data-remove-trade]');if(!button||busy)return;if(!confirm('删除这条交易？保存后生效。'))return;selectedStock().trades.splice(Number(button.dataset.removeTrade),1);changed();renderTrades();});
$('stocks').addEventListener('click',event=>{const button=event.target.closest('[data-stock]');if(!button)return;selected=Number(button.dataset.stock);renderStocks();});
$('add-stock').onclick=()=>{
 if(current.items.length>=50){message('editor-message','最多50个持仓');return;}
 current.items.push({position:{symbol:'',name:'',currency:current.category==='hk_stock'?'HKD':['us_stock','global','future','gold'].includes(current.category)?'USD':'CNY',latestPrice:'1.0000',dailyProfit:'0.00',quantity:0,availableQuantity:0,quoteTime:new Date().toISOString()},trades:[]});selected=current.items.length-1;changed();renderStocks();
};
$('remove-stock').onclick=()=>{if(!confirm('删除该股票及其全部交易？保存后生效。'))return;current.items.splice(selected,1);selected=current.items.length?Math.min(selected,current.items.length-1):-1;changed();renderStocks();};
$('add-trade').onclick=()=>{const item=selectedStock();if(item.trades.length>=500){message('editor-message','每只股票最多500条交易');return;}item.trades.unshift({side:'buy',quantity:['a_stock','etf'].includes(current.category)?100:current.category==='convertible_bond'?10:1,price:item.position.latestPrice||'1.0000',fees:'0.00',tradedAt:new Date().toISOString()});changed();renderTrades();};
$('sync-quantity').onclick=()=>{const n=netQuantity();selectedStock().position.quantity=n;selectedStock().position.availableQuantity=n;changed();renderStocks();};
$('assets').oninput=changed;
$('close-editor').onclick=closeEditor;$('cancel').onclick=closeEditor;
async function persist(generate,counts){
 if(busy)return;
 if(!current.isNew&&generate&&!confirm('自动生成将替换此分类全部股票及交易，并丢弃未保存修改，确定继续？'))return;
 const body={expectedGeneration:current.isNew?'':current.account.generation,totalAssets:$('assets').value,items:generate?[]:current.items};
 if(generate)Object.assign(body,counts);
 lock(true);message('editor-message','');
 try{
  const result=await api(path()+(generate?'/generate':''),{method:generate?'POST':current.isNew?'POST':'PUT',body:JSON.stringify(body)});
  current={...result,userId:current.userId,category:current.category,isNew:false};dirty=false;selected=current.items.length?Math.max(0,Math.min(selected,current.items.length-1)):-1;
  summary();renderStocks();$('dirty-state').textContent='全部修改已保存';message('editor-message',generate?'已生成并保存新持仓':'保存成功，会员端已同步','success');await load();
 }catch(e){message('editor-message',e.message);}finally{lock(false);}
}
$('save').onclick=()=>persist(false);
$('generate').onclick=()=>{if(busy)return;$('generation-positions').value='10';$('generation-trades').value='30';$('generation-dialog').showModal();};
$('generation-cancel').onclick=()=>$('generation-dialog').close();
$('generation-form').onsubmit=e=>{e.preventDefault();if(busy||!$('generation-form').reportValidity())return;const positionCount=Number($('generation-positions').value),tradesPerPosition=Number($('generation-trades').value);if(!Number.isInteger(positionCount)||positionCount<1||positionCount>50||!Number.isInteger(tradesPerPosition)||tradesPerPosition<3||tradesPerPosition>500)return;$('generation-dialog').close();persist(true,{positionCount,tradesPerPosition});};
$('rows').addEventListener('click',async event=>{
 const edit=event.target.closest('[data-edit]'),del=event.target.closest('[data-delete]');if(!edit&&!del)return;const row=rows[Number(edit?.dataset.edit??del.dataset.delete)];
 try{
  if(edit){const data=await api(`/holdings/${row.userId}/${row.category}`);openEditor(data,row.userId,row.category);}
  else{if(!confirm(`删除会员 ${row.userId} 的${categories[row.category]}持仓及全部交易？会员端再次访问会重新生成。若需空仓，请在编辑中删除全部股票后保存。`))return;await api(`/holdings/${row.userId}/${row.category}`,{method:'DELETE',body:JSON.stringify({expectedGeneration:row.generation})});if(rows.length===1&&page>1)page--;await load();}
 }catch(e){message('page-message',e.message);}
});
async function members(){
 $('create-message').textContent='';try{const data=await api('/holding-members?q='+encodeURIComponent($('member-query').value));$('member').innerHTML=data.map(m=>`<option value="${m.id}">ID ${m.id} · ${esc(m.realName)} · ${esc(m.phone||m.email||'')}</option>`).join('');if(!data.length)$('create-message').textContent='没有匹配的会员';}catch(e){$('create-message').textContent=e.message;}
}
$('new').onclick=()=>{creator.show();members();};$('member-search').onclick=members;
$('member-query').onkeydown=e=>{if(e.key==='Enter'){e.preventDefault();members();}};
$('create').onclick=async()=>{
 const uid=$('member').value,category=$('new-category').value,assets=$('new-assets').value;
 if(!uid||!assets||Number(assets)<10000||Number(assets)>1000000000){$('create-message').textContent='请选择会员并输入10000至1000000000元总资产';return;}
 try{await api(`/holdings/${uid}/${category}`);$('create-message').textContent='该会员此分类已存在，请从列表编辑';return;}catch(e){if(e.message!=='会员或分类持仓不存在'){$('create-message').textContent=e.message;return;}}
 creator.hide();$('create-modal').addEventListener('hidden.bs.modal',()=>openEditor({account:{totalAssets:assets},items:[]},Number(uid),category,true),{once:true});
};
$('filters').onsubmit=e=>{e.preventDefault();page=1;load();};$('reset').onclick=()=>{$('filter-user').value='';$('filter-category').value='';page=1;load();};$('previous').onclick=()=>{if(page>1){page--;load();}};$('next').onclick=()=>{if(page*20<total){page++;load();}};
$('logout').onclick=async()=>{try{await api('/auth/logout',{method:'POST'});location.replace('login.html');}catch(e){message('page-message',e.message);}};
window.addEventListener('beforeunload',e=>{if(dirty){e.preventDefault();e.returnValue='';}});
(async()=>{try{const a=await api('/auth/me');$('admin-name').textContent=a.username;document.body.style.visibility='visible';await load();}catch(e){document.body.style.visibility='visible';message('page-message',e.message);}})();
