const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const elements=new Map();let fetches=0;
function element(id){if(!elements.has(id))elements.set(id,{id,value:'',textContent:'',classList:{toggle(k,v){this[k]=v;}},reset(){},addEventListener(k,fn){this[k]=fn;},querySelectorAll(){return [];},closest(){return {before(){}};}});return elements.get(id);}
const context=vm.createContext({document:{getElementById:element,createElement:()=>element('panel')},bootstrap:{Modal:class {show(){} hide(){}}},window:{addEventListener(){}},confirm:()=>false,fetch:async()=>{fetches++;throw Error('unexpected request');},URLSearchParams,console});
let source=fs.readFileSync(__dirname+'/web/js/bills.js','utf8');
source=source.slice(0,source.lastIndexOf('(async()=>'));
vm.runInContext(source,context);
vm.runInContext(`
 const saved={id:9,userId:3,billMonth:'2026-09',amount:'272.44',totalUsage:'400.40',detail:{note:'keep'},billing:{pricing_mode:'tiered',tiers:[{usage:'260',unit_price:'0.66',amount:'172.35'},{usage:'140.40',unit_price:'0.71',amount:'100.09'},{usage:'88.88',unit_price:'0.96',amount:'123.45'}]}};
 fill(saved);
`,context);
assert.equal(element('generate').classList['d-none'],true);
assert.equal(element('tier-3-usage').value,'88.88');
element('calculate-tiers').onclick();
assert.equal(element('tier-3-usage').value,'88.88','cancel split must preserve manual third tier');
assert.equal(element('tier-3-amount').value,'123.45');
element('generate').onclick();
assert.equal(fetches,0,'edit dialog must not regenerate all bills');
element('billing-pricing_mode').value='flat';
element('panel').input({target:element('tier-3-usage')});
assert.equal(element('billing-pricing_mode').value,'tiered');
assert.equal(vm.runInContext('readBilling().tiers[2].amount',context),'123.45');
vm.runInContext('fill(null)',context);
assert.equal(element('generate').classList['d-none'],false);
console.log('bill editor regression checks passed');

(async()=>{
 vm.runInContext('fill(saved)',context);
 let preview=JSON.parse(element('detail').value);
 assert.equal(preview.billing.tiers.length,3);
 assert.equal(preview.billing.tiers[0].amount,'172.35');
 assert.equal(preview.billing.tiers[1].amount,'100.09');
 assert.equal(preview.billing.tiers[2].amount,'123.45');
 assert.equal(preview.note,'keep');
 for(let n=1;n<=3;n++)element(`tier-${n}-amount`).value=`${n}11.11`;
 element('bill-form').input();
 assert.deepEqual(JSON.parse(element('detail').value).billing.tiers.map(t=>t.amount),['111.11','211.11','311.11']);
 let stored;
 context.fetch=async(url,options={})=>{
  if(options.method==='PUT'){
   const body=JSON.parse(options.body);
   stored={...body,id:9,userId:3,billMonth:'2026-09',version:'saved-version',billing:body.detail.billing};
   return {ok:true,status:200,json:async()=>({code:0,data:stored})};
  }
  return {ok:true,status:200,json:async()=>({code:0,data:{items:[stored],total:1}})};
 };
 await element('bill-form').onsubmit({preventDefault(){}});
 assert.deepEqual(stored.detail.billing.tiers.map(t=>t.amount),['111.11','211.11','311.11']);
 assert.equal(stored.detail.note,'keep');
 assert.equal(JSON.parse(element('detail').value).billing.tiers[2].amount,'311.11');
 context.reopened=stored;
 vm.runInContext('fill(reopened)',context);
 for(let n=1;n<=3;n++)assert.equal(element(`tier-${n}-amount`).value,`${n}11.11`);
 assert.equal(JSON.parse(element('detail').value).billing.tiers[0].amount,'111.11');
 console.log('all three tiers persist through submit response and reopening, with full preview');
})().catch(e=>{console.error(e);process.exitCode=1;});
