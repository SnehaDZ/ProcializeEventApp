package com.procialize.eventsapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.procialize.eventsapp.Adapter.AgendaFolderListAdapter;
import com.procialize.eventsapp.DbHelper.DBHelper;
import com.procialize.eventsapp.GetterSetter.AgendaVacationList;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul on 03-07-2018.
 */

public class AgendaWebFragment extends Fragment {

    private ListView agendaList;
    private List<AgendaVacationList> agendaDBList;
    private List<AgendaVacationList> agendaOneList;
    private DBHelper dbHelper;
    private AgendaFolderListAdapter adapter;
    private String sessionDate;
    TextView agenda_time_slot, agenda_title, attendee_comp_name,
            agenda_desc;
    LinearLayout agenda_list_layout;
    WebView webviewAgenda;
//    MixpanelAPI mixpanel;
    private SQLiteDatabase db;

    public static String bodyData = "Agenda Data";
    // Session Manager Class
    SessionManager session;
   // Bundle b = new Bundle();
    String topMgmtFlag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.agenda_list_row,
                container, false);

        // Session Manager
        session = new SessionManager(getActivity().getApplicationContext());

        topMgmtFlag = session.getSkipFlag();

        Bundle b = getArguments();
        if (b != null)
            sessionDate = b.getString("sessionDate");

        // int current = tabHost.getTabHost() ;

        // Toast.makeText(getActivity(), "Inside Agenda Fragment One",
        // Toast.LENGTH_LONG).show();
        System.out.println("Inside Agenda Fragment One");
        // Runtime rt = Runtime.getRuntime();
        // long maxMemory = rt.maxMemory();
        // Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));

        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getWritableDatabase();
        agendaDBList = new ArrayList<AgendaVacationList>();
        agendaOneList = new ArrayList<AgendaVacationList>();

        webviewAgenda = (WebView) rootView
                .findViewById(R.id.webviewAgenda);

        agenda_desc = (TextView) getActivity()
                .findViewById(R.id.agenda_desc);
        agendaDBList = dbHelper.getAgendaFolderList();

        if(agendaOneList.size()>0){
            agendaOneList.clear();
        }

        // TODO
        for (int i = 0; i < agendaDBList.size(); i++) {
            if (agendaDBList.get(i).getFolder_name()
                    .equalsIgnoreCase(sessionDate)) {
                agendaOneList.add(agendaDBList.get(i));
            }
        }

        ArrayList<String> ratedList = new ArrayList<String>();
        for (int j = 0; j < agendaOneList.size(); j++) {

            ratedList.add(agendaOneList.get(j).getSessionId());

        }
       // Toast.makeText(getActivity(), sessionDate, Toast.LENGTH_SHORT).show();


        // Strore Rated List to Shared Preference
        SharedPreferences prefs = getActivity().getApplicationContext()
                .getSharedPreferences("RatedList" + sessionDate,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();

        int size = prefs.getInt("size", 0);

        if (size == 0) {
            for (int i = 0; i < ratedList.size(); i++) {
                edit.putString("val" + i, ratedList.get(i));
            }
            edit.putInt("size", ratedList.size());
            edit.commit();

        }


        Log.e("check.....",agendaOneList.get(0).getSession_description());

        bodyData = agendaOneList.get(0).getSession_description();
        System.out.println(bodyData);


        webviewAgenda.getSettings().setLoadsImagesAutomatically(true);
        webviewAgenda.clearCache(true);
        webviewAgenda.clearHistory();
        webviewAgenda.getSettings().setJavaScriptEnabled(true);
        webviewAgenda.setWebViewClient(new WebViewClient());

        //holder.webviewAgenda.setBackgroundColor(Color.TRANSPARENT);
        //holder.webviewAgenda.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

		/*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			agenda_desc.setText(Html.fromHtml(bodyData,Html.FROM_HTML_MODE_LEGACY));
		} else {
			agenda_desc.setText(Html.fromHtml(bodyData));
		}*/
        /*webviewAgenda.postDelayed(new Runnable() {
            @Override
            public void run() {
                webviewAgenda.loadDataWithBaseURL("", bodyData, "text/html", "UTF-8", "");
            }
        }, 500);*/
        // Toast.makeText(getActivity(), bodyData, Toast.LENGTH_SHORT).show();
        //webviewAgenda.loadDataWithBaseURL("", bodyData, "text/html", "UTF-8", "");
        print();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);





    }

    public void print(){
       // agenda_desc.setText(bodyData);
        webviewAgenda.loadDataWithBaseURL("", bodyData, "text/html", "UTF-8", "");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        agendaOneList.clear();
       // b.clear();
        super.onDestroy();
    }


}
