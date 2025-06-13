package com.pvc.circleup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.pvc.circleup.Fragments.ChatListFragment;
import com.pvc.circleup.Fragments.GroupChatFragment;
import com.pvc.circleup.Fragments.HomeFragment;
import com.pvc.circleup.Fragments.NotificationsFragment;
import com.pvc.circleup.Fragments.ProfileFragment;
import com.pvc.circleup.Fragments.UsersFragment;
import com.pvc.circleup.Notification.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;


public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    String mUid;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");

        firebaseAuth=FirebaseAuth.getInstance();

        bottomNavigationView=findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar.setTitle("Home");
        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();

        FirebaseUser fuser=firebaseAuth.getCurrentUser();

        checkUserStatus();



    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    private void updateToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
            Token mToken = new Token(token);
            ref.child(uid).setValue(mToken);
        } else {
            Log.w("UpdateToken", "User is null, token not updated");
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener
            =new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId())
            {
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment fragment1=new HomeFragment();
                    FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content,fragment1,"");
                    ft1.commit();
                    return true;

                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment2=new ProfileFragment();
                    FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content,fragment2,"");
                    ft2.commit();
                    return true;

                case R.id.nav_users:

                    actionBar.setTitle("Users");
                    UsersFragment fragment3=new UsersFragment();
                    FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content,fragment3,"");
                    ft3.commit();
                    return true;

                case R.id.nav_chats:

                    actionBar.setTitle("Chats");
                    ChatListFragment fragment4=new ChatListFragment();
                    FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content,fragment4,"");
                    ft4.commit();
                    return true;

                case R.id.nav_more:
                    showMoreOption();

                    return true;
            }

            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOption() {
        PopupMenu popupMenu=new PopupMenu(this,bottomNavigationView, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Notification");
        popupMenu.getMenu().add(Menu.NONE,1,0,"Group Chat");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();
                if (id==0){
                    actionBar.setTitle("Notifications");
                    NotificationsFragment fragment5=new NotificationsFragment();
                    FragmentTransaction ft5=getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.content,fragment5,"");
                    ft5.commit();
                }
                else if (id==1){

                    actionBar.setTitle("Group Chat");
                    GroupChatFragment fragment6=new GroupChatFragment();
                    FragmentTransaction ft6=getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.content,fragment6,"");
                    ft6.commit();

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // user is signed in, get token
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        updateToken(token); // custom method to save token in DB
                    });
        } else {
            // user is not signed in, redirect to login
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }


}