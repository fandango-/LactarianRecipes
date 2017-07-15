package com.abhinav.lactarianrecipes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class RecipeActivity extends AppCompatActivity implements PreparationAdapter.ViewHolder.ClickListener {
    private boolean unStarred = false;
    private String url;
    private String name;
    private String description;

    private TextToSpeech tts;

    public void setAdapterPreparation(PreparationAdapter adapterPreparation) {
        mAdapterPreparation = adapterPreparation;
    }

    private PreparationAdapter mAdapterPreparation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(RecipeActivity.this.getResources().getString(R.string.title_activity_recipe));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        url = extras.getString("QUERY_URL");
        name = extras.getString("RECIPE_NAME");
        description = extras.getString("RECIPE_DESCRIPTION");
        boolean starred = extras.getBoolean("IS_STARRED");

        final ImageView recipeImageView = (ImageView) findViewById(R.id.recipe_image);
        final TextView recipeTitleView = (TextView) findViewById(R.id.recipe_title);
        final TextView recipeYieldTimeView = (TextView) findViewById(R.id.recipe_yield_time);
        final RecyclerView recipeIngredientsView = (RecyclerView) findViewById(R.id.recipe_ingredients);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setAutoMeasureEnabled(true);
        if (recipeIngredientsView != null) {
            recipeIngredientsView.setLayoutManager(mLayoutManager);
            recipeIngredientsView.setItemAnimator(new DefaultItemAnimator());
        }
        final RecyclerView recipePreparationView = (RecyclerView) findViewById(R.id.recipe_preparation);
        LinearLayoutManager mLayoutManagerPrep = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManagerPrep.setAutoMeasureEnabled(true);
        if (recipePreparationView != null) {
            recipePreparationView.setLayoutManager(mLayoutManagerPrep);
            recipePreparationView.setItemAnimator(new DefaultItemAnimator());
        }

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        if (starred) {
            String urlLast = url.substring(url.lastIndexOf('/') + 1);
            String filename = "lact_" + urlLast + ".txt";
            String imgFilename = "lact_" + urlLast + ".png";
            final File file = new File(getFilesDir(), filename);
            final File imgFile = new File(getFilesDir(), imgFilename);
            try {
                FileInputStream inputStream = new FileInputStream(imgFile);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                recipeImageView.setImageBitmap(bitmap);
                Scanner sc = new Scanner(file);
                sc.nextLine();
                sc.nextLine();
                sc.nextLine();
                String title = sc.nextLine();
                recipeTitleView.setText(title);
                StringBuilder yieldTime = new StringBuilder();
                String line;
                while (!(line = sc.nextLine()).equals("INGREDIENTS")) {
                    yieldTime.append(line).append('\n');
                }
                recipeYieldTimeView.setText(yieldTime.toString().trim());
                List<String> ingredients = new ArrayList<>();
                while (!(line = sc.nextLine()).equals("PREPARATION")) {
                    ingredients.add(line.substring(line.indexOf(' ') + 1));
                }
                ShoppingAdapter adapter = new ShoppingAdapter(generateShopping(ingredients));
                if (recipeIngredientsView != null) {
                    recipeIngredientsView.setAdapter(adapter);
                }
                Log.d("LACT_RECIPES", getText(recipeIngredientsView));
                List<String> preparation = new ArrayList<>();
                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    preparation.add(line);
                }
                mAdapterPreparation = new PreparationAdapter(getBaseContext(), generatePrep(preparation), this);
                if (recipePreparationView != null) {
                    recipePreparationView.setAdapter(mAdapterPreparation);
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] bytes = outputStream.toByteArray();
                String imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                final String dataURL = "data:image/png;base64," + imageBase64;
                recipeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), RecipeImageActivity.class);
                        intent.putExtra("QUERY_URL", dataURL);
                        v.getContext().startActivity(intent);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            ProgressDialog dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);

            Recipe.showRecipe(url, recipeImageView, recipeTitleView, recipeYieldTimeView, recipeIngredientsView, recipePreparationView,
                    dialog);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, recipeTitleView.getText());
                final String shareBody = String.valueOf(recipeTitleView.getText()) + "\n\n"
                        + String.valueOf(recipeYieldTimeView.getText()) + "\n\n"
                        + "INGREDIENTS\n"
                        + getText(recipeIngredientsView) + "\n\n"
                        + "PREPARATION\n"
                        + getText(recipePreparationView.getAdapter());
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        final FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        if (url != null) {
            String urlLast = url.substring(url.lastIndexOf('/') + 1);
            String filename = "lact_" + urlLast + ".txt";
            String imgFilename = "lact_" + urlLast + ".png";
            final File file = new File(getFilesDir(), filename);
            final File imgFile = new File(getFilesDir(), imgFilename);
            if (file.exists()) {
                fab2.setImageResource(android.R.drawable.star_big_on);
            }
            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file.exists()) {
                        fab2.setImageResource(android.R.drawable.star_big_off);
                        file.delete();
                        imgFile.delete();
                        unStarred = true;
                    } else {
                        try {
                            PrintStream ps = new PrintStream(new FileOutputStream(file));
                            ps.println(name);
                            ps.println(description);
                            ps.println(url);
                            ps.println(recipeTitleView.getText());
                            ps.println(recipeYieldTimeView.getText());
                            ps.println("INGREDIENTS");
                            ps.println(getText(recipeIngredientsView));
                            ps.println("PREPARATION");
                            ps.println(getText(recipePreparationView.getAdapter()));
                            FileOutputStream outputStream = new FileOutputStream(imgFile);
                            Bitmap bitmap = ((BitmapDrawable) recipeImageView.getDrawable()).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            fab2.setImageResource(android.R.drawable.star_big_on);
                            unStarred = false;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String shareBody = String.valueOf(recipeTitleView.getText()) + "\n\n"
                        + String.valueOf(recipeYieldTimeView.getText()) + "\n\n"
                        + "INGREDIENTS\n"
                        + getText(recipeIngredientsView) + "\n\n"
                        + "PREPARATION\n"
                        + getText(recipePreparationView.getAdapter());
                ConvertTextToSpeech(shareBody);
            }
        });
    }

    private String getText(RecyclerView.Adapter adapter) {
        PreparationAdapter adapter1 = (PreparationAdapter) adapter;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < adapter1.getItemCount(); i++) {
            sb.append(adapter1.getItem(i).getStep()).append('\n').append('\n');
        }
        return sb.toString().trim();
    }

    public static List<ItemPreparation> generatePrep(List<String> lines) {
        List<ItemPreparation> itemList = new ArrayList<>();
        int i = 0;
        for (String line : lines) {
            if (!line.isEmpty()) {
                ++i;
                itemList.add(new ItemPreparation(line, String.valueOf(i)));
            }
        }
        return itemList;
    }

    private String getText(RecyclerView recyclerView) {
        ShoppingAdapter adapter = (ShoppingAdapter) recyclerView.getAdapter();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            sb.append(getString(R.string.bullet)).append(' ').append(adapter.getItem(i).getName()).append('\n');
        }
        return sb.toString().trim();
    }

    public static List<ItemShopping> generateShopping(List<String> lines) {
        List<ItemShopping> itemList = new ArrayList<>();
        for (String line : lines) {
            if (!line.isEmpty()) {
                itemList.add(new ItemShopping(line));
            }
        }
        return itemList;
    }

    @Override
    protected void onPause() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });
        super.onResume();
    }

    private void ConvertTextToSpeech(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            //noinspection deprecation
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", unStarred);
        returnIntent.putExtra("url", url);
        returnIntent.putExtra("name", name);
        returnIntent.putExtra("desc", description);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", unStarred);
            returnIntent.putExtra("url", url);
            returnIntent.putExtra("name", name);
            returnIntent.putExtra("desc", description);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(int position) {

    }

    @Override
    public boolean onItemLongClicked(int position) {
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        mAdapterPreparation.toggleSelection(position);
    }
}
