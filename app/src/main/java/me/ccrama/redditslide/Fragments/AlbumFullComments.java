package me.ccrama.redditslide.Fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Submission;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.Shadowbox;
import me.ccrama.redditslide.Activities.ShadowboxComments;
import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.ImgurAlbum.AlbumUtils;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;


/**
 * Created by ccrama on 6/2/2015.
 */
public class AlbumFullComments extends Fragment {

    boolean gallery = false;
    private View list;
    private int i = 0;
    private Comment s;
    boolean hidden;
    View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.submission_albumcard, container, false);
        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity(), true);

        String body = s.getDataNode().get("body_html").asText();
        int start = body.indexOf("&lt;a href=\"");
        String url = body.substring(start + 12, body.indexOf("\"", start + 13));

        if (url.contains("gallery")) {
            gallery = true;
        }

        list = rootView.findViewById(R.id.images);

        list.setVisibility(View.VISIBLE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView) list).setLayoutManager(layoutManager);

        ((RecyclerView) list).setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ValueAnimator va = null;

                if (dy > 0 && !hidden) {
                    hidden = true;

                    if (va != null && va.isRunning())
                        va.cancel();

                    final View base = rootView.findViewById(R.id.base);
                    va = ValueAnimator.ofFloat(1.0f, 0.2f);
                    int mDuration = 250; //in millis
                    va.setDuration(mDuration);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Float value = (Float) animation.getAnimatedValue();
                            base.setAlpha(value);
                        }
                    });

                    va.start();

                } else if (hidden && dy <= 0) {
                    final View base = rootView.findViewById(R.id.base);

                    if (va != null && va.isRunning())
                        va.cancel();

                    hidden = false;
                    va = ValueAnimator.ofFloat(0.2f, 1.0f);
                    int mDuration = 250; //in millis
                    va.setDuration(mDuration);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Float value = (Float) animation.getAnimatedValue();
                            base.setAlpha(value);
                        }
                    });

                    va.start();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        final View.OnClickListener openClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        };
        rootView.findViewById(R.id.base).setOnClickListener(openClick);
        final View title = rootView.findViewById(R.id.title);
        title.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelHeight(title.getMeasuredHeight());
                title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                            i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, ((Shadowbox) getActivity()).subreddit);
                            (getActivity()).startActivity(i2);
                        }
                    });
                } else {
                    rootView.findViewById(R.id.base).setOnClickListener(openClick);
                }
            }
        });

        new LoadIntoRecycler(url, getActivity()).execute();

        return rootView;
    }

    public class LoadIntoRecycler extends AlbumUtils.GetAlbumWithCallback {

        String url;

        public LoadIntoRecycler(@NotNull String url, @NotNull Activity baseActivity) {
            super(url, baseActivity);
            //todo htis dontClose = true;
            this.url = url;
        }

        @Override
        public void doWithData(final List<Image> jsonElements) {
            super.doWithData(jsonElements);
            AlbumView adapter = new AlbumView(baseActivity, jsonElements, 0);
            ((RecyclerView) list).setAdapter(adapter);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        s = ((ShadowboxComments) getActivity()).comments.get(i);
    }


}