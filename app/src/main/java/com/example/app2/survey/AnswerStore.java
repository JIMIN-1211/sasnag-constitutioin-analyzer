package com.example.app2.survey;

import android.util.SparseIntArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerStore {
    private static final AnswerStore INSTANCE = new AnswerStore();
    private final SparseIntArray map = new SparseIntArray(); // questionId -> selectedIndex(0~2)

    private AnswerStore() {}

    public static AnswerStore get() { return INSTANCE; }

    public void put(int questionId, int selectedIndex) { map.put(questionId, selectedIndex); }

    public int get(int questionId) { return map.get(questionId, -1); }

    public void clear() { map.clear(); }

    /** 백엔드 스펙: answerId는 1-based */
    public JSONArray toJsonArray1Based() throws JSONException {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < map.size(); i++) {
            int qid = map.keyAt(i);
            int idx = map.valueAt(i);
            JSONObject o = new JSONObject();
            o.put("questionId", qid);
            o.put("answerId", idx + 1);
            arr.put(o);
        }
        return arr;
    }
}
