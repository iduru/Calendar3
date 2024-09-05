package com.example.calendar3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CalendarView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView selectedDate;
    private TextView eventTitleView;
    private TextView eventDescriptionView;
    private TextView kakunin1;
    private long selectedDateInMillis;
    private EventRepository repository;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        selectedDate = view.findViewById(R.id.selectedDate);
        eventTitleView = view.findViewById(R.id.selectedDate1); // 予定
        eventDescriptionView = view.findViewById(R.id.selectedDate2); // 説明
        kakunin1 = view.findViewById(R.id.kakunin);
        repository = new EventRepository(requireContext()); // EventRepositoryの初期化

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDateInMillis = CalendarUtils.getDateInMillis(year, month, dayOfMonth);
            updateSelectedDateText(year, month, dayOfMonth);
            showAddEventDialog(); // タッチされた日付でダイアログを表示
            kakunin1.setText("CalendarFragment" + "Selected Date in Millis: " + selectedDateInMillis);
        });

        return view;

    }

    private void updateSelectedDateText(int year, int month, int dayOfMonth) {


        // イベント情報を取得
        Event event = repository.getEvent(selectedDateInMillis);
        if (event != null) {
            selectedDate.setText("Selected Date: " + year + "/" + (month + 1) + "/" + dayOfMonth);
            eventTitleView.setText("予定: " + event.getTitle());
            eventDescriptionView.setText("説明: " + event.getDescription());
        } else {
            selectedDate.setText("Selected Date: " + year + "/" + (month + 1) + "/" + dayOfMonth);
            eventTitleView.setText("予定: なし");
            eventDescriptionView.setText("説明: なし");
        }
    }

    private void showAddEventDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);

        EditText editTextEventTitle = dialogView.findViewById(R.id.editTextEventTitle);
        EditText editTextEventDescription = dialogView.findViewById(R.id.editTextEventDescription);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Event")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTextEventTitle.getText().toString();
                    String description = editTextEventDescription.getText().toString();
                    saveEvent(selectedDateInMillis, title, description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveEvent(long date, String title, String description) {
        // 既存のイベントを取得
        Event existingEvent = repository.getEvent(date);

        if (existingEvent != null) {
            // 既存のイベントがある場合は更新
            repository.updateEvent(date, title, description);
            System.out.println("Event updated: " + title + " on " + date);
        } else {
            // 新しいイベントを追加
            repository.addEvent(date, title, description);
            System.out.println("Event saved: " + title + " on " + date);
        }

        // イベントが保存または更新された後に`selectedDate`を更新する
        updateSelectedDateText(
                CalendarUtils.getYear(date),
                CalendarUtils.getMonth(date),
                CalendarUtils.getDayOfMonth(date)
        );
    }
}