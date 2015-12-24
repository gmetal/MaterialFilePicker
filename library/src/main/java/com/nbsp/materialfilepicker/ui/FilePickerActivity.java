package com.nbsp.materialfilepicker.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.nbsp.materialfilepicker.R;
import com.nbsp.materialfilepicker.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by Dimorinny on 24.10.15.
 */
public class FilePickerActivity extends AppCompatActivity implements DirectoryFragment.FileClickListener {
    private static final String ARG_CURRENT_PATH = "arg_title_state";
    private static final String ARG_MODE = "arg_mode";
    public static final String RESULT_FILE_PATH = "result_file_path";
    private static final String START_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final int HANDLE_CLICK_DELAY = 150;

    public static final int FILE_MODE = 1;
    public static final int DIRECTORY_MODE = 2;

    private Toolbar mToolbar;
    private String mCurrentPath = START_PATH;
    private int mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        if (savedInstanceState != null) {
            mCurrentPath = savedInstanceState.getString(ARG_CURRENT_PATH);
            mMode = savedInstanceState.getInt(ARG_MODE);
        } else {
            initFragment();
        }

        initViews();
        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        // Show back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Truncate start of toolbar title
        try {
            Field f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);

            TextView textView = (TextView) f.get(mToolbar);
            textView.setEllipsize(TextUtils.TruncateAt.START);
        } catch (Exception ignored) {
        }

        updateTitle();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void initFragment() {
        mMode = getIntent().getIntExtra(ARG_MODE, FILE_MODE);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, DirectoryFragment.getInstance(START_PATH, mMode == DIRECTORY_MODE ? DirectoryFragment.DIRECTORY_MODE : DirectoryFragment.FILE_MODE))
                .commit();
    }

    private void updateTitle() {
        if (getSupportActionBar() != null) {
            String title = mCurrentPath.isEmpty() ? "/" : mCurrentPath;
            if (title.startsWith(START_PATH)) {
                title = title.replaceFirst(START_PATH, getString(R.string.start_path_name));
            }
            getSupportActionBar().setTitle(title);
        }
    }

    private void addFragmentToBackStack(String path) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, DirectoryFragment.getInstance(path, mMode == DIRECTORY_MODE ? DirectoryFragment.DIRECTORY_MODE : DirectoryFragment.FILE_MODE))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (menuItem.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        } else {
            // TODO
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath);
            updateTitle();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_CURRENT_PATH, mCurrentPath);
        outState.putInt(ARG_MODE, mMode);
    }

    @Override
    public void onFileClicked(final File clickedFile) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleFileClicked(clickedFile);
            }
        }, HANDLE_CLICK_DELAY);
    }

    @Override
    public void onDirectorySelected(final File directorySelected) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleDirectorySelected(directorySelected);
            }
        }, HANDLE_CLICK_DELAY);
    }


    private void handleFileClicked(final File clickedFile) {
        if (clickedFile.isDirectory()) {
            addFragmentToBackStack(clickedFile.getPath());
            mCurrentPath = clickedFile.getPath();
            updateTitle();
        } else {
            setResultAndFinish(clickedFile.getPath());
        }
    }

    private void handleDirectorySelected(final File selectedDirectory) {
        setResultAndFinish(selectedDirectory.getPath());
    }

    private void setResultAndFinish(String filePath) {
        Intent data = new Intent();
        data.putExtra(RESULT_FILE_PATH, filePath);
        setResult(RESULT_OK, data);
        finish();
    }
}
