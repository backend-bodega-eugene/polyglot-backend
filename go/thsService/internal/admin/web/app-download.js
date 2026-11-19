'use strict';
const root=document.getElementById('packages');
(async()=>{
 try {
  const r=await fetch('/api/app-download/info',{cache:'no-store'}),b=await r.json();
  if(!r.ok||b.code!==0||!Array.isArray(b.data))throw Error(b.message||'暂时无法获取安装包');
  root.replaceChildren();
  for(const d of b.data){
   const card=document.createElement('section');card.className='package';
   const title=document.createElement('h2');title.textContent=d.name;card.append(title);
   const meta=document.createElement('p');meta.textContent=d.available?`Android 安装包 · ${(d.sizeBytes/1000000).toFixed(2)} MB`:'暂未开放下载';card.append(meta);
   const frame=document.createElement('iframe');frame.name='result-'+d.app;frame.title=d.name+'下载结果';frame.hidden=true;
   const form=document.createElement('form');form.method='post';form.action='/api/app-download/download?app='+encodeURIComponent(d.app);form.target=frame.name;
   const label=document.createElement('label');label.htmlFor='passcode-'+d.app;label.textContent=d.name+'下载口令';
   const input=document.createElement('input');input.id=label.htmlFor;input.name='passcode';input.type='password';input.required=true;input.maxLength=64;input.autocomplete='off';input.placeholder='请输入对应下载口令';input.disabled=!d.available;
   const button=document.createElement('button');button.type='submit';button.textContent='下载'+d.name+' APK';button.disabled=!d.available;
   const status=document.createElement('p');status.setAttribute('role','status');status.setAttribute('aria-live','polite');
   form.append(label,input,button);card.append(form,status,frame);root.append(card);
   form.addEventListener('submit',()=>{status.textContent='已提交请求，口令正确后将开始下载。';button.disabled=true;setTimeout(()=>{button.disabled=false;},2000);});
   frame.addEventListener('load',()=>{try{const text=frame.contentDocument.body.textContent;if(!text.trim())return;const result=JSON.parse(text);status.textContent=result.message||'下载失败，请稍后重试';}catch{status.textContent='下载失败，请刷新后重试';}button.disabled=false;});
  }
 } catch(e){root.textContent=e.message;}
})();
