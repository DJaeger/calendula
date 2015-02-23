package es.usc.citius.servando.calendula.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 2/23/15.
 */
public class RingtonePreference extends android.preference.RingtonePreference {

    String[] titles;
    String[] values;
    SimpleListAdapter adapter;
    int selectedIndex = -1;
    Ringtone ringtone;

    public RingtonePreference(Context context) {
        super(context);
        getEntriesAndValues();
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        getEntriesAndValues();
        adapter = new SimpleListAdapter(getContext(), R.layout.ringtone_list_item, titles);
    }

    @Override
    protected void onClick() {

        ListView list = new ListView(getContext());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(values[position]);
                selectedIndex = position;
                adapter.notifyDataSetChanged();
                stopRingtone();
                ringtone = RingtoneManager.getRingtone(getContext(), uri);

                if (ringtone != null) {
                    ringtone.play();
                }
            }
        });

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setCancelable(true);
        builderSingle.setTitle(getTitle())
                .setView(list)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopRingtone();
                                dialog.dismiss();
                            }
                        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedIndex != -1) {
                    onSaveRingtone(Uri.parse(values[selectedIndex]));
                    setSummary(titles[selectedIndex]);
                }
                stopRingtone();
                dialog.dismiss();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                stopRingtone();
            }
        }).show();

    }

    void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    void getEntriesAndValues() {
        RingtoneManager manager = new RingtoneManager(getContext());
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        List<CharSequence> _titles = new ArrayList<CharSequence>();
        List<CharSequence> _values = new ArrayList<CharSequence>();

        Cursor cursor = manager.getCursor();
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + id;

            Log.d("TAG", title + ", " + uri + ", " + id);

            _titles.add(title);
            _values.add(uri);
        }
        titles = _titles.toArray(new String[_titles.size()]);
        values = _values.toArray(new String[_values.size()]);

        Uri value = onRestoreRingtone();
        if (value != null) {
            selectedIndex = _values.indexOf(value.toString());
        }
    }

    public class SimpleListAdapter extends ArrayAdapter<String> {

        public SimpleListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public SimpleListAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.ringtone_list_item, null);
            }

            CheckedTextView tv = ((CheckedTextView) v.findViewById(R.id.text1));
            tv.setText(titles[position]);

            if (position == selectedIndex) {
                tv.setChecked(true);
            } else {
                tv.setChecked(false);
            }
            return v;
        }
    }
}
