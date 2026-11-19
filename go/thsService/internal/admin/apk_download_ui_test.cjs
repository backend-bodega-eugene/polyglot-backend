const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
class Element {
 constructor(tag){this.tag=tag;this.children=[];this.events={};this.contentDocument={body:{textContent:''}};}
 append(...items){this.children.push(...items);}
 replaceChildren(){this.children=[];}
 setAttribute(k,v){this[k]=v;}
 addEventListener(k,f){this.events[k]=f;}
}
const root=new Element('main');
const apps=[['nanwang','南网'],['alipay','支付宝'],['tax','个税'],['ths','同花顺']];
vm.runInNewContext(fs.readFileSync(__dirname+'/web/app-download.js','utf8'),{
 document:{getElementById:()=>root,createElement:tag=>new Element(tag)},
 fetch:async()=>({ok:true,json:async()=>({code:0,data:apps.map(([app,name])=>({app,name,available:app!=='tax',sizeBytes:12345}))})}),
 setTimeout:f=>f(),encodeURIComponent
});
setImmediate(()=>{
 try{
  assert.equal(root.children.length,4);
  root.children.forEach((card,i)=>{
   const [title,,form,status,frame]=card.children;
   assert.equal(title.textContent,apps[i][1]);
   assert.equal(form.action,'/api/app-download/download?app='+apps[i][0]);
   assert.equal(form.target,frame.name);assert.equal(form.method,'post');
   assert.equal(form.children[1].disabled,i===2);
   assert.equal(form.children[1].name,'passcode');
   if(i!==2){form.events.submit();assert.match(status.textContent,/已提交/);}
  });
  const first=root.children[0],second=root.children[1];
  first.children[4].contentDocument.body.textContent='{"message":"口令错误"}';
  first.children[4].events.load();
  assert.equal(first.children[3].textContent,'口令错误');
  assert.notEqual(second.children[3].textContent,'口令错误');
  console.log('Four download cards use independent passcode forms, targets, availability and errors');
 }catch(e){console.error(e);process.exitCode=1;}
});
