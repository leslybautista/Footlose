package com.example.maplistpage;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.airbnb.lottie.LottieAnimationView;

public class TutorialsFragment extends Fragment {

    EditText searchInput;
    MaterialButton searchButton;
    MaterialButton infoButtonHigh, infoButtonMedium, infoButtonLow;

    LinearLayout highMobilityButtons;
    LinearLayout mediumMobilityButtons;
    LinearLayout lowMobilityButtons;

    LottieAnimationView doritoButton;
    TextView helpLabel;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_tutorials, container, false);

        // Bind views
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.button_search);

        highMobilityButtons = view.findViewById(R.id.high_mobility_buttons);
        mediumMobilityButtons = view.findViewById(R.id.medium_mobility_buttons);
        lowMobilityButtons = view.findViewById(R.id.low_mobility_buttons);

        infoButtonHigh = view.findViewById(R.id.info_button_high);
        infoButtonMedium = view.findViewById(R.id.info_button_medium);
        infoButtonLow = view.findViewById(R.id.info_button_low);

        doritoButton = view.findViewById(R.id.dorito_button);
        helpLabel = view.findViewById(R.id.help_label);

        // Database
        dbHelper = new DatabaseHelper(requireContext());
        database = dbHelper.getReadableDatabase();

        // Load dances by mobility level
        loadDancesByMobility();

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            searchDances(query);
        });

        // Info buttons
        infoButtonHigh.setOnClickListener(v -> showMobilityInfo("High mobility - Standing up",
                "Videos for seniors who can stand up.\n\n" +
                        "These dances involve:\n" +
                        "• Standing positions\n" +
                        "• Full body movement\n" +
                        "• Balance exercises\n" +
                        "• Active footwork"));

        infoButtonMedium.setOnClickListener(v -> showMobilityInfo("Medium mobility - Walker/Cane users",
                "This category is for users who use walkers, canes, or need some support while dancing.\n\n" +
                        "These dances include:\n" +
                        "• Seated or supported movement\n" +
                        "• Upper body exercises\n" +
                        "• Gentle rhythmic movements\n" +
                        "• Balance support available"));

        infoButtonLow.setOnClickListener(v -> showMobilityInfo("Low mobility - Wheelchair users",
                "This category is for wheelchair users or those with limited mobility.\n\n" +
                        "All dances can be performed:\n" +
                        "• While seated\n" +
                        "• Upper body focus\n" +
                        "• Arm and hand movements\n" +
                        "• Adapted rhythms"));

        // Dorito Help Button
        View.OnClickListener helpClickListener = v -> {
            doritoButton.playAnimation();
            showHelpText();
        };

        doritoButton.setOnClickListener(helpClickListener);
        helpLabel.setOnClickListener(v -> doritoButton.performClick());

        return view;
    }

    private void loadDancesByMobility() {
        // Clear previous buttons
        highMobilityButtons.removeAllViews();
        mediumMobilityButtons.removeAllViews();
        lowMobilityButtons.removeAllViews();

        // Query high mobility dances
        Cursor highCursor = database.rawQuery(
                "SELECT Rhythm, Url FROM dances WHERE LOWER(Mobility) = 'high'",
                null
        );
        addDanceButtons(highMobilityButtons, highCursor);

        // Query medium mobility dances
        Cursor mediumCursor = database.rawQuery(
                "SELECT Rhythm, Url FROM dances WHERE LOWER(Mobility) = 'medium'",
                null
        );
        addDanceButtons(mediumMobilityButtons, mediumCursor);

        // Query low mobility dances
        Cursor lowCursor = database.rawQuery(
                "SELECT Rhythm, Url FROM dances WHERE LOWER(Mobility) = 'low'",
                null
        );
        addDanceButtons(lowMobilityButtons, lowCursor);
    }

    private void addDanceButtons(LinearLayout container, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String rhythm = cursor.getString(cursor.getColumnIndexOrThrow("Rhythm"));
                String url = cursor.getString(cursor.getColumnIndexOrThrow("Url"));

                // Format rhythm name: first letter uppercase, rest lowercase
                String formattedRhythm = formatDanceName(rhythm);

                MaterialButton button = new MaterialButton(requireContext());
                button.setText(formattedRhythm);
                button.setTextColor(getResources().getColor(android.R.color.white));
                button.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                button.setCornerRadius(50);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 16, 0); // Margin right for spacing
                button.setLayoutParams(params);

                button.setPadding(40, 24, 40, 24);

                button.setOnClickListener(v -> openYouTubeLink(url));

                container.addView(button);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private String formatDanceName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        // Convert to lowercase and capitalize first letter of each word
        String[] words = name.toLowerCase().split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    }

    private void searchDances(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadDancesByMobility();
            return;
        }

        // Clear all sections
        highMobilityButtons.removeAllViews();
        mediumMobilityButtons.removeAllViews();
        lowMobilityButtons.removeAllViews();

        // Search and categorize results
        Cursor searchCursor = database.rawQuery(
                "SELECT Rhythm, Mobility, Url FROM dances " +
                        "WHERE Rhythm LIKE ? OR Mobility LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"}
        );

        if (searchCursor != null && searchCursor.moveToFirst()) {
            do {
                String rhythm = searchCursor.getString(searchCursor.getColumnIndexOrThrow("Rhythm"));
                String mobility = searchCursor.getString(searchCursor.getColumnIndexOrThrow("Mobility"));
                String url = searchCursor.getString(searchCursor.getColumnIndexOrThrow("Url"));

                MaterialButton button = createDanceButton(rhythm, url);

                // Add to appropriate section based on mobility
                switch (mobility.toLowerCase()) {
                    case "high":
                        highMobilityButtons.addView(button);
                        break;
                    case "medium":
                        mediumMobilityButtons.addView(button);
                        break;
                    case "low":
                        lowMobilityButtons.addView(button);
                        break;
                }
            } while (searchCursor.moveToNext());

            searchCursor.close();
        } else {
            Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
        }
    }

    private MaterialButton createDanceButton(String rhythm, String url) {
        String formattedRhythm = formatDanceName(rhythm);

        MaterialButton button = new MaterialButton(requireContext());
        button.setText(formattedRhythm);
        button.setTextColor(getResources().getColor(android.R.color.white));
        button.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        button.setCornerRadius(50);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0);
        button.setLayoutParams(params);
        button.setPadding(40, 24, 40, 24);

        button.setOnClickListener(v -> openYouTubeLink(url));

        return button;
    }

    private void openYouTubeLink(String url) {
        try {
            // Try to open in browser instead of YouTube app
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Force to open in browser, not YouTube app
            intent.setPackage("com.android.chrome"); // Chrome browser
            startActivity(intent);
        } catch (Exception e) {
            // If Chrome is not available, try default browser
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(requireContext(),
                        "Cannot open video. Please install a browser.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMobilityInfo(String title, String message) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_help, null);

        TextView txtHelp = dialogView.findViewById(R.id.txt_help);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close);

        // Format message with HTML
        String formattedMessage = "<b>" + title + "</b><br><br>" + message.replace("\n", "<br>");
        txtHelp.setText(android.text.Html.fromHtml(formattedMessage));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Set transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void showHelpText() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_help, null);

        TextView txtHelp = dialogView.findViewById(R.id.txt_help);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close);

        String helpText =
                "Explore dance tutorials:<br><br>"
                        + "• Use the <b>search bar</b> to find the tutorial that best suits you.<br>"
                        + "• Click the <b>i (info) button</b> to see extra details, like mobility levels.<br>"
                        + "• Click the <b>rhythm buttons</b> to open a YouTube video and follow along.";

        txtHelp.setText(android.text.Html.fromHtml(helpText));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    @Override
    public void onDestroyView() {
        if (database != null) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroyView();
    }
}