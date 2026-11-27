const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
async function scenario(admin,fail){
 const elements=new Map(),el=id=>{if(!elements.has(id))elements.set(id,{disabled:true,textContent:''});return elements.get(id);};let downloaded,exportCalls=0;
 const body={style:{},appendChild(){}};
 const context=vm.createContext({document:{getElementById:el,body,createElement:()=>({click(){downloaded=this.download;},remove(){}})},location:{replace(){}},window:{addEventListener(){}},confirm:()=>true,setTimeout:f=>f(),URL:{createObjectURL:()=> 'blob:private',revokeObjectURL(){}},fetch:async(path,options)=>{
  if(path.endsWith('/auth/me'))return {ok:true,status:200,json:async()=>({code:0,data:admin})};
  exportCalls++;assert.equal(path,'/api/admin/data-backup/download');assert.equal(options.method,'POST');
  if(fail)return {ok:false,status:500,json:async()=>({message:'备份失败'})};
  return {ok:true,status:200,headers:{get:k=>k==='Content-Type'?'application/sql; charset=utf-8':'attachment; filename="thsbackup20261127123000.sql"'},blob:async()=>({size:123})};
 }});
 vm.runInContext(fs.readFileSync(__dirname+'/web/js/data-backup.js','utf8'),context);await new Promise(setImmediate);
 const allowed=admin.username==='eugene'&&admin.isSuperAdmin;assert.equal(el('export-backup').disabled,!allowed);
 if(allowed){await el('export-backup').onclick();assert.equal(el('export-backup').disabled,false);assert.equal(exportCalls,1);if(fail){assert.equal(downloaded,undefined);assert.equal(el('page-message').textContent,'备份失败');}else assert.equal(downloaded,'thsbackup20261127123000.sql');}
}
(async()=>{await scenario({username:'eugene',isSuperAdmin:true},false);await scenario({username:'eugene',isSuperAdmin:true},true);await scenario({username:'Asen',isSuperAdmin:false},false);console.log('Backup page permission, attachment download, error and button recovery passed');})().catch(e=>{console.error(e);process.exitCode=1;});
