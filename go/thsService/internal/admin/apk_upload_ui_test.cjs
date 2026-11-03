const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const elements=new Map();function el(id){if(!elements.has(id))elements.set(id,{value:'',textContent:'',disabled:false,reset(){},files:[{name:'app.apk',size:37654639}]});return elements.get(id);}
let xhr;
class Request{constructor(){xhr=this;this.upload={};}open(){}send(){}}
const metadata={available:false,passcodeConfigured:true,sizeBytes:0};
const context=vm.createContext({document:{getElementById:el,body:{style:{}},querySelectorAll:()=>[el('upload-button')]},location:{origin:'https://example.com',replace(){}},window:{addEventListener(){}},fetch:async path=>({ok:true,status:200,json:async()=>({code:0,data:path.endsWith('/auth/me')?{username:'eugene'}:metadata})}),XMLHttpRequest:Request,FormData:class{append(){}},confirm:()=>true});
vm.runInContext(fs.readFileSync(__dirname+'/web/js/app-package.js','utf8'),context);
(async()=>{
 await new Promise(setImmediate);
 await el('upload-form').onsubmit({preventDefault(){}});
 assert.equal(xhr.timeout,600000);xhr.status=413;xhr.responseText='<html>too large</html>';xhr.onload();
 assert.match(el('page-message').textContent,/413/);assert.equal(el('upload-button').disabled,false);
 await el('upload-form').onsubmit({preventDefault(){}});xhr.ontimeout();assert.match(el('page-message').textContent,/10/);assert.equal(el('upload-button').disabled,false);
 await el('upload-form').onsubmit({preventDefault(){}});xhr.status=502;xhr.responseText='<html>Bad Gateway</html>';xhr.onload();assert.match(el('page-message').textContent,/502/);
 for(const size of [100000000,100000001]){el('apk-file').files[0].size=size;const previous=xhr;await el('upload-form').onsubmit({preventDefault(){}});assert.equal(xhr,previous);assert.match(el('page-message').textContent,/100MB/);}
 el('apk-file').files[0].size=99999999;const previous=xhr;await el('upload-form').onsubmit({preventDefault(){}});assert.notEqual(xhr,previous);xhr.onabort();
 console.log('APK upload HTTP errors and timeout recover the form');
})().catch(e=>{console.error(e);process.exitCode=1;});
