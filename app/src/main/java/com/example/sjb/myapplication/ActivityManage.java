package com.example.sjb.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wz_03 on 2019/1/10.
 */

public class ActivityManage {

    private static ActivityManage Instance;

    public static ActivityManage getInstance() {
        if (Instance == null) {
            Instance = new ActivityManage();
        }
        return Instance;
    }

    public static ArrayList<AppCompatActivity> activityList = new ArrayList<>();

    public void addActivity(AppCompatActivity activity) {
        activityList.add(0, activity);
//        loadList();
    }

    public void removeActivity() {
        int oldNum = currentActivity;
        if (activityList.get(1) instanceof MainActivity)
            currentActivity = 1;
        if (activityList.get(1) instanceof ActivityTwo)
            currentActivity = 2;
        if (activityList.get(1) instanceof ActivityThree)
            currentActivity = 3;
        if (activityList.get(1) instanceof ActivityFour)
            currentActivity = 4;
        if (activityList.get(1) instanceof ActivityFive)
            currentActivity = 5;
        Log.e("ActivityManage","["+oldNum+"/"+currentActivity+"]");
        activityList.get(0).finish();
        activityList.remove(0);
    }

    public void toHome() {
        while (true) {
            removeActivity();
            if (currentActivity == 1)
                break;
        }
    }

    void removeAll(){
        while (true){
            removeActivity();
            if(activityList.size() == 0 ) break;
        }
    }

    private int currentActivity = 1;

    public void changeActivity(String url, Class c, int current) {
        Log.e("ActivityManage", "currentActivity[" + currentActivity + "/" + current + "]");
        if (current != currentActivity) {
            currentActivity = current;
            Intent intent = new Intent(activityList.get(0), c);
            intent.putExtra("url", url);
            activityList.get(0).startActivity(intent);
//            loadList();
        }
    }


    private void loadList() {
        int size = activityList.size();
        while (true) {
            if (activityList.get(size - 1) instanceof MainActivity)
                Log.e("", "MainActivity[" + (size - 1) + "]");
            if (activityList.get(size - 1) instanceof ActivityTwo)
                Log.e("", "ActivityTwo[" + (size - 1) + "]");
            if (activityList.get(size - 1) instanceof ActivityThree)
                Log.e("", "ActivityThree[" + (size - 1) + "]");
            if (activityList.get(size - 1) instanceof ActivityFour)
                Log.e("", "ActivityFour[" + (size - 1) + "]");
            if (activityList.get(1) instanceof ActivityFive)
                Log.e("", "ActivityFive[" + (size - 1) + "]");
            size -= 1;
            if (size == 0)
                break;
        }
    }

}
