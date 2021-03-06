/*
 * Copyright 2014 Paul Tsouchlos
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.devpaul.filepickerlibrary;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devpaul.filepickerlibrary.adapter.FileListAdapter;
import com.devpaul.filepickerlibrary.enums.FileScopeType;
import com.devpaul.filepickerlibrary.enums.FileType;
import com.devpaul.filepickerlibrary.enums.ThemeType;
import com.devpaul.materialfabmenu.MaterialFloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.io.File;

/**
 * Created by Paul Tsouchlos
 * Contains all the logic for selecting files or directories.
 */
public class FilePickerActivity extends ListActivity implements NameFileDialogInterface {

    /**
     * Request code for when you want the file path to a directory.
     */
    public static final int REQUEST_DIRECTORY = 101;

    /**
     * Request code for when you want the file path to a specific file.
     */
    public static final int REQUEST_FILE = 102;

    /**
     * Constant value for adding the REQUEST_CODE int as an extra to the {@code FilePickerActivity}
     * {@code Intent}
     */
    public static final String REQUEST_CODE = "requestCode";

    /**
     * Constant value for adding the SCOPE_TYPE enum as an extra to the {@code FilePickerActivity}
     * {@code Intent} The default is {@code FileType.ALL} see
     * {@link com.devpaul.filepickerlibrary.enums.FileScopeType} for other types.
     */
    public static final String SCOPE_TYPE = "scopeType";

    /**
     * Constant label value for sending a color id extra in the calling intent for this
     * {@code FilePickerActivity}
     */
    public static final String INTENT_EXTRA_COLOR_ID = "intentExtraColorId";

    /**
     * Constant label value for sending a drawable image id in the calling intent for this
     * {@code FilePickerActivity}
     */
    public static final String INTENT_EXTRA_DRAWABLE_ID = "intentExtraDrawableId";

    /**
     * Constant label value for sending a color id to be used for the floating action button.
     */
    public static final String INTENT_EXTRA_FAB_COLOR_ID = "intentExtraFabColorId";

    /**
     * Constant for retrieving the return file path in {@link #onActivityResult(int, int, android.content.Intent)}
     * If the result code is RESULT_OK then the file path will not be null. This should always be
     * checked though.
     * <p/>
     * Example:
     * <p/>
     * {@code
     * <p/>
     * protected void onActivityResult(int resultCode, int requestCode, Intent data) {
     * <p/>
     * if(resultCode == RESULT_OK && requestCode == FILEPICKER) {
     * String filePath = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
     * <p/>
     * if(filePath != null) {
     * //do something with the string.
     * }
     * }
     * }
     * }
     */
    public static final String FILE_EXTRA_DATA_PATH = "fileExtraPath";

    /**
     * List view for list of files.
     */
    private ListView listView;
    /**
     * Button that allows user to selet the file or directory.
     */
    private Button selectButton;
    /**
     * Allows user to enter a directory tree.
     */
    private Button openButton;
    /**
     * Container that encloses the two buttons above.
     */
    private LinearLayout buttonContainer;
    /**
     * {@code TextView} that titles the view.
     */
    private TextView directoryTitle;

    /**
     * Floating action button.
     */
    private MaterialFloatingActionButton addButton;

    /**
     * Relative layout that holds the header.
     */
    private RelativeLayout header;

    /**
     * {@code Animation} for showing the buttonContainer
     */
    private Animation slideUp;
    /**
     * {@code Animation} for hiding the buttonContainer
     */
    private Animation slideDown;

    /**
     * {@code File} current directory
     */
    private File curDirectory;
    /**
     * {@code File} the directory one level up from the current one
     */
    private File lastDirectory;
    /**
     * Array of files
     */
    File[] files;
    /**
     * {@code FileListAdapter} object
     */
    private FileListAdapter adapter;
    /**
     * The currently selected file
     */
    private File currentFile;

    private boolean areButtonsShowing;

    /**
     * {@link com.devpaul.filepickerlibrary.enums.FileScopeType} enum
     */
    private FileScopeType scopeType;

