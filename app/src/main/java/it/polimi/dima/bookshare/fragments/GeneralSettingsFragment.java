package it.polimi.dima.bookshare.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.Profile;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MainActivity;
import it.polimi.dima.bookshare.activities.SettingsActivity;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class GeneralSettingsFragment extends Fragment {

    private ManageUser manageUser;
    private TextView userName, userSurname, email, phone;
    private LinearLayout setUserName, setUserSurname, setEmail, setPhone;

    public GeneralSettingsFragment() {

    }

    public static Fragment newInstance() {

        return new GeneralSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_general_settings, container, false);

        // get screen dimensions
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;


        manageUser = new ManageUser(getActivity());

        userName = (TextView) view.findViewById(R.id.set_username_text);
        userSurname = (TextView) view.findViewById(R.id.set_usersurname_text);
        email = (TextView) view.findViewById(R.id.set_email_text);
        phone = (TextView) view.findViewById(R.id.set_phone_text);

        setUserName = (LinearLayout) view.findViewById(R.id.set_username);
        setUserSurname = (LinearLayout) view.findViewById(R.id.set_usersurname);
        setEmail = (LinearLayout) view.findViewById(R.id.set_email);
        setPhone = (LinearLayout) view.findViewById(R.id.set_phone);

        User user = manageUser.getUser();
        userName.setText(user.getName());
        userSurname.setText(user.getSurname());
        email.setText(user.getEmail());
        phone.setText(user.getPhoneNumber());

        setUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.change_username));

                View promptsView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_settings_dialog, null);
                builder.setView(promptsView);

                final EditText dialogEdit = (EditText) promptsView.findViewById(R.id.dialog_edittxt);
                dialogEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setPositiveButton(getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!dialogEdit.getText().equals("")) {

                            userName.setText(dialogEdit.getText().toString());

                            User user = manageUser.getUser();

                            user.setName(dialogEdit.getText().toString());

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);
                        }

                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        setUserSurname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.change_usersurname));

                View promptsView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_settings_dialog, null);
                builder.setView(promptsView);

                final EditText dialogEdit = (EditText) promptsView.findViewById(R.id.dialog_edittxt);

                dialogEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setPositiveButton(getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!dialogEdit.getText().equals("")) {

                            userSurname.setText(dialogEdit.getText().toString());

                            User user = manageUser.getUser();

                            user.setSurname(dialogEdit.getText().toString());

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);
                        }

                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        setEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.change_email));

                View promptsView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_settings_dialog, null);
                builder.setView(promptsView);

                final EditText dialogEdit = (EditText) promptsView.findViewById(R.id.dialog_edittxt);

                dialogEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                builder.setPositiveButton(getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!dialogEdit.getText().equals("")) {

                            email.setText(dialogEdit.getText().toString());

                            User user = manageUser.getUser();

                            user.setEmail(dialogEdit.getText().toString());

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);
                        }

                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        setPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.change_phone));

                View promptsView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_settings_dialog, null);
                builder.setView(promptsView);

                final EditText dialogEdit = (EditText) promptsView.findViewById(R.id.dialog_edittxt);

                dialogEdit.setInputType(InputType.TYPE_CLASS_PHONE);

                builder.setPositiveButton(getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!dialogEdit.getText().equals("")) {

                            phone.setText(dialogEdit.getText().toString());

                            User user = manageUser.getUser();

                            user.setPhoneNumber(dialogEdit.getText().toString());

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);
                        }

                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        return view;

    }
}
