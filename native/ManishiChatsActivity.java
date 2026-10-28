/* Manishi Telegram — REAL account-bound native chats vertical slice.
 * GPL-2.0-or-later. Visual contract derived from uploaded Manishi WebApp
 * colors, search, filter tabs and chat cards, not Telegram's default list.
 * Chat detail is intentionally delegated to Telegram's proven ChatActivity.
 */
package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public final class ManishiChatsActivity extends BaseFragment
        implements NotificationCenter.NotificationCenterDelegate {
    private static final int BG = Color.rgb(238,243,247);
    private static final int PAPER = Color.WHITE;
    private static final int TEXT = Color.rgb(17,24,39);
    private static final int MUTED = Color.rgb(112,123,139);
    private static final int BLUE = Color.rgb(34,158,217);
    private static final int SOFT = Color.rgb(244,247,249);
    private final ArrayList<TLRPC.Dialog> visible = new ArrayList<>();
    private final Runnable refreshRunnable = this::refresh;
    private RecyclerView list;
    private RecyclerView.Adapter<Holder> adapter;
    private TextView status;
    private EditText search;
    private LinearLayout filters;
    private int selected = 0;
    private String query = "";

    private int dp(int n) { return AndroidUtilities.dp(n); }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        return d;
    }
    private TextView text(Context c, String s, int size, int color, boolean bold) {
        TextView v = new TextView(c);
        v.setText(s); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL); v.setIncludeFontPadding(false);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    @Override public boolean onFragmentCreate() {
        super.onFragmentCreate();
        getNotificationCenter().addObserver(this, NotificationCenter.dialogsNeedReload);
        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.didReceiveNewMessages);
        return true;
    }
    @Override public void onFragmentDestroy() {
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
        getNotificationCenter().removeObserver(this, NotificationCenter.dialogsNeedReload);
        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().removeObserver(this, NotificationCenter.didReceiveNewMessages);
        super.onFragmentDestroy();
    }
    @Override public void didReceivedNotification(int id, int account, Object... args) {
        if (account != currentAccount) return;
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
        AndroidUtilities.runOnUIThread(refreshRunnable, 120);
    }
    @Override public void onResume() { super.onResume(); refresh(); }

    @Override public View createView(Context c) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Manishi Telegram");
        actionBar.setTitleColor(TEXT);
        actionBar.setBackgroundColor(PAPER);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        fragmentView = root;
        LinearLayout header = new LinearLayout(c);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18),dp(12),dp(18),dp(12));
        header.setBackgroundColor(PAPER);
        header.addView(text(c,"Chats",23,TEXT,true),
            new LinearLayout.LayoutParams(-1,dp(38)));
        status = text(c,"Live Telegram conversations",12,MUTED,false);
        header.addView(status,new LinearLayout.LayoutParams(-1,dp(27)));
        search = new EditText(c);
        search.setSingleLine(true); search.setTextSize(15);
        search.setTextColor(TEXT); search.setHintTextColor(MUTED);
        search.setHint("Search Chats"); search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setPadding(dp(15),0,dp(15),0);
        search.setBackground(shape(SOFT,15));
        header.addView(search,new LinearLayout.LayoutParams(-1,dp(46)));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) {
                query = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                refresh();
            }
            @Override public void afterTextChanged(Editable e) {}
        });
        filters = new LinearLayout(c);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(-1,dp(46));
        flp.topMargin = dp(12);
        header.addView(filters,flp);
        root.addView(header,new LinearLayout.LayoutParams(-1,-2));
        list = new RecyclerView(c);
        list.setLayoutManager(new LinearLayoutManager(c));
        list.setClipToPadding(false);
        list.setPadding(dp(12),dp(10),dp(12),dp(16));
        adapter = new RecyclerView.Adapter<Holder>() {
            @Override public Holder onCreateViewHolder(ViewGroup parent,int type) {
                Context ctx = parent.getContext();
                LinearLayout row = new LinearLayout(ctx);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(13),dp(8),dp(13),dp(8));
                row.setBackground(shape(PAPER,16));
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(-1,dp(82));
                lp.bottomMargin = dp(8); row.setLayoutParams(lp);
                TextView avatar = text(ctx,"?",18,PAPER,true);
                avatar.setGravity(Gravity.CENTER);
                avatar.setBackground(shape(BLUE,24));
                row.addView(avatar,new LinearLayout.LayoutParams(dp(46),dp(46)));
                LinearLayout middle = new LinearLayout(ctx);
                middle.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0,-2,1f);
                mp.leftMargin = dp(12); row.addView(middle,mp);
                TextView name = text(ctx,"",16,TEXT,true);
                name.setSingleLine(true); name.setEllipsize(TextUtils.TruncateAt.END);
                middle.addView(name,new LinearLayout.LayoutParams(-1,dp(26)));
                TextView preview = text(ctx,"",13,MUTED,false);
                preview.setSingleLine(true); preview.setEllipsize(TextUtils.TruncateAt.END);
                middle.addView(preview,new LinearLayout.LayoutParams(-1,dp(23)));
                LinearLayout trailing = new LinearLayout(ctx);
                trailing.setOrientation(LinearLayout.VERTICAL);
                trailing.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                row.addView(trailing,new LinearLayout.LayoutParams(dp(59),-1));
                TextView time = text(ctx,"",11,MUTED,false);
                time.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                trailing.addView(time,new LinearLayout.LayoutParams(-1,dp(24)));
                TextView unread = text(ctx,"",12,PAPER,true);
                unread.setGravity(Gravity.CENTER);
                unread.setBackground(shape(BLUE,12));
                trailing.addView(unread,new LinearLayout.LayoutParams(dp(27),dp(23)));
                return new Holder(row,avatar,name,preview,time,unread);
            }
            @Override public void onBindViewHolder(Holder h,int position) {
                TLRPC.Dialog d = visible.get(position);
                String name = displayName(d.id);
                h.name.setText(name);
                h.avatar.setText(name.isEmpty() ? "?" :
                    name.substring(0,name.offsetByCodePoints(0,1)).toUpperCase(Locale.ROOT));
                h.preview.setText(lastMessage(d.id));
                h.time.setText(d.last_message_date <= 0 ? "" :
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(
                        new Date(d.last_message_date * 1000L)));
                h.unread.setVisibility(d.unread_count > 0 ? View.VISIBLE : View.INVISIBLE);
                h.unread.setText(d.unread_count > 99 ? "99+" :
                    String.valueOf(d.unread_count));
                h.itemView.setContentDescription(name + ", " + d.unread_count + " unread messages");
                h.itemView.setOnClickListener(v -> open(d.id));
            }
            @Override public int getItemCount() { return visible.size(); }
        };
        list.setAdapter(adapter);
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        updateFilters(c);
        refresh();
        return fragmentView;
    }
    private void updateFilters(Context c) {
        if (filters == null) return;
        filters.removeAllViews();
        String[] names = {"All","Unread","Groups","Channels"};
        for (int i=0;i<names.length;i++) {
            final int choice=i;
            TextView b=text(c,names[i],13,i==selected ? PAPER : MUTED,i==selected);
            b.setGravity(Gravity.CENTER);
            b.setBackground(shape(i==selected ? BLUE : SOFT,14));
            b.setContentDescription(names[i]+(i==selected ? ", selected" : ""));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(35),1f);
            if(i!=0)p.leftMargin=dp(6);
            filters.addView(b,p);
            b.setOnClickListener(v->{selected=choice;updateFilters(c);refresh();});
        }
    }
    private String displayName(long id) {
        if (DialogObject.isEncryptedDialog(id)) {
            TLRPC.EncryptedChat e = getMessagesController().getEncryptedChat(
                DialogObject.getEncryptedChatId(id));
            TLRPC.User u=e==null ? null : getMessagesController().getUser(e.user_id);
            return u==null ? "Secret chat" : UserObject.getUserName(u);
        }
        if (DialogObject.isUserDialog(id)) {
            TLRPC.User u=getMessagesController().getUser(id);
            return u==null ? "User" : UserObject.getUserName(u);
        }
        TLRPC.Chat c=getMessagesController().getChat(-id);
        return c==null || c.title==null ? "Group or channel" : c.title;
    }
    private int kind(long id) {
        if(DialogObject.isEncryptedDialog(id)||DialogObject.isUserDialog(id))return 0;
        TLRPC.Chat c=getMessagesController().getChat(-id);
        return c!=null && ChatObject.isChannel(c) && !c.megagroup ? 3 : 2;
    }
    private String lastMessage(long id) {
        ArrayList<MessageObject> items=getMessagesController().dialogMessage.get(id);
        if(items==null||items.isEmpty()||items.get(0)==null||
           TextUtils.isEmpty(items.get(0).messageText))return "Open conversation";
        String s=items.get(0).messageText.toString().replace('\n',' ').trim();
        return s.length()>180 ? s.substring(0,180)+"…" : s;
    }
    private void refresh() {
        if(adapter==null||isFinished)return;
        ArrayList<TLRPC.Dialog> all=getMessagesController().getDialogs(0);
        visible.clear();
        if(all!=null)for(TLRPC.Dialog d:all) {
            if(d==null)continue;
            if(selected==1&&d.unread_count<=0)continue;
            if(selected==2&&kind(d.id)!=2)continue;
            if(selected==3&&kind(d.id)!=3)continue;
            if(!query.isEmpty()&&!displayName(d.id).toLowerCase(Locale.ROOT).contains(query))continue;
            visible.add(d);
        }
        adapter.notifyDataSetChanged();
        if(status!=null) status.setText(
            visible.isEmpty() ? "No matching synced chats — check your account" :
            "Live Telegram conversations · "+visible.size());
    }
    private void open(long id) {
        Bundle b=new Bundle();
        if(DialogObject.isEncryptedDialog(id))
            b.putInt("enc_id",DialogObject.getEncryptedChatId(id));
        else if(DialogObject.isUserDialog(id))b.putLong("user_id",id);
        else b.putLong("chat_id",-id);
        if(getMessagesController().checkCanOpenChat(b,this))
            presentFragment(new ChatActivity(b));
    }
    private static final class Holder extends RecyclerView.ViewHolder {
        final TextView avatar,name,preview,time,unread;
        Holder(View view,TextView a,TextView n,TextView p,TextView t,TextView u) {
            super(view);avatar=a;name=n;preview=p;time=t;unread=u;
        }
    }
}
