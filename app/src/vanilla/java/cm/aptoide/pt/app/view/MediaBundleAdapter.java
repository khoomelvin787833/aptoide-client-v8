package cm.aptoide.pt.app.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import cm.aptoide.pt.R;
import java.util.List;
import rx.subjects.PublishSubject;

/**
 * Created by D01 on 29/08/2018.
 */

class MediaBundleAdapter extends RecyclerView.Adapter<MediaViewHolder> {
  private List<EditorialMedia> media;
  private PublishSubject<String> editorialMediaClicked;

  public MediaBundleAdapter(List<EditorialMedia> media,
      PublishSubject<String> editorialMediaClicked) {
    this.media = media;
    this.editorialMediaClicked = editorialMediaClicked;
  }

  @Override public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new MediaViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.media_layout, parent, false), editorialMediaClicked);
  }

  @Override public void onBindViewHolder(MediaViewHolder mediaViewHolder, int position) {
    mediaViewHolder.setVisibility(media.get(position));
  }

  @Override public int getItemCount() {
    return media.size();
  }

  public void update(List<EditorialMedia> media) {
    this.media = media;
    notifyDataSetChanged();
  }

  public void add(List<EditorialMedia> media) {
    this.media.addAll(media);
    notifyDataSetChanged();
  }

  public void remove(int itemPosition) {
    media.remove(itemPosition);
    notifyItemRemoved(itemPosition);
  }
}