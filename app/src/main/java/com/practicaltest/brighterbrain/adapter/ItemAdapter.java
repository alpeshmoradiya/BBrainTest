package com.practicaltest.brighterbrain.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.practicaltest.brighterbrain.R;
import com.practicaltest.brighterbrain.interfaces.DeleteDailogClickListener;
import com.practicaltest.brighterbrain.model.ItemDetails;
import com.practicaltest.brighterbrain.ui.AddItemActivity;
import com.practicaltest.brighterbrain.utils.Utils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by alpesh.moradiya on 01/01/2016.
 */
public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private static final int ANIMATED_ITEMS_COUNT = 2;
    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animateItems = false;
    private ArrayList<ItemDetails> itemDetails;
    private DeleteDailogClickListener mListener;

    public ItemAdapter(Context context, ArrayList<ItemDetails> itemDetails) {
        this.context = context;
        this.itemDetails = itemDetails;
    }

    public void setDeleteDailogClickListener(DeleteDailogClickListener mListener){
        this.mListener = mListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_post,parent,false);
        final CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);

        cellFeedViewHolder.imgEdit.setOnClickListener(this);
        cellFeedViewHolder.imgDelete.setOnClickListener(this);
        cellFeedViewHolder.ivFeedCenter.setOnClickListener(this);
        return cellFeedViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        final CellFeedViewHolder holder = (CellFeedViewHolder) viewHolder;
        bindDefaultFeedItem(position, holder);
    }

    private void bindDefaultFeedItem(int position, CellFeedViewHolder holder) {

        holder.imgEdit.setTag(position);
        holder.imgDelete.setTag(position);

        holder.tvItemName.setText(itemDetails.get(position).getItemName());
        holder.tvDescription.setText(itemDetails.get(position).getItemDiscription());
        holder.tvLocation.setText(itemDetails.get(position).getItemLocation());
        holder.tvCost.setText(itemDetails.get(position).getItemCost());
        Glide.with(context).load(itemDetails.get(position).getItemImagePath()).placeholder(R.drawable.ic_empty_post).into(holder.ivFeedCenter);
    }

    @Override
    public int getItemCount() {
        return itemDetails.size();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.img_edit:
                AddItemActivity.openWithPhotoUri(context, itemDetails.get((Integer)view.getTag()).getItemImagePath(), true, itemDetails.get((Integer)view.getTag()));
                break;
            case R.id.img_delete:
               deleteConfirmationDialog(context, context.getResources().getString(R.string.app_name1), context.getResources().getString(R.string.delete_confirm_message), (Integer)view.getTag());
                break;
            case R.id.ivFeedCenter:
                break;
        }

    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.vImageRoot) FrameLayout vImageRoot;
        @Bind(R.id.ivFeedCenter) ImageView ivFeedCenter;
        @Bind(R.id.tvItemName) TextView tvItemName;
        @Bind(R.id.tvDesription) TextView tvDescription;
        @Bind(R.id.tvLocation) TextView tvLocation;
        @Bind(R.id.tvCost) TextView tvCost;
        @Bind(R.id.img_edit) ImageView imgEdit;
        @Bind(R.id.img_delete) ImageView imgDelete;

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
   }

    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(context));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    public void deleteConfirmationDialog(Context context, String title, String message, final int position) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDeleteAction(position);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }
}
