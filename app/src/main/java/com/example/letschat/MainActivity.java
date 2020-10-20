
/**
 * Main Activity: Has TabView for Navigation
 **/

package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.letschat.LoginActivity;
import com.example.letschat.R;
import com.example.letschat.TabsAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabAccessorAdapter;

    private FirebaseUser currentuser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;
    private String currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializations
        //References to Firebase Database
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();
        RootReference = FirebaseDatabase.getInstance().getReference();


        //Setting Custom Toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Let's Chat");


        //Setting Tab Nav View
        mViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mTabAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabAccessorAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }


    /** Checking if user exists otherwise send to login Activity **/
    @Override
    protected void onStart() {
        super.onStart();
        if (currentuser == null) {
            SendUserToLoginActivity();
        } else {
            //Updating State
            updateUserStatus("online");

            VerifyUserExistance();
        }
    }

    //Updating State if the app is minimized or crashed
    @Override
    protected void onStop() {
        super.onStop();
        if (currentuser != null){
            updateUserStatus("offline");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentuser != null){
            updateUserStatus("offline");
        }
    }



    private void VerifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    /** Creating Options Available on toolbar**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    //Functions according to selected options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.logout_option:
                //If user logs out
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

            case R.id.find_friends_option:
                SendUserToFindFriendsActivity();
                break;

            case R.id.settings_option:
                SendUserToSettingsActivity();

                break;

            case R.id.creategroup_option:
                RequestNewGroup();
                break;
        }

        return true;
    }

    private void SendUserToFindFriendsActivity() {
        Intent FindFriendIntent = new Intent(MainActivity.this, FindFriends.class);
        startActivity(FindFriendIntent);
    }


    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, Settings.class);
        startActivity(settingsIntent);
    }

    private void RequestNewGroup() {

        //PopUp Alert Dialog Box with EditText
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("E.g: Community");
        groupNameField.setHintTextColor(getResources().getColor(R.color.extraText));
        groupNameField.setTextColor(getResources().getColor(R.color.extraText));
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Provide GroupName", Toast.LENGTH_SHORT).show();
                } else {

                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


    }

    private void CreateNewGroup(final String groupName) {
        RootReference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, groupName + " is Created Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        RootReference.child("Groups").child(groupName).setValue("messageKey", "");

    }


    //To store user's online or offline state
    public void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;

        //fetching current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        //Storing data to database
        HashMap<String, Object> onlineStateMap= new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserId= mAuth.getCurrentUser().getUid();

        RootReference.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);
    }
}