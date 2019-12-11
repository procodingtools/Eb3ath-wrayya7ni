package org.procodingtools.eb3athwrayya7ni;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by amiirr on 4/20/18.
 */

public class SendingSmsDialogFragment extends DialogFragment {
    private TextView queuedNumbers;
    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_dialog_sending_sms, container);

        queuedNumbers = v.findViewById(R.id.progress_numbers_tv);

        String title = getArguments().getString("title");
        queuedNumbers.setText(getArguments().getString("txt"));
        getDialog().setTitle(title);


        return v;
    }

    public static SendingSmsDialogFragment newInstance(String title, String str) {
        SendingSmsDialogFragment frag = new SendingSmsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("txt", str);
        frag.setArguments(args);
        return frag;
    }


    public void update(String queued){
        queuedNumbers.setText(queued);
    }
}