    /**
     * {@link com.devpaul.filepickerlibrary.enums.ThemeType} enum for the type of them for this
     * activity.
     */
    private ThemeType themeType;
    /**
     * Constant used for passing a {@link com.devpaul.filepickerlibrary.enums.ThemeType} enum
     * to this activity from the calling activity.
     */
    public static final String THEME_TYPE = "themeType";
    /**
     * Actual mime type to be used for file browsing
     */
    private String mimeType;
    /**
     * Constant used for setting the mime type of the files that the user is supposed to choose.
     */
    public static final String MIME_TYPE = "mimeType";
    /**
     * Request code for this activity
     */
    private int requestCode;

    /**
     * {@code Intent} used to send back the data to the calling activity
     */
    private Intent data;

    /**
     * {@code int} used to store the color resource id sent as an extra to this activity.
     */
    private int colorId;

    /**
     * {@code int} used to store the color for the floating action button.
     */
    private int fabColorId;

    /**
     * {@code int} used to store the drawable resource id sent as an extra to this activity.
     */
    private int drawableId;

    /**
     * (@code int) saves the previous first visible item when scrolling, used to make the buttons
     * disappear
     */
    private int mLastFirstVisibleItem;

    /**
     * (@code Context) saves the context of activity so that you can use it in onClick calls, etc.
     */
    private Context mContext;

    private View listHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        //get the theme type for this activity
        themeType = (ThemeType) getIntent().getSerializableExtra(THEME_TYPE);
        if (themeType == null) {
            themeType = ThemeType.ACTIVITY;
        }

        setThemeType(themeType);

        areButtonsShowing = false;

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //set up the mime type for the file.
        Object rawMimeTypeParameter = getIntent().getExtras().get(MIME_TYPE);
        if (rawMimeTypeParameter instanceof String) {
            mimeType = (String) rawMimeTypeParameter;
        } else if (rawMimeTypeParameter instanceof FileType) {
            mimeType = ((FileType) rawMimeTypeParameter).getMimeType();
        } else {
            mimeType = null;
        }

        //set up the animations
        setUpAnimations();

        Intent givenIntent = getIntent();

        //get the scope type and request code. Defaults are all files and request of a directory
        //path.
        scopeType = (FileScopeType) givenIntent.getSerializableExtra(SCOPE_TYPE);
        if (scopeType == null) {
            //set default if it is null
            scopeType = FileScopeType.ALL;
        }
        requestCode = givenIntent.getIntExtra(REQUEST_CODE, REQUEST_DIRECTORY);

        colorId = givenIntent.getIntExtra(INTENT_EXTRA_COLOR_ID, android.R.color.holo_blue_light);
        drawableId = givenIntent.getIntExtra(INTENT_EXTRA_DRAWABLE_ID, -1);
        fabColorId = givenIntent.getIntExtra(INTENT_EXTRA_FAB_COLOR_ID, -1);

