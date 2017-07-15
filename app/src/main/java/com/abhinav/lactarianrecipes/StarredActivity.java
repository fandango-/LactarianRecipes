package com.abhinav.lactarianrecipes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StarredActivity extends AppCompatActivity {
    private RecipesAdapter adapter;
    private List<Recipe> allStarredRecipes;
    private TextView textView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boolean result = data.getBooleanExtra("result", false);
                if (result) {
                    String url = data.getStringExtra("url");
                    String name = data.getStringExtra("name");
                    String description = data.getStringExtra("desc");
                    Recipe toDelete = new Recipe(name, description, url, null);
                    int delIndex = -1;
                    for (int i = 0; i < allStarredRecipes.size(); i++) {
                        Recipe recipe = allStarredRecipes.get(i);
                        if (recipe.equals(toDelete)) {
                            delIndex = i;
                            break;
                        }
                    }
                    allStarredRecipes.remove(delIndex);
                    Log.d("LACT_RECIPES", "Removed: " + toDelete + " at " + delIndex);
                    CharSequence charSequence = allStarredRecipes.size() + " matching recipes found";
                    textView.setText(charSequence);
                    adapter.notifyItemRemoved(delIndex);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textView = (TextView) findViewById(R.id.number_of_results);

        RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvRecipes);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvItems.setLayoutManager(linearLayoutManager);

        allStarredRecipes = getStarredRecipes(18, 0, textView);
        adapter = new RecipesAdapter(allStarredRecipes, true);
        rvItems.setAdapter(adapter);

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                List<Recipe> moreRecipes = getStarredRecipes(18, page, null);
                final int curSize = adapter.getItemCount();
                allStarredRecipes.addAll(moreRecipes);

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemRangeInserted(curSize, allStarredRecipes.size() - 1);
                    }
                });
            }
        };
        rvItems.addOnScrollListener(scrollListener);
    }

    private List<Recipe> getStarredRecipes(int numRecipes, int offset, TextView textView) {
        File directory = getFilesDir();
        File[] list = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".txt");
            }
        });
        if (textView != null) {
            CharSequence charSequence = list.length + " matching recipes found";
            textView.setText(charSequence);
        }
        List<Recipe> recipes = new ArrayList<>();
        for (int i = offset * numRecipes; i < Math.min(offset * numRecipes + numRecipes, list.length); i++) {
            File f = list[i];
            try {
                Scanner sc = new Scanner(f);
                String name = sc.nextLine();
                String description = sc.nextLine();
                String link = sc.nextLine();
                String urlLast = link.substring(link.lastIndexOf('/') + 1);
                String imgFilename = "lact_" + urlLast + ".png";
                final File imgFile = new File(getFilesDir(), imgFilename);
                FileInputStream inputStream = new FileInputStream(imgFile);
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                recipes.add(new Recipe(name, description, link, image));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return recipes;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
