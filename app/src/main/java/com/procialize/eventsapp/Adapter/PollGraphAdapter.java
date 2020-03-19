package com.procialize.eventsapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.procialize.eventsapp.GetterSetter.LivePollOptionList;
import com.procialize.eventsapp.R;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

/**
 * Created by Rahul on 05-10-2018.
 */

public class PollGraphAdapter extends RecyclerView.Adapter<PollGraphAdapter.MyViewHolder> {

    String questionId;
    private List<LivePollOptionList> optionLists;
    private Context context;

    public PollGraphAdapter(Context context, List<LivePollOptionList> optionLists, String questionId) {
        this.optionLists = optionLists;
        this.context = context;
        this.questionId = questionId;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poll_row_graph, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final LivePollOptionList pollList = optionLists.get(position);

        String[] color = {"#0e73ba", "#00a89c", "#4d4d4d", "#949494", "#0e73ba", "#00a89c", "#4d4d4d", "#949494", "#0e73ba", "#00a89c", "#4d4d4d", "#949494"};
        Float totalUser = 0.0f;


        for (int k = 0; k < optionLists.size(); k++) {

            if (optionLists.get(k).getLivePollId()
                    .equalsIgnoreCase(questionId)) {
                totalUser = (totalUser + Float.parseFloat(optionLists
                        .get(k).getTotalUser()));

            }

        }
        LinearLayout.LayoutParams rpms2, rprms;
        LinearLayout l3 = new LinearLayout(context);

        LinearLayout ll2 = new LinearLayout(context);
        ll2.setOrientation(LinearLayout.HORIZONTAL);
        ll2.setPadding(10, 10, 10, 10);
        ll2.setBackgroundColor(Color.parseColor(color[position]));
        ll2.setMinimumHeight(40);
        Float weight = 0.0f;


        weight = ((Float.parseFloat(optionLists.get(position)
                .getTotalUser()) / totalUser) * 100);


        int num = Math.round(weight);

        rprms = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rprms.setMargins(0, 0, 0, 0);


        rpms2 = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, weight);
        rpms2.setMargins(0, 0, 0, 0);
        l3.setLayoutParams(rprms);
        l3.setWeightSum(100);
        l3.setMinimumHeight(40);

        holder.txtOptMessage.setText(StringEscapeUtils.unescapeJava(pollList.getOption()));
        holder.txtper.setText(num + "%");
        // holder.linGraph.setBackgroundColor(Color.parseColor(color[position]));

        l3.addView(ll2, rpms2);
        holder.linGraph.addView(l3);

    }

    @Override
    public int getItemCount() {
        return optionLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtOptMessage, txtper;
        public LinearLayout linGraph;

        public MyViewHolder(View view) {
            super(view);
            txtOptMessage = view.findViewById(R.id.txtOptMessage);
            txtper = view.findViewById(R.id.txtper);

            linGraph = view.findViewById(R.id.linGraph);


        }
    }


}