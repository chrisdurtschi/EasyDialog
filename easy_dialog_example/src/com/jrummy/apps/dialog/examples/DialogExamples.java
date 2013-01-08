package com.jrummy.apps.dialog.examples;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.jrummy.apps.dialogs.EasyDialog;
import com.jrummy.apps.dialogs.EasyDialog.ListItem;

public class DialogExamples extends Activity implements OnClickListener {

	private static final Handler mHandler = new Handler();

	private static final String[ ] BUTTON_NAMES = {
		"Simple Dialog",
		"Single Choice Dialog",
		"Multi Choice Dialog",
		"List Dialog",
		"Progress Dialog",
		"Horizontal Progress Dialog",
		"EditText Dialog",
		"WebView Dialog",
		"View All Controls",
		"App List"
	};

	private boolean mCancelProgressDialog;
	private int mSelectedPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ScrollView sv = new ScrollView(this);
		sv.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.MATCH_PARENT));

		LinearLayout main = new LinearLayout(this);
		main.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT));
		main.setOrientation(LinearLayout.VERTICAL);
		sv.addView(main);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int sideMargin = (int) (8 * (metrics.densityDpi / 160f)); // dp to px
		int topMargin  = (int) (2 * (metrics.densityDpi / 160f)); // dp to px

		for (int i = 0; i < BUTTON_NAMES.length; i++) {
			Button button = new Button(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(sideMargin, topMargin, sideMargin, 0);
			button.setLayoutParams(params);
			button.setText(BUTTON_NAMES[i]);
			button.setId(i);
			button.setOnClickListener(this);
			main.addView(button);
		}

		setContentView(sv);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case 0:
			showSimpleDialog();
			break;
		case 1:
			showSingleChoiceDialog();
			break;
		case 2:
			showMultiChoiceDialog();
			break;
		case 3:
			showListDialog();
			break;
		case 4:
			showProgressDialog();
			break;
		case 5:
			showHorizontalProgressDialog();
			break;
		case 6:
			showEditTextDialog();
			break;
		case 7:
			showWebViewDialog();
			break;
		case 8:
			showAllControls();
			break;
		case 9:
			showAppsDialog();
			break;
		}
	}

	private void showSimpleDialog() {
		new EasyDialog.Builder(this)
		.setIcon(R.drawable.info)
		.setTitle("A Simple Dialog")
		.setMessage("This shows how you can display a simple dialog.")
		.setPositiveButton(R.string.db_close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
	}

	private void showSingleChoiceDialog() {
		mSelectedPosition = -1;
		final String[] items = {
				"Android",
				"iOS",
				"Windows Mobile",
				"BlackBerry",
				"Other"
		};

		new EasyDialog.Builder(this)
		.setIcon(R.drawable.android)
		.setTitle("Favorite Mobile OS")
		.setPositiveButtonEnabledState(false)
		.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((EasyDialog) dialog).setButtonEnabledState(Dialog.BUTTON_POSITIVE, true);
				mSelectedPosition = which;
			}
		})
		.setNegativeButton(R.string.db_close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton(R.string.db_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (items[mSelectedPosition].equals("Android")) {
					Toast.makeText(getApplicationContext(), "Good Choice!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Really! You suck :P", Toast.LENGTH_LONG).show();
				}
				dialog.dismiss();
			}
		})
		.show();
	}

	private void showMultiChoiceDialog() {
		final String[] items = {
				"Facebook",
				"Twitter",
				"Google+",
				"YouTube",
				"Linkedin",
				"Pinterest",
				"Foursquare"
		};

		final boolean[] checkedItems = new boolean[items.length];

		new EasyDialog.Builder(this)
		.setTitle("Social Media Sites")
		.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checkedItems[which] = isChecked;
			}
		})
		.setNegativeButton(R.string.db_close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton(R.string.db_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<ListItem> items = ((EasyDialog) dialog).getCheckedItems();
				String message;
				if (!items.isEmpty()) {
					message = "You selected " + items.get(0).label;
					for (int i = 1; i < items.size(); i++) {
						message += ", " + items.get(i).label;
					}
				} else {
					message = "You didn't select anything";
				}

				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				dialog.dismiss();
			}
		})
		.show();
	}

	/**
	 * Used in {@link #showListDialog()} to get a list of files
	 * @param path The path to list
	 * @return
	 */
	private List<ListItem> getFiles(String path) {
		List<ListItem> items = new ArrayList<ListItem>();
		Resources res = getResources();
		Drawable folderIcon = res.getDrawable(R.drawable.folder);
		Drawable fileIcon = res.getDrawable(R.drawable.file);

		// Add the parent directory if this isn't the root directory
		if (!path.equals("/")) {
			ListItem item = new ListItem(folderIcon, "..", "Parent Folder");
			File parentFile = new File(path).getParentFile();
			String parent = parentFile == null ? "/" : parentFile.getPath();
			item.data = new File(parent);
			items.add(item);
		}

		File[] files = new File(path).listFiles();
		if (files == null) {
			return items;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMMMM, yyyy HH:mm:ss");

		for (File file : files) {
			String date = sdf.format(file.lastModified());
			Drawable icon = file.isDirectory() ? folderIcon : fileIcon;
			ListItem item = new ListItem(icon, file.getName(), date); // icon, label, sub-label
			item.data = file;
			items.add(item);
		}

		Collections.sort(items, EasyDialog.LIST_ITEM_COMPARATOR);

		return items;
	}

	private void showListDialog() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		List<ListItem> items = getFiles(path);

		EasyDialog dialog = new EasyDialog.Builder(this)
		.setIcon(R.drawable.folder)
		.setTitle("File List")
		.setSubtitle(path)
		.setFastScrollEnabled(true)
		.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EasyDialog easyDialog = (EasyDialog) dialog;
				ListItem item = easyDialog.getListItem(which);
				File file = (File) item.data;
				if (file.isDirectory()) {
					List<ListItem> files = getFiles(file.getPath());
					easyDialog.setListItems(files);
					easyDialog.setSubtitle(file.getPath());
					easyDialog.getListView().setSelection(0);
				} else {
					Toast.makeText(getApplicationContext(), "You clicked on " + item.label, 
							Toast.LENGTH_LONG).show();
				}
			}
		})
		.setPositiveButton(R.string.db_close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();

		// We want the dialog to fill the screen even if not many items are in the list.
		dialog.getListView().getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

		dialog.show();
	}

	private void showProgressDialog() {
		new EasyDialog.Builder(this)
		.setCancelable(false)
		.setTitle(R.string.please_wait)
		.setIndeterminateProgress("Calculating...")
		.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
	}

	private void showHorizontalProgressDialog() {
		final EasyDialog dialog = new EasyDialog.Builder(this)
		.setTitle(R.string.please_wait)
		.setCancelable(false)
		.setIndeterminateProgress("Searching files...")
		.setHorizontalProgress(1000, 0, "Searching 1,000 files...")
		.setPositiveButton("Stop Search", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCancelProgressDialog = true;
			}
		})
		.show();

		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				mCancelProgressDialog = false;

				for (int i = 0; i < 100; i++) {
					try {
						Thread.sleep(80);
					} catch (InterruptedException e) {
					}

					final boolean almostThere = i == 75;

					mHandler.post(new Runnable() {
						public void run() {
							if (mCancelProgressDialog) {
								dialog.dismiss();
								return;
							}

							if (almostThere) {
								dialog.updateProgressMessage("Almost done...");
							}

							dialog.incrementProgressBy(10);
						}
					});

					if (mCancelProgressDialog) {
						break;
					}
				}

				mHandler.post(new Runnable() {
					public void run() {
						EasyDialog.Builder finishedProgressDialog = new EasyDialog.Builder(DialogExamples.this)
						.setTitle("Search Complete!")
						.setMessage("We finished the job!")
						.setPositiveButton("Close", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});

						dialog.build(finishedProgressDialog);
					}
				});

			}
		}.start();
	}

	private void showEditTextDialog() {
		new EasyDialog.Builder(this)
		.setTitle("What's your name?")
		.setMessage("Please enter your name:")
		.setEditText("", "Your Name", null)
		.setNegativeButton(R.string.db_close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton(R.string.db_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText editText = ((EasyDialog) dialog).getEditText();
				String name = editText.getText().toString();
				Toast.makeText(DialogExamples.this, "Hello " + name, Toast.LENGTH_LONG).show();
			}
		})
		.show();
	}

	private void showWebViewDialog() {
		new EasyDialog.Builder(this)
		.setTitle("Changelog")
		.setWebViewUrl("file:///android_asset/html/changelog.html")
		.setPositiveButton("Close", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
	}

	private void showAllControls() {
		final String[] items = new String[] { "Item1", "Item2", "Item3", "Item4" };

		int[] colors = { 0xFF00CC99, 0xFF32cd32, 0xFFeb0258, 0xFFa35200 };
		List<ListItem> listItems = new ArrayList<ListItem>();
		for (int i = 0; i < items.length; i++) {
			ListItem item = new ListItem(items[i]);
			item.labelColor = colors[i];
			listItems.add(item);
		}

		final EasyDialog dialog = new EasyDialog.Builder(this)
		.setCancelable(true)
		.setCanceledOnTouchOutside(true)
		.setIcon(R.drawable.warning)
		.setTitle("All Controls")
		.setTitleFont(getAssets(), "fonts/font.ttf")
		.setTitleBarProgress(true)
		.setMessage("This dialog shows most of the controls you can have in a single dialog")
		.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				Toast.makeText(getApplicationContext(), "You closed the dialog", 
						Toast.LENGTH_LONG).show();
			}
		})
		.setEditText("", "Enter Something", new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

		})
		.setIndeterminateProgress(R.string.please_wait)
		.setIndeterminateHorizontalProgress("Calculating...")
		.setGridViewItems(listItems, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "You clicked " + items[which], Toast.LENGTH_LONG).show();
			}
		})
		.setNegativeButton(R.string.db_close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setNeutralButton(R.string.db_exit, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		})
		.setPositiveButton(R.string.db_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();

		dialog.setCheckBox("Show title progress", true, new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dialog.showTitleProgress(isChecked);
			}
		});

		dialog.show();

	}

	private void showAppsDialog() {
		final EasyDialog dialog = new EasyDialog.Builder(this)
		.setTitle("Please Wait...")
		.setIndeterminateProgress("Loading Applications...")
		.show();

		new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				PackageManager pm = getPackageManager();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);

				dialog.getBuilder().setHorizontalProgress(
						resolves.size(), 0, "Loading app info...");
				dialog.rebuild(mHandler);

				List<ListItem> items = new ArrayList<ListItem>();
				for (ResolveInfo app : resolves) {
					ListItem item = new ListItem();
					item.label = app.loadLabel(pm).toString();
					item.icon = app.loadIcon(pm);
					item.subLabel = app.activityInfo.packageName;
					item.checked = false;
					item.data = app;
					items.add(item);
					dialog.incrementProgress(mHandler);
				}

				Collections.sort(items, EasyDialog.LIST_ITEM_COMPARATOR);

				dialog.build(new EasyDialog.Builder(DialogExamples.this)
				.setTitle("Installed Apps")
				.setTitleCheckBox(false, new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						dialog.checkAll(isChecked);
					}
				})
				.setFastScrollEnabled(true)
				.setMultiChoiceItems(items, new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {

					}
				})
				.setPositiveButton("Done", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showSelectedApps(((EasyDialog) dialog).getCheckedItems());
						dialog.dismiss();
					}
				}), mHandler);
			}
		}.start();
	}

	private void showSelectedApps(List<ListItem> selectedItems) {
		if (selectedItems.isEmpty()) {
			Toast.makeText(DialogExamples.this, "You didn't select any apps", 
					Toast.LENGTH_LONG).show();
			return;
		}

		// We need to set the Boolean to null to hide the CheckBox in the list.
		for (ListItem item : selectedItems) {
			item.checked = null;
		}

		new EasyDialog.Builder(DialogExamples.this)
		.setTitle("Selected Apps")
		.setMessage("Click on an app to calculate the size of the APK file.")
		.setItems(selectedItems, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ListItem item = ((EasyDialog) dialog).getListItem(which);
				ResolveInfo ri = (ResolveInfo) item.data;

				File file = new File(ri.activityInfo.applicationInfo.sourceDir);
				String apkSize = Formatter.formatFileSize(DialogExamples.this, file.length());

				new EasyDialog.Builder(DialogExamples.this)
				.setIcon(item.icon)
				.setTitle(item.label)
				.setMessage("The size of this app's APK file is " + apkSize)
				.show();
			}
		})
		.setPositiveButton("Close", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}
}
