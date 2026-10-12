// Reference output only. Run using a pinned external checkout, never TianJi code.
// Reference engine MIT; calendar shared with 6tail, so these are NOT human goldens.
import {createRequire} from 'node:module';
import {pathToFileURL} from 'node:url';
import {writeFileSync} from 'node:fs';
const root = process.argv[2];
const require = createRequire(root + '/package.json');
const {Solar} = require('lunar-typescript');
const {QimenUtil} = await import(pathToFileURL(root + '/src/qimen/QimenUtil.ts').href);
const cases:any[] = [], excluded:any[] = [];
for (const year of [2024, 2026]) for (let month=1;month<=12;month++) for (const day of [8,18,28]) for (const hour of [0,4,10,16,20]) {
    const lunar=Solar.fromYmdHms(year,month,day,hour,30,0).getLunar();
    const local=lunar.getSolar().toYmdHms();
    // Predeclared incompatible convention: reference uses whole-day solar terms.
    if (lunar.getPrevJieQi(true).getName() !== lunar.getPrevJieQi(false).getName()) {
        excluded.push({local,reason:'reference uses whole-day solar terms'}); continue;
    }
    const c=QimenUtil.create(lunar);
    const norm=(s:string|undefined)=>s?.replaceAll('騰','螣').replaceAll('冲','沖') ?? null;
    cases.push({local,day:c.八字[2],hour:c.八字[3],yuan:c.上中下元,dun:c.遁,ju:c.局數,
      palaces:c.九宮.map((p:any,i:number)=>({index:i+1,door:norm(p.八門),star:norm(p.九星),deity:norm(p.八神),
        heaven_stem:p.天盤干.join('、')||null,earth_stem:p.地盤干.join('、')}))});
}
writeFileSync(process.argv[3],JSON.stringify({source:'https://github.com/anthonylee1994/qimen',commit:process.argv[4],calendar:'lunar-typescript 1.8.6',kind:'cross-implementation; NOT independently adjudicated human goldens',exclusions:excluded,cases},null,2));
console.log(JSON.stringify({cases:cases.length,excluded:excluded.length}));
