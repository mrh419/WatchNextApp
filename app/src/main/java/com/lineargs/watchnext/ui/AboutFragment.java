package com.lineargs.watchnext.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lineargs.watchnext.R;
import com.lineargs.watchnext.data.DataDbHelper;
import com.lineargs.watchnext.utils.ServiceUtils;
import com.lineargs.watchnext.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A placeholder fragment containing a simple view.
 */
public class AboutFragment extends Fragment {

    @BindView(R.id.tmdb_terms)
    AppCompatTextView terms;
    @BindView(R.id.tmdb_api_terms)
    AppCompatTextView apiTerms;
    @BindView(R.id.version_text)
    AppCompatTextView version;
    private Unbinder unbinder;

    public AboutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        version.setText(Utils.versionString(getActivity()));
        return rootView;
    }

    @OnClick(R.id.tmdb_terms)
    public void openTerms() {
        ServiceUtils.openTMDbTerms(getActivity(), getString(R.string.tmdb_terms_link));
    }

    @OnClick(R.id.tmdb_api_terms)
    public void openTermsApi() {
        ServiceUtils.openTMDbTerms(getActivity(), getString(R.string.tmdb_api_terms_link));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
