package com.heyoe.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.fragments.DetailPostFragment;
import com.heyoe.controller.fragments.LikeUsersFragment;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.controller.fragments.NewPostFragment;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DetailPostActivity extends AppCompatActivity {

    private ImageButton ibBack;
    private static FragmentManager fragmentManager;
    private static Activity mActivity;
    public static PostModel postModel;
    private int fromWhere;//0; from main wall, 1; from profile
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);


        initVariables();
        initUI();

    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
        postModel = (PostModel) getIntent().getSerializableExtra("post");
        if (postModel != null) {
            if (postModel.getMedia_type().endsWith("post_photo")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        fromWhere = getIntent().getIntExtra("from", 1);
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("post", DetailPostFragment.postModel);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

       pushFragment(0);
    }

    @Override
    public void onBackPressed() {

    }

    public static void pushFragment(int num) {
        switch (num) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new DetailPostFragment())
                        .commit();
                break;
            case 1:
                Bundle bundle1 = new Bundle();
                bundle1.putInt("isEdit", 1);
                bundle1.putSerializable("post", postModel);
                NewPostFragment fragobj = new NewPostFragment();
                fragobj.setArguments(bundle1);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragobj)
                        .commit();
                break;
            case 2:
                Bundle bundle2 = new Bundle();
                bundle2.putInt("isEdit", 2);
                bundle2.putSerializable("post", postModel);
                NewPostFragment fragobj1 = new NewPostFragment();
                fragobj1.setArguments(bundle2);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragobj1)
                        .commit();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 102:
                DetailPostFragment.updateSharedCount();
                if (resultCode == RESULT_OK) {

                }
                break;
        }
    }




    private static MenuItem edit, delete;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);

        edit = menu.findItem(R.id.ic_edit);
        delete = menu.findItem(R.id.ic_delete);

        if (postModel.getPoster_id().equals(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
            edit.setVisible(true);
            delete.setVisible(true);
        } else {
            edit.setVisible(false);
            delete.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ic_delete) {
            askToConfirmDeletePost();
            return true;
        } else if (id == R.id.ic_edit) {
            pushFragment(1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void askToConfirmDeletePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.confirm_delete_post));
        builder.setCancelable(true);
        builder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePost();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private void deletePost() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", postModel.getPost_id());

        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.DELETE_MY_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Intent intent = getIntent();
                                intent.putExtra("post", DetailPostFragment.postModel);
                                setResult(40, intent);
                                finish();
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, "post not exists");
                                finish();
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customRequest);
    }
}
