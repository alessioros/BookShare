package it.polimi.dima.bookshare.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.tables.User;

/**
 * Created by matteo on 14/04/16.
 */
public class DialogContact extends DialogFragment {

    private User user;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        user=getArguments().getParcelable("user");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view=inflater.inflate(R.layout.dialog_contact,null);

        CircularImageView imageContact=(CircularImageView) view.findViewById(R.id.image_contact);
        TextView nameContact=(TextView) view.findViewById(R.id.name_contact);
        TextView locationContact=(TextView) view.findViewById(R.id.location_contact);

        Picasso.with(getActivity()).load(user.getImgURL()).into(imageContact);
        nameContact.setText(user.getName()+" "+user.getSurname());
        locationContact.setText(user.getCity()+", "+user.getCountry());

        TextView textCellphone=(TextView) view.findViewById(R.id.textCellphone);
        TextView textEmail=(TextView) view.findViewById(R.id.textMail);
        TextView textFB=(TextView) view.findViewById(R.id.textFB);

        LinearLayout cellPhone=(LinearLayout) view.findViewById(R.id.cellphone);
        LinearLayout email=(LinearLayout) view.findViewById(R.id.mail);
        LinearLayout FB=(LinearLayout) view.findViewById(R.id.fb_messenger);


        if(user.getPhoneNumber()!=null) {
            textCellphone.setText(user.getPhoneNumber());
            cellPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startPhone();
                }
            });
        } else {
            cellPhone.setVisibility(LinearLayout.GONE);
        }

        if(user.getEmail()!=null) {
            textEmail.setText(user.getEmail());
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startEmail();
                }
            });
        } else {
            email.setVisibility(LinearLayout.GONE);
        }

        textFB.setText(R.string.send_fb_message);
        FB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFB();
            }
        });

        builder.setView(view);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void startPhone(){
        Uri number = Uri.parse("tel:"+user.getPhoneNumber());
        Intent callIntent = new Intent(Intent.ACTION_VIEW, number);
        startActivity(callIntent);
    }

    public void startEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",user.getEmail(), null));
        startActivity(emailIntent);
    }

    public void startFB(){
        Uri uri = Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/" + user.getUserID());
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }

}
