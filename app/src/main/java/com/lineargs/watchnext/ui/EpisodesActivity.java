package com.lineargs.watchnext.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lineargs.watchnext.R;
import com.lineargs.watchnext.data.DataContract;
import com.lineargs.watchnext.data.EpisodesQuery;
import com.lineargs.watchnext.data.SeasonsQuery;
import com.lineargs.watchnext.jobs.ReminderFirebaseUtilities;
import com.lineargs.watchnext.sync.syncseries.SeasonUtils;
import com.lineargs.watchnext.utils.ServiceUtils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EpisodesActivity extends BaseTopActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 667, BACK_LOADER_ID = 888;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.container)
    ViewPager mViewPager;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private String title = "", subtitle = "";
    private String seasonId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(SeasonsFragment.SEASON_TITLE) && getIntent().hasExtra(SeasonsFragment.EPISODES)) {
            title = getIntent().getStringExtra(SeasonsFragment.SEASON_TITLE);
            subtitle = getIntent().getStringExtra(SeasonsFragment.EPISODES);
        }

        setupActionBar();
        setupNavDrawer();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        if (getIntent().hasExtra(SeasonsFragment.SEASON_ID) && getIntent().hasExtra(SeasonsFragment.SERIE_ID) && getIntent().hasExtra(SeasonsFragment.SEASON_NUMBER)) {
            String serieId = getIntent().getStringExtra(SeasonsFragment.SERIE_ID);
            String number = getIntent().getStringExtra(SeasonsFragment.SEASON_NUMBER);
            seasonId = getIntent().getStringExtra(SeasonsFragment.SEASON_ID);
            SeasonUtils.syncEpisodes(this, serieId, number, seasonId);
            startLoading();
        }
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(BACK_LOADER_ID, null, this);
    }

    private void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);
    }

    private void showData() {
        mProgressBar.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void setDrawerIndicatorEnabled() {
        super.setDrawerIndicatorEnabled();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(this,
                        DataContract.Episodes.CONTENT_URI,
                        EpisodesQuery.EPISODE_PROJECTION,
                        DataContract.Episodes.COLUMN_SEASON_ID + " = ? ",
                        new String[]{seasonId},
                        null);
            case BACK_LOADER_ID:
                return new CursorLoader(this,
                        DataContract.Seasons.CONTENT_URI,
                        SeasonsQuery.SEASON_PROJECTION,
                        DataContract.Seasons.COLUMN_SEASON_ID + " = ? ",
                        new String[]{seasonId},
                        null);
            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                if (data != null && data.getCount() != 0) {
                    data.moveToFirst();
                    mSectionsPagerAdapter.swapCursor(data);
                    showData();
                }
                break;
            case BACK_LOADER_ID:
                if (data != null && data.getCount() != 0) {
                    data.moveToFirst();
                    mSectionsPagerAdapter.swapBackCursor(data);
                }
                break;
            default:
                throw new RuntimeException("Loader not implemented: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSectionsPagerAdapter.swapCursor(null);
        mSectionsPagerAdapter.swapBackCursor(null);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_NAME = "name";
        private static final String ARG_STILL_PATH = "still_path";
        private static final String ARG_VOTE = "vote";
        private static final String ARG_DATE = "date";
        private static final String ARG_OVERVIEW = "overview";
        private static final String ARG_IMG = "img";
        private static final String ARG_ID = "id";
        private static final String ARG_TITLE = "title";
        private static final String ARG_GUEST_STARS = "guest_stars";

        @BindView(R.id.name)
        AppCompatTextView name;
        @BindView(R.id.still_path)
        ImageView stillPath;
        @BindView(R.id.vote_average)
        AppCompatTextView voteAverage;
        @BindView(R.id.release_date)
        AppCompatTextView releaseDate;
        @BindView(R.id.overview)
        AppCompatTextView overview;
        @BindView(R.id.guest_stars)
        AppCompatTextView guestStars;
        @BindView(R.id.guest_stars_container)
        LinearLayout guestStarsContainer;
        @BindView(R.id.cover_poster)
        ImageView poster;
        @BindView(R.id.notification_fab)
        FloatingActionButton notification;
        private Unbinder unbinder;


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String name, String stillPath, String vote, String releaseDate,
                                                      String overview, String poster, int id, String title, String guestStars) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_NAME, name);
            args.putString(ARG_STILL_PATH, stillPath);
            args.putString(ARG_VOTE, vote);
            args.putString(ARG_DATE, releaseDate);
            args.putString(ARG_OVERVIEW, overview);
            args.putString(ARG_IMG, poster);
            args.putInt(ARG_ID, id);
            args.putString(ARG_TITLE, title);
            args.putString(ARG_GUEST_STARS, guestStars);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_episodes, container, false);
            unbinder = ButterKnife.bind(this, rootView);

            name.setText(getArguments().getString(ARG_NAME));
            voteAverage.setText(getArguments().getString(ARG_VOTE));
            releaseDate.setText(getArguments().getString(ARG_DATE));
            overview.setText(getArguments().getString(ARG_OVERVIEW));
            if (TextUtils.isEmpty(getArguments().getString(ARG_GUEST_STARS))) {
                guestStarsContainer.setVisibility(View.GONE);
            } else {
                guestStars.setText(getArguments().getString(ARG_GUEST_STARS));
            }
            Picasso.with(poster.getContext())
                    .load(getArguments().getString(ARG_IMG))
                    .centerInside()
                    .fit()
                    .into(poster);
            ServiceUtils.loadPicasso(stillPath.getContext(), getArguments().getString(ARG_STILL_PATH))
                    .resizeDimen(R.dimen.movie_poster_width_default, R.dimen.movie_poster_height_default)
                    .centerInside()
                    .into(stillPath);
            return rootView;
        }

        private boolean airedAlready(String date) {
            Date date1 = null;
            if (date != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault());
                try {
                    date1 = simpleDateFormat.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return date1 != null && (System.currentTimeMillis() > date1.getTime());
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }

        private int getSeconds(long today, String date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault());
            Date releaseDay = null;
            try {
                releaseDay = simpleDateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert releaseDay != null;
            return (int) (TimeUnit.MILLISECONDS.toSeconds(releaseDay.getTime() - today));
        }

        @OnClick(R.id.notification_fab)
        public void setNotification() {
            if (!airedAlready(getArguments().getString(ARG_DATE))) {
                int intervalSeconds = getSeconds(System.currentTimeMillis(), getArguments().getString(ARG_DATE));
                ReminderFirebaseUtilities.scheduleReminder(getContext(), intervalSeconds, getArguments().getInt(ARG_ID),
                        getArguments().getString(ARG_TITLE), getArguments().getString(ARG_NAME));
                Toast.makeText(getContext(), getString(R.string.toast_notification_reminder), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.toast_aired_already), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Cursor mCursor, mBackCursor;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            String name = mCursor.getString(EpisodesQuery.NAME);
            String stillPath = mCursor.getString(EpisodesQuery.STILL_PATH);
            String vote = mCursor.getString(EpisodesQuery.VOTE_AVERAGE);
            String date = mCursor.getString(EpisodesQuery.RELEASE_DATE);
            String overview = mCursor.getString(EpisodesQuery.OVERVIEW);
            String poster = mBackCursor.getString(SeasonsQuery.POSTER_PATH);
            int id = mCursor.getInt(EpisodesQuery.EPISODE_ID);
            String title = mBackCursor.getString(SeasonsQuery.SHOW_NAME);
            String guestStars = mCursor.getString(EpisodesQuery.GUEST_STARS);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(name, stillPath, vote, date, overview, poster, id, title, guestStars);
        }

        @Override
        public int getCount() {
            if (mCursor == null) return 0;
            return mCursor.getCount();
        }

        void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }

        void swapBackCursor(Cursor cursor) {
            mBackCursor = cursor;
            notifyDataSetChanged();
        }
    }
}
