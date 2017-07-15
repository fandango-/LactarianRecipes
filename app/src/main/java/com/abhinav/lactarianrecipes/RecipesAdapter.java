package com.abhinav.lactarianrecipes;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class RecipesAdapter extends
        RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<Recipe> mRecipes;
    private boolean mStarred;

    // Pass in the contact array into the constructor
    public RecipesAdapter(List<Recipe> recipes, boolean starred) {
        mRecipes = recipes;
        mStarred = starred;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView descTextView;
        public RelativeLayout layout;
        public ImageView thumbnailView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.recipe_name);
            descTextView = (TextView) itemView.findViewById(R.id.recipe_desc);
            layout = (RelativeLayout) itemView.findViewById(R.id.recipe_layout);
            thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.item_recipe, parent, false);

        return new ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Recipe recipe = mRecipes.get(position);

        TextView nameView = viewHolder.nameTextView;
        nameView.setText(recipe.getName());

        TextView descView = viewHolder.descTextView;
        descView.setText(recipe.getDescription());

        ImageView thumbView = viewHolder.thumbnailView;
        thumbView.setImageBitmap(recipe.getImage());

        RelativeLayout layout = viewHolder.layout;
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager manager = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = manager.getActiveNetworkInfo();
                boolean isConnected = info != null && info.isConnected();
                if (isConnected || mStarred) {
                    Intent intent = new Intent(v.getContext(), RecipeActivity.class);
                    intent.putExtra("QUERY_URL", recipe.getLink());
                    intent.putExtra("RECIPE_NAME", recipe.getName());
                    intent.putExtra("RECIPE_DESCRIPTION", recipe.getDescription());
                    intent.putExtra("IS_STARRED", mStarred);
                    if (mStarred) {
                        ((StarredActivity) v.getContext()).startActivityForResult(intent, 1);
                    } else {
                        v.getContext().startActivity(intent);
                    }
                } else {
                    Toast.makeText(v.getContext().getApplicationContext(), "You must be connected" +
                            " to the internet to view the recipe details", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }
}