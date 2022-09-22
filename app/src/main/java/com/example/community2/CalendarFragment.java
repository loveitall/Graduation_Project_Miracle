package com.example.community2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.community2.model.dayModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private String todayD;
    private Button previousMonthBtn, nextMonthBtn;
    private CalendarAdapter calendarAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarRecyclerView = v.findViewById(R.id.calendarRecyclerView);
        monthYearText = v.findViewById(R.id.monthYearTV);
        previousMonthBtn = v.findViewById(R.id.previousMonthBtn);
        nextMonthBtn = v.findViewById(R.id.nextMonthBtn);

        previousMonthBtn.setOnClickListener(view -> previousMonthAction());
        nextMonthBtn.setOnClickListener(view -> nextMonthAction());

        selectedDate = LocalDate.now();
        LocalDate now = LocalDate.now();
        setMonthView();
        return v;
    }

    private void setMonthView() {
        todayD = todayDate(selectedDate);
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        calendarAdapter = new CalendarAdapter(daysInMonth, todayD, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
/*        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);*/
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String todayDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        return date.format(formatter);
    }

    public String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            List<dayModel> days = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("daily").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    days.clear();
                    for(DataSnapshot item : snapshot.getChildren()) {
                        dayModel dayModel = item.getValue(com.example.community2.model.dayModel.class);
                        days.add(dayModel);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Intent intent = new Intent(getActivity(), DailyActivity.class);
            intent.putExtra("day", dayText);
            intent.putExtra("clickMonth", monthYearFromDate(selectedDate) );
            intent.putExtra("todo1", days.get(position).todo1);
            intent.putExtra("todo2", days.get(position).todo2);
            intent.putExtra("todo3", days.get(position).todo3);
            intent.putExtra("diary", days.get(position).diary);
            startActivity(intent);
        }
    }

}
