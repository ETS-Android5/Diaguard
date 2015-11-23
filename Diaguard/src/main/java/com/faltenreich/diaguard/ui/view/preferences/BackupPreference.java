package com.faltenreich.diaguard.ui.view.preferences;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.util.FileUtils;
import com.faltenreich.diaguard.util.Helper;
import com.faltenreich.diaguard.util.IFileListener;
import com.faltenreich.diaguard.util.export.Export;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Filip on 04.11.13.
 */
public class BackupPreference extends Preference implements Preference.OnPreferenceClickListener, IFileListener {

    private ProgressDialog progressDialog;

    public BackupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceClickListener(this);
    }

    private void createBackup() {
        showProgressDialog();
        Export.exportCsv(this, true, null, null, null);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getContext().getString(R.string.export_progress));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        createBackup();
        return true;
    }

    @Override
    public void onProgress(String message) {
        progressDialog.setMessage(message);
    }

    @Override
    public void onComplete(File file, String mimeType) {
        progressDialog.dismiss();
        String confirmationText = String.format(getContext().getString(R.string.export_complete), file.getAbsolutePath());
        Toast.makeText(getContext(), confirmationText, Toast.LENGTH_LONG).show();
    }
}