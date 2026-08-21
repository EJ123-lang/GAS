from pathlib import Path

path = Path('beeswins-v1.1/BeesWins/app/src/main/java/za/co/beeswins/MainActivity.java')
text = path.read_text(encoding='utf-8')


def rep(old: str, new: str):
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'Expected exactly one match, got {count}: {old[:120]}')
    text = text.replace(old, new, 1)

rep('Field transport=field("Vervoer (R)","0.00",numberType()); Field commission=field("Kommissie (R)","0.00",numberType());',
    'Field transport=field("Vervoer (R)","0.00",numberType()); Field commission=field("Kommissie (%)","Bv. 6",numberType());')

rep('set(commission,num(existing.commission));',
    'set(commission,num(existing.commissionPercent>0?existing.commissionPercent:(existing.salePrice>0&&existing.commission>0?existing.commission/existing.salePrice*100.0:0)));')

rep('double pp=val(pprice),pw=val(pweight),sp=val(sprice),sw=val(sweight),cost=val(transport)+val(commission)+val(feed)+val(medicine);',
    'double pp=val(pprice),pw=val(pweight),sp=val(sprice),sw=val(sweight),commissionRand=sp*val(commission)/100.0,cost=val(transport)+commissionRand+val(feed)+val(medicine);')

rep('net.setTextColor(n<0?RED:GREEN);',
    'net.setTextColor(n<0?RED:GREEN); ((TextView)commission.box.getChildAt(0)).setText("Kommissie (%) • "+money(commissionRand));')

rep('if(val(pweight)<=0){pweight.input.setError("Aankoopgewig is nodig");return null;}',
    'if(val(pweight)<=0){pweight.input.setError("Aankoopgewig is nodig");return null;} if(val(commission)<0||val(commission)>100){commission.input.setError("Persentasie moet tussen 0 en 100 wees");return null;}')

rep('r.transport=val(transport);r.commission=val(commission);r.feed=val(feed);',
    'r.transport=val(transport);r.commissionPercent=val(commission);r.commission=r.salePrice*r.commissionPercent/100.0;r.feed=val(feed);')

rep('double purchasePrice,purchaseWeight,salePrice,saleWeight,transport,commission,feed,medicine;',
    'double purchasePrice,purchaseWeight,salePrice,saleWeight,transport,commissionPercent,commission,feed,medicine;')

rep('DB(Context c){super(c,"beeswins_v11.db",null,1);}',
    'DB(Context c){super(c,"beeswins_v11.db",null,2);}')

rep('commission REAL DEFAULT 0, feed REAL DEFAULT 0,',
    'commission REAL DEFAULT 0, commissionPercent REAL DEFAULT 0, feed REAL DEFAULT 0,')

rep('public void onUpgrade(SQLiteDatabase d,int old,int n){}',
    'public void onUpgrade(SQLiteDatabase d,int old,int n){if(old<2){d.execSQL("ALTER TABLE cattle ADD COLUMN commissionPercent REAL DEFAULT 0");}}')

rep('v.put("commission",r.commission);v.put("feed",r.feed);',
    'v.put("commission",r.commission);v.put("commissionPercent",r.commissionPercent);v.put("feed",r.feed);')

rep('r.commission=d(c,"commission");r.feed=d(c,"feed");',
    'r.commission=d(c,"commission");r.commissionPercent=d(c,"commissionPercent");r.feed=d(c,"feed");')

rep('Vervoer,Kommissie,Voer,Medisyne,Status,Bruto_wins,Netto_wins',
    'Vervoer,Kommissie_persentasie,Kommissie_bedrag,Voer,Medisyne,Status,Bruto_wins,Netto_wins')

rep(".append(r.transport).append(',').append(r.commission).append(',').append(r.feed)",
    ".append(r.transport).append(',').append(r.commissionPercent).append(',').append(r.commission).append(',').append(r.feed)")

path.write_text(text, encoding='utf-8')
print('Applied BeesWins v1.1.3 commission percentage patch successfully.')
