package za.co.beeswins;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.*;
import android.view.*;
import android.widget.*;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(0, 90, 60);
    private static final int GREEN_DARK = Color.rgb(5, 72, 51);
    private static final int GREEN_SOFT = Color.rgb(235, 245, 239);
    private static final int GOLD = Color.rgb(242, 190, 74);
    private static final int BG = Color.rgb(247, 249, 247);
    private static final int TEXT = Color.rgb(25, 35, 30);
    private static final int MUTED = Color.rgb(112, 120, 116);
    private static final int RED = Color.rgb(184, 44, 44);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");
    private static final int EXPORT_CSV = 88;

    private DB db;
    private LinearLayout shell;
    private long editId = -1;
    private boolean editSold = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(GREEN);
        getWindow().setNavigationBarColor(GREEN_DARK);
        db = new DB(this);
        showDashboard();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius)); return g;
    }
    private GradientDrawable roundedStroke(int color, float radius, int strokeColor, float sw) {
        GradientDrawable g = rounded(color, radius); g.setStroke(dp(sw), strokeColor); return g;
    }
    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private Space space(int h) { Space s = new Space(this); s.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h))); return s; }

    private LinearLayout baseShell(boolean dashboard, String title, String subtitle) {
        shell = new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL); shell.setBackgroundColor(BG);
        setContentView(shell);
        shell.addView(dashboard ? heroHeader(subtitle) : slimHeader(title, subtitle));
        return shell;
    }

    private View heroHeader(String subtitle) {
        LinearLayout wrap = new LinearLayout(this); wrap.setOrientation(LinearLayout.HORIZONTAL); wrap.setGravity(Gravity.CENTER_VERTICAL);
        wrap.setPadding(dp(20), dp(14), dp(20), dp(14)); wrap.setBackgroundColor(GREEN);
        wrap.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(130)));

        ImageView logo = new ImageView(this); logo.setImageResource(R.drawable.beeswins_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(70), dp(70)); ilp.setMargins(0,0,dp(14),0); wrap.addView(logo, ilp);

        LinearLayout words = new LinearLayout(this); words.setOrientation(LinearLayout.VERTICAL);
        words.addView(text("BeesWins", 32, Color.WHITE, true));
        words.addView(text(subtitle == null ? "Vandag se oorsig" : subtitle, 15, GOLD, false));
        wrap.addView(words, new LinearLayout.LayoutParams(0, -2, 1));

        TextView bell = text("●", 22, Color.WHITE, true); bell.setGravity(Gravity.CENTER);
        bell.setBackground(roundedStroke(GREEN_DARK, 24, Color.argb(80,255,255,255), 1));
        wrap.addView(bell, new LinearLayout.LayoutParams(dp(48), dp(48)));
        return wrap;
    }

    private View slimHeader(String title, String subtitle) {
        LinearLayout outer = new LinearLayout(this); outer.setOrientation(LinearLayout.VERTICAL); outer.setBackgroundColor(GREEN); outer.setPadding(dp(18), dp(8), dp(18), dp(14));
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this); logo.setImageResource(R.drawable.beeswins_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        top.addView(logo, new LinearLayout.LayoutParams(dp(46), dp(46)));
        TextView brand = text("BeesWins", 25, Color.WHITE, true); LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0,dp(54),1); blp.setMargins(dp(10),0,0,0); top.addView(brand, blp);
        outer.addView(top);
        outer.addView(text(title, 26, Color.WHITE, true));
        if (subtitle != null) outer.addView(text(subtitle, 13, Color.rgb(226,239,231), false));
        return outer;
    }

    private ScrollView scrollBody() {
        ScrollView sv = new ScrollView(this); sv.setFillViewport(true); sv.setBackgroundColor(BG);
        shell.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1)); return sv;
    }

    private View bottomNav(int active) {
        LinearLayout nav = new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setPadding(dp(6),dp(5),dp(6),dp(5)); nav.setGravity(Gravity.CENTER); nav.setBackgroundColor(GREEN_DARK);
        String[] labels = {"Dashboard","Beeste","Voeg by","Verslae"}; String[] icons = {"⌂","♉","＋","▥"};
        for (int i=0;i<4;i++) {
            final int idx=i;
            LinearLayout item = new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL); item.setGravity(Gravity.CENTER);
            TextView ic=text(icons[i], i==2?28:22, i==active?GOLD:Color.WHITE, true); ic.setGravity(Gravity.CENTER); item.addView(ic,new LinearLayout.LayoutParams(-1,dp(31)));
            TextView lb=text(labels[i],12,i==active?GOLD:Color.WHITE,i==active); lb.setGravity(Gravity.CENTER); item.addView(lb,new LinearLayout.LayoutParams(-1,dp(24)));
            item.setOnClickListener(v->{ if(idx==0)showDashboard(); else if(idx==1)showCattle(); else if(idx==2)showForm(-1); else showReports(); });
            nav.addView(item,new LinearLayout.LayoutParams(0,dp(62),1));
        }
        return nav;
    }

    private LinearLayout contentColumn() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16), dp(16), dp(16), dp(22)); return c;
    }

    private void showDashboard() {
        baseShell(true, null, "Vandag se oorsig");
        ScrollView sv = scrollBody(); LinearLayout c=contentColumn(); sv.addView(c);
        DB.Summary s=db.summary();
        c.addView(metricRow(metricCard("Beeste in voorraad", String.valueOf(s.stockCount), "Totale waarde", money(s.stockCapital), "♉"),
                metricCard("Verkoop totaal", String.valueOf(s.soldCount), "Totale verkope", money(s.totalSales), "⚖")));
        c.addView(space(12));
        c.addView(metricRow(metricCard("Totale wins", money(s.netProfit), s.netProfit>=0?"Wins":"Verlies", s.netProfit>=0?"▲ positief":"▼ negatief", "↗"),
                metricCard("Kapitaal in beeste", money(s.stockCapital), "Gem. waarde per bees", money(s.stockCount==0?0:s.stockCapital/s.stockCount), "R")));
        c.addView(space(14));

        LinearLayout chartCard=card(); chartCard.setPadding(dp(16),dp(14),dp(16),dp(14));
        chartCard.addView(text("Wins oor tyd",20,TEXT,true));
        chartCard.addView(text(money(s.netProfit),28,GREEN,true));
        chartCard.addView(text(s.soldCount==0?"Nog geen verkope nie":"Netto wins uit verkope",13,MUTED,false));
        chartCard.addView(new ProfitChart(this,db.profitSeries()),new LinearLayout.LayoutParams(-1,dp(150)));
        c.addView(chartCard);
        c.addView(space(18));

        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(text("Onlangse beeste aktiwiteit",19,TEXT,true),new LinearLayout.LayoutParams(0,dp(40),1));
        TextView all=text("Sien alles  ›",14,GREEN,true); all.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); all.setOnClickListener(v->showCattle()); head.addView(all,new LinearLayout.LayoutParams(dp(100),dp(40)));
        c.addView(head);
        List<Record> recent=db.recent(3);
        if(recent.isEmpty()) {
            LinearLayout empty=card(); empty.setPadding(dp(18),dp(20),dp(18),dp(20)); empty.addView(text("Nog geen beeste nie. Tik ‘Voeg by’ om jou eerste rekord te skep.",15,MUTED,false)); c.addView(empty);
        } else for(Record r:recent) c.addView(recordRow(r));
        shell.addView(bottomNav(0));
    }

    private LinearLayout metricRow(View a, View b) {
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(0,-2,1); lp1.setMargins(0,0,dp(6),0); row.addView(a,lp1);
        LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(0,-2,1); lp2.setMargins(dp(6),0,0,0); row.addView(b,lp2); return row;
    }
    private LinearLayout metricCard(String title,String value,String caption,String sub,String icon) {
        LinearLayout box=card(); box.setPadding(dp(14),dp(14),dp(14),dp(14)); box.setMinimumHeight(dp(138));
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView ic=text(icon,20,GREEN,true); ic.setGravity(Gravity.CENTER); ic.setBackground(rounded(GREEN_SOFT,25)); top.addView(ic,new LinearLayout.LayoutParams(dp(46),dp(46)));
        TextView ti=text(title,14,TEXT,false); LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(0,-2,1); tlp.setMargins(dp(10),0,0,0); top.addView(ti,tlp); box.addView(top);
        TextView val=text(value,22,GREEN,true); val.setPadding(0,dp(8),0,0); box.addView(val);
        TextView cap=text(caption,12,MUTED,false); cap.setPadding(0,dp(7),0,0); box.addView(cap);
        box.addView(text(sub,14, sub.contains("negatief")?RED:GREEN,true)); return box;
    }
    private LinearLayout card() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setBackground(rounded(Color.WHITE,18)); l.setElevation(dp(2)); return l; }

    private View recordRow(Record r) {
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(12),dp(12),dp(12)); row.setBackground(rounded(Color.WHITE,16)); row.setElevation(dp(1));
        TextView avatar=text("♉",23,Color.WHITE,true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(rounded(GREEN,30)); row.addView(avatar,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.addView(text(r.tag,17,TEXT,true)); info.addView(text(r.auction,13,MUTED,false)); info.addView(text(r.purchaseDate+(r.sold?" • Verkoop":" • Voorraad"),12,MUTED,false)); LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(0,-2,1); ilp.setMargins(dp(12),0,dp(8),0); row.addView(info,ilp);
        LinearLayout price=new LinearLayout(this); price.setOrientation(LinearLayout.VERTICAL); price.setGravity(Gravity.RIGHT);
        TextView pv=text(money(r.sold?r.salePrice:r.purchasePrice),16,GREEN,true); pv.setGravity(Gravity.RIGHT); price.addView(pv);
        TextView st=text(r.sold?(r.netProfit()>=0?"Wins: "+money(r.netProfit()):"Verlies: "+money(Math.abs(r.netProfit()))):"In voorraad",12,r.sold?(r.netProfit()>=0?GREEN:RED):MUTED,r.sold); st.setGravity(Gravity.RIGHT); price.addView(st); row.addView(price,new LinearLayout.LayoutParams(dp(120),-2));
        row.setOnClickListener(v->showForm(r.id));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(8)); row.setLayoutParams(lp); return row;
    }

    private void showCattle() { showCattleFiltered("all",""); }
    private void showCattleFiltered(String filter,String query) {
        baseShell(false,"Beeste","Soek en bestuur jou vee-rekords");
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(14),dp(12),dp(14),0); shell.addView(page,new LinearLayout.LayoutParams(-1,0,1));
        EditText search=new EditText(this); search.setHint("Soek beesnommer of veiling"); search.setSingleLine(true); search.setText(query); styleInput(search); page.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout filters=new LinearLayout(this); filters.setOrientation(LinearLayout.HORIZONTAL); filters.setPadding(0,dp(10),0,dp(10));
        String[] fs={"all","stock","sold"}; String[] fl={"Almal","Voorraad","Verkoop"};
        for(int i=0;i<3;i++){ final String f=fs[i]; TextView b=text(fl[i],13,f.equals(filter)?Color.WHITE:GREEN,true); b.setGravity(Gravity.CENTER); b.setBackground(f.equals(filter)?rounded(GREEN,20):roundedStroke(Color.WHITE,20,GREEN,1)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(40),1); if(i>0)lp.setMargins(dp(7),0,0,0); filters.addView(b,lp); b.setOnClickListener(v->showCattleFiltered(f,search.getText().toString())); }
        page.addView(filters);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(0,0,0,dp(16)); sv.addView(list); page.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        Runnable rebuild=()->{ list.removeAllViews(); List<Record> rs=db.list(filter,search.getText().toString()); if(rs.isEmpty()) { LinearLayout e=card(); e.setPadding(dp(18),dp(20),dp(18),dp(20)); e.addView(text("Geen rekords gevind nie.",15,MUTED,false)); list.addView(e); } else for(Record r:rs)list.addView(recordRow(r)); };
        rebuild.run(); search.addTextChangedListener(new SimpleWatcher(){ public void afterTextChanged(Editable e){ rebuild.run(); }});
        shell.addView(bottomNav(1));
    }

    private static class Field { LinearLayout box; EditText input; Field(LinearLayout b,EditText i){box=b;input=i;} }
    private void showForm(long id) {
        editId=id; Record existing=id<0?null:db.get(id); editSold=existing!=null&&existing.sold;
        baseShell(false, id<0?"Nuwe Bees":"Bees Rekord", id<0?"Voeg 'n nuwe bees rekord by":"Werk aankoop of verkoop besonderhede by");
        ScrollView sv=scrollBody(); LinearLayout c=contentColumn(); sv.addView(c);

        Field tag=field("Oorplaatjie / Beesnommer","Bv. ABC1234",android.text.InputType.TYPE_CLASS_TEXT);
        Field auction=field("Veiling gekoop","Bv. OVK Vryburg",android.text.InputType.TYPE_CLASS_TEXT); c.addView(formRow(tag.box,auction.box)); c.addView(space(8));
        Field pdate=field("Aankoopdatum","dd/mm/jjjj",android.text.InputType.TYPE_CLASS_TEXT); Field pprice=field("Aankoopprys (R)","0.00",numberType()); c.addView(formRow(pdate.box,pprice.box)); c.addView(space(8));
        Field pweight=field("Aankoopgewig (kg)","0.0",numberType()); Field sdate=field("Verkoopdatum","dd/mm/jjjj",android.text.InputType.TYPE_CLASS_TEXT); c.addView(formRow(pweight.box,sdate.box)); c.addView(space(8));
        Field sprice=field("Verkoopprys (R)","0.00",numberType()); Field sweight=field("Verkoopgewig (kg)","0.0",numberType()); c.addView(formRow(sprice.box,sweight.box)); c.addView(space(12));
        Field transport=field("Vervoer (R)","0.00",numberType()); Field commission=field("Kommissie (R)","0.00",numberType()); c.addView(formRow(transport.box,commission.box)); c.addView(space(8));
        Field feed=field("Voer (R)","0.00",numberType()); Field medicine=field("Medisyne (R)","0.00",numberType()); c.addView(formRow(feed.box,medicine.box)); c.addView(space(14));

        LinearLayout calc=card(); calc.setBackground(roundedStroke(GREEN_SOFT,18,Color.rgb(213,231,220),1)); calc.setPadding(dp(14),dp(12),dp(14),dp(14)); calc.addView(text("▥  Outomatiese Berekeninge",16,GREEN_DARK,true)); calc.addView(space(9));
        TextView buyKg=calcTile("Aankoop R/kg","R0.00",GREEN); TextView sellKg=calcTile("Verkoop R/kg","R0.00",GREEN); calc.addView(formRow((View)buyKg.getParent(),(View)sellKg.getParent())); calc.addView(space(8));
        TextView gross=calcTile("Bruto wins","R0.00",GOLD); TextView net=calcTile("Netto wins","R0.00",GREEN); calc.addView(formRow((View)gross.getParent(),(View)net.getParent())); c.addView(calc); c.addView(space(14));

        if(existing!=null){ set(tag,existing.tag);set(auction,existing.auction);set(pdate,existing.purchaseDate);set(pprice,num(existing.purchasePrice));set(pweight,num(existing.purchaseWeight));set(sdate,existing.saleDate);set(sprice,num(existing.salePrice));set(sweight,num(existing.saleWeight));set(transport,num(existing.transport));set(commission,num(existing.commission));set(feed,num(existing.feed));set(medicine,num(existing.medicine)); }

        TextWatcher tw=new SimpleWatcher(){ public void afterTextChanged(Editable e){ double pp=val(pprice),pw=val(pweight),sp=val(sprice),sw=val(sweight),cost=val(transport)+val(commission)+val(feed)+val(medicine); buyKg.setText(pp>0&&pw>0?"R"+fmt2(pp/pw):"R0.00"); sellKg.setText(sp>0&&sw>0?"R"+fmt2(sp/sw):"R0.00"); double g=sp-pp,n=g-cost; gross.setText((g<0?"-R":"R")+fmt2(Math.abs(g))); gross.setTextColor(g<0?RED:GOLD); net.setText((n<0?"-R":"R")+fmt2(Math.abs(n))); net.setTextColor(n<0?RED:GREEN); }};
        for(Field f:new Field[]{pprice,pweight,sprice,sweight,transport,commission,feed,medicine}) f.input.addTextChangedListener(tw); tw.afterTextChanged(null);

        Button save=button("▣  Stoor Rekord",GREEN,Color.WHITE); save.setOnClickListener(v->{ Record r=collect(existing,tag,auction,pdate,pprice,pweight,sdate,sprice,sweight,transport,commission,feed,medicine,editSold); if(r==null)return; if(editId<0)editId=db.insert(r); else db.update(r); Toast.makeText(this,"Rekord gestoor",Toast.LENGTH_SHORT).show(); showForm(editId); }); c.addView(save,new LinearLayout.LayoutParams(-1,dp(56))); c.addView(space(9));
        Button sold=button(editSold?"✓  Reeds as verkoop gemerk":"♉  Merk as verkoop",Color.WHITE,GREEN); sold.setBackground(roundedStroke(Color.WHITE,16,GREEN,2)); sold.setOnClickListener(v->{ if(val(sprice)<=0||val(sweight)<=0||sdate.input.getText().toString().trim().isEmpty()){Toast.makeText(this,"Vul verkoopdatum, verkoopprys en verkoopgewig in",Toast.LENGTH_LONG).show();return;} editSold=true; Record r=collect(existing,tag,auction,pdate,pprice,pweight,sdate,sprice,sweight,transport,commission,feed,medicine,true); if(r==null)return; if(editId<0)editId=db.insert(r); else db.update(r); Toast.makeText(this,"Bees as verkoop gemerk",Toast.LENGTH_SHORT).show(); showForm(editId); }); c.addView(sold,new LinearLayout.LayoutParams(-1,dp(54)));
        if(existing!=null){ c.addView(space(9)); Button del=button("Verwyder rekord",Color.TRANSPARENT,RED); del.setBackground(roundedStroke(Color.TRANSPARENT,16,RED,1)); del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Verwyder rekord?").setMessage("Hierdie aksie kan nie ongedaan gemaak word nie.").setNegativeButton("Kanselleer",null).setPositiveButton("Verwyder",(d,w)->{db.delete(editId);showCattle();}).show()); c.addView(del,new LinearLayout.LayoutParams(-1,dp(48))); }
        shell.addView(bottomNav(2));
    }

    private int numberType(){ return android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL; }
    private void set(Field f,String s){f.input.setText(s==null?"":s);} private String num(double d){return d==0?"":(Math.rint(d)==d?String.valueOf((long)d):String.valueOf(d));}
    private double val(Field f){try{return Double.parseDouble(f.input.getText().toString().replace(",","."));}catch(Exception e){return 0;}}
    private Field field(String label,String hint,int inputType){ LinearLayout b=new LinearLayout(this); b.setOrientation(LinearLayout.VERTICAL); b.addView(text(label,13,TEXT,false),new LinearLayout.LayoutParams(-1,dp(28))); EditText e=new EditText(this); e.setHint(hint); e.setTextSize(15); e.setTextColor(TEXT); e.setHintTextColor(Color.rgb(155,160,158)); e.setSingleLine(true); e.setInputType(inputType); styleInput(e); b.addView(e,new LinearLayout.LayoutParams(-1,dp(52))); return new Field(b,e); }
    private void styleInput(EditText e){ e.setPadding(dp(13),0,dp(13),0); e.setBackground(roundedStroke(Color.WHITE,14,Color.rgb(205,211,208),1)); }
    private LinearLayout formRow(View a,View b){LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams x=new LinearLayout.LayoutParams(0,-2,1); x.setMargins(0,0,dp(5),0); r.addView(a,x); LinearLayout.LayoutParams y=new LinearLayout.LayoutParams(0,-2,1); y.setMargins(dp(5),0,0,0); r.addView(b,y); return r;}
    private TextView calcTile(String label,String value,int color){ LinearLayout tile=new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setPadding(dp(12),dp(10),dp(12),dp(10)); tile.setBackground(rounded(Color.WHITE,14)); tile.addView(text(label,12,MUTED,false)); TextView v=text(value,19,color,true); tile.addView(v); return v; }
    private Button button(String label,int bg,int fg){ Button b=new Button(this); b.setText(label); b.setTextSize(16); b.setTextColor(fg); b.setAllCaps(false); b.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD); b.setBackground(rounded(bg,16)); return b; }

    private Record collect(Record existing,Field tag,Field auction,Field pdate,Field pprice,Field pweight,Field sdate,Field sprice,Field sweight,Field transport,Field commission,Field feed,Field medicine,boolean sold){
        String tg=tag.input.getText().toString().trim(), au=auction.input.getText().toString().trim(); if(tg.isEmpty()){tag.input.setError("Beesnommer is nodig");return null;} if(au.isEmpty()){auction.input.setError("Veiling is nodig");return null;} if(val(pprice)<=0){pprice.input.setError("Aankoopprys is nodig");return null;} if(val(pweight)<=0){pweight.input.setError("Aankoopgewig is nodig");return null;}
        Record r=existing==null?new Record():existing; r.id=editId; r.tag=tg;r.auction=au;r.purchaseDate=pdate.input.getText().toString().trim();r.purchasePrice=val(pprice);r.purchaseWeight=val(pweight);r.saleDate=sdate.input.getText().toString().trim();r.salePrice=val(sprice);r.saleWeight=val(sweight);r.transport=val(transport);r.commission=val(commission);r.feed=val(feed);r.medicine=val(medicine);r.sold=sold; return r;
    }

    private void showReports(){ baseShell(false,"Verslae","Wins, kapitaal en rugsteun"); ScrollView sv=scrollBody(); LinearLayout c=contentColumn(); sv.addView(c); DB.Summary s=db.summary();
        c.addView(metricRow(metricCard("Netto wins",money(s.netProfit),"Verkoopte beeste",String.valueOf(s.soldCount),"↗"),metricCard("Voorraad",String.valueOf(s.stockCount),"Kapitaal",money(s.stockCapital),"♉"))); c.addView(space(14));
        LinearLayout chartCard=card();chartCard.setPadding(dp(16),dp(14),dp(16),dp(14));chartCard.addView(text("Winsgrafiek",19,TEXT,true));chartCard.addView(new ProfitChart(this,db.profitSeries()),new LinearLayout.LayoutParams(-1,dp(180)));c.addView(chartCard);c.addView(space(14));
        LinearLayout stats=card();stats.setPadding(dp(16),dp(14),dp(16),dp(14));stats.addView(text("Handelsopsomming",18,TEXT,true));stats.addView(statLine("Totale aankope",money(s.totalPurchases)));stats.addView(statLine("Totale verkope",money(s.totalSales)));stats.addView(statLine("Ekstra koste",money(s.totalCosts)));stats.addView(statLine("Netto wins",money(s.netProfit)));c.addView(stats);c.addView(space(14));
        Button csv=button("⇩  Voer CSV-rugsteun uit",GREEN,Color.WHITE);csv.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/csv");i.putExtra(Intent.EXTRA_TITLE,"BeesWins-rugsteun.csv");startActivityForResult(i,EXPORT_CSV);});c.addView(csv,new LinearLayout.LayoutParams(-1,dp(56)));c.addView(space(8));
        c.addView(text("Alles word plaaslik op hierdie foon gestoor. Maak gereeld ’n CSV-rugsteun.",13,MUTED,false)); shell.addView(bottomNav(3)); }
    private View statLine(String a,String b){LinearLayout r=new LinearLayout(this);r.setPadding(0,dp(9),0,dp(9));r.addView(text(a,14,MUTED,false),new LinearLayout.LayoutParams(0,dp(32),1));TextView v=text(b,15,GREEN,true);v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);r.addView(v,new LinearLayout.LayoutParams(dp(160),dp(32)));return r;}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==EXPORT_CSV&&resultCode==RESULT_OK&&data!=null){Uri uri=data.getData();try(OutputStream os=getContentResolver().openOutputStream(uri)){os.write(db.csv().getBytes(StandardCharsets.UTF_8));Toast.makeText(this,"CSV-rugsteun gestoor",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Kon nie CSV stoor nie: "+e.getMessage(),Toast.LENGTH_LONG).show();}}}

    private String money(double v){ return (v<0?"-R":"R")+MONEY.format(Math.abs(v)); }
    private String fmt2(double v){ return String.format(Locale.US,"%.2f",v); }
    private abstract static class SimpleWatcher implements TextWatcher { public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){} }

    static class Record {
        long id=-1; String tag="",auction="",purchaseDate="",saleDate=""; double purchasePrice,purchaseWeight,salePrice,saleWeight,transport,commission,feed,medicine; boolean sold;
        double costs(){return transport+commission+feed+medicine;} double grossProfit(){return salePrice-purchasePrice;} double netProfit(){return grossProfit()-costs();}
    }

    static class DB extends SQLiteOpenHelper {
        static class Summary { int stockCount,soldCount; double stockCapital,totalSales,totalPurchases,totalCosts,netProfit; }
        DB(Context c){super(c,"beeswins_v11.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE cattle(id INTEGER PRIMARY KEY AUTOINCREMENT, tag TEXT NOT NULL, auction TEXT, purchaseDate TEXT, purchasePrice REAL, purchaseWeight REAL, saleDate TEXT, salePrice REAL, saleWeight REAL, transport REAL DEFAULT 0, commission REAL DEFAULT 0, feed REAL DEFAULT 0, medicine REAL DEFAULT 0, sold INTEGER DEFAULT 0)");}
        public void onUpgrade(SQLiteDatabase d,int old,int n){}
        ContentValues cv(Record r){ContentValues v=new ContentValues();v.put("tag",r.tag);v.put("auction",r.auction);v.put("purchaseDate",r.purchaseDate);v.put("purchasePrice",r.purchasePrice);v.put("purchaseWeight",r.purchaseWeight);v.put("saleDate",r.saleDate);v.put("salePrice",r.salePrice);v.put("saleWeight",r.saleWeight);v.put("transport",r.transport);v.put("commission",r.commission);v.put("feed",r.feed);v.put("medicine",r.medicine);v.put("sold",r.sold?1:0);return v;}
        long insert(Record r){return getWritableDatabase().insert("cattle",null,cv(r));}
        void update(Record r){getWritableDatabase().update("cattle",cv(r),"id=?",new String[]{String.valueOf(r.id)});}
        void delete(long id){getWritableDatabase().delete("cattle","id=?",new String[]{String.valueOf(id)});}
        Record get(long id){try(Cursor c=getReadableDatabase().query("cattle",null,"id=?",new String[]{String.valueOf(id)},null,null,null)){return c.moveToFirst()?read(c):null;}}
        List<Record> recent(int n){List<Record> a=new ArrayList<>();try(Cursor c=getReadableDatabase().query("cattle",null,null,null,null,null,"id DESC",String.valueOf(n))){while(c.moveToNext())a.add(read(c));}return a;}
        List<Record> list(String filter,String query){List<Record>a=new ArrayList<>();String where="1=1",q="%"+query+"%";List<String>args=new ArrayList<>();if("stock".equals(filter))where+=" AND sold=0";if("sold".equals(filter))where+=" AND sold=1";if(query!=null&&!query.trim().isEmpty()){where+=" AND (tag LIKE ? OR auction LIKE ?)";args.add(q);args.add(q);}try(Cursor c=getReadableDatabase().query("cattle",null,where,args.toArray(new String[0]),null,null,"id DESC")){while(c.moveToNext())a.add(read(c));}return a;}
        Record read(Cursor c){Record r=new Record();r.id=c.getLong(c.getColumnIndexOrThrow("id"));r.tag=c.getString(c.getColumnIndexOrThrow("tag"));r.auction=n(c,"auction");r.purchaseDate=n(c,"purchaseDate");r.purchasePrice=d(c,"purchasePrice");r.purchaseWeight=d(c,"purchaseWeight");r.saleDate=n(c,"saleDate");r.salePrice=d(c,"salePrice");r.saleWeight=d(c,"saleWeight");r.transport=d(c,"transport");r.commission=d(c,"commission");r.feed=d(c,"feed");r.medicine=d(c,"medicine");r.sold=c.getInt(c.getColumnIndexOrThrow("sold"))==1;return r;}
        String n(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);} double d(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?0:c.getDouble(i);}
        Summary summary(){Summary s=new Summary();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM cattle",null)){while(c.moveToNext()){Record r=read(c);s.totalPurchases+=r.purchasePrice;s.totalCosts+=r.costs();if(r.sold){s.soldCount++;s.totalSales+=r.salePrice;s.netProfit+=r.netProfit();}else{s.stockCount++;s.stockCapital+=r.purchasePrice+r.costs();}}}return s;}
        List<Float> profitSeries(){List<Float>a=new ArrayList<>();float sum=0;try(Cursor c=getReadableDatabase().query("cattle",null,"sold=1",null,null,null,"id ASC")){while(c.moveToNext()){Record r=read(c);sum+=(float)r.netProfit();a.add(sum);}}if(a.isEmpty()){a.add(0f);a.add(0f);}else if(a.size()==1)a.add(a.get(0));return a;}
        String csv(){StringBuilder b=new StringBuilder();b.append("Beesnommer,Veiling,Aankoopdatum,Aankoopprys,Aankoopgewig,Aankoop_R_per_kg,Verkoopdatum,Verkoopprys,Verkoopgewig,Verkoop_R_per_kg,Vervoer,Kommissie,Voer,Medisyne,Status,Bruto_wins,Netto_wins\n");for(Record r:list("all","")){double pk=r.purchaseWeight>0?r.purchasePrice/r.purchaseWeight:0,sk=r.saleWeight>0?r.salePrice/r.saleWeight:0;b.append(q(r.tag)).append(',').append(q(r.auction)).append(',').append(q(r.purchaseDate)).append(',').append(r.purchasePrice).append(',').append(r.purchaseWeight).append(',').append(pk).append(',').append(q(r.saleDate)).append(',').append(r.salePrice).append(',').append(r.saleWeight).append(',').append(sk).append(',').append(r.transport).append(',').append(r.commission).append(',').append(r.feed).append(',').append(r.medicine).append(',').append(r.sold?"Verkoop":"Voorraad").append(',').append(r.sold?r.grossProfit():0).append(',').append(r.sold?r.netProfit():0).append('\n');}return b.toString();}
        String q(String s){if(s==null)s="";return "\""+s.replace("\"","\"\"")+"\"";}
    }

    static class ProfitChart extends View {
        private final List<Float> values; private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final int green=Color.rgb(0,110,70); private final int grid=Color.rgb(225,231,227);
        ProfitChart(Context c,List<Float> v){super(c);values=v;setPadding(0,10,0,0);}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),l=12,r=w-12,t=18,b=h-20;p.setStrokeWidth(2);p.setColor(grid);for(int i=0;i<4;i++){float y=t+(b-t)*i/3f;c.drawLine(l,y,r,y,p);}float min=0,max=0;for(float v:values){min=Math.min(min,v);max=Math.max(max,v);}if(max-min<1){max=min+1;}Path line=new Path();Path area=new Path();for(int i=0;i<values.size();i++){float x=l+(r-l)*i/(values.size()-1f),y=b-(values.get(i)-min)/(max-min)*(b-t);if(i==0){line.moveTo(x,y);area.moveTo(x,b);area.lineTo(x,y);}else{line.lineTo(x,y);area.lineTo(x,y);}}area.lineTo(r,b);area.close();p.setColor(Color.argb(35,0,110,70));p.setStyle(Paint.Style.FILL);c.drawPath(area,p);p.setColor(green);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);c.drawPath(line,p);p.setStyle(Paint.Style.FILL);for(int i=0;i<values.size();i++){float x=l+(r-l)*i/(values.size()-1f),y=b-(values.get(i)-min)/(max-min)*(b-t);c.drawCircle(x,y,5,p);}}
    }
}
