package com.abhinav.lactarianrecipes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.abhinav.lactarianrecipes.RecipeActivity.generatePrep;
import static com.abhinav.lactarianrecipes.RecipeActivity.generateShopping;

public class Recipe {
    private String mName;
    private String mDesc;
    private String mLink;
    private Bitmap mImage;

    public Recipe(String name, String desc, String link, Bitmap image) {
        mName = name;
        mDesc = desc;
        mLink = link;
        mImage = image;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDesc;
    }

    public String getLink() {
        return mLink;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public static void createRecipesListInitial(String url, TextView textView, RecyclerView rvItems, ProgressDialog dialog) {
        new GetResultsTask().execute(url, textView);
        new GetRecipesTask().execute(url, rvItems, dialog);
    }

    public static void createRecipesList(String url, int page, RecipesAdapter adapter, List<Recipe> allRecipes, RecyclerView view) {
        new GetMoreRecipesTask().execute(url, page, adapter, allRecipes, view);
    }

    public static void showRecipe(String url, ImageView recipeImageView, TextView recipeTitleView, TextView recipeYieldTimeView, RecyclerView recipeIngredientsView, RecyclerView recipePreparationView, ProgressDialog dialog) {
        new GetRecipeTask().execute(url, recipeImageView, recipeTitleView, recipeYieldTimeView, recipeIngredientsView, recipePreparationView, dialog);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recipe recipe = (Recipe) o;

        if (mName != null ? !mName.equals(recipe.mName) : recipe.mName != null) return false;
        if (mDesc != null ? !mDesc.equals(recipe.mDesc) : recipe.mDesc != null) return false;
        return mLink != null ? mLink.equals(recipe.mLink) : recipe.mLink == null;
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + (mDesc != null ? mDesc.hashCode() : 0);
        result = 31 * result + (mLink != null ? mLink.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "mName='" + mName + '\'' +
                ", mDesc='" + mDesc + '\'' +
                ", mLink='" + mLink + '\'' +
                '}';
    }

    private static class GetResultsTask extends AsyncTask<Object, Void, Integer> {
        private TextView mTextView;

        @Override
        protected Integer doInBackground(Object... params) {
            String url = (String) params[0];
            mTextView = (TextView) params[1];

            try {
                Document doc = Jsoup.connect(url).get();
                Elements numResults = doc.select("div.filter-action-panel p");
                if (numResults.hasText()) {
                    String text = numResults.text();
                    return Integer.parseInt(text.split(" ")[0].replace(",", ""));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer numResults) {
            CharSequence charSequence = numResults + " matching recipes found";
            mTextView.setText(charSequence);
        }
    }

    private static class GetRecipesTask extends AsyncTask<Object, Void, List<Recipe>> {
        private String url;
        private RecyclerView mRvItems;
        private ProgressDialog mDialog;

        @Override
        protected List<Recipe> doInBackground(Object... params) {
            url = (String) params[0];
            mRvItems = (RecyclerView) params[1];
            mDialog = (ProgressDialog) params[2];

            try {
                Document doc = Jsoup.connect(url).get();
                Elements recipeTitles = doc.select("h4.hed");
                if (recipeTitles.hasText()) {
                    List<String> names = recipeTitles.eachText();
                    Elements recipeDescriptions = recipeTitles.next();
                    List<String> descriptions = new ArrayList<>();
                    for (Element recipeDescription : recipeDescriptions) {
                        if (recipeDescription.tagName().equals("p") && recipeDescription.className().equals("dek")) {
                            descriptions.add(recipeDescription.text());
                        } else {
                            descriptions.add("");
                        }
                    }
                    List<String> links = new ArrayList<>();
                    for (Element recipeTitle : recipeTitles) {
                        Element recipeLink = recipeTitle.child(0);
                        String link = recipeLink.attr("abs:href");
                        links.add(link);
                    }
                    List<Recipe> recipes = new ArrayList<>();
                    for (int i = 0; i < names.size(); i++) {
                        String name = names.get(i);
                        String description = descriptions.get(i);
                        String link = links.get(i);
                        doc = Jsoup.connect(link).get();
                        Elements rImages = doc.select("img.photo");
                        Element rImage = rImages.first();
                        String imgLink = rImage == null ? "" : rImage.attr("abs:srcset");
                        Bitmap image;
                        try {
                            image = BitmapFactory.decodeStream(new URL(imgLink).openStream());
                        } catch (IOException e) {
                            image = Bitmap.createBitmap(274, 169, Bitmap.Config.ARGB_8888);
                        }
                        recipes.add(new Recipe(name, description, link, image));
                    }
                    return recipes;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(final List<Recipe> recipes) {
            final RecipesAdapter adapter = new RecipesAdapter(recipes, false);
            mRvItems.setAdapter(adapter);
            EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) mRvItems.getLayoutManager()) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    Recipe.createRecipesList(url, page + 1, adapter, recipes, view);
                }
            };
            mRvItems.addOnScrollListener(scrollListener);
            mDialog.dismiss();
        }
    }

    private static class GetMoreRecipesTask extends AsyncTask<Object, Void, List<Recipe>> {
        private RecipesAdapter mAdapter;
        private List<Recipe> mRecipes;
        private RecyclerView mView;

        @Override
        protected List<Recipe> doInBackground(Object... params) {
            String url = (String) params[0];
            int page = (Integer) params[1];
            url += "&page=" + page;
            mAdapter = (RecipesAdapter) params[2];
            mRecipes = (List<Recipe>) params[3];
            mView = (RecyclerView) params[4];

            try {
                Document doc = Jsoup.connect(url).get();
                Elements recipeTitles = doc.select("h4.hed");
                if (recipeTitles.hasText()) {
                    List<String> names = recipeTitles.eachText();
                    Elements recipeDescriptions = recipeTitles.next();
                    List<String> descriptions = new ArrayList<>();
                    for (Element recipeDescription : recipeDescriptions) {
                        if (recipeDescription.tagName().equals("p") && recipeDescription.className().equals("dek")) {
                            descriptions.add(recipeDescription.text());
                        } else {
                            descriptions.add("");
                        }
                    }
                    List<String> links = new ArrayList<>();
                    for (Element recipeTitle : recipeTitles) {
                        Element recipeLink = recipeTitle.child(0);
                        String link = recipeLink.attr("abs:href");
                        links.add(link);
                    }
                    List<Recipe> recipes = new ArrayList<>();
                    for (int i = 0; i < names.size(); i++) {
                        String name = names.get(i);
                        String description = descriptions.get(i);
                        String link = links.get(i);
                        doc = Jsoup.connect(link).get();
                        Elements rImages = doc.select("img.photo");
                        Element rImage = rImages.first();
                        String imgLink = rImage == null ? "" : rImage.attr("abs:srcset");
                        Bitmap image;
                        try {
                            image = BitmapFactory.decodeStream(new URL(imgLink).openStream());
                        } catch (IOException e) {
                            image = Bitmap.createBitmap(274, 169, Bitmap.Config.ARGB_8888);
                        }
                        recipes.add(new Recipe(name, description, link, image));
                    }
                    return recipes;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Recipe> recipes) {
            final int curSize = mAdapter.getItemCount();
            mRecipes.addAll(recipes);

            mView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyItemRangeInserted(curSize, mRecipes.size() - 1);
                }
            });
        }
    }

    private static class GetRecipeTask extends AsyncTask<Object, Void, List<Object>> {
        private ImageView mImage;
        private TextView mTitle;
        private TextView mYTime;
        private RecyclerView mIngre;
        private RecyclerView mPrepe;
        private ProgressDialog mDialo;

        @Override
        protected List<Object> doInBackground(Object... params) {
            String url = (String) params[0];
            mImage = (ImageView) params[1];
            mTitle = (TextView) params[2];
            mYTime = (TextView) params[3];
            mIngre = (RecyclerView) params[4];
            mPrepe = (RecyclerView) params[5];
            mDialo = (ProgressDialog) params[6];

            List<Object> toReturn = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(url).get();
                Elements rImages = doc.select("img.photo");
                Element rImage = rImages.first();
                String link = rImage == null ? "" : rImage.attr("abs:srcset");
                try {
                    Bitmap b = BitmapFactory.decodeStream(new URL(link).openStream());
                    toReturn.add(b);
                } catch (IOException e) {
                    toReturn.add(Bitmap.createBitmap(274, 169, Bitmap.Config.ARGB_8888));
                }
                Elements rTitle = doc.select("h1[itemprop]");
                if (rTitle.hasText()) {
                    toReturn.add(rTitle.text());
                } else {
                    toReturn.add("");
                }
                StringBuilder yTime = new StringBuilder();
                yTime.append("<b>YIELD:</b> ");
                Elements rYield = doc.select("dd.yield");
                if (rYield.hasText()) {
                    yTime.append(rYield.text());
                }
                yTime.append("<br />");
                yTime.append("<b>ACTIVE TIME:</b> ");
                Elements rATime = doc.select("dd.active-time");
                if (rATime.hasText()) {
                    yTime.append(rATime.text());
                }
                yTime.append("<br />");
                yTime.append("<b>TOTAL TIME:</b> ");
                Elements rTTime = doc.select("dd.total-time");
                if (rTTime.hasText()) {
                    yTime.append(rTTime.text());
                }
                yTime.append("<br />");
                toReturn.add(Html.fromHtml(yTime.toString()));
                List<String> ingredients = new ArrayList<>();
                Elements rIngredients = doc.select("li.ingredient");
                for (Element rIngredient : rIngredients) {
                    StringBuilder line = new StringBuilder();
//                    line.append("• ");
                    if (rIngredient.hasText()) {
                        line.append(rIngredient.text());
                    }
                    ingredients.add(line.toString());
                }
                ShoppingAdapter adapter = new ShoppingAdapter(generateShopping(ingredients));
                toReturn.add(adapter);
                List<String> prep = new ArrayList<>();
                Elements rPreparationSteps = doc.select("li.preparation-step");
                for (Element rPreparationStep : rPreparationSteps) {
                    StringBuilder line = new StringBuilder();
                    if (rPreparationStep.hasText()) {
                        line.append(rPreparationStep.text());
                    }
                    prep.add(line.toString());
                }
                toReturn.add(prep);
                toReturn.add(link);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toReturn;
        }

        @Override
        protected void onPostExecute(List<Object> objects) {
            Bitmap bImage = (Bitmap) objects.get(0);
            mImage.setImageBitmap(bImage);
            String title = (String) objects.get(1);
            mTitle.setText(title);
            Spanned yTime = (Spanned) objects.get(2);
            mYTime.setText(yTime);
            mYTime.setText(mYTime.getText().toString().trim());
            ShoppingAdapter adapter = (ShoppingAdapter) objects.get(3);
            mIngre.setAdapter(adapter);
            List<String> prep = (List<String>) objects.get(4);
            PreparationAdapter adapter1 = new PreparationAdapter(mPrepe.getContext(), generatePrep(prep), (PreparationAdapter.ViewHolder.ClickListener) mPrepe.getContext());
            mPrepe.setAdapter(adapter1);
            ((RecipeActivity) mPrepe.getContext()).setAdapterPreparation(adapter1);
            final String link = (String) objects.get(5);
            if (!link.isEmpty()) {
                mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), RecipeImageActivity.class);
                        intent.putExtra("QUERY_URL", link);
                        v.getContext().startActivity(intent);
                    }
                });
            }
            mDialo.dismiss();
        }
    }
}