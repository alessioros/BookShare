package it.polimi.dima.bookshare.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class GeneralSettingsFragment extends Fragment {

    private ManageUser manageUser;
    private TextView userName, userSurname, email, phone, distance;
    private LinearLayout setUserName, setUserSurname, setEmail, setPhone, setDistance;

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

        manageUser = new ManageUser(getActivity());

        userName = (TextView) view.findViewById(R.id.set_username_text);
        userSurname = (TextView) view.findViewById(R.id.set_usersurname_text);
        email = (TextView) view.findViewById(R.id.set_email_text);
        phone = (TextView) view.findViewById(R.id.set_phone_text);
        distance = (TextView) view.findViewById(R.id.set_distance_text);

        setUserName = (LinearLayout) view.findViewById(R.id.set_username);
        setUserSurname = (LinearLayout) view.findViewById(R.id.set_usersurname);
        setEmail = (LinearLayout) view.findViewById(R.id.set_email);
        setPhone = (LinearLayout) view.findViewById(R.id.set_phone);
        setDistance = (LinearLayout) view.findViewById(R.id.set_distance);

        User user = manageUser.getUser();
        userName.setText(user.getName());
        userSurname.setText(user.getSurname());
        email.setText(user.getEmail());
        phone.setText(user.getPhoneNumber());
        distance.setText("" + (int) manageUser.getDistance() / 1000 + " Km");

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

                        String nameString = dialogEdit.getText().toString();

                        if (!nameString.equals("") && isOnlyLetters(nameString)) {

                            userName.setText(nameString);

                            User user = manageUser.getUser();

                            user.setName(nameString);

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);

                        } else {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.invalid_name), Toast.LENGTH_SHORT);
                            toast.show();
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

                        String surnameString = dialogEdit.getText().toString();

                        if (!surnameString.equals("") && isOnlyLetters(surnameString)) {

                            userSurname.setText(surnameString);

                            User user = manageUser.getUser();

                            user.setSurname(surnameString);

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);

                        } else {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.invalid_surname), Toast.LENGTH_SHORT);
                            toast.show();
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

                        String emailString = dialogEdit.getText().toString();

                        if (isValidEmail(emailString)) {

                            email.setText(emailString);

                            User user = manageUser.getUser();

                            user.setEmail(emailString);

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);

                        } else {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.invalid_email), Toast.LENGTH_SHORT);
                            toast.show();
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

                        String phoneString = dialogEdit.getText().toString();

                        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneString) && phoneString.length() < 14 && phoneString.length() > 8) {

                            phone.setText(phoneString);

                            User user = manageUser.getUser();

                            user.setPhoneNumber(phoneString);

                            new DynamoDBManager(getActivity()).insertUser(user);

                            manageUser.saveUser(user);

                        } else {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.invalid_phone), Toast.LENGTH_SHORT);
                            toast.show();

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

        setDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.change_distance));

                View promptsView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_settings_dialog, null);
                builder.setView(promptsView);

                final EditText dialogEdit = (EditText) promptsView.findViewById(R.id.dialog_edittxt);
                dialogEdit.setInputType(InputType.TYPE_CLASS_NUMBER);

                builder.setPositiveButton(getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String distanceString = dialogEdit.getText().toString();
                        float distanceF = 0;
                        try {

                            distanceF = Float.parseFloat(distanceString);

                        } catch (Exception e) {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.invalid_distance), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        if (distanceF > 9 && distanceF < 10001) {

                            distance.setText(distanceString + " Km");

                            manageUser.setDistance(1000 * distanceF);


                        } else {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.invalid_distance_number), Toast.LENGTH_SHORT);
                            toast.show();

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

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public boolean isOnlyLetters(String name) {
        return name.matches("[a-zA-Z]+");
    }
}
