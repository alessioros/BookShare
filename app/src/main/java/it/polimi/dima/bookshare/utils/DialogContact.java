package it.polimi.dima.bookshare.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

        Button buttonCellphone=(Button) view.findViewById(R.id.cellphone);
        Button buttonEmail=(Button) view.findViewById(R.id.mail);
        Button buttonFB=(Button) view.findViewById(R.id.fb_messenger);


        buttonCellphone.setText(user.getPhoneNumber());
        buttonCellphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri number = Uri.parse("tel:"+user.getPhoneNumber());
                Intent callIntent = new Intent(Intent.ACTION_VIEW, number);
                startActivity(callIntent);
            }
        });

        buttonEmail.setText(user.getEmail());
        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",user.getEmail(), null));
                startActivity(emailIntent);
            }
        });

        buttonFB.setText(R.string.send_fb_message);

        builder.setView(view);

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
