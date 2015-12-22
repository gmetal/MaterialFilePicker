package com.nbsp.materialfilepicker.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nbsp.materialfilepicker.R;
import com.nbsp.materialfilepicker.utils.FileUtils;
import com.nbsp.materialfilepicker.widget.EmptyRecyclerView;

import java.io.File;

/**
 * Created by Dimorinny on 24.10.15.
 */
public class DirectoryFragment extends Fragment {

    interface FileClickListener {
        void onFileClicked(File clickedFile);

        boolean onDirectoryLongClicked(File directoryLongClickedFile);
    }

    private static final String ARG_FILE_PATH = "arg_file_path";
    private static final String ARG_MODE = "arg_mode";

    public static final int FILE_MODE = 0;
    public static final int DIRECTORY_MODE = 1;

    private View mEmptyView;
    private String mPath;
    private int mMode;
    private EmptyRecyclerView mDirectoryRecyclerView;
    private DirectoryAdapter mDirectoryAdapter;
    private FileClickListener mFileClickListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFileClickListener = (FileClickListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFileClickListener = null;
    }

    public static DirectoryFragment getInstance(String path, int mode) {
        DirectoryFragment instance = new DirectoryFragment();

        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, path);
        args.putInt(ARG_MODE, mode);
        instance.setArguments(args);

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directory, container, false);
        mDirectoryRecyclerView = (EmptyRecyclerView) view.findViewById(R.id.directory_recycler_view);
        mEmptyView = view.findViewById(R.id.directory_empty_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initArgs();
        initFilesList();
    }

    private void initFilesList() {
        mDirectoryAdapter = new DirectoryAdapter(getActivity(), FileUtils.getFileListByDirPath(mPath, mMode == FILE_MODE));

        mDirectoryAdapter.setOnItemClickListener(new DirectoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mFileClickListener != null) {
                    mFileClickListener.onFileClicked(mDirectoryAdapter.getModel(position));
                }
            }

            @Override
            public boolean onItemLongClick(View view, int position) {
                if (mMode == DIRECTORY_MODE) {
                    return mFileClickListener.onDirectoryLongClicked(mDirectoryAdapter.getModel(position));
                }
                return false;
            }
        });

        mDirectoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDirectoryRecyclerView.setAdapter(mDirectoryAdapter);
        mDirectoryRecyclerView.setEmptyView(mEmptyView);
    }

    private void initArgs() {
        if (getArguments().getString(ARG_FILE_PATH) != null) {
            mPath = getArguments().getString(ARG_FILE_PATH);
        }

        if ((getArguments().getInt(ARG_MODE) == DIRECTORY_MODE) || (getArguments().getInt(ARG_MODE) == FILE_MODE)) {
            mMode = getArguments().getInt(ARG_MODE);
        }
    }
}
