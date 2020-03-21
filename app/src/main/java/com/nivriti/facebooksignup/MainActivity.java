package com.nivriti.facebooksignup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    ProgressDialog mdialog;
    TextView txtEmail, txt_birthday, txt_friends;
    ImageView imgAvatar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        txt_birthday = (TextView) findViewById(R.id.txtBirthday);
        txt_friends = (TextView) findViewById(R.id.txtFriends);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        imgAvatar=(ImageView)findViewById(R.id.avatar);
        LoginButton loginButton = (LoginButton) findViewById(R.id.fblogin);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mdialog = new ProgressDialog(MainActivity.this);
                mdialog.setMessage("Reterving data");
                mdialog.show();

                String accesstoken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
//                        Bundle facebookdata = getData(object);
                        mdialog.dismiss();
                        Log.d("response", response.toString());
                        getData(object);
                    }
                });
                //Request Graph Api
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,birthday,friends");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {

            }
            @Override
            public void onError(FacebookException error) {

            }
        });
        if (AccessToken.getCurrentAccessToken() != null) {
            txtEmail.setText(AccessToken.getCurrentAccessToken().getUserId());
        }
        printkeyHash();
    }

    private void getData(JSONObject object) {
        try {
            URL profile_picture = new URL("https://graph.facebook.com/"+ object.getString("id") + "/picture?width=250&height=250");
            Picasso.with(MainActivity.this).load(profile_picture.toString()).into(imgAvatar);
            txtEmail.setText(object.getString("email"));
            txt_birthday.setText(object.getString("birthday"));
            txt_friends.setText("Friends:"+ object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void printkeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.nivriti.facebooksignup", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("keyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
