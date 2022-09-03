/*
 * Copyright 2012 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.utils.file;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import eu.vranckaert.worktime.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * User: DIRK VRANCKAERT
 * Date: 28/08/12
 * Time: 18:22
 *
 * Based on: http://www.bgreco.net/directorypicker/
 */
public class DirectoryPicker extends ListActivity {
    private static final String LOG_TAG = DirectoryPicker.class.getSimpleName();

	public static final String START_DIR = "startDir";
	public static final String ONLY_DIRS = "onlyDirs";
	public static final String SHOW_HIDDEN = "showHidden";
	public static final String CHOSEN_DIRECTORY = "chosenDir";
    public static final String ALLOW_CREATE_DIR = "allowCreateDir";
	public static final int PICK_DIRECTORY = 43522432;
	private File dir;
	private boolean showHidden = false;
	private boolean onlyDirs = true ;
    private boolean allowCreateDir = false;

    private ArrayList<File> files;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        dir = Environment.getExternalStorageDirectory();
        if (extras != null) {
        	String preferredStartDir = extras.getString(START_DIR);
        	showHidden = extras.getBoolean(SHOW_HIDDEN, false);
        	onlyDirs = extras.getBoolean(ONLY_DIRS, true);
            allowCreateDir = extras.getBoolean(ALLOW_CREATE_DIR, false);
        	if(preferredStartDir != null) {
            	File startDir = new File(preferredStartDir);
            	if(startDir.isDirectory()) {
            		dir = startDir;
            	}
            }
        }

        setContentView(R.layout.directorypicker_chooser_list);
        setTitle(dir.getAbsolutePath());
        Button btnChoose = (Button) findViewById(R.id.directoryPickerBtnChoose);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	returnDir(dir);
            }
        });
        Button btnCreate = (Button) findViewById(R.id.directoryPickerBtnCreate);
        if (!allowCreateDir) {
            btnCreate.setVisibility(View.GONE);
        } else {
            btnCreate.setVisibility(View.VISIBLE);
        }
        btnCreate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText input = new EditText(DirectoryPicker.this);
                AlertDialog.Builder alert = new AlertDialog.Builder(DirectoryPicker.this)
                .setTitle(R.string.directorypicker_dialog_create_title)
                .setMessage(R.string.directorypicker_dialog_create_message)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String dirName = input.getText().toString().trim();
                        File newDir = new File(dir.getAbsolutePath() + File.separator + dirName);
                        if (newDir.exists()) {
                            Toast.makeText(DirectoryPicker.this, R.string.directorypicker_exc_create_already_exists, Toast.LENGTH_LONG).show();
                        } else {
                            boolean result = newDir.mkdir();
                            if (!result)
                                Toast.makeText(DirectoryPicker.this, R.string.directorypicker_exc_create_unknown_error, Toast.LENGTH_LONG).show();
                            FileUtil.applyPermissions(newDir, true, true, false);
                            FileUtil.enableForMTP(DirectoryPicker.this, newDir);

                            updateFileNames();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
            }
        });

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File selectedDir;
                if (position == 0 && !isRootDir()) {
                    selectedDir = dir.getParentFile();
                } else if(!files.get(position).isDirectory()) {
                    return;
                } else {
                    selectedDir = files.get(position);
                }
                dir = selectedDir;
                setTitle(dir.getAbsolutePath());

                updateFileNames();
        	}
        });

        updateFileNames();
    }

    private void updateFileNames() {
        if (files == null) {
            files = new ArrayList<File>();
        }
        files.clear();

        if(!dir.canRead()) {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.directorypicker_exc_cannot_read_dir, Toast.LENGTH_LONG);
            toast.show();
        } else {
            files = filter(dir.listFiles(), onlyDirs, showHidden);
        }

        if (!isRootDir()) {
            files.add(0, new File("/", "..."));
        }
        String[] names = names(files);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.directorypicker_list_item, names));
    }

    private boolean isRootDir() {
        if (dir.getParentFile() == null) {
            return true;
        } else {
            return false;
        }
    }
	
    private void returnDir(File path) {
    	Intent result = new Intent();
    	result.putExtra(CHOSEN_DIRECTORY, path);
        setResult(RESULT_OK, result);
    	finish();    	
    }

	public ArrayList<File> filter(File[] file_list, boolean onlyDirs, boolean showHidden) {
		ArrayList<File> files = new ArrayList<File>();
		for(File file: file_list) {
			if(onlyDirs && !file.isDirectory())
				continue;
			if(!showHidden && file.isHidden())
				continue;
			files.add(file);
		}
		Collections.sort(files);
		return files;
	}
	
	public String[] names(ArrayList<File> files) {
		String[] names = new String[files.size()];
		int i = 0;
		for(File file: files) {
			names[i] = file.getName();
			i++;
		}
		return names;
	}
}