        setContentView(R.layout.file_picker_activity_layout);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (areButtonsShowing) {
                    if (Math.abs(firstVisibleItem - mLastFirstVisibleItem) >= 3) {
                        hideButtons();
                        adapter.setSelectedPosition(-1);
                        mLastFirstVisibleItem = firstVisibleItem;
                    } else if (firstVisibleItem > adapter.getSelectedPosition()) {
                        hideButtons();
                        adapter.setSelectedPosition(-1);
                    }
                } else {
                    mLastFirstVisibleItem = firstVisibleItem;
                }

            }
        });

        listHeaderView = getLayoutInflater()
                .inflate(R.layout.file_list_header_view, null);
        listHeaderView.setFocusable(false);
        listHeaderView.setClickable(false);
        listHeaderView.setOnClickListener(null);
        listHeaderView.setActivated(false);

        initializeViews();

        //drawable has not been set so set the color.
        setHeaderBackground(colorId, drawableId);

        curDirectory = new File(Environment.getExternalStorageDirectory().getPath());
        currentFile = new File(curDirectory.getPath());
        lastDirectory = curDirectory.getParentFile();

        if (curDirectory.isDirectory()) {
            new UpdateFilesTask(this).execute(curDirectory);
        } else {
            try {
                throw new Exception("Initial file must be a directory.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (lastDirectory != null && !curDirectory.getPath()
                .equals(Environment.getExternalStorageDirectory().getPath())) {
            new UpdateFilesTask(FilePickerActivity.this).execute(lastDirectory);
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Initializes all the views in the layout of the activity.
     */
    private void initializeViews() {
        directoryTitle = (TextView) findViewById(R.id.file_directory_title);
        addButton = (MaterialFloatingActionButton) findViewById(R.id.file_picker_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NameFileDialog nfd = NameFileDialog.newInstance();
                nfd.show(getFragmentManager(), "NameDialog");
            }
        });

        if (fabColorId != -1) {
            addButton.setButtonColor(getResources().getColor(fabColorId));
        }
        selectButton = (Button) findViewById(R.id.select_button);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestCode == REQUEST_DIRECTORY) {
                    if (currentFile.isDirectory()) {
                        curDirectory = currentFile;
                        data = new Intent();
                        data.putExtra(FILE_EXTRA_DATA_PATH, currentFile.getAbsolutePath());
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        SnackbarManager.show(Snackbar.with(FilePickerActivity.this)
                                .text("Please select a directory.")
                                .duration(1500));
                    }
                } else { //request code is for a file
                    if (currentFile.isDirectory()) {
                        curDirectory = currentFile;
                        new UpdateFilesTask(FilePickerActivity.this).execute(curDirectory);
                    } else {
                        if (mimeType != null) {
                            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                            String requiredExtension = "." + mimeTypeMap.getExtensionFromMimeType(mimeType);
                            if (requiredExtension.equalsIgnoreCase(fileExt(currentFile.toString()))) {
                                data = new Intent();
                                data.putExtra(FILE_EXTRA_DATA_PATH, currentFile.getAbsolutePath());
                                setResult(RESULT_OK, data);
                                finish();
                            } else {
                                SnackbarManager.show(Snackbar.with(FilePickerActivity.this)
                                        .text("Please select a " + requiredExtension + " file.")
                                        .duration(1500));
                            }
                        } else {
                            data = new Intent();
                            data.putExtra(FILE_EXTRA_DATA_PATH, currentFile.getAbsolutePath());
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    }
                }
            }
        });

        openButton = (Button) findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFile.isDirectory()) {
                    curDirectory = currentFile;
                    directoryTitle.setText(curDirectory.getName());

                    new UpdateFilesTask(FilePickerActivity.this).execute(curDirectory);
                } else {
                    Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
                    String file = currentFile.toString();
                    if (file != null) {
                        newIntent.setDataAndType(Uri.fromFile(currentFile), mimeType);
                        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(newIntent);
                        } catch (android.content.ActivityNotFoundException e) {
                            SnackbarManager.show(Snackbar.with(FilePickerActivity.this)
                                    .text("No handler for this type of file."));
                        }
                    } else {
                        SnackbarManager.show(Snackbar.with(FilePickerActivity.this)
                                .text("Couldn't get file type."));
                    }

                }
            }
        });

        buttonContainer = (LinearLayout) findViewById(R.id.button_container);
        buttonContainer.setVisibility(View.INVISIBLE);

        header = (RelativeLayout) findViewById(R.id.header_container);
    }

    /**
     * Returns the file extension of a file.
     *
     * @param url the file path
     * @return
     */
    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    /**
     * Initializes the animations used in this activity.
     */
    private void setUpAnimations() {
        slideUp = AnimationUtils.loadAnimation(FilePickerActivity.this,
                com.devpaul.filepickerlibrary.R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(FilePickerActivity.this,
                com.devpaul.filepickerlibrary.R.anim.slide_down);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (l.getHeaderViewsCount() != 1) {
            //adjust for list view header view.
            position += 1;
        }
        if (position > 0) {
            currentFile = files[position - 1];
        }
        if (adapter.getSelectedPosition() == position) {
            hideButtons();
            adapter.setSelectedPosition(-1);
        } else {
            adapter.setSelectedPosition(position - 1);
            showButtons();
        }
    }

    /**
     * Method that shows the sliding panel
     */
    private void showButtons() {
        if (!areButtonsShowing) {
            buttonContainer.clearAnimation();
            buttonContainer.startAnimation(slideUp);
            buttonContainer.setVisibility(View.VISIBLE);
            areButtonsShowing = true;
        }
    }

    /**
     * Method that hides the sliding panel
     */
    private void hideButtons() {
        if (areButtonsShowing) {
            buttonContainer.clearAnimation();
            buttonContainer.startAnimation(slideDown);
            buttonContainer.setVisibility(View.INVISIBLE);
            areButtonsShowing = false;
        }
    }

    @Override
    public void onReturnFileName(String fileName) {

        if (fileName.equalsIgnoreCase("") || fileName.isEmpty()) {
            fileName = null;
        }
        if (fileName != null) {
            File file = new File(curDirectory.getPath() + "//" + fileName);
            boolean created = false;
            if (!file.exists()) {
                created = file.mkdirs();
            }
            if (created) {
                new UpdateFilesTask(this).execute(curDirectory);
            }
        }
    }


    /**
     * Set the background color of the header
     *
     * @param colorResId    Resource Id of the color
     * @param drawableResId Resource Id of the drawable
     */
    private void setHeaderBackground(int colorResId, int drawableResId) {
        if (drawableResId == -1) {
            try {
                header.setBackgroundColor(getResources().getColor(colorResId));
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                header.setBackgroundDrawable(getResources().getDrawable(drawableResId));
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Sets the theme for this activity
     *
     * @param themeType the {@code ThemeType} enum set in the calling intent.
     */
    public void setThemeType(ThemeType themeType) {
        if (themeType == ThemeType.ACTIVITY) {
            setTheme(android.R.style.Theme_Holo_Light);
        } else if (themeType == ThemeType.DIALOG) {
            setTheme(android.R.style.Theme_Holo_Light_Dialog);
        } else if (themeType == ThemeType.DIALOG_NO_ACTION_BAR) {
            setTheme(android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        }
    }

    public ThemeType getThemeType() {
        return themeType;
    }

    /**
     * Class that updates the list view with a new array of files. Resets the adapter and the
     * directory title.
     */
    private class UpdateFilesTask extends AsyncTask<File, Void, File[]> {

        private File[] fileArray;
        private Context mContext;
        private ProgressDialog dialog;
        private File directory;

        private UpdateFilesTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            Log.i("FilePicker", "AsyncCalled");
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
            hideButtons();
            super.onPreExecute();
        }

        @Override
        protected File[] doInBackground(File... files) {
            directory = files[0];
            fileArray = files[0].listFiles();
            return fileArray;
        }

        @Override
        protected void onPostExecute(File[] localFiles) {
            files = localFiles;
            if (directory.getPath().equalsIgnoreCase(Environment
                    .getExternalStorageDirectory().getPath())) {
                directoryTitle.setText("Parent Directory");
            } else {
                directoryTitle.setText(directory.getName());
            }
            lastDirectory = directory.getParentFile();
            curDirectory = directory;
            if (directory.listFiles().length > 0 && directoryExists(files)
                    && listView.getHeaderViewsCount() == 0) {
                listView.addHeaderView(listHeaderView);
            } else if (directory.listFiles().length == 0 || !directoryExists(files)) {
                if (listView.getHeaderViewsCount() == 1) {
                    listView.removeHeaderView(listHeaderView);
                }
            }
            adapter = new FileListAdapter(FilePickerActivity.this, files, scopeType);
            setListAdapter(adapter);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            super.onPostExecute(files);
        }

        /**
         * Checks if the files contain a directory.
         *
         * @param files the files.
         * @return a boolean, true if there is a file that is a directory.
         */
        public boolean directoryExists(File[] files) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    return true;
                }
            }
            return false;
        }
    }
}
