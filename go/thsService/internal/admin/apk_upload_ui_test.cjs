const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const elements=new Map();function el(id){if(!elements.has(id))elements.set(id,{value:'',textContent:'',disabled:false,reset(){},files:[{name:'app.apk',size:37654639}]});return elements.get(id);}
let xhr;
class Request{constructor(){xhr=this;this.upload={};}open(method,url){this.url=url;}send(){}}
const metadata={available:false,passcodeConfigured:true,sizeBytes:0};
const apps=['nanwang','alipay','tax','ths'];let lastFetch;
const context=vm.createContext({document:{getElementById:el,body:{style:{}},querySelectorAll:()=>[el('upload-button')]},location:{origin:'https://example.com',replace(){}},window:{addEventListener(){}},fetch:async(path,options)=>{lastFetch={path,options};return {ok:true,status:200,json:async()=>({code:0,data:path.endsWith('/auth/me')?{username:'eugene'}:options?.method==='PUT'?{...metadata,app:new URL('https://example.com'+path).searchParams.get('app')}:apps.map(app=>({...metadata,app}))})};},XMLHttpRequest:Request,FormData:class{append(){}},confirm:()=>true,URL});
vm.runInContext(fs.readFileSync(__dirname+'/web/js/app-package.js','utf8'),context);
(async()=>{
 await new Promise(setImmediate);
 await el('nanwang-upload-form').onsubmit({preventDefault(){}});
 assert.equal(xhr.timeout,600000);xhr.status=413;xhr.responseText='<html>too large</html>';xhr.onload();
 assert.match(el('nanwang-page-message').textContent,/413/);assert.equal(el('upload-button').disabled,false);
 await el('nanwang-upload-form').onsubmit({preventDefault(){}});xhr.ontimeout();assert.match(el('nanwang-page-message').textContent,/10/);assert.equal(el('upload-button').disabled,false);
 await el('nanwang-upload-form').onsubmit({preventDefault(){}});xhr.status=502;xhr.responseText='<html>Bad Gateway</html>';xhr.onload();assert.match(el('nanwang-page-message').textContent,/502/);
 for(const size of [100000000,100000001]){el('nanwang-apk-file').files[0].size=size;const previous=xhr;await el('nanwang-upload-form').onsubmit({preventDefault(){}});assert.equal(xhr,previous);assert.match(el('nanwang-page-message').textContent,/100MB/);}
 el('nanwang-apk-file').files[0].size=99999999;const previous=xhr;await el('nanwang-upload-form').onsubmit({preventDefault(){}});assert.notEqual(xhr,previous);xhr.onabort();
 for(const app of apps){
  await el(app+'-upload-form').onsubmit({preventDefault(){}});
  assert.equal(xhr.url,'/api/admin/app-package/upload?app='+app);
  xhr.status=200;xhr.responseText=JSON.stringify({code:0,data:{...metadata,app,releaseKey:'20261104123456'}});xhr.onload();
  assert.equal(el(app+'-current-version').textContent,'20261104123456');
  el(app+'-new-passcode').value='独立下载口令'+app;el(app+'-confirm-passcode').value=el(app+'-new-passcode').value;
  await el(app+'-passcode-form').onsubmit({preventDefault(){}});
  assert.equal(lastFetch.path,'/api/admin/app-package/passcode?app='+app);
  assert.equal(JSON.parse(lastFetch.options.body).passcode,'独立下载口令'+app);
 }
 assert.equal(el('nanwang-current-version').textContent,'尚未上传');
 console.log('APK upload HTTP errors and timeout recover the form');
})().catch(e=>{console.error(e);process.exitCode=1;});

