package com.example.fbu_app.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fbu_app.activities.MainActivity;
import com.example.fbu_app.R;
import com.example.fbu_app.fragments.DetailsFragments.DetailsFragmentCreate;
import com.example.fbu_app.fragments.DetailsFragments.DetailsFragmentGo;
import com.example.fbu_app.fragments.NextVisitsFragment;
import com.example.fbu_app.models.Business;
import com.example.fbu_app.models.Visit;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

// Class to bind data to compare recyclerview
public class BusinessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // CODES FOR DIFFERENT LAYOUTS
    public static final int CODE_COMPARE = 0;
    public static  final  int CODE_LIKED = 1;

    // FIELDS
    private Context context;
    private List<Business> businesses; //
    private String visitDateStr; // Date values for visit creation
    private Date visitDate;
    int viewType;

    public BusinessAdapter(Context context, List<Business> businesses, int viewType) {
        this.context = context;
        this.businesses = businesses;
        this.viewType = viewType;
    }

    public BusinessAdapter(Context context, List<Business> businesses, String visitDateStr, Date visitDate, int viewType) {
        this.context = context;
        this.businesses = businesses;
        this.visitDateStr = visitDateStr; // visit info required to create new visit
        this.visitDate = visitDate;
        this.viewType = viewType;
    }

    @Override
    public int getItemViewType(int position) {
        return this.viewType;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == CODE_COMPARE) {
            View view = LayoutInflater.from(context).inflate(R.layout.businesses_compare_layout, parent, false);
            return new CompareViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.businesses_favorite_layout, parent, false);
            return new LikedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (viewType == CODE_COMPARE) {
            ((CompareViewHolder) holder).bind(businesses.get(position));
        } else {
            ((LikedViewHolder) holder).bind(businesses.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return businesses.size();
    }

    public class CompareViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // VIEWS
        private Business business;
        private ImageView ivBusinessImage;
        private TextView tvName, tvRating;
        private Button btnGo;


        public CompareViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            // Get views from layout
            ivBusinessImage = itemView.findViewById(R.id.ivBusinessImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnGo = itemView.findViewById(R.id.btnGo);

            itemView.setOnClickListener(this);

        }

        public void bind(Business businessToBind) {
            // TODO: FIND A BETTER WAY TO VERIFY IF BUSINESS IS ALREADY IN DATABASE
            // assign value to business
            business = businessToBind;
            // Query Business
            ParseQuery<Business> query = ParseQuery.getQuery(Business.class);
            query.whereEqualTo("yelpId", business.getYelpId());
            query.getFirstInBackground(new GetCallback<Business>() {
                @Override
                public void done(Business object, ParseException e) {
                    if(e != null) {
                        return;
                    }
                    if (object != null) {
                        business = object;
                    }
                }
            });
            // Bind data to views
            Glide.with(context)
                    .load(business.getImageUrl())
                    .into(ivBusinessImage);
            tvName.setText(business.getName());
            tvRating.setText("Rating: " + business.getRating() + "/5");

            // Button to create a new visit
            btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create new visit
                    Visit newVisit = new Visit();
                    newVisit.setBusiness(business);
                    // Add fields
                    newVisit.setUser(ParseUser.getCurrentUser());
                    newVisit.setDate(visitDate);
                    newVisit.setDateStr(visitDateStr);
                    // Save visit using background thread
                    newVisit.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null) {
                                Log.i("ParseSave", "Failed to save visit", e);
                                return;
                            }
                            // Display success message
                            Toast.makeText(context, "Succesfully created visit!", Toast.LENGTH_SHORT).show();
                            // Transaction to new fragment
                            NextVisitsFragment nextVisitsFragment = new NextVisitsFragment();
                            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.flContainer, nextVisitsFragment)
                                    .commit();
                            // Change selected item in bottom nav bar
                            BottomNavigationView bottomNavigationView = ((MainActivity) context).findViewById(R.id.bottomNavigation);
                            bottomNavigationView.setSelectedItemId(R.id.action_history);
                        }
                    });
                }
            });
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("business", business);

            DetailsFragmentGo detailsFragmentGo = new DetailsFragmentGo();
            detailsFragmentGo.setArguments(bundle);

            ((MainActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flContainer, detailsFragmentGo)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public class LikedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // VIEWS
        private Business business;
        private ImageView ivBusinessImage;
        private TextView tvName, tvRating;

        public LikedViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            // Get views from layout
            ivBusinessImage = itemView.findViewById(R.id.ivBusinessImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvRating = itemView.findViewById(R.id.tvRating);

            itemView.setOnClickListener(this);

        }

        public void bind(Business businessToBind) {
            // TODO: FIND A BETTER WAY TO VERIFY IF BUSINESS IS ALREADY IN DATABASE
            // assign value to business
            business = businessToBind;
            // Query Business
            ParseQuery<Business> query = ParseQuery.getQuery(Business.class);
            query.whereEqualTo("yelpId", business.getYelpId());
            query.getFirstInBackground(new GetCallback<Business>() {
                @Override
                public void done(Business object, ParseException e) {
                    if(e != null) {
                        return;
                    }
                    if (object != null) {
                        business = object;
                    }
                }
            });
            // Bind data to views
            Glide.with(context)
                    .load(business.getImageUrl())
                    .into(ivBusinessImage);
            tvName.setText(business.getName());
            tvRating.setText("Rating: " + business.getRating());

        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("business", business);

            DetailsFragmentCreate detailsFragmentCreate = new DetailsFragmentCreate();
            detailsFragmentCreate.setArguments(bundle);

            ((MainActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flContainer, detailsFragmentCreate)
                    .addToBackStack(null)
                    .commit();
        }
    }
}