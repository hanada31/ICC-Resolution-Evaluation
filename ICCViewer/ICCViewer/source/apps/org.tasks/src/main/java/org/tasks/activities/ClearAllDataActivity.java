package org.tasks.activities;

import android.content.DialogInterface;
import android.os.Bundle;

import com.todoroo.astrid.dao.Database;

import org.tasks.R;
import org.tasks.dialogs.DialogBuilder;
import org.tasks.injection.InjectingAppCompatActivity;
import org.tasks.preferences.Preferences;

import javax.inject.Inject;

public class ClearAllDataActivity extends InjectingAppCompatActivity {

    @Inject Database database;
    @Inject DialogBuilder dialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialogBuilder.newMessageDialog(R.string.EPr_delete_task_data_warning)
                .setPositiveButton(R.string.EPr_delete_task_data, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDatabase(database.getName());
                        System.exit(0);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }
}
