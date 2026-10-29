
const fs = require('fs');
const dagre = require('@dagrejs/dagre');

const N = [
 ['M0','M0 · 已完成','环境闸门 + 骨架冒烟','done'],
 ['D1','D1 · 施工票编排','to-tickets：落台帐 + blockedBy 边','dec'],
 ['D2','D2 · 追溯审定','grill-with-docs → to-spec：SRS×施工票对齐','dec'],
 ['B1','B1 · 骨架扩域 + 横切底座','统一响应/异常/JWT骨架/MP配置/Knife4j','infra'],
 ['T1','T1 · 建表迁移 + 种子','P0 九张表 + 唯一索引 + 预置 admin','infra'],
 ['B2','B2 · 注册/登录/JWT 全链路','BCrypt · 2h token · 拦截器 · Pinia/守卫','infra'],
 ['T2','T2 · 文件上传 /api/file','本地磁盘 · ≤5MB · jpg/png/webp','infra'],
 ['T3','T3 · 攻略读路径最小闭环','列表 + 详情 + 仅 approved 可见 + 浏览量','front'],
 ['A1','A1 · 发布与编辑重审','富文本/封面/标签/草稿·提交·编辑即撤回','front'],
 ['A2','A2 · 列表排序与搜索筛选','最新/最热 · 关键词+目的地+标签+花费区间','front'],
 ['A3','A3 · 审核状态机','五态八边 · 驳回理由 · 乐观锁守卫','front'],
 ['A4','A4 · 点赞 / 收藏','唯一索引防重 · 计数同步 · 事务','front'],
 ['A5','A5 · 一级评论 + 自删','仅 approved 可评 · 逻辑删除','front'],
 ['A6','A6 · 我的发布 + 聚合页','五态徽标 · 收藏/点赞/评论三列表','front'],
 ['U1','U1 · 个人信息','昵称/头像/简介/改密','feat'],
 ['DST1','DST1 · 目的地管理 + 聚合页','后台 CRUD + 前台按目的地取数','feat'],
 ['H1','H1 · 轮播图管理与渲染','增删改 · 排序 · 启用状态','feat'],
 ['H2','H2 · 首页四区块取数','热门/最新/热门目的地/搜索框','feat'],
 ['AD1','AD1 · 用户管理','禁用启用 · 重置密码 · 角色分配','admin'],
 ['AD2','AD2 · 评论管理','列表 · 删除违规 · 定位攻略','admin'],
 ['AD3','AD3 · 标签管理','维护标签体系 · 逻辑删','admin'],
 ['AD4','AD4 · 数据看板','四项指标 · TOP10 · ECharts','admin'],
 ['M1','M1 · CP1 联调闭环','第 4 周末闸门','mile'],
 ['M2','M2 · 前台可见性闭环','发布→待审→通过→下架 端到端','mile'],
 ['M3','M3 · CP2 核心闭环','第 10 周末：P0 全部可演示','mile'],
 ['V1','V1 · 功能测试用例','对照 SRS 验收判据逐条','acc'],
 ['V2','V2 · 非功能验证','NFR-01/02/03/04/05 实测','acc'],
 ['P1','P1 · 闸门裁决','grill-with-docs：P1 五项做/砍','dec'],
 ['P2b','P2b · P1 文档回写','to-spec：补齐可测判据','dec'],
 ['P2a','P2a · P1 实施','implement-spec（可砍）','acc'],
 ['M4','M4 · CP3 功能冻结','第 13 周末：停止新增','mile'],
 ['V3','V3 · Nginx 本地部署','断网可演示','acc'],
 ['V4','V4 · 论文材料包','测试用例表 + 图表清单','acc'],
 ['V5','V5 · 论文初稿','七章式','acc'],
 ['V6','V6 · 答辩 PPT 与演练','演示脚本 + 兜底截图','acc'],
 ['M5','M5 · 里程碑完成','论文定稿 + 答辩通过','mile'],
];
const E = [
 ['M0','D1'],['D1','D2'],['D2','B1'],['D2','T1'],['B1','B2'],['T1','B2'],['B2','M1'],
 ['B2','T2'],['T1','T3'],['B2','T3'],['T2','T3'],
 ['T3','A1'],['T3','A2'],['T3','A3'],['A1','A3'],['T3','A4'],['T3','A5'],
 ['A1','A6'],['A4','A6'],['A5','A6'],
 ['B2','U1'],['T2','U1'],['B2','AD1'],['T2','DST1'],['T3','DST1'],
 ['B2','H1'],['T2','H1'],['T3','H2'],['A4','H2'],['H1','H2'],
 ['A1','AD3'],['A5','AD2'],['T3','AD4'],['A4','AD4'],['B2','AD4'],
 ['A3','M2'],['M2','M3'],
 ['A2','M3'],['A6','M3'],['U1','M3'],['AD1','M3'],['AD2','M3'],['AD3','M3'],['AD4','M3'],['DST1','M3'],['H2','M3'],
 ['M3','V1'],['M3','V2'],['M3','P1'],['P1','P2b'],['P2b','P2a'],
 ['V1','V3'],['V2','V3'],['V2','M4'],['P2a','M4'],
 ['V3','V4'],['V4','V5'],['M4','V5'],['V5','V6'],['V6','M5'],
];

function tw(s, fs){ let w=0; for(const ch of s){ w += ch.charCodeAt(0)>255 ? fs : fs*0.53; } return w; }
function wrap(s, fs, maxW){
  const out=[];
  for (const para of s.split('\n')) {
    let cur='';
    for (const ch of para) {
      if (tw(cur+ch, fs) > maxW && cur) { out.push(cur); cur=ch; }
      else cur += ch;
    }
    out.push(cur);
  }
  return out;
}
function measure(label){ // returns {lines:[{t,fs,bold}], h}
  const res=[]; let h=0;
  label.split('\n').forEach((ln,i)=>{
    const fs = i===0?12.5:11;
    const parts = wrap(ln, fs, 228);
    parts.forEach((t,j)=>res.push({t, fs, bold:(i===0)}));
  });
  h = 16 + res.length*16;
  return {lines:res, h:Math.max(64,h)};
}

const SKILL = {M0:'',D1:'/to-tickets',D2:'/grill-with-docs → /to-spec',P1:'/grill-with-docs',P2b:'/to-spec'};
const g = new dagre.graphlib.Graph({multigraph:true});
g.setGraph({rankdir:'LR', nodesep:26, ranksep:80, marginx:0, marginy:0});
g.setDefaultEdgeLabel(()=>({}));
const W=250, MAXW=W-22;
const measured={};
const labelOf = (id,t,s,k)=>{ const sk=SKILL[id]; return sk? t+'\n'+s+'\n〔'+sk+'〕' : t+'\n'+s; };
for (const [id,t,s,k] of N){ measured[id]=measure(labelOf(id,t,s,k)); }
const H=(id)=>measured[id].h;
for (const [id,t,s,k] of N) g.setNode(id,{width:W,height:H(id)});
for (const [a,b] of E) g.setEdge(a,b);
dagre.layout(g);

const OFF_X = 30, OFF_Y = 190;
const pos = {};
for (const [id] of N){ const n=g.node(id); pos[id]={x:Math.round(n.x-W/2+OFF_X), y:Math.round(n.y-H(id)/2+OFF_Y)}; }
let maxX=0,maxY=0;
for (const id in pos){ maxX=Math.max(maxX,pos[id].x+W); maxY=Math.max(maxY,pos[id].y+H(id)); }

const STYLE = {
 done:'rounded=1;whiteSpace=wrap;html=1;fillColor=#eeeeee;strokeColor=#999999;dashed=1;fontSize=12;',
 dec:'rounded=1;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;strokeWidth=2;fontSize=12;',
 mile:'rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;strokeWidth=3;fontSize=13;fontStyle=1;',
 infra:'rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;fontSize=12;',
 front:'rounded=1;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontSize=12;',
 feat:'rounded=1;whiteSpace=wrap;html=1;fillColor=#ffe6cc;strokeColor=#d79b00;fontSize=12;',
 admin:'rounded=1;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;fontSize=12;',
 acc:'rounded=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;fontSize=12;',
};
const kindOf = Object.fromEntries(N.map(([id,t,s,k])=>[id,k]));
for (const [id,t,s,k] of N){ measured[id]=measure(labelOf(id,t,s,k)); }

let cells='';
let idx=2;
const idmap={};
for (const [id,t,s,k] of N){ idmap[id]='c'+(idx++); }
for (const [id,t,s,k] of N){
  const p=pos[id];
  cells += '        <mxCell id="'+idmap[id]+'" value="'+esc(labelOf(id,t,s,k))+'" style="'+STYLE[k]+'" vertex="1" parent="1">\n'
        + '          <mxGeometry x="'+p.x+'" y="'+p.y+'" width="'+W+'" height="'+H(id)+'" as="geometry" />\n        </mxCell>\n';
}
for (const [a,b] of E){
  cells += '        <mxCell id="e'+(idx++)+'" style="edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;endArrow=block;endSize=8;strokeColor=#555555;" edge="1" parent="1" source="'+idmap[a]+'" target="'+idmap[b]+'">\n'
        + '          <mxGeometry relative="1" as="geometry" />\n        </mxCell>\n';
}
function esc(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/\n/g,'&lt;br&gt;');}

const title = 'P0 实施依赖图（DAG）· 工程骨架 → 里程碑完成 — 2026-10-30';
const legend = '图例：紫=决策票（grill-with-docs→to-spec）　蓝=基础设施　黄=前台功能　橙=前台配套/取数　红=后台管理　灰=验收与文书　绿=里程碑闸门\n'
 + '括号内为该节点主调用技能；实现票一律 /tdd 打底、/code-review 收口。箭头 A→B 读作「A 先完成，B 才能开工」。\n'
 + '已完成起点：M0（环境闸门 + 前后端骨架冒烟，票 05）；本图只覆盖 P0，P1 仅留闸门节点，P2 全部在图外。';
const head = '        <mxCell id="title" value="'+esc(title)+'" style="text;html=1;align=left;verticalAlign=middle;fontSize=18;fontStyle=1;fillColor=none;strokeColor=none;" vertex="1" parent="1"><mxGeometry x="30" y="20" width="900" height="40" as="geometry" /></mxCell>\n'
 + '        <mxCell id="legend" value="'+esc(legend)+'" style="rounded=0;whiteSpace=wrap;html=1;align=left;verticalAlign=top;fillColor=#fafafa;strokeColor=#999999;dashed=1;fontSize=11;spacing=8;" vertex="1" parent="1"><mxGeometry x="30" y="66" width="980" height="104" as="geometry" /></mxCell>\n';

const xml = '<?xml version="1.0" encoding="UTF-8"?>\n<mxfile host="dsh-drawai" agent="dsh-drawai" type="device">\n  <diagram id="p0dag" name="P0 DAG">\n'
 + '    <mxGraphModel dx="0" dy="0" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="'+Math.round(maxX+60)+'" pageHeight="'+Math.round(maxY+40)+'" math="0" shadow="0">\n'
 + '      <root>\n        <mxCell id="0" />\n        <mxCell id="1" parent="0" />\n'
 + head + cells + '      </root>\n    </mxGraphModel>\n  </diagram>\n</mxfile>\n';
fs.writeFileSync(process.argv[2], xml);

// standalone SVG for a quick visual check
const COL={done:'#eeeeee',dec:'#e1d5e7',mile:'#d5e8d4',infra:'#dae8fc',front:'#fff2cc',feat:'#ffe6cc',admin:'#f8cecc',acc:'#f5f5f5'};
const STR={done:'#999999',dec:'#9673a6',mile:'#82b366',infra:'#6c8ebf',front:'#d6b656',feat:'#d79b00',admin:'#b85450',acc:'#666666'};
let svg='<svg xmlns="http://www.w3.org/2000/svg" width="'+(maxX+60)+'" height="'+(maxY+40)+'" viewBox="0 0 '+(maxX+60)+' '+(maxY+40)+'"><rect width="100%" height="100%" fill="#ffffff"/>';
svg+='<text x="30" y="42" font-family="Microsoft YaHei,Segoe UI" font-size="18" font-weight="bold" fill="#222">'+title.replace(/&/g,'&amp;')+'</text>';
svg+='<rect x="30" y="66" width="980" height="104" fill="#fafafa" stroke="#999" stroke-dasharray="4 3"/>';
legend.split('\n').forEach((ln,i)=>{svg+='<text x="42" y="'+(90+i*30)+'" font-family="Microsoft YaHei,Segoe UI" font-size="12" fill="#333">'+ln.replace(/&/g,'&amp;')+'</text>';});
for (const [a,b] of E){
  const p=g.edge(a,b); const pts=[{x:pos[a].x+W,y:pos[a].y+H(a)/2},...(p.points||[]),{x:pos[b].x,y:pos[b].y+H(b)/2}];
  const d=pts.map((q,i)=>(i?'L':'M')+q.x.toFixed(0)+' '+q.y.toFixed(0)).join(' ');
  svg+='<path d="'+d+'" fill="none" stroke="#666" stroke-width="1.2" marker-end="url(#ar)"/>';
}
svg+='<defs><marker id="ar" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto"><path d="M0,0 L0,6 L8,3 z" fill="#666"/></marker></defs>';
for (const [id,t,s,k] of N){
  const p=pos[id]; const lines=measured[id].lines;
  svg+='<rect x="'+p.x+'" y="'+p.y+'" width="'+W+'" height="'+H(id)+'" rx="7" fill="'+COL[k]+'" stroke="'+STR[k]+'" stroke-width="'+(k==='mile'?3:1.5)+'"/>';
  let yy=p.y+(H(id)-lines.length*16)/2+13;
  lines.forEach((ln,i)=>{ const y=yy+i*16;
    svg+='<text x="'+(p.x+W/2)+'" y="'+y+'" text-anchor="middle" font-family="Microsoft YaHei,Segoe UI" font-size="'+ln.fs+'" font-weight="'+(ln.bold?600:400)+'" fill="#222">'+esc2(ln.t)+'</text>'; });
}
function esc2(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');}
svg+='</svg>';
fs.writeFileSync(process.argv[3], svg);
console.log(JSON.stringify({w:Math.round(maxX+60),h:Math.round(maxY+40),nodes:N.length,edges:E.length}));