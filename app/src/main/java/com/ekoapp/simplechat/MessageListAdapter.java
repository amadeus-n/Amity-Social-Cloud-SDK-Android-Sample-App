package com.ekoapp.simplechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekoapp.ekosdk.EkoMessage;
import com.ekoapp.ekosdk.EkoObjects;
import com.ekoapp.ekosdk.EkoUser;
import com.ekoapp.ekosdk.adapter.EkoMessageAdapter;
import com.ekoapp.ekosdk.messaging.data.TextData;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

import static com.ekoapp.simplechat.MessageListAdapter.MessageViewHolder;

public class MessageListAdapter extends EkoMessageAdapter<MessageViewHolder> {

    private final PublishSubject<EkoMessage> onLongClickSubject = PublishSubject.create();


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        EkoMessage m = getItem(position);

        ButterKnife.Setter<View, Integer> visibility = (view, value, index) -> view.setVisibility(value);

        if (EkoObjects.isProxy(m)) {
            ButterKnife.apply(holder.optionalViews, visibility, View.GONE);
            holder.messageIdTextView.setText(String.format("loading adapter position: %s", position));
        } else {
            ButterKnife.apply(holder.optionalViews, visibility, View.VISIBLE);
            Context context = holder.itemView.getContext();
            String type = m.getType();
            String senderId = m.getUserId();
            EkoUser sender = m.getUser();
            DateTime created = m.getCreatedAt();

            holder.messageIdTextView.setText(String.format("%s: %s",
                    m.getChannelSegment(),
                    m.getMessageId()));

            holder.senderTextView.setText(String.format("uid: %s (%s) %s:%s",
                    senderId,
                    sender != null ? sender.getDisplayName() : "",
                    sender != null && sender.isFlaggedByMe() ? "\uD83C\uDFC1" : "\uD83C\uDFF3️",
                    sender != null ? sender.getFlagCount() : 0));

            holder.syncStateTextview.setText(m.getSyncState().name());
            holder.timeTextview.setText(created.toString(DateTimeFormat.longDateTime()));

            if ("text".equalsIgnoreCase(type)) {
                holder.dataTextview.setText(String.format("%s %s:%s",
                        m.getData(TextData.class).getText(),
                        m.isFlaggedByMe() ? "\uD83C\uDFC1" : "\uD83C\uDFF3️",
                        m.getFlagCount()));
            } else {
                holder.dataTextview.setText(String.format("data type: %s", type));
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            onLongClickSubject.onNext(m);
            return true;
        });
    }

    Flowable<EkoMessage> getOnLongClickFlowable() {
        return onLongClickSubject.toFlowable(BackpressureStrategy.BUFFER);
    }


    static class MessageViewHolder extends BaseViewHolder {

        @BindViews({
                R.id.sender_textview,
                R.id.data_textview,
                R.id.sync_state_textview,
                R.id.time_textview
        })
        List<View> optionalViews;

        @BindView(R.id.message_id_textview)
        TextView messageIdTextView;
        @BindView(R.id.sender_textview)
        TextView senderTextView;
        @BindView(R.id.data_textview)
        TextView dataTextview;
        @BindView(R.id.sync_state_textview)
        TextView syncStateTextview;
        @BindView(R.id.time_textview)
        TextView timeTextview;


        MessageViewHolder(View itemView) {
            super(itemView);
        }
    }
}
