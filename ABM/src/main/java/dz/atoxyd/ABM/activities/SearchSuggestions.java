package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.content.SearchRecentSuggestionsProvider;

/* SearchSuggestions
 * 		Pretty simple to provide search suggestions */
public class SearchSuggestions extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "dz.atoxyd.authority";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestions() {
        setupSuggestions(AUTHORITY, MODE);
    }
} // end class SearchSuggestions
