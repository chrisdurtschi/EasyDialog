/*
 * Copyright (C) Jared Rummler (jrummy16@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrummy.apps.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jrummy.apps.R;

/**
 * A custom dialog that simplifies adding a list, checkbox, edittext, buttons, etc.
 * <br><br>
 * Example code:
 * <br>
 * <pre>
 * {@code
 * new EasyDialog.Builder(context, themeId)
 * .setTitle(R.string.dialog_title)
 * .setMessage(R.string.dialog_message)
 * .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
 *		
 * 		public void onClick(DialogInterface dialog, int which) {
 * 			dialog.dismiss();
 * 		}
 * 	})
 * .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 *		
 * 		public void onClick(DialogInterface dialog, int which) {
 * 			dialog.dismiss();
 * 		}
 * 	})
 *  .show();
 * 
 * </pre>
 * @author Jared Rummler
 *
 */
public class EasyDialog extends Dialog {

	/** The default theme for the dialog. This resembles Android's Holo theme */
	public static final int THEME_HOLO = R.style.Theme_Dialog;
	/** A theme that resembles Android's Holo.Light theme. */
	public static final int THEME_HOLO_LIGHT = R.style.Theme_Dialog_Light;
	/** Much like the Holo theme but with a glowing holo colored border and transparent buttons */
	public static final int THEME_ICS = R.style.Theme_Dialog_ICS;
	/** Much like the Holo.Light theme but with transparent buttons */
	public static final int THEME_ICS_LIGHT = R.style.Theme_Dialog_Light_ICS;
	/** Much like the Holo theme but with a glowing holo colored border */
	public static final int THEME_JB = R.style.Theme_Dialog_JB;

	public static final int LIST_STYLE_LISTVIEW = 0x01;
	public static final int LIST_STYLE_GRIDVIEW = 0x02;
	public static final int LIST_STYLE_SINGLE_CHOICE = 0x03;
	public static final int LIST_STYLE_MULTI_CHOICE = 0x04;

	/** The Builder */
	private Builder mBuilder;

	/** If the divider over the buttons show be shown */
	private boolean mHideButtonDividers;

	/** The ListAdapter for the ListView and GridView */
	private EasyDialogListAdapter mAdapter;

	// Title View
	private RelativeLayout mTitleLayout;
	private TextView mTitleText;
	private TextView mSubtitleText;
	private ImageView mTitleIcon;
	private ProgressBar mTitleProgress;
	private View mTitleDivider;
	private CheckBox mTitleCheckBox;

	// Main Content Views
	private RelativeLayout mDialogLayout;
	private ScrollView mMessageLayout;
	private TextView mMessageText;
	private LinearLayout mIndeterminateProgressLayout;
	private ProgressBar mIndeterminateProgress;
	private TextView mIndeterminateProgressText;
	private RelativeLayout mHorizontalProgressLayout;
	private ProgressBar mHorizontalProgress;
	private TextView mHorizontalProgressPercentText;
	private TextView mHorizontalProgressMessageText;
	private TextView mHorizontalProgressCountText;
	private WebView mWebView;
	private ListView mListView;
	private GridView mGridView;
	private EditText mEditText;
	private CheckBox mCheckBox;

	// Dialog Buttons
	private View mDialogButtonDivider;
	private LinearLayout mDialogButtonLayout;
	private Button mNegativeButton;
	private Button mNeutralButton;
	private Button mPositiveButton;
	private View mNegativeButtonDivider;
	private View mPositiveButtonDivider;

	public EasyDialog(Builder builder) {
		super(builder.mContext, builder.mThemeId);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		build(builder);
	}

	/**
	 * Builds the dialog from the Builder
	 */
	public void build(Builder builder) {
		// Set the builder
		mBuilder = builder;
		// Set the content view of the dialog
		setContentView(R.layout.dialog_main);
		// Find the various views
		findViews();
		// Set if the dialog is cancelable
		setCancelable(builder.mCancelable);
		setCanceledOnTouchOutside(builder.mCancelableOutsideTouch);
		// Set various dialog listeners
		setListeners();
		// Set any custom fonts
		setCustomFonts();
		// Sets the dialog background
		setBackgroundFromBuilder();
		// Set the title view from the builder
		setTitleView();
		// Set the main dialog view from the builder
		setDialogView();
		// Set the EditText view from the builder
		setEditText();
		// Set the CheckBox from the builder
		setCheckBoxView();
		// Set the positive, negative, and neutral buttons
		setButtonView();
	}

	/**
	 * Rebuilds the dialog from the {@link Builder} this dialog was initialized with
	 */
	public void rebuild() {
		build(mBuilder);
	}

	/**
	 * Runs {@link #build(Builder)} from the UI thread.
	 * 
	 * @see #build(Builder)
	 * @param builder The {@link Builder}
	 * @param handler A {@link Handler} initialized on the UI thread.
	 */
	public void build(final Builder builder, Handler handler) {
		handler.post(new Runnable( ) {
			public void run() {
				build(builder);
			}
		});
	}

	/**
	 * Runs {@link #rebuild()} from the UI thread.
	 * 
	 * @param handler A {@link Handler} initialized on the UI thread.
	 */
	public void rebuild(Handler handler) {
		handler.post(new Runnable( ) {
			public void run() {
				rebuild();
			}
		});
	}

	/**
	 * Finds all the views in the dialog
	 */
	private void findViews() {
		// Dialog Title
		mTitleLayout = (RelativeLayout) findViewById(R.id.layout_dialog_title);
		mTitleText = (TextView) findViewById(R.id.title_text);
		mSubtitleText = (TextView) findViewById(R.id.subtitle_text);
		mTitleIcon = (ImageView) findViewById(R.id.title_icon);
		mTitleProgress = (ProgressBar) findViewById(R.id.title_progress);
		mTitleCheckBox = (CheckBox) findViewById(R.id.title_checkbox);
		mTitleDivider = (View) findViewById(R.id.title_divider);
		// Dialog Content
		mDialogLayout = (RelativeLayout) findViewById(R.id.layout_dialog_view);
		mMessageLayout = (ScrollView) findViewById(R.id.scroll_dialog_message);
		mMessageText = (TextView) findViewById(R.id.dialog_message);
		mIndeterminateProgressLayout = (LinearLayout) findViewById(R.id.layout_indeterminate_progress);
		mIndeterminateProgress = (ProgressBar) findViewById(R.id.dialog_progress_indeterminate);
		mIndeterminateProgressText = (TextView) findViewById(R.id.progress_indeterminate_message);
		mHorizontalProgressLayout = (RelativeLayout) findViewById(R.id.layout_horizontal_progress);
		mHorizontalProgress = (ProgressBar) findViewById(R.id.horizontal_progress_bar);
		mHorizontalProgressPercentText = (TextView) findViewById(R.id.horizontal_progress_percent);
		mHorizontalProgressMessageText = (TextView) findViewById(R.id.horizontal_progress_message);
		mHorizontalProgressCountText = (TextView) findViewById(R.id.horizontal_progress_count);
		mWebView = (WebView) findViewById(R.id.dialog_webview);
		mListView = (ListView) findViewById(R.id.dialog_listview);
		mGridView = (GridView) findViewById(R.id.dialog_gridview);
		mEditText = (EditText) findViewById(R.id.dialog_edittext);
		mCheckBox = (CheckBox) findViewById(R.id.dialog_checkbox);
		// Dialog Buttons
		mDialogButtonDivider = (View) findViewById(R.id.dialog_button_divider);
		mDialogButtonLayout = (LinearLayout) findViewById(R.id.dialog_buttons);
		mNegativeButton = (Button) findViewById(R.id.negative_dialog_button);
		mNeutralButton = (Button) findViewById(R.id.neutral_dialog_button);
		mPositiveButton = (Button) findViewById(R.id.positive_dialog_button);
		mNegativeButtonDivider = (View) findViewById(R.id.negative_button_divider);
		mPositiveButtonDivider = (View) findViewById(R.id.positive_button_divider);
	}

	private void setBackgroundFromBuilder() {
		if (mBuilder.mDialogBackground != null) {
			setBackground(mBuilder.mDialogBackground);
		}

		if (mBuilder.mDialogBackgroundResId != -1) {
			setBackground(mBuilder.mDialogBackgroundResId);
		}
	}

	/**
	 * Sets the initial callbacks for the dialog
	 */
	private void setListeners() {
		if (mBuilder.mOnKeyListener != null) {
			setOnKeyListener(mBuilder.mOnKeyListener);
		}

		if (mBuilder.mOnCancelListener != null) {
			setOnCancelListener(mBuilder.mOnCancelListener);
		}

		if (mBuilder.mOnShowListener != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			setOnShowListener(mBuilder.mOnShowListener);
		}

		if (mBuilder.mOnDismissListener != null) {
			setOnDismissListener(mBuilder.mOnDismissListener);
		}
	}

	/**
	 * Sets the initial title
	 */
	private void setTitleView() {
		if (mBuilder.mCustomTitleView != null) {
			setCustomTitle(mBuilder.mCustomTitleView);
		} else if (mBuilder.mTitleIcon == null && mBuilder.mTitleText == null) {
			removeTitle();
			hideTitle();
		} else {
			if (mBuilder.mTitleIcon != null) {
				setIcon(mBuilder.mTitleIcon);
			} else {
				mTitleIcon.setVisibility(View.GONE);
			}

			if (mBuilder.mSubtitleText != null) {
				setSubtitle(mBuilder.mSubtitleText);
			} else if (mSubtitleText.getVisibility() == View.VISIBLE) {
				removeSubtitle();
			}

			setTitle(mBuilder.mTitleText);
			showTitleProgress(mBuilder.mShowTitlebarProgress);

			if (mBuilder.mTitleCheckbox != null) {
				mTitleCheckBox.setVisibility(View.VISIBLE);
				mTitleCheckBox.setChecked(mBuilder.mTitleCheckbox);
				mTitleCheckBox.setOnCheckedChangeListener(mBuilder.mTitleCheckBoxListener);
			} else {
				mTitleCheckBox.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Sets the initial views in the dialog
	 */
	private void setDialogView() {
		if (mBuilder.mMainDialogView != null) {
			setDialogView(mBuilder.mMainDialogView);
		} else {
			if (mBuilder.mDialogMessage != null) {
				setDialogMessageVisibility(View.VISIBLE);
				mMessageText.setText(mBuilder.mDialogMessage);
			} else {
				setDialogMessageVisibility(View.GONE);
			}

			if (mBuilder.mShowIndeterminateProgress) {
				setIndeterminateProgressVisibility(View.VISIBLE);
				mIndeterminateProgressText.setText(mBuilder.mProgressMessage);
			} else {
				setIndeterminateProgressVisibility(View.GONE);
			}

			if (mBuilder.mShowHorzProgress) {
				if (mBuilder.mIndeterminateHorizontalProgress != null) {
					setHorizontalProgressVisibility(View.VISIBLE);
					mHorizontalProgress.setIndeterminate(true);
					int resId = mBuilder.mThemeId == THEME_HOLO_LIGHT || mBuilder.mThemeId == THEME_ICS_LIGHT ? 
							R.drawable.progress_horizontal_holo_light : R.drawable.progress_indeterminate_horizontal_holo_dark;
					mHorizontalProgress.setIndeterminateDrawable(getContext().getResources().getDrawable(resId));  // Not sure why I need to do this
					mHorizontalProgressPercentText.setVisibility(View.GONE);
					mHorizontalProgressCountText.setVisibility(View.GONE);
					mHorizontalProgressMessageText.setText(mBuilder.mHorzProgressMessage);
				} else {
					setHorizontalProgressVisibility(View.VISIBLE);
					mHorizontalProgressPercentText.setVisibility(View.VISIBLE);
					mHorizontalProgressCountText.setVisibility(View.VISIBLE);
					setProgress(mBuilder.mHorzMaxProgress, mBuilder.mHorzMinProgress);
					updateProgress(mBuilder.mHorzMaxProgress, 
							mBuilder.mHorzMinProgress, mBuilder.mHorzProgressMessage);
				}				
			} else {
				setHorizontalProgressVisibility(View.GONE);
			}

			if (mBuilder.mWebViewUrl != null) {
				setWebViewVisibility(View.VISIBLE);
				loadUrl(mBuilder.mWebViewUrl, mBuilder.mOverrideLoadingOnWebView);
				if (mBuilder.mWebViewBackgroundColor != -1) {
					setWebViewBackgroundColor(mBuilder.mWebViewBackgroundColor);
				}
			} else {
				setWebViewVisibility(View.GONE);
			}

			if (mBuilder.mListItems != null) {
				mAdapter = new EasyDialogListAdapter(mBuilder);
				mGridView.setFastScrollEnabled(mBuilder.mSetFastScrollEnabled);
				mListView.setFastScrollEnabled(mBuilder.mSetFastScrollEnabled);
				if (mBuilder.mListStyle != LIST_STYLE_GRIDVIEW) {
					setGridViewVisibility(View.GONE);
					setListViewVisibility(View.VISIBLE);
					setListViewAdapter(mAdapter);
					setListViewItemClickListener(mDefaultListItemClickListener);
				} else {
					setListViewVisibility(View.GONE);
					setGridViewVisibility(View.VISIBLE);
					setGridViewAdapter(mAdapter);
					setGridViewItemClickListener(mDefaultListItemClickListener);
				}
			}
		}
	}

	private void setEditText() {
		if (mBuilder.mEditTextText != null || mBuilder.mEditTextHint != null) {
			mEditText.setVisibility(View.VISIBLE);
			mEditText.setText(mBuilder.mEditTextText);
			mEditText.setHint(mBuilder.mEditTextHint);
			if (mBuilder.mTextWatcher != null) {
				mEditText.addTextChangedListener(mBuilder.mTextWatcher);
			}
		} else {
			mEditText.setVisibility(View.GONE);
		}
	}

	/**
	 * Sets the initial state of the CheckBox
	 */
	private void setCheckBoxView() {
		if (mBuilder.mCheckBoxText != null) {
			setCheckBoxVisibility(View.VISIBLE);
			setCheckBox(mBuilder.mCheckBoxText, mBuilder.mCheckBoxIsChecked, 
					mBuilder.mOnCheckedChangeListener);
		} else {
			setCheckBoxVisibility(View.GONE);
		}
	}

	private void setCustomFonts() {
		if (mBuilder.mTitleFont != null) {
			setFonts(mBuilder.mTitleFont, mTitleText, mSubtitleText);
		}

		if (mBuilder.mMainFont != null) {
			setFonts(mBuilder.mMainFont, 
					mMessageText, 
					mIndeterminateProgressText,
					mHorizontalProgressPercentText,
					mHorizontalProgressMessageText,
					mHorizontalProgressCountText,
					mEditText,
					mCheckBox,
					mNegativeButton,
					mNeutralButton,
					mPositiveButton);
		}
	}

	private void setFonts(Typeface typeface, View...views) {
		for (View view : views) {
			if (view instanceof TextView) {
				((TextView) view).setTypeface(typeface);
			} else if (view instanceof Button) {
				((Button) view).setTypeface(typeface);
			} else if (view instanceof EditText) {
				((EditText) view).setTypeface(typeface);
			} else if (view instanceof CheckBox) {
				((CheckBox) view).setTypeface(typeface);
			}
		}
	}

	/**
	 * Sets the initial state of the buttons
	 */
	private void setButtonView() {
		setButtonEnabledState(BUTTON_NEGATIVE, mBuilder.mNegativeButtonEnabledState);
		setButtonEnabledState(BUTTON_NEUTRAL, mBuilder.mNeutralButtonEnabledState);
		setButtonEnabledState(BUTTON_POSITIVE, mBuilder.mPositiveButtonEnabledState);

		if (mBuilder.mNegativeButtonText != null) {
			setNegativeButton(mBuilder.mNegativeButtonText, mBuilder.mNegativeButtonClickListener);
		} else {
			setNegativeButtonVisibility(View.GONE);
		}

		if (mBuilder.mNeutralButtonText != null) {
			setNeutralButton(mBuilder.mNeutralButtonText, mBuilder.mNeutralButtonClickListener);
		} else {
			setNeutralButtonVisibility(View.GONE);
		}

		if (mBuilder.mPositiveButtonText != null) {
			setPositiveButton(mBuilder.mPositiveButtonText, mBuilder.mPositiveButtonClickListener);
		} else {
			setPositiveButtonVisibility(View.GONE);
		}
	}

	/**
	 * Sets the background image of the dialog.
	 * 
	 * @param background
	 */
	public void setBackground(Drawable background) {
		View rootView = findViewById(android.R.id.content).getRootView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackground(background);
		} else {
			rootView.setBackgroundDrawable(background);
		}
	}

	/**
	 * Sets the background image of the dialog.
	 * 
	 * @param resid
	 */
	public void setBackground(int resid) {
		findViewById(android.R.id.content).getRootView().setBackgroundResource(resid);
	}

	/**
	 * Removes all views in the title layout.
	 * @see #hideTitle()
	 */
	public void removeTitle() {
		mTitleLayout.removeAllViews();
	}

	/**
	 * Hides the title layout.
	 * @see #showTitle()
	 */
	public void hideTitle() {
		mTitleLayout.setVisibility(View.GONE);
	}

	/**
	 * Shows the title layout
	 * @see #hideTitle()
	 */
	public void showTitle() {
		mTitleLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * Sets a custom view for the dialog title
	 * 
	 * @param view
	 */
	public void setCustomTitle(View view) {
		mTitleLayout.removeAllViews();
		mTitleLayout.addView(view);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitleText.setText(title);
		if (title.length() >= 25) {
			mTitleText.setSelected(true);
		}
	}

	@Override
	public void setTitle(int stringId) {
		setTitle(getContext().getString(stringId));
	}

	/**
	 * Shows an indeterminate progress bar in the dialog title.
	 */
	public void showTitleProgress() {
		showTitleProgress(true);
	}

	/**
	 * Hides the indeterminate progress bar in the dialog title.
	 */
	public void hideTitleProgress() {
		showTitleProgress(false);
	}

	/**
	 * Shows an indeterminate progress bar in the dialog title.
	 * 
	 * @param show Set to <code>true</code> to show the progress bar.
	 */
	public void showTitleProgress(boolean show) {
		mTitleProgress.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	/**
	 * Sets the icon in the dialog's title.
	 * 
	 * @param icon
	 */
	public void setIcon(Drawable icon) {
		mTitleIcon.setImageDrawable(icon);
	}

	/**
	 * Sets the icon in the dialog's title.
	 * 
	 * @param resId The resource id of the drawable.
	 */
	public void setIcon(int resId) {
		mTitleIcon.setImageResource(resId);
	}

	/**
	 * Sets a subtitle to be displayed right under the dialog title.
	 * 
	 * @param subtitle
	 */
	public void setSubtitle(String subtitle) {
		mSubtitleText.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.title_icon);
		layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.title_progress);
		Resources resources = getContext().getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		layoutParams.leftMargin = (int) (6 * (metrics.densityDpi / 160f));
		mTitleText.setLayoutParams(layoutParams);
		mSubtitleText.setText(subtitle);
		if (subtitle.length() >= 45)
			mSubtitleText.setSelected(true);
	}

	/**
	 * Sets the subtitle to be displayed right under the dialog title.
	 * 
	 * @param stringId The resource id of the string.
	 */
	public void setSubtitle(int stringId) {
		setSubtitle(getContext().getString(stringId));
	}

	/**
	 * Removes the subtitle from the dialog's title.
	 */
	public void removeSubtitle() {
		mSubtitleText.setVisibility(View.GONE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.title_icon);
		layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.title_progress);
		Resources resources = getContext().getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		layoutParams.leftMargin = (int) (6 * (metrics.densityDpi / 160f));
		mTitleText.setLayoutParams(layoutParams);
	}

	/**
	 * Sets the view between the dialog's title and the dialog's Button and CheckBox layouts.
	 * 
	 * @param view
	 */
	public void setDialogView(View view) {
		mDialogLayout.removeAllViews();
		mDialogLayout.addView(view);
	}

	/**
	 * Sets the visibility of the dialog message.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility 
	 */
	public void setDialogMessageVisibility(int visibility) {
		mMessageLayout.setVisibility(visibility);
	}

	/**
	 * Sets the visibility of the indeterminate progress bar.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setIndeterminateProgressVisibility(int visibility) {
		mIndeterminateProgressLayout.setVisibility(visibility);
	}

	/**
	 * Sets the visibility of the horizontal progress bar layout.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setHorizontalProgressVisibility(int visibility) {
		mHorizontalProgressLayout.setVisibility(visibility);
	}

	/**
	 * Sets the progress for the horizontal progress bar.
	 * 
	 * @param max      The max progress
	 * @param progress The current progress
	 */
	public void setProgress(int max, int progress) {
		mHorizontalProgress.setMax(max);
		mHorizontalProgress.setProgress(progress);
	}

	/**
	 * Updates the message for the horizontal progress bar that gets
	 * displayed right under the progress bar.
	 * 
	 * @param message
	 */
	public void updateProgressMessage(String message) {
		mHorizontalProgressMessageText.setText(message);
		if (message.length() >= 35)
			mHorizontalProgressMessageText.setSelected(true);
	}

	/**
	 * Sets the horizontal progress bar's percent and count
	 * 
	 * @param max      The max value of the progress bar.
	 * @param progress The current progress of the progress bar.
	 */
	public void updateProgress(int max, int progress) {
		int percent = (int) Math.floor((((double)progress / max) * 100));
		mHorizontalProgressCountText.setText(progress + "/" + max);
		mHorizontalProgressPercentText.setText(percent + "%");
	}

	/**
	 * Sets the horizontal progress bar's percent, count and message.
	 * 
	 * @param max      The max value of the progress bar.
	 * @param progress The current progress of the progress bar.
	 * @param message  The message that gets displayed right under the progress bar.
	 */
	public void updateProgress(int max, int progress, String message) {
		updateProgress(max, progress);
		updateProgressMessage(message);
	}

	/**
	 * Sets the horizontal progress bar's percent, count and message.
	 * 
	 * @param progress The current progress of the progress bar.
	 * @param message  The message that gets displayed right under the progress bar.
	 */
	public void updateProgress(int progress, String message) {
		updateProgress(mBuilder.mHorzMaxProgress, progress);
		updateProgressMessage(message);
	}

	/**
	 * Updates the horizontal progress bar.
	 * @see #updateProgress(int, int)
	 */
	public void updateProgress(int progress) {
		updateProgress(mBuilder.mHorzMaxProgress, progress);
	}

	/**
	 * Increments the progress bar by 1.
	 * You can use this method from a thread.
	 * 
	 * @see #incrementProgressBy(int)
	 * @param handler The handler initialized on the UI thread
	 */
	public void incrementProgress(Handler handler) {
		handler.post(new Runnable( ) {
			@Override
			public void run() {
				incrementProgressBy(1);
			}
		});
	}

	/**
	 * Increments the progress bar by 1
	 * @see #incrementProgressBy(int)
	 */
	public void incrementProgress() {
		incrementProgressBy(1);
	}

	/**
	 * Increments and updates the current progress bar
	 * 
	 * @see #updateProgress(int)
	 * @param diff The value to increment
	 */
	public void incrementProgressBy(int diff) {
		mHorizontalProgress.incrementProgressBy(diff);
		updateProgress(mHorizontalProgress.getProgress());
	}

	/**
	 * Increments and updates the current progress bar by 1
	 * 
	 * @param message The message right under the progress bar.
	 */
	public void incrementProgress(String message) {
		incrementProgressBy(1, message);
	}

	/**
	 * Increments and updates the current progress bar
	 * 
	 * @param diff The value to increment
	 * @param message The message right under the progress bar.
	 */
	public void incrementProgressBy(int diff, String message) {
		mHorizontalProgress.incrementProgressBy(diff);
		updateProgress(mHorizontalProgress.getProgress());
		updateProgressMessage(message);
	}

	/**
	 * Set the visibility of the WebView
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setWebViewVisibility(int visibility) {
		mWebView.setVisibility(visibility);
	}

	/**
	 * overrideLoading defaults to <code>false</code>
	 * @see #loadUrl(String)
	 */
	public void loadUrl(String url) {
		loadUrl(url, false);
	}

	/**
	 * Loads the url in the dialog's WebView.
	 * 
	 * @param url             The URL or html to load
	 * @param overrideLoading Whether or not to load clicked URL's in the dialog.
	 */
	public void loadUrl(String url, boolean overrideLoading) {
		if (overrideLoading) {
			mWebView.setWebViewClient(new WebViewClient() {  
				/* On Android 1.1 shouldOverrideUrlLoading() will be called every time the user clicks a link, 
				 * but on Android 1.5 it will be called for every page load, even if it was caused by calling loadUrl()! */  
				@Override  
				public boolean shouldOverrideUrlLoading(WebView view, String url) {  
					if (!url.startsWith("http")) {  
						view.loadUrl(url);  
						return true;  
					}				  
					return false;  
				}  
			}); 
		}				
		if (url.startsWith("http") || url.endsWith("html")) {
			mWebView.loadUrl(url);
		} else {
			mWebView.loadData(url, "text/html", null);
		}
	}

	/**
	 * Sets the background color of the WebView
	 * @param color
	 */
	public void setWebViewBackgroundColor(int color) {
		mWebView.setBackgroundColor(color);
	}

	/**
	 * 
	 * @see {@link #LIST_STYLE_GRIDVIEW}
	 * @see {@link #LIST_STYLE_LISTVIEW}
	 * @see {@link #LIST_STYLE_SINGLE_CHOICE}
	 * @see {@link #LIST_STYLE_MULTI_CHOICE}
	 * 
	 * @return The current list style being used.
	 */
	public int getCurrentListStyle() {
		if (mAdapter != null) {
			return mAdapter.getListStyle();
		}

		return mBuilder.mListStyle;
	}

	/**
	 * 
	 * @return The ListAdapter used in the ListView or GridView.<br> 
	 *         It will return null if the GridView or ListView isn't being used.
	 */
	public ListAdapter getAdapter() {
		return getCurrentListStyle() == LIST_STYLE_GRIDVIEW ?
				mGridView.getAdapter() : mListView.getAdapter();
	}

	/**
	 * @return The {@link EasyDialogListAdapter} being used in the ListView or GridView or null.
	 */
	public EasyDialogListAdapter getDialogListAdapter() {
		if (mAdapter instanceof EasyDialogListAdapter) {
			return (EasyDialogListAdapter) mAdapter;
		}

		return null;
	}

	/**
	 * Adds list items to the adapter.
	 * @param listItems
	 */
	public void addListItems(List<ListItem> listItems) {
		if (mAdapter != null) {
			mAdapter.getListItems().addAll(listItems);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Adds list items to the adapter
	 * @see #addListItems(List)
	 * @param labels The labels for the list items
	 */
	public void addListItems(String[] labels) {
		if (mAdapter != null) {
			List<ListItem> items = new ArrayList<ListItem>();
			for (String label : labels) {
				items.add(new ListItem(label));
			}
			mAdapter.getListItems().addAll(items);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Set the ListItems for the ListAdapter.
	 * @see #setListItems(int, List)
	 * @param listItems
	 */
	public void setListItems(List<ListItem> listItems) {
		setListItems(mBuilder.mListStyle, listItems);
	}

	/**
	 * Set the ListItems for the ListAdapter.
	 * @param listStyle The style of the list
	 * @param listItems The list items
	 */
	public void setListItems(int listStyle, List<ListItem> listItems) {
		if (mAdapter == null) {
			mAdapter = new EasyDialogListAdapter(getContext(), listItems, listStyle);
			if (listStyle != LIST_STYLE_GRIDVIEW) {
				mListView.setAdapter(mAdapter);
			} else {
				mGridView.setAdapter(mAdapter);
			}
		} else {
			mAdapter.setListStyle(listStyle);
			mAdapter.setListItems(listItems);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Sets the {@link OnItemClickListener} for the ListView.
	 * @param listener
	 */
	public void setListViewItemClickListener(OnItemClickListener listener) {
		mListView.setOnItemClickListener(listener);
	}

	/**
	 * Set the visibility of the ListView.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setListViewVisibility(int visibility) {
		mListView.setVisibility(visibility);
	}

	/**
	 * Sets the ListAdapter for the ListView.
	 * @param adapter
	 */
	public void setListViewAdapter(ListAdapter adapter) {
		mListView.setAdapter(adapter);
	}

	/**
	 * Sets the {@link OnItemClickListener} for the GridView.
	 * @param listener
	 */
	public void setGridViewItemClickListener(OnItemClickListener listener) {
		mGridView.setOnItemClickListener(listener);
	}

	/**
	 * Set the visibility of the GridView.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setGridViewVisibility(int visibility) {
		mGridView.setVisibility(visibility);
	}

	/**
	 * Sets the ListAdapter for the GridView.
	 * @param adapter
	 */
	public void setGridViewAdapter(ListAdapter adapter) {
		mGridView.setAdapter(adapter);
	}

	/**
	 * @see {@link EasyDialogListAdapter#getListItems()}
	 * @return The list items in the {@link #mAdapter} or <code>null</code> if none have been set.
	 */
	public List<ListItem> getListItems() {
		if (mAdapter == null) return null;
		return mAdapter.getListItems();
	}

	/**
	 * @param position
	 * @return The {@link ListItem} from the {@link EasyDialogListAdapter} or <code>null</code> if the item doesn't exist.
	 */
	public ListItem getListItem(int position) {
		if (mAdapter != null) {
			return mAdapter.getItem(position);
		}
		return null;
	}

	/**
	 * 
	 * @return A List of all the items that are checked in the adapter.
	 */
	public List<ListItem> getCheckedItems() {
		List<ListItem> items = new ArrayList<ListItem>();
		if (mAdapter != null) {
			for (ListItem item : mAdapter.getListItems()) {
				if (item.checked) {
					items.add(item);
				}
			}
		}
		return items;
	}

	/** Checks/Unchecks all the items in the list */
	public void checkAll(boolean check) {
		if (mAdapter == null) return;
		for (ListItem item : mAdapter.getListItems()) {
			item.checked = check;
		}
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * The default OnItemClickListener for the ListView or GridView. 
	 * This handles checking CheckBoxes and RadioButtons and sending callbacks to the {@link DialogInterface} listeners.
	 */
	private OnItemClickListener mDefaultListItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (mBuilder.mListStyle == LIST_STYLE_SINGLE_CHOICE) {
				// uncheck all radio buttons except the list item that was clicked.
				for (ListItem listItem : getListItems()) {
					listItem.checked = false;
				}

				ListItem listItem = mAdapter.getItem(position);
				listItem.checked = true;
				mAdapter.notifyDataSetChanged();

				if (mBuilder.mOnItemClickListener != null) {
					mBuilder.mOnItemClickListener.onClick(EasyDialog.this, position);
				}
			} else {
				// Toggle the CheckBox if it is visible.
				ListItem listItem = mAdapter.getItem(position);
				if (listItem.checked != null) {
					listItem.checked = !listItem.checked;
					mAdapter.notifyDataSetChanged();
				}

				if (mBuilder.mListStyle == LIST_STYLE_MULTI_CHOICE) {
					if (mBuilder.mOnMultiChoiceClickListener != null) {
						mBuilder.mOnMultiChoiceClickListener.onClick(
								EasyDialog.this, position, listItem.checked);
					}
				} else if (mBuilder.mOnItemClickListener != null) {
					mBuilder.mOnItemClickListener.onClick(EasyDialog.this, position);
				}
			}
		}

	};

	/**
	 * Set the visibility of the CheckBox.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setCheckBoxVisibility(int visibility) {
		mCheckBox.setVisibility(visibility);
	}

	/**
	 * Sets the dialog's CheckBox that gets display right above the dialog buttons.
	 * @param text     The text of the CheckBox
	 * @param checked  Whether to check the CheckBox
	 * @param listener The {@link CompoundButton.OnCheckedChangeListener}
	 */
	public void setCheckBox(String text, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
		mCheckBox.setText(text);
		mCheckBox.setChecked(checked);
		if (listener != null) {
			mCheckBox.setOnCheckedChangeListener(listener);
		}
		setCheckBoxVisibility(View.VISIBLE);
	}

	/**
	 * Sets the dividers depending on how many buttons are visible.
	 */
	private void setButtonDividers() {
		int numButtons = 0;
		Button[] buttons = {
				mNegativeButton, mNeutralButton, mPositiveButton	
		};

		for (Button button : buttons) {
			if (button.getVisibility() == View.VISIBLE) {
				numButtons++;
			}
		}

		if (numButtons > 0 && mDialogButtonLayout.getVisibility() != View.VISIBLE) {
			mDialogButtonLayout.setVisibility(View.VISIBLE);
		}

		if (mHideButtonDividers) {
			mDialogButtonDivider.setVisibility(View.GONE);
			mNegativeButtonDivider.setVisibility(View.GONE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			return;
		}

		switch (numButtons) {
		case 0:
			mDialogButtonDivider.setVisibility(View.GONE);
			mNegativeButtonDivider.setVisibility(View.GONE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			break;
		case 1:
			mDialogButtonDivider.setVisibility(View.VISIBLE);
			mNegativeButtonDivider.setVisibility(View.GONE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			break;
		case 2:
			mDialogButtonDivider.setVisibility(View.VISIBLE);
			mNegativeButtonDivider.setVisibility(View.VISIBLE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			break;
		case 3:
			mDialogButtonDivider.setVisibility(View.VISIBLE);
			mNegativeButtonDivider.setVisibility(View.VISIBLE);
			mPositiveButtonDivider.setVisibility(View.VISIBLE);
			break;
		}
	}

	/**
	 * Hides the dividers in-between buttons
	 */
	public void hideButtonDividers() {
		mHideButtonDividers = false;
		setButtonDividers();
	}

	/**
	 * Shows the dividers in-between buttons.
	 */
	public void showButtonDividers() {
		mHideButtonDividers = true;
		setButtonDividers();
	}

	public void setButtonEnabledState(int whichButton, boolean enabled) {
		if (whichButton == BUTTON_NEGATIVE) {
			mNegativeButton.setEnabled(enabled);
		} else if (whichButton == BUTTON_NEUTRAL) {
			mNeutralButton.setEnabled(enabled);
		} else if (whichButton == BUTTON_POSITIVE) {
			mPositiveButton.setEnabled(enabled);
		}
	}

	/**
	 * Set the visibility of the negative button.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setNegativeButtonVisibility(int visibility) {
		mNegativeButton.setVisibility(visibility);
		setButtonDividers();
	}

	/**
	 * Sets the dialog's negative button.
	 * @param text     The text for the button. The button is limited to a single line.
	 * @param listener The {@link DialogInterface.OnCLickListener} to be called when the button is clicked.
	 */
	public void setNegativeButton(String text, final DialogInterface.OnClickListener listener) {
		setNegativeButtonVisibility(View.VISIBLE);
		mNegativeButton.setText(text);
		mNegativeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClick(EasyDialog.this, BUTTON_NEGATIVE);
				}
			}
		});
	}

	/**
	 * Set the visibility of the neutral button.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setNeutralButtonVisibility(int visibility) {
		mNeutralButton.setVisibility(visibility);
		setButtonDividers();
	}

	/**
	 * Sets the dialog's neutral button.
	 * @param text     The text for the button. The button is limited to a single line.
	 * @param listener The {@link DialogInterface.OnCLickListener} to be called when the button is clicked.
	 */
	public void setNeutralButton(String text, final DialogInterface.OnClickListener listener) {
		setNeutralButtonVisibility(View.VISIBLE);
		mNeutralButton.setText(text);
		mNeutralButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClick(EasyDialog.this, BUTTON_NEUTRAL);
				}
			}
		});
	}

	/**
	 * Set the visibility of the positive button.
	 * 
	 * @see {@link View#VISIBLE}
	 * @see {@link View#GONE}
	 * @see {@link View#INVISIBLE}
	 * @param visibility
	 */
	public void setPositiveButtonVisibility(int visibility) {
		mPositiveButton.setVisibility(visibility);
		setButtonDividers();
	}

	/**
	 * Sets the dialog's positive button.
	 * @param text     The text for the button. The button is limited to a single line.
	 * @param listener The {@link DialogInterface.OnCLickListener} to be called when the button is clicked.
	 */
	public void setPositiveButton(String text, final DialogInterface.OnClickListener listener) {
		setPositiveButtonVisibility(View.VISIBLE);
		mPositiveButton.setText(text);
		mPositiveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClick(EasyDialog.this, BUTTON_POSITIVE);
				}
			}
		});
	}

	/** ~-~-~-~-~-~-~-~-~-~-~-~-~ *
	 *  ~-       Getters       -~ *
	 ** ~-~-~-~-~-~-~-~-~-~-~-~-~ */

	/** @return The Builder this was initialized with */
	public Builder getBuilder() {
		return mBuilder;
	}

	/** @return The title layout */
	public RelativeLayout getTitleLayout() {
		return mTitleLayout;
	}

	/** @return The TextView for the dialog's title */
	public TextView getTitleText() {
		return mTitleText;
	}

	/** @return The TextView for the dialog's subtitle */
	public TextView getSubtitleText() {
		return mSubtitleText;
	}

	/** @return The ImageView of the dialog's title icon */
	public ImageView getTitleIcon() {
		return mTitleIcon;
	}

	/** @return The ProgressBar in the dialog's title */
	public ProgressBar getTitleProgress() {
		return mTitleProgress;
	}

	/** @return The CheckBox in the dialog's title */
	public CheckBox getTitleCheckBox() {
		return mTitleCheckBox;
	}

	/** @return The dialog's title divider */
	public View getTitleDivider() {
		return mTitleDivider;
	}

	/** @return The RelativeLayout between the title and CheckBox/Buttons */
	public RelativeLayout getDialogLayout() {
		return mDialogLayout;
	}

	/** @return The ScrollView that contains the dialog's message */
	public ScrollView getMessageLayout() {
		return mMessageLayout;
	}

	/** @return The TextVew for the dialog's main message */
	public TextView getMessageText() {
		return mMessageText;
	}

	/** @return The layout that contains the indeterminate progress bar and progress message */
	public LinearLayout getIndeterminateProgressLayout() {
		return mIndeterminateProgressLayout;
	}

	/** @return The indeterminate progress bar */
	public ProgressBar getIndeterminateProgress() {
		return mIndeterminateProgress;
	}

	/** @return The indeterminate progress bar message */
	public TextView getIndeterminateProgressText() {
		return mIndeterminateProgressText;
	}

	/** @return The layout containing the horizontal progress bar */
	public RelativeLayout getHorizontalProgressLayout() {
		return mHorizontalProgressLayout;
	}

	/** @return The horizontal ProgressBar */
	public ProgressBar getHorizontalProgress() {
		return mHorizontalProgress;
	}

	/** @return The TextView for the percent of the horizontal progress bar */
	public TextView getHorizontalProgressPercentText() {
		return mHorizontalProgressPercentText;
	}

	/** @return The TextView for the horizontal progress bar message */
	public TextView getHorizontalProgressMessageText() {
		return mHorizontalProgressMessageText;
	}

	/** @return The TextView for the horizontal progress bar current count */
	public TextView getHorizontalProgressCountText() {
		return mHorizontalProgressCountText;
	}

	/** @return The dialog's WebView */
	public WebView getWebView() {
		return mWebView;
	}

	/** @return The dialog's ListView */
	public ListView getListView() {
		return mListView;
	}

	/** @return The dialog's GridView */
	public GridView getGridView() {
		return mGridView;
	}

	/** @return The EditText view */
	public EditText getEditText() {
		return mEditText;
	}

	/** @return The dialog's CheckBox */
	public CheckBox getCheckBox() {
		return mCheckBox;
	}

	/** @return The View for the divider between the dialog content and dialog buttons */
	public View getDialogButtonDivider() {
		return mDialogButtonDivider;
	}

	/** @return The layout that contains the dialog buttons */
	public LinearLayout getDialogButtonLayout() {
		return mDialogButtonLayout;
	}

	/** @return The dialog's negative button */
	public Button getNegativeButton() {
		return mNegativeButton;
	}

	/** @return The dialog's neutral button */
	public Button getNeutralButton() {
		return mNeutralButton;
	}

	/** @return The dialog's positive button */
	public Button getPositiveButton() {
		return mPositiveButton;
	}

	/** @return The dialog's negative button divider */
	public View getNegativeButtonDivider() {
		return mNegativeButtonDivider;
	}

	/** @return The dialog's positive button divider */
	public View getPositiveButtonDivider() {
		return mPositiveButtonDivider;
	}

	/**
	 * The builder class for the dialog
	 */
	public static class Builder {

		protected Context mContext;
		protected int mThemeId;

		protected Drawable mDialogBackground;
		protected int mDialogBackgroundResId = -1;

		protected boolean mCancelable = true;
		protected boolean mCancelableOutsideTouch = false;

		protected View mCustomTitleView;
		protected View mMainDialogView;

		protected Typeface mTitleFont;
		protected Typeface mMainFont;

		protected Drawable mTitleIcon;
		protected String mTitleText;
		protected String mSubtitleText;
		protected boolean mShowTitlebarProgress;
		protected Boolean mTitleCheckbox;
		protected CompoundButton.OnCheckedChangeListener mTitleCheckBoxListener;

		protected String mDialogMessage;
		protected boolean mShowIndeterminateProgress;
		protected String mProgressMessage;

		protected boolean mShowHorzProgress;
		protected int mHorzMaxProgress;
		protected int mHorzMinProgress;
		protected String mHorzProgressMessage;
		protected Boolean mIndeterminateHorizontalProgress;

		protected int mWebViewBackgroundColor;
		protected String mWebViewUrl;
		protected boolean mOverrideLoadingOnWebView;

		protected String mEditTextText;
		protected String mEditTextHint;
		protected TextWatcher mTextWatcher;

		protected String mCheckBoxText;
		protected boolean mCheckBoxIsChecked;
		protected CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

		protected boolean mSetFastScrollEnabled;
		protected int mListItemLayout;
		protected int mListItemBackground;
		protected int mListItemTextColor;
		protected int mListItemCheckBoxDrawable;
		protected int mListItemRadioButtonDrawable;
		protected int mListStyle;
		protected List<ListItem> mListItems;

		protected DialogInterface.OnClickListener mOnItemClickListener;
		protected DialogInterface.OnMultiChoiceClickListener mOnMultiChoiceClickListener;

		protected String mNegativeButtonText;
		protected String mNeutralButtonText;
		protected String mPositiveButtonText;

		protected boolean mNegativeButtonEnabledState = true;
		protected boolean mNeutralButtonEnabledState = true;
		protected boolean mPositiveButtonEnabledState = true;

		protected DialogInterface.OnClickListener mNegativeButtonClickListener;
		protected DialogInterface.OnClickListener mNeutralButtonClickListener;
		protected DialogInterface.OnClickListener mPositiveButtonClickListener;

		protected DialogInterface.OnCancelListener mOnCancelListener;
		protected DialogInterface.OnKeyListener mOnKeyListener;
		protected DialogInterface.OnShowListener mOnShowListener;
		protected DialogInterface.OnDismissListener mOnDismissListener;

		public Builder(Context context) {
			this(context, 0);
		}

		public Builder(Context context, int themeId) {
			TypedArray a = context.obtainStyledAttributes(null, R.styleable.EasyDialog, R.attr.dialogStyle, 0);
			if (themeId == 0) {
				themeId = a.getResourceId(R.styleable.EasyDialog_dialogStyle, THEME_HOLO);
			}
			mListItemLayout = a.getResourceId(
					R.styleable.EasyDialog_dialogListItemLayout,
					R.layout.dialog_list_item);
			mListItemBackground = a.getResourceId(
					R.styleable.EasyDialog_dialogListItemBackground,
					R.drawable.gv_border_black);
			mListItemTextColor = a.getColor(
					R.styleable.EasyDialog_dialogListItemTextColor,
					0xFFFFFFFF);
			mListItemCheckBoxDrawable = a.getResourceId(
					R.styleable.EasyDialog_dialogListItemCheckBoxDrawable,
					R.drawable.btn_check_holo_dark);
			mListItemRadioButtonDrawable = a.getResourceId(
					R.styleable.EasyDialog_dialogListItemRadioButtonDrawable,
					R.drawable.btn_radio_holo_dark);
			a.recycle();

			mContext = context;
			mThemeId = themeId;
		}

		/**
		 * Sets the dialog's background image
		 * 
		 * @see EasyDialog#setBackground(Drawable)
		 * @param background
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setBackground(Drawable background) {
			this.mDialogBackground = background;
			return this;
		}

		/**
		 * Sets the dialog's background resource
		 * 
		 * @see EasyDialog#setBackground(int)
		 * @param background
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setBackground(int resid) {
			this.mDialogBackgroundResId = resid;
			return this;
		}

		/**
		 * Sets whether this dialog is cancelable with the BACK key.
		 * 
		 * @see Dialog#setCancelable(boolean)
		 * @param cancelable
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setCancelable(boolean cancelable) {
			this.mCancelable = cancelable;
			return this;
		}

		/**
		 * Sets whether this dialog is canceled when touched outside the window's bounds. 
		 * If setting to true, the dialog is set to be cancelable if not already set.
		 * 
		 * @see Dialog#setCanceledOnTouchOutside(boolean)
		 * @param cancelableOutsideTouch
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setCanceledOnTouchOutside(boolean cancelableOutsideTouch) {
			this.mCancelableOutsideTouch = cancelableOutsideTouch;
			return this;
		}

		/**
		 * Set the title using the custom view customTitleView. The methods 
		 * {@link #setTitle(int)} and {@link #setIcon(Drawable)} should be 
		 * sufficient for most titles, but this is provided if the title needs 
		 * more customization. Using this will replace the title and icon set 
		 * via the other methods.
		 * 
		 * @param view The custom view to use as the title.
		 * @return This Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setCustomTitle(View view) {
			this.mCustomTitleView = view;
			return this;
		}

		/**
		 * Set a custom view to be the contents of the Dialog.
		 * The dialog buttons and dialog title will still be used.
		 * 
		 * @param view The view to use as the contents of the dialog
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setView(View view) {
			this.mMainDialogView = view;
			return this;
		}

		/**
		 * Set the {@linkplain Drawable} to be used in the title
		 * 
		 * @param icon
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setIcon(Drawable icon) {
			this.mTitleIcon = icon;
			return this;
		}

		/**
		 * Set the resource id of the {@linkplain Drawable} to be used in the title.
		 * 
		 * @param drawableId
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setIcon(int drawableId) {
			this.mTitleIcon = mContext.getResources().getDrawable(drawableId);
			return this;
		}

		/**
		 * Set the title displayed in the dialog.
		 * 
		 * @param title
		 * @return Builder object to allow for chaining of calls to set methods 
		 */
		public Builder setTitle(String title) {
			this.mTitleText = title;
			return this;
		}

		/**
		 * Set the resource id of the title displayed in the dialog.
		 * 
		 * @param stringId
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setTitle(int stringId) {
			this.mTitleText = mContext.getResources().getString(stringId);
			return this;
		}

		/**
		 * Shows a {@link CheckBox} in the title bar.
		 * @param checked  Whether the CheckBox should be checked.
		 * @param listener The {@link CompoundButton.OnCheckedChangeListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setTitleCheckBox(boolean checked, CompoundButton.OnCheckedChangeListener listener) {
			this.mTitleCheckbox = checked;
			this.mTitleCheckBoxListener = listener;
			return this;
		}

		/**
		 * Set the message to display.
		 * 
		 * @param message
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMessage(String message) {
			this.mDialogMessage = message;
			return this;
		}

		/**
		 * Set the resource id of the message to display.
		 * 
		 * @param message
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMessage(int stringId) {
			this.mDialogMessage = mContext.getString(stringId);
			return this;
		}

		/**
		 * Set a listener to be invoked when the negative button of the dialog is pressed.
		 * 
		 * @param text The text to display on the negative button
		 * @param listener The {@link DialogInterface.OnClickListener} to use
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setNegativeButton(String text, DialogInterface.OnClickListener listener) {
			this.mNegativeButtonText = text;
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the negative button of the dialog is pressed.
		 * 
		 * @param stringId The resource id of the text to display on the negative button
		 * @param listener The {@link DialogInterface.OnClickListener} to use
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setNegativeButton(int stringId, DialogInterface.OnClickListener listener) {
			this.mNegativeButtonText = mContext.getString(stringId);
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the neutral button of the dialog is pressed.
		 * 
		 * @param text The text to display on the neutral button
		 * @param listener The {@link DialogInterface.OnClickListener} to use
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setNeutralButton(String text, DialogInterface.OnClickListener listener) {
			this.mNeutralButtonText = text;
			this.mNeutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the neutral button of the dialog is pressed.
		 * 
		 * @param stringId The resource id of the text to display on the neutral button
		 * @param listener The {@link DialogInterface.OnClickListener} to use
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setNeutralButton(int stringId, DialogInterface.OnClickListener listener) {
			this.mNeutralButtonText = mContext.getString(stringId);
			this.mNeutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the positive button of the dialog is pressed.
		 * 
		 * @param text The text to display on the positive button
		 * @param listener The {@link DialogInterface.OnClickListener} to use
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setPositiveButton(String text, DialogInterface.OnClickListener listener) {
			this.mPositiveButtonText = text;
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set a listener to be invoked when the positive button of the dialog is pressed.
		 * 
		 * @param stringId The resource id of the text to display on the positive button
		 * @param listener The {@link DialogInterface.OnClickListener} to use
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setPositiveButton(int stringId, DialogInterface.OnClickListener listener) {
			this.mPositiveButtonText = mContext.getString(stringId);
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Sets the enabled state of the negative Button.
		 * 
		 * @param state
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setNegativeButtonEnabledState(boolean state) {
			this.mNegativeButtonEnabledState = state;
			return this;
		}

		/**
		 * Sets the enabled state of the neutral Button.
		 * 
		 * @param state
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setNeutralButtonEnabledState(boolean state) {
			this.mNeutralButtonEnabledState = state;
			return this;
		}

		/**
		 * Sets the enabled state of the positive Button.
		 * 
		 * @param state
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setPositiveButtonEnabledState(boolean state) {
			this.mPositiveButtonEnabledState = state;
			return this;
		}

		/**
		 * Sets the callback that will be called if the dialog is canceled.
		 * 
		 * @see #setCancelable(boolean)
		 * @see #setCanceledOnTouchOutside(boolean)
		 * @see Dialog#setOnCancelListener(android.content.DialogInterface.OnCancelListener)
		 * @param listener The {@link DialogInterface.OnCancelListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setOnCancelListener(DialogInterface.OnCancelListener listener) {
			this.mOnCancelListener = listener;
			return this;
		}

		/**
		 * Sets the {@link DialogInterface.OnDismissListener} for the dialog.
		 * 
		 * @param listener 
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setOnDismissListener(DialogInterface.OnDismissListener listener) {
			this.mOnDismissListener = listener;
			return this;
		}

		/**
		 * Sets the callback that will be called if a key is dispatched to the dialog. 
		 * 
		 * @see Dialog#setOnKeyListener(android.content.DialogInterface.OnKeyListener)
		 * @param listener The {@link Dialog.OnKeyListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setOnKeyListener(DialogInterface.OnKeyListener listener) {
			this.mOnKeyListener = listener;
			return this;
		}

		/**
		 * Sets a listener to be invoked when the dialog is shown.
		 * 
		 * @see Dialog#setOnShowListener(android.content.DialogInterface.OnShowListener)
		 * @param listener The {@link DialogInterface.OnShowListener} to be used.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setOnShowListener(DialogInterface.OnShowListener listener) {
			this.mOnShowListener = listener;
			return this;
		}

		/**
		 * Sets the visibility of the indeterminate progress bar in the dialog's title bar
		 * 
		 * @param visible Set to <code>true</code> to show the progress bar. False by default.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setTitleBarProgress(boolean visible) {
			this.mShowTitlebarProgress = visible;
			return this;
		}

		/**
		 * Sets the subtitle that gets displayed right under the dialog title.
		 * 
		 * @param subtitle
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setSubtitle(String subtitle) {
			this.mSubtitleText = subtitle;
			return this;
		}

		/**
		 * Sets the resource id for the text that gets displayed right under the dialog title.
		 * 
		 * @param stringId
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setSubtitle(int stringId) {
			this.mSubtitleText = mContext.getString(stringId);
			return this;
		}

		/**
		 * Shows the indeterminate progress bar in the dialog's view.
		 * 
		 * @see #setIndeterminateProgress(String)
		 * @see #setIndeterminateProgress(int)
		 * @param visible Set to <code>true</code> to show the indeterminate progress bar.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setIndeterminateProgressVisibility(boolean visible) {
			this.mShowIndeterminateProgress = visible;
			return this;
		}

		/**
		 * Sets the message that gets displayed to the right of the indeterminate progress bar.
		 * This method also calls {@link #setIndeterminateProgressVisibility(boolean)} to show the progress bar.
		 * 
		 * @param message The message to display
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setIndeterminateProgress(String message) {
			setIndeterminateProgressVisibility(true);
			this.mProgressMessage = message;
			return this;
		}

		/**
		 * Sets the message that gets displayed to the right of the indeterminate progress bar.
		 * This method also calls {@link #setIndeterminateProgressVisibility(boolean)} to show the progress bar.
		 * 
		 * @param stringId The resource id of the message to display
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setIndeterminateProgress(int stringId) {
			setIndeterminateProgressVisibility(true);
			this.mProgressMessage = mContext.getString(stringId);
			return this;
		}

		public Builder setIndeterminateHorizontalProgress(String message) {
			this.mShowHorzProgress = true;
			this.mIndeterminateHorizontalProgress = true;
			this.mHorzProgressMessage = message;
			return this;
		}

		/**
		 * Sets the horizontal progress bar.
		 * 
		 * @param max      The max progress (see: {@link android.widget.ProgressBar#setMax(int)})
		 * @param progress The current progress (see: {@link android.widget.ProgressBar#setProgress(int)}
		 * @param message  The message that gets displayed right under the progress bar.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setHorizontalProgress(int max, int progress, String message) {
			this.mShowHorzProgress = true;
			this.mHorzMaxProgress = max;
			this.mHorzMinProgress = progress;
			this.mHorzProgressMessage = message;
			return this;
		}

		/**
		 * Sets the horizontal progress bar.
		 * 
		 * @param max      The max progress (see: {@link android.widget.ProgressBar#setMax(int)})
		 * @param progress The current progress (see: {@link android.widget.ProgressBar#setProgress(int)}
		 * @param stringId The resource id of the message that gets displayed right under the progress bar.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setHorizontalProgress(int max, int progress, int stringId) {
			this.mShowHorzProgress = true;
			this.mHorzMaxProgress = max;
			this.mHorzMinProgress = progress;
			this.mHorzProgressMessage = mContext.getString(stringId);
			return this;
		}

		/**
		 * Sets the url to load in the WebView. 
		 * If the url does not start with "http" then the data will be loaded using
		 * {@link android.webkit.WebView#loadData(String, String, String)}
		 * 
		 * @param url 
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setWebViewUrl(String url) {

			this.mWebViewUrl = url;
			return this;
		}

		/**
		 * Sets the url to load in the WebView. 
		 * If the url does not start with "http" then the data will be loaded using
		 * {@link android.webkit.WebView#loadData(String, String, String)}
		 * 
		 * @param url
		 * @param overrideLoading Set to <code>true</code> to load any URL in the dialog instead of using another app.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setWebViewUrl(String url, boolean overrideLoading) {
			this.mWebViewUrl = url;
			this.mOverrideLoadingOnWebView = overrideLoading;
			return this;
		}

		/**
		 * Sets the background color for the {@link WebView}.
		 * 
		 * @param backgroundColor
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setWebViewBackgroundColor(int backgroundColor) {
			this.mWebViewBackgroundColor = backgroundColor;
			return this;
		}

		/**
		 * Enables fast scrolling on the ListView and GridView.
		 * 
		 * @param fastScroll
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setFastScrollEnabled(boolean fastScroll) {
			this.mSetFastScrollEnabled = fastScroll;
			return this;
		}

		/**
		 * Sets the text color of the items in the {@link EasyDialogListAdapter}
		 *
		 * @param color
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setListItemTextColor(int color) {
			this.mListItemTextColor = color;
			return this;
		}

		/**
		 * Set the items to list in the GridView.
		 * 
		 * @param items    A list of {@link ListItem}'s
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setGridViewItems(List<ListItem> items, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_GRIDVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = items;
			return this;
		}

		/**
		 * Set the items to list in the GridView.
		 * 
		 * @param items    An array for the label of each item to display
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setGridViewItems(String[] items, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_GRIDVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			for (String label : items) {
				this.mListItems.add(new ListItem(label));
			}
			return this;
		}

		/**
		 * Set the items to list in the GridView.
		 * 
		 * @param arrayId  The resource id of the array to use. Should be R.array.resource_name
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setGridViewItems(int arrayId, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_GRIDVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (String label : items) {
				this.mListItems.add(new ListItem(label));
			}
			return this;
		}

		/**
		 * Set the items to list in the ListView.
		 * 
		 * @param items    A list of {@link ListItem}'s
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setItems(List<ListItem> items, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = items;
			return this;
		}

		/**
		 * Set the items to list in the ListView.
		 * 
		 * @param items    An array for the label of each item to display
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setItems(String[] items, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			for (String label : items) {
				this.mListItems.add(new ListItem(label));
			}
			return this;
		}

		/**
		 * Set the items to list in the ListView.
		 * 
		 * @param icons    An array of icons for each item. Must be the same length as items.
		 * @param items    An array for the label of each item to display. Must be the same length as icons
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setItems(Drawable[] icons, String[] items, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			for (int i = 0; i < icons.length; i++) {
				this.mListItems.add(new ListItem(icons[i], items[i]));
			}
			return this;
		}

		/**
		 * Set the items to list in the ListView.
		 * 
		 * @param icons    An array of icons for each item. Must be the same length as items.
		 * @param arrayId  The resource id for the array for the label of each item to display. Must be the same length as icons
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setItems(Drawable[] icons, int arrayId, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (int i = 0; i < icons.length; i++) {
				this.mListItems.add(new ListItem(icons[i], items[i]));
			}
			return this;
		}

		/**
		 * Set the items to list in the GridView.
		 * 
		 * @param arrayId  The resource id for the array for the label of each item to display.
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setItems(int arrayId, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (String label : items) {
				this.mListItems.add(new ListItem(label));
			}
			return this;
		}

		/**
		 * Displays items in a ListView with a CheckBox for each item.
		 * 
		 * @param items    A list of {@link ListItem}'s
		 * @param listener The {@link DialogInterface.OnMultiChoiceListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMultiChoiceItems(List<ListItem> items, DialogInterface.OnMultiChoiceClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_MULTI_CHOICE;
			this.mOnMultiChoiceClickListener = listener;
			this.mListItems = items;
			return this;
		}

		/**
		 * Displays items in a ListView with a CheckBox for each item.
		 * 
		 * @param items        An array for the label of each item to display. Must be the same length as checkedItems
		 * @param checkedItems An array that determines if each item is checked. Must be the same length as items.
		 * @param listener     The {@link DialogInterface.OnMultiChoiceListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMultiChoiceItems(String[] items, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_MULTI_CHOICE;
			this.mOnMultiChoiceClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			for (int i = 0; i < checkedItems.length; i++) {
				this.mListItems.add(new ListItem(items[i], checkedItems[i]));
			}
			return this;
		}

		/**
		 * Displays items in a ListView with a CheckBox for each item.
		 * 
		 * @param arrayId      The resource id for the array for the label of each item to display. Must be the same length as checkedItems
		 * @param checkedItems An array that determines if each item is checked. Must be the same length as items.
		 * @param listener     The {@link DialogInterface.OnMultiChoiceListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMultiChoiceItems(int arrayId, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_MULTI_CHOICE;
			this.mOnMultiChoiceClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (int i = 0; i < checkedItems.length; i++) {
				this.mListItems.add(new ListItem(items[i], checkedItems[i]));
			}
			return this;
		}

		/**
		 * Displays items in a ListView with a RadioButton for each item.
		 * 
		 * @param items    A list of {@link ListItem}'s
		 * @param listener The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setSingleChoiceItems(List<ListItem> items, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_SINGLE_CHOICE;
			this.mListItems = items;
			this.mOnItemClickListener = listener;
			return this;
		}

		/**
		 * Displays items in a ListView with a RadioButton for each item.
		 * 
		 * @param items       An array of labels for the list item.
		 * @param checkedItem The position of the item to be checked.
		 * @param listener    The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setSingleChoiceItems(String[] items, int checkedItem, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_SINGLE_CHOICE;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			for (int i = 0; i < items.length; i++) {
				this.mListItems.add(new ListItem(items[i], (i == checkedItem)));
			}
			return this;
		}

		/**
		 * Displays items in a ListView with a RadioButton for each item.
		 * 
		 * @param arrayId     The resource id of the array of labels for the list item from R.array
		 * @param checkedItem The position of the item to be checked.
		 * @param listener    The {@link DialogInterface.OnClickListener} to be used
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setSingleChoiceItems(int arrayId, int checkedItem, DialogInterface.OnClickListener listener) {
			this.mListStyle = EasyDialog.LIST_STYLE_SINGLE_CHOICE;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<ListItem>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (int i = 0; i < items.length; i++) {
				this.mListItems.add(new ListItem(items[i], (i == checkedItem)));
			}
			return this;
		}

		/**
		 * Sets an EditText view in the dialog.
		 * 
		 * @see #setEditText(String, String, TextWatcher)
		 * @param text        The text for the EditText
		 * @param textWatcher The {@link TextWatcher} to listen for when text is changed.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setEditText(String text, TextWatcher textWatcher) {
			this.mEditTextText = text;
			this.mTextWatcher = textWatcher;
			return this;
		}

		/**
		 * Sets an EditText view in the dialog.
		 * 
		 * @param text        The text for the EditText
		 * @param hint        The hint that gets displayed when the EditText box is empty.
		 * @param textWatcher The {@link TextWatcher} to listen for when text is changed.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setEditText(String text, String hint, TextWatcher textWatcher) {
			this.mEditTextText = text;
			this.mEditTextHint = hint;
			this.mTextWatcher = textWatcher;
			return this;
		}

		/**
		 * Sets a CheckBox right above the dialog buttons.
		 * 
		 * @param text     The text of the CheckBox
		 * @param checked  <code>true</code> to check the CheckBox
		 * @param listener The {@link CompoundButton.OnCheckedChangeListener} to be used.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setCheckBox(String text, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
			this.mCheckBoxText = text;
			this.mCheckBoxIsChecked = checked;
			this.mOnCheckedChangeListener = listener;
			return this;
		}

		/**
		 * Sets a CheckBox right above the dialog buttons.
		 * 
		 * @param stringId The resource id of the text of the CheckBox from R.string
		 * @param checked  <code>true</code> to check the CheckBox
		 * @param listener The {@link CompoundButton.OnCheckedChangeListener} to be used.
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setCheckBox(int stringId, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
			this.mCheckBoxText = mContext.getString(stringId);
			this.mCheckBoxIsChecked = checked;
			this.mOnCheckedChangeListener = listener;
			return this;
		}

		/**
		 * Sets the typeface for the title and subtitle of the dialog.
		 * 
		 * @param typeface
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setTitleFont(Typeface typeface) {
			this.mTitleFont = typeface;
			return this;
		}

		/**
		 * Sets the typeface for the dialog message, progress messages, buttons, etc.
		 * 
		 * @param path Path to the font file
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMainFont(String path) {
			this.mMainFont = Typeface.createFromFile(path);
			return this;
		}

		/**
		 * Sets the typeface for the title and subtitle of the dialog.
		 * 
		 * @param path Path to the font file
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setTitleFont(String path) {
			this.mTitleFont = Typeface.createFromFile(path);
			return this;
		}

		/**
		 * Sets the typeface for the dialog message, progress messages, buttons, etc.
		 * 
		 * @param mgr The {@link AssetManager}
		 * @param path Path to the font file in your assets folder
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMainFont(AssetManager mgr, String path) {
			try {
				this.mMainFont = Typeface.createFromAsset(mgr, path);
			} catch (RuntimeException e) {
			}

			return this;
		}

		/**
		 * Sets the typeface for the title and subtitle of the dialog.
		 * 
		 * @param mgr The {@link AssetManager}
		 * @param path Path to the font file in your assets folder
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setTitleFont(AssetManager mgr, String path) {
			try {
				this.mTitleFont = Typeface.createFromAsset(mgr, path);
			} catch (RuntimeException e) {
			}

			return this;
		}

		/**
		 * Sets the typeface for the dialog message, progress messages, buttons, etc.
		 * 
		 * @param typeface
		 * @return Builder object to allow for chaining of calls to set methods
		 */
		public Builder setMainFont(Typeface typeface) {
			this.mMainFont = typeface;
			return this;
		}

		/**
		 * Creates the dialog
		 * 
		 * @return
		 */
		public EasyDialog create() {
			return new EasyDialog(this);
		}

		/**
		 * Creates and shows the dialog
		 * @return
		 */
		public EasyDialog show() {
			EasyDialog dialog = new EasyDialog(this);
			dialog.show();
			return dialog;
		}

		/**
		 * Creates and shows the dialog from a thread.
		 * 
		 * @param handler The {@link Handler} initialized on the UI thread.
		 * @return
		 */
		public void show(Handler handler) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					EasyDialog dialog = new EasyDialog(Builder.this);
					dialog.show();
				}
			});
		}
	}

	/**
	 * Class holding data for the {@link EasyDialogListAdapter}'s getView method
	 */
	public static class ListItem {

		/** The label for the list item */
		public String label;

		/** The icon for the list item. If null then no icon will be shown */
		public Drawable icon;

		/** The text that gets displayed right under the label. 
		 * If null then the view's visibility will be set to {@link View#GONE} */
		public String subLabel;

		/** Whether the CheckBox or RadioButton should be checked. 
		 * If null the CheckBox and RadioButton will not be displayed. */
		public Boolean checked;

		/** Can be used to store some data */
		public Object data;

		/** The color of the label */
		public int labelColor = -1;

		/** The color of the sub-label */
		public int subLabelColor = -1;

		public ListItem() {
		}

		public ListItem(String label) {
			this(null, label, null, null);
		}

		public ListItem(String label, Boolean checked) {
			this(null, label, null, checked);
		}

		public ListItem(Drawable icon, String label) {
			this(icon, label, null, null);
		}

		public ListItem(Drawable icon, String label, Boolean checked) {
			this(icon, label, null, checked);
		}

		public ListItem(Drawable icon, String label, String subLabel) {
			this(icon, label, subLabel, null);
		}

		public ListItem(Drawable icon, String label, String subLabel, Boolean checked) {
			this.icon = icon;
			this.label = label;
			this.subLabel = subLabel;
			this.checked = checked;
		}
	}

	/**
	 * Sorts the list items by their labels in alphabetical order.
	 * <br><br>
	 * Example code:
	 * <br>
	 * <code>
	 * Collections.sort(items, LIST_ITEM_COMPARATOR);
	 * </code>
	 */
	public static final Comparator<ListItem> LIST_ITEM_COMPARATOR = new Comparator<ListItem>() {

		@Override
		public int compare(ListItem item1, ListItem item2) {
			return item1.label.compareToIgnoreCase(item2.label);
		}

	};
}
