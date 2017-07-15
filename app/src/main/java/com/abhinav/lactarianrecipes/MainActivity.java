package com.abhinav.lactarianrecipes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Set<String> mMealAndCourses;
    private Set<String> mIngredients;
    private Set<String> mCuisines;

    private Set<String> mIncludes;
    private Set<String> mExcludes;

    private String mFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mMealAndCourses = new HashSet<>();
        mIngredients = new HashSet<>();
        mCuisines = new HashSet<>();
        mIncludes = new HashSet<>();
        mExcludes = new HashSet<>();
        mFind = "";
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                ConnectivityManager manager = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = manager.getActiveNetworkInfo();
                boolean isConnected = info != null && info.isConnected();
                if (isConnected) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("http://www.epicurious.com/search/").append(mFind.replace(" ", "%20")).append("?special-consideration=vegetarian");
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("vegan_switch", false)) {
                        sb.append("%2Cvegan");
                    }
                    if (!mMealAndCourses.isEmpty()) {
                        sb.append("&meal=");
                        sb.append(TextUtils.join("%2C", mMealAndCourses));
                    }
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("jain_switch", false)) {
                        mIngredients.remove("carrot");
                        mIngredients.remove("potato");
                        mIngredients.remove("sweet-potato-yam");
                    }
                    if (!mIngredients.isEmpty()) {
                        sb.append("&ingredient=");
                        sb.append(TextUtils.join("%2C", mIngredients));
                    }
                    if (!mCuisines.isEmpty()) {
                        sb.append("&cuisine=");
                        sb.append(TextUtils.join("%2C", mCuisines));
                    }
                    sb.append("&content=recipe");
                    if (!mIncludes.isEmpty()) {
                        sb.append("&include=");
                        boolean first = true;
                        for (String mInclude : mIncludes) {
                            String mReplaceSpace = mInclude.replace(" ", "%20");
                            if (first) {
                                sb.append(mReplaceSpace);
                            } else {
                                sb.append("%2C");
                                sb.append(mReplaceSpace);
                            }
                            first = false;
                        }
                    }
                    sb.append("&exclude=Egg%2CFish%2CHam%2CSeafood%2CShrimp");
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("jain_switch", false)) {
                        sb.append("%2CCarrot%2CPotato%2CSweet%20Potato%2COnion%2CGarlic%2CGinger%2CShallot");
                    }
                    if (!mExcludes.isEmpty()) {
                        for (String mExclude : mExcludes) {
                            String mReplaceSpace = mExclude.replace(" ", "%20");
                            sb.append("%2C");
                            sb.append(mReplaceSpace);
                        }
                    }
                    Log.d("LACT_RECIPES", sb.toString());
                    Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                    intent.putExtra("QUERY_URL", sb.toString());
                    MainActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(view.getContext().getApplicationContext(), "You must be connected" +
                            " to the internet to view the matching recipes", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_include) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            editText.setGravity(Gravity.CENTER);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            editText.requestFocus();
            alert.setTitle("Include ingredients:");
            if (mIncludes.isEmpty()) {
                alert.setMessage("No includes added yet");
            } else {
                alert.setMessage("Includes: " + TextUtils.join(", ", mIncludes) + "\n" + "Click RESET to clear");
            }
            alert.setView(editText);
            alert.setPositiveButton("INCLUDE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String editTextVal = editText.getText().toString();
                    if (!editTextVal.isEmpty()) {
                        mExcludes.remove(editTextVal);
                        mIncludes.add(editTextVal);
                    }
                }
            });
            alert.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("RESET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mIncludes.clear();
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
            if (mIncludes.isEmpty()) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            }
            return true;
        } else if (id == R.id.action_exclude) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            editText.setGravity(Gravity.CENTER);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            editText.requestFocus();
            alert.setTitle("Exclude ingredients:");
            if (mExcludes.isEmpty()) {
                alert.setMessage("No excludes added yet");
            } else {
                alert.setMessage("Excludes: " + TextUtils.join(", ", mExcludes) + "\n" + "Click RESET to clear");
            }
            alert.setView(editText);
            alert.setPositiveButton("EXCLUDE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String editTextVal = editText.getText().toString();
                    if (!editTextVal.isEmpty()) {
                        mIncludes.remove(editTextVal);
                        mExcludes.add(editTextVal);
                    }
                }
            });
            alert.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("RESET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mExcludes.clear();
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
            if (mExcludes.isEmpty()) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            }
            return true;
        } else if (id == R.id.action_saved) {
            Intent intent = new Intent(this, StarredActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_find) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            editText.setGravity(Gravity.CENTER);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            editText.requestFocus();
            alert.setTitle("Find a Recipe:");
            if (mFind.isEmpty()) {
                alert.setMessage("No string set yet");
            } else {
                alert.setMessage("Matched recipes will contain: " + mFind + "\n" + "Click RESET to clear");
            }
            alert.setView(editText);
            alert.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String editTextVal = editText.getText().toString();
                    if (!editTextVal.isEmpty()) {
                        mFind = editTextVal;
                    }
                }
            });
            alert.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("RESET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mFind = "";
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
            if (mFind.isEmpty()) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.checkboxes);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            String text = "";
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
//                    text = "MEAL & COURSE";
                    linearLayout.addView(getCheckBox(R.string.dessert, "dessert", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.side, "side", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.dinner, "dinner", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.appetizer, "appetizer", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.lunch, "lunch", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.brunch, "brunch", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.breakfast, "breakfast", ((MainActivity) getActivity()).mMealAndCourses));
                    linearLayout.addView(getCheckBox(R.string.buffet, "buffet", ((MainActivity) getActivity()).mMealAndCourses));
                    break;
                case 2:
//                    text = "INGREDIENT";
                    linearLayout.addView(getCheckBox(R.string.apple, "apple", ((MainActivity) getActivity()).mIngredients));
//                    linearLayout.addView(getCheckBox(R.string.baby_corn, "baby corn", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.bean, "bean", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.broccoli, "broccoli", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.cabbage, "cabbage", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.carrot, "carrot", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.chocolate, "chocolate", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.citrus, "citrus", ((MainActivity) getActivity()).mIngredients));
//                    linearLayout.addView(getCheckBox(R.string.cottage_cheese, "cottage cheese", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.cranberry, "cranberry", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.eggplant, "eggplant", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.fruit, "fruit", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.green_bean, "green-bean", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.kale, "kale", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.leafy_green, "leafy-green", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.lemon, "lemon", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.mushroom, "mushroom", ((MainActivity) getActivity()).mIngredients));
//                    linearLayout.addView(getCheckBox(R.string.paneer, "paneer", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.pasta, "pasta", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.potato, "potato", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.rice, "rice", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.spinach, "spinach", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.sweet_potato, "sweet-potato-yam", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.tomato, "tomato", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.vegetable, "vegetable", ((MainActivity) getActivity()).mIngredients));
                    linearLayout.addView(getCheckBox(R.string.zucchini, "zucchini", ((MainActivity) getActivity()).mIngredients));
                    break;
                case 3:
//                    text = "CUISINE";
                    linearLayout.addView(getCheckBox(R.string.african, "african", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.american, "american", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.asian, "asian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.british, "british", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.cajun_creole, "cajun-creole", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.californian, "californian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.caribbean, "caribbean", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.central_south_american, "central-south-american", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.chinese, "chinese", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.cuban, "cuban", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.eastern_european_russian, "eastern-european-russian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.english, "english", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.european, "european", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.french, "french", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.german, "german", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.greek, "greek", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.indian, "indian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.irish, "irish", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.italian, "italian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.italian_american, "italian-american", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.japanese, "japanese", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.jewish, "jewish", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.korean, "korean", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.latin_american, "latin-american", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.mediterranean, "mediterranean", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.mexican, "mexican", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.middle_eastern, "middle-eastern", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.moroccan, "moroccan", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.nuevo_latino, "nuevo-latino", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.scandinavian, "scandinavian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.south_american, "south-american", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.south_asian, "south-asian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.southeast_asian, "southeast-asian", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.southern, "southern", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.southwestern, "southwestern", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.spanish_portuguese, "spanish-portuguese", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.tex_mex, "tex-mex", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.thai, "thai", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.turkish, "turkish", ((MainActivity) getActivity()).mCuisines));
                    linearLayout.addView(getCheckBox(R.string.vietnamese, "vietnamese", ((MainActivity) getActivity()).mCuisines));
                    break;
            }
//            textView.setText(text);
            return rootView;
        }

        private CheckBox getCheckBox(int resource, final String name, final Set<String> set) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            checkBox.setPadding(getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin), getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin), getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin), getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin));
            checkBox.setText(resource);
            checkBox.setChecked(set.contains(name));
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = ((CheckBox) v).isChecked();
                    if (checked) {
                        set.add(name);
                    } else {
                        set.remove(name);
                    }
                }
            });
            return checkBox;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MEAL & COURSE";
                case 1:
                    return "INGREDIENT";
                case 2:
                    return "CUISINE";
            }
            return null;
        }
    }
}
