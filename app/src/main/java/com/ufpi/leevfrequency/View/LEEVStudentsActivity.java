package com.ufpi.leevfrequency.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ufpi.leevfrequency.Adapters.LeevStudentsAdapter;
import com.ufpi.leevfrequency.Model.User;
import com.ufpi.leevfrequency.R;
import com.ufpi.leevfrequency.Utils.ConstantUtils;
import com.ufpi.leevfrequency.Utils.NavigationDrawerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LEEVStudentsActivity extends AppCompatActivity {

    //------------------------- NavigationDrawer---------------------------------------------------
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private SharedPreferences prefs = null;
    private DatabaseReference mDatabase;

    private ListView lLeevStudents;
    private ArrayList<User> leevStudents;
    private LeevStudentsAdapter leevStudentsAdapter;

    private LinearLayout linearLayoutLeevStudents;
    private LinearLayout linearLayoutNoStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leevstudents);

        linearLayoutLeevStudents = findViewById(R.id.linearLayoutLeevStudents);
        linearLayoutNoStudents = findViewById(R.id.linearLayoutNoStudents);

        prefs = getSharedPreferences("com.ufpi.leevfrequency", MODE_PRIVATE);

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child(ConstantUtils.DATABASE_ACTUAL_BRANCH)
                .child(ConstantUtils.USERS_BRANCH);

        lLeevStudents = findViewById(R.id.lLeevStudents);
        leevStudents = new ArrayList<>();

        leevStudentsAdapter = new LeevStudentsAdapter(leevStudents, getContext());
        lLeevStudents.setAdapter(leevStudentsAdapter);

        //----------------------------Configure NavigationDrawer------------------------------------
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_menu_white, getContext().getTheme());
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, myToolbar, R.string.open_drawer, R.string.close_drawer);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(drawable);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.getMenu().clear();

        View headerView = navigationView.getHeaderView(0);
        TextView nav_header_nome = (TextView) headerView.findViewById(R.id.nav_header_name);
        nav_header_nome.setText(prefs.getString("name", ""));

        TextView nav_header_email = (TextView) headerView.findViewById(R.id.nav_header_email);
        nav_header_email.setText(prefs.getString("email",""));

        if(prefs.getInt(ConstantUtils.USER_FIELD_USERTYPE, -1) == ConstantUtils.USER_TYPE_STUDENT){
            //Usuário é um estudante
            navigationView.inflateMenu(R.menu.menu_student);
        }
        else{
            //Usuário é um professor
            navigationView.inflateMenu(R.menu.menu_teacher);
        }

        //Configura o evento de seleção de algum item do menu do DrawerLayout
        navigationView.setNavigationItemSelectedListener(
                NavigationDrawerUtils.getNavigationDrawerItemSelectedListener(getContext(),
                        prefs.getInt(ConstantUtils.USER_FIELD_USERTYPE,-1), drawerLayout));

        mDatabase
                .orderByChild(ConstantUtils.USER_FIELD_USERTYPE)
                .equalTo(ConstantUtils.USER_TYPE_STUDENT)
                .addValueEventListener(getLeevStudents());
    }

    private ValueEventListener getLeevStudents(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    showLeevStudentsLayout();
                    notShowNoStudentsLayout();

                    for(DataSnapshot d: dataSnapshot.getChildren()){

                        if((Boolean)d.child(ConstantUtils.USER_FIELD_VISIBLE).getValue()){

                            User user = new User();
                            user.setId(d.getKey());
                            user.setName((String) d.child(ConstantUtils.USER_FIELD_NAME).getValue());
                            user.setEmail((String) d.child(ConstantUtils.USER_FIELD_EMAIL).getValue());
                            user.setProjects((String) d.child(ConstantUtils.USER_FIELD_PROJECTS).getValue());
                            user.setVisible((Boolean) d.child(ConstantUtils.USER_FIELD_VISIBLE).getValue());
                            user.setIdAdvisor((String) d.child(ConstantUtils.USER_FIELD_IDADVISOR).getValue());
                            user.setRegisterFinalized((Boolean) d.child(ConstantUtils.USER_FIELD_REGISTERFINALIZED).getValue());

                            leevStudents.add(user);
                        }

                    }

                    Collections.sort(leevStudents, new Comparator<User>() {
                        public int compare(User u1, User u2) {
                            return u1.getName().compareTo(u2.getName());
                        }
                    });

                    //Verifica se há alterações na lista de estudantes
                    leevStudentsAdapter.notifyDataSetChanged();
                }
                else{
                    notShowLeevStudentsLayout();
                    showNoStudentsLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private Context getContext(){
        return this;
    }

    private void showLeevStudentsLayout(){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 10.0f);
        linearLayoutLeevStudents.setLayoutParams(params);
    }
    private void notShowLeevStudentsLayout(){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0f);
        linearLayoutLeevStudents.setLayoutParams(params);
    }

    private void showNoStudentsLayout(){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 10.0f);
        linearLayoutNoStudents.setLayoutParams(params);
    }
    private void notShowNoStudentsLayout(){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0f);
        linearLayoutNoStudents.setLayoutParams(params);
    }
}
