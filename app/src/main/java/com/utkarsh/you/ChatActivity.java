package com.utkarsh.you;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    TextView recivername, asdada;
    CircleImageView profileimgg;
    EditText textmsg;
    CardView sendbtnn;
    // FirebaseDatabase database;
    String senderRoom;
    String receiverRoom;
    static String myUserid;
    RecyclerView messageRecyclerView;
    ArrayList<MsgModelClass> messagesArrayList;

    MessagesAdapter messagesAdapter;
    DatabaseReference chatRef;

    public static String profileImageUrl, myProfileimageurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }
        // Retrieve data passed from previous activity
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String username = intent.getStringExtra("username");
        profileImageUrl = intent.getStringExtra("profileImageUrl");
        myProfileimageurl = intent.getStringExtra("myProfileimageurl");
        myUserid = intent.getStringExtra("myUserid");
        String myUsername = intent.getStringExtra("myUsername");

        recivername = findViewById(R.id.recivername);
        profileimgg = findViewById(R.id.profileimgg);
        asdada = findViewById(R.id.asdada);
        textmsg = findViewById(R.id.textmsg);
        sendbtnn = findViewById(R.id.sendbtnn);
        messageRecyclerView = findViewById(R.id.msgadpter);
        messagesArrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        senderRoom = myUserid + userId;
        receiverRoom = userId + myUserid;
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(ChatActivity.this, messagesArrayList, senderRoom, receiverRoom);
        messageRecyclerView.setAdapter(messagesAdapter);

        recivername.setText(username);
        asdada.setText("Chatting with " + username);
        Glide.with(ChatActivity.this)
                .load(profileImageUrl)
                .placeholder(R.drawable.youicon) // Optional: placeholder image
                .error(R.drawable.youicon) // Optional: error image
                .into(profileimgg);

        chatRef = FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MsgModelClass messages = dataSnapshot.getValue(MsgModelClass.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
                messageRecyclerView.scrollToPosition(messagesArrayList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });

        sendbtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString().trim();
                if (message.isEmpty()) {
                    textmsg.setError("No Message found!");
                } else {
                    textmsg.setText("");
                    Date date = new Date();
                    MsgModelClass msgModelClass = new MsgModelClass(message, myUserid, date.getTime());

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference().child("chats").child(senderRoom).child("messages").push()
                            .setValue(msgModelClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    database.getReference().child("chats").child(receiverRoom).child("messages").push()
                                            .setValue(msgModelClass);
                                    messageRecyclerView.scrollToPosition(messagesArrayList.size() - 1);
                                }
                            });
                }
            }
        });
    }
}
