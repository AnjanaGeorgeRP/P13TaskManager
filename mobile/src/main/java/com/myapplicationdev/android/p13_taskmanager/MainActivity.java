package com.myapplicationdev.android.p13_taskmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ListView lv;
    ArrayList<Task> tasks;
    ArrayAdapter<Task> adapter;
    Button btnAdd;
    int actReqCode = 1;

    int idOfTask = -1;
    Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.lv);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        DBHelper dbh = new DBHelper(this);
        tasks = dbh.getAllTasks();
        adapter = new ArrayAdapter<Task>(this, android.R.layout.simple_list_item_1, tasks);
        lv.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddActivity.class);
                startActivityForResult(i, actReqCode);
            }
        });
        //set onItemClickListener to the list view
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                task = (Task) lv.getItemAtPosition(position);
                idOfTask = task.getId();
                registerForContextMenu(lv);
                return false;
            }
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0,0,0,"Edit");
        menu.add(0,1,1,"Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getItemId()==0) {
            //edit
            LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.edit_dialog, null);
            final EditText etName = (EditText)viewDialog.findViewById(R.id.etName);
            final EditText etDesc = (EditText)viewDialog.findViewById(R.id.etDescription);
            final EditText etTime = (EditText)viewDialog.findViewById(R.id.etTime);

            etName.setText(task.getName());
            etDesc.setText(task.getDescription());

            AlertDialog.Builder myBuilder = new AlertDialog.Builder(MainActivity.this);

            // Set the view of the dialog
            myBuilder.setView(viewDialog);
            myBuilder.setTitle("Edit My Task!");

            myBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newTitle = etName.getText().toString();
                    String newDesc = etDesc.getText().toString();
                    task.setName(newTitle);
                    task.setDescription(newDesc);
                    DBHelper dbh = new DBHelper(MainActivity.this);
                    dbh.updateTask(task);
                    int index = tasks.indexOf(task);
                    tasks.set(index,task);
                    adapter.notifyDataSetChanged();
                }
            });
            myBuilder.setNegativeButton("Cancel", null);

            AlertDialog myDialog = myBuilder.create();
            myDialog.show();

            return true;
        }else if(item.getItemId()==1) {
            //delete
            DBHelper dbh = new DBHelper(MainActivity.this);
            dbh.deleteTask(idOfTask);
            dbh.close();
            tasks.remove(task);
            adapter.notifyDataSetChanged();
            return true;

        }
        return super.onContextItemSelected(item); //pass menu item to the superclass implementation.
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == actReqCode) {
            if (resultCode == RESULT_OK) {
                DBHelper dbh = new DBHelper(MainActivity.this);
                tasks.clear();
                tasks.addAll(dbh.getAllTasks());
                dbh.close();
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
