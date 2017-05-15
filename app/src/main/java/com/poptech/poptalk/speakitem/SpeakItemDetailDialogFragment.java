package com.poptech.poptalk.speakitem;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.utils.AndroidUtilities;
import com.poptech.poptalk.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

public class SpeakItemDetailDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    public interface SpeakItemDetailDialogFragmentCallback {
        void onClickSpeakItemDialogDismiss(SpeakItem speakItem);
    }

    @Inject
    CollectionsModel mCollectionModel;

    @Inject
    SpeakItemModel mSpeakItemModel;

    private SpeakItem mSpeakItem;
    private List<Collection> mCollections;

    private ImageView mSpeakItemPhoto;
    private TextView mSpeakItemLanguage;
    private TextView mSpeakItemCollection;
    private Button mSpeakItemShareButton;
    private Button mSpeakItemLanguageButton;
    private AutoCompleteTextView mAddCollectionEdit;
    private Button mAddCollectionButton;
    private Button mSaveButton;
    private SpeakItemDetailDialogFragmentCallback mCallBack;

    private String mLanguageString;
    private String mDescriptionString;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpeakItemDetailDialogFragmentCallback) {
            mCallBack = (SpeakItemDetailDialogFragmentCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSpeakItem = args.getParcelable(Constants.KEY_SPEAK_ITEM);
        DaggerSpeakItemDetailDialogComponent.builder().appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent()).build().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private BottomSheetBehavior.BottomSheetCallback
            mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
        mCallBack.onClickSpeakItemDialogDismiss(mSpeakItem);
        super.onDismiss(dialog);
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_speak_items_dialog_layout, null);
        dialog.setContentView(contentView);

        mSpeakItemPhoto = (ImageView) contentView.findViewById(R.id.speak_item_photo_id);
        mSpeakItemCollection = (TextView) contentView.findViewById(R.id.speak_item_collection_id);
        mSpeakItemLanguage = (TextView) contentView.findViewById(R.id.speak_item_language_id);
        mSpeakItemShareButton = (Button) contentView.findViewById(R.id.share_button_id);
        mSpeakItemShareButton.setOnClickListener(this);
        mSpeakItemLanguageButton = (Button) contentView.findViewById(R.id.language_button_id);
        mSpeakItemLanguageButton.setOnClickListener(this);

        mAddCollectionEdit = (AutoCompleteTextView) contentView.findViewById(R.id.add_collection_edit_id);
        mAddCollectionEdit.setOnClickListener(this);
        mAddCollectionButton = (Button) contentView.findViewById(R.id.add_collection_button_id);
        mAddCollectionButton.setOnClickListener(this);

        mSaveButton = (Button) contentView.findViewById(R.id.save_button_id);
        mSaveButton.setOnClickListener(this);

        initData();

    }

    private void initData() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                mCollections = mCollectionModel.getCollections();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                super.onPermissionDenied(response);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permission, token);
            }
        }).check();

        Glide.with(getActivity())
                .load(mSpeakItem.getPhotoPath())
                .centerCrop()
                .thumbnail(0.5f)
                .placeholder(R.color.colorAccent)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mSpeakItemPhoto);
        for (Collection collection : mCollections) {
            if (collection.getId() == mSpeakItem.getCollectionId()) {
                mLanguageString = collection.getLanguage();
                mDescriptionString = collection.getDescription();
                mSpeakItemCollection.setText(collection.getDescription());
                break;
            }
        }
        mSpeakItemLanguage.setText(mSpeakItem.getLanguage());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_collection_edit_id:
                selectCollection();
                break;
            case R.id.add_collection_button_id:
                addNewCollection();
                break;
            case R.id.share_button_id:
                break;
            case R.id.language_button_id:
                selectLanguage();
                break;
            case R.id.save_button_id:
                updateCollection();
                dismiss();
                break;
            default:
                break;
        }
    }

    private void selectLanguage() {
        Locale[] locales = Locale.getAvailableLocales();
        List<String> languages = new ArrayList<>();
        for (Locale l : locales) {
            String language = l.getDisplayLanguage();
            if (!StringUtils.isNullOrEmpty(language)) {
                String[] regexChars = {"\\s+", "\\s*-\\s*", "\\s*'\\s*"};
                String space = " ";
                for (String regex : regexChars) {
                    language = language.replaceAll(regex, space);
                }
                language = language.replaceAll("^\\s+", "");
                language = language.replaceAll("\\s+$", "");
                languages.add(language);
            }

        }
        List<String> sortedLanguages = new ArrayList<>(new HashSet<>(languages));
        Collections.sort(sortedLanguages, String.CASE_INSENSITIVE_ORDER);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.item_listview_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Select Language");
        alertDialog.setIcon(R.drawable.ic_language);
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        final AlertDialog dialog = alertDialog.create();

        ListView listView = (ListView) convertView.findViewById(R.id.list_view_id);
        EditText searchText = (EditText) convertView.findViewById(R.id.search_id);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, sortedLanguages);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String language = (String) parent.getItemAtPosition(position);
                mLanguageString = language;
                mSpeakItemLanguage.setText(language);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void selectCollection() {
        int index = 0;
        List<String> descriptions = new ArrayList<>();
        for (Collection collection : mCollections) {
            if (!StringUtils.isNullOrEmpty(collection.getDescription())) {
                descriptions.add(collection.getDescription());
            }
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.item_listview_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Select Collection");
        alertDialog.setIcon(R.drawable.ic_add_circle);
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        final AlertDialog dialog = alertDialog.create();

        ListView listView = (ListView) convertView.findViewById(R.id.list_view_id);
        EditText searchText = (EditText) convertView.findViewById(R.id.search_id);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, descriptions);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                mDescriptionString = description;
                mSpeakItemCollection.setText(description);
                mAddCollectionEdit.setText(description);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }


    private void addNewCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Collection");
        builder.setIcon(R.drawable.ic_add_circle);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.item_edittext_dialog, null);
        final EditText editText = (EditText) convertView.findViewById(R.id.edit_text_id);
        builder.setView(convertView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = editText.getText().toString();
                mDescriptionString = description;
                mSpeakItemCollection.setText(description);
                mAddCollectionEdit.setText(description);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);

        builder.show();
    }

    private void updateCollection() {
        boolean existed = false;
        long collectionId = -1;
        if (!StringUtils.isNullOrEmpty(mLanguageString)) {
            mSpeakItem.setLanguage(mLanguageString);
        }

        if (!StringUtils.isNullOrEmpty(mDescriptionString)) {
            // Update current collection
            updateCollectionItem(mSpeakItem.getCollectionId());

            // Update new collection
            for (Collection collection : mCollections) {
                if (collection.getDescription().equals(mDescriptionString)) {
                    collectionId = collection.getId();
                    collection.setThumbPath(mSpeakItem.getPhotoPath());
                    collection.setLanguage(mSpeakItem.getLanguage());
                    collection.setNumSpeakItem(collection.getNumSpeakItem() + 1);
                    mCollectionModel.updateCollection(collection);
                    existed = true;
                    break;
                }
            }
            if (!existed) {
                collectionId = new Random().nextInt(Integer.MAX_VALUE);
                Collection collection = new Collection();
                collection.setDescription(mDescriptionString);
                collection.setId(collectionId);
                collection.setThumbPath(mSpeakItem.getPhotoPath());
                collection.setLanguage(mSpeakItem.getLanguage());
                collection.setNumSpeakItem(1);
                collection.setAddedTime(System.currentTimeMillis());
                mCollectionModel.addNewCollection(collection);
                mCollections.add(collection);
            }
            mSpeakItem.setCollectionId(collectionId);
        }

    }

    private void updateCollectionItem(long collectionId) {
        List<SpeakItem> speakItems = mSpeakItemModel.getSpeakItems(collectionId);
        for (int i = 0; i < mCollections.size(); i++) {
            if (mCollections.get(i).getId() == collectionId) {
                for (int j = 0; j < speakItems.size(); j++) {
                    if (speakItems.get(j).getId() == mSpeakItem.getId()) {
                        speakItems.remove(j);
                    }
                }
                mCollections.get(i).setNumSpeakItem(speakItems.size());
                if (speakItems.size() > 0) {
                    mCollections.get(i).setThumbPath(speakItems.get(speakItems.size() - 1).getPhotoPath());
                    mCollections.get(i).setLanguage(speakItems.get(speakItems.size() - 1).getLanguage());
                } else {
                    mCollections.get(i).setThumbPath("");
                    mCollections.get(i).setLanguage("");
                }
                mCollectionModel.updateCollection(mCollections.get(i));
            }
        }

    }
}