package com.procialize.eventsapp.GetterSetter;

/**
 * Created by Naushad on 12/18/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QASessionFetch {

    @SerializedName("agenda_question")
    @Expose
    private List<AgendaQuestion> agendaQuestion = null;
    @SerializedName("question_agenda_list")
    @Expose
    private List<AgendaLisQA> agendaList = null;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("msg")
    @Expose
    private String msg;

    public List<AgendaQuestion> getAgendaQuestion() {
        return agendaQuestion;
    }

    public void setAgendaQuestion(List<AgendaQuestion> agendaQuestion) {
        this.agendaQuestion = agendaQuestion;
    }

    public List<AgendaLisQA> getAgendaList() {
        return agendaList;
    }

    public void setAgendaList(List<AgendaLisQA> agendaList) {
        this.agendaList = agendaList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}