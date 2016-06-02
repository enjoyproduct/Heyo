package com.heyoe_chat.controller.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.heyoe_chat.R;
import com.heyoe_chat.controller.fragments.NewPostFragment;

import java.util.ArrayList;

/**
 * Created by dell17 on 4/21/2016.
 */
public class TagSelectDialog extends Dialog {
    private Activity mActivity;
    private AutoCompleteTextView autoCompleteTextView;

    public TagSelectDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.mActivity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_tag_selection);


        initUI();
    }
    private void initUI() {
//        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.actv_tag_selection);
        setAutoCompleteTextView();
    }
    private void setAutoCompleteTextView() {
        final ArrayList<String> arrayList = makeSampleData();
        ArrayAdapter<String> searchAgentAdapter;
        searchAgentAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1,arrayList);
        autoCompleteTextView.setAdapter(searchAgentAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewPostFragment.inputTag(arrayList.get(position));
                dismiss();
            }
        });
    }
//    for test
    private ArrayList<String> makeSampleData() {
        ArrayList<String> arrayList  = new ArrayList<>();
        for (int i = 0; i < 30; i ++) {
            String str = "@" + "Test User - " + String.valueOf(i);
            arrayList.add(str);
        }
        return arrayList;
    }
}
