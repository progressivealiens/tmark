package com.trackkers.tmark.adapter.fieldofficer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.model.fieldofficer.SurveyAnswerModel;
import com.trackkers.tmark.webApi.SurveyResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<SurveyResponse.DataBean.AllQuestionsBean> surveyResponse = new ArrayList<>();
    List<SurveyAnswerModel> surveyAnswerBeans;

    public SurveyAdapter(Context context, List<SurveyResponse.DataBean.AllQuestionsBean> surveyResponse,List<SurveyAnswerModel> surveyAnswerBeans) {
        this.context = context;
        this.surveyResponse = surveyResponse;
        this.surveyAnswerBeans=surveyAnswerBeans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_survey_layout, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyViewHolder holder1 = (MyViewHolder) holder;

        holder1.tvSurveyQuestion.setText(surveyResponse.get(position).getQuestion());

        if (surveyResponse.get(position).getOptions().size() == 4) {
            holder1.rbOptionOne.setVisibility(View.VISIBLE);
            holder1.rbOptionTwo.setVisibility(View.VISIBLE);
            holder1.rbOptionThree.setVisibility(View.VISIBLE);
            holder1.rbOptionFour.setVisibility(View.VISIBLE);


            holder1.rbOptionOne.setText(surveyResponse.get(position).getOptions().get(0).getOption());
            holder1.rbOptionTwo.setText(surveyResponse.get(position).getOptions().get(1).getOption());
            holder1.rbOptionThree.setText(surveyResponse.get(position).getOptions().get(2).getOption());
            holder1.rbOptionFour.setText(surveyResponse.get(position).getOptions().get(3).getOption());

        } else if (surveyResponse.get(position).getOptions().size() == 3) {
            holder1.rbOptionOne.setVisibility(View.VISIBLE);
            holder1.rbOptionTwo.setVisibility(View.VISIBLE);
            holder1.rbOptionThree.setVisibility(View.VISIBLE);
            holder1.rbOptionFour.setVisibility(View.GONE);

            holder1.rbOptionOne.setText(surveyResponse.get(position).getOptions().get(0).getOption());
            holder1.rbOptionTwo.setText(surveyResponse.get(position).getOptions().get(1).getOption());
            holder1.rbOptionThree.setText(surveyResponse.get(position).getOptions().get(2).getOption());

        } else if (surveyResponse.get(position).getOptions().size() == 2) {
            holder1.rbOptionOne.setVisibility(View.VISIBLE);
            holder1.rbOptionTwo.setVisibility(View.VISIBLE);
            holder1.rbOptionThree.setVisibility(View.GONE);
            holder1.rbOptionFour.setVisibility(View.GONE);

            holder1.rbOptionOne.setText(surveyResponse.get(position).getOptions().get(0).getOption());
            holder1.rbOptionTwo.setText(surveyResponse.get(position).getOptions().get(1).getOption());
        } else {
            holder1.rbOptionOne.setVisibility(View.VISIBLE);
            holder1.rbOptionTwo.setVisibility(View.GONE);
            holder1.rbOptionThree.setVisibility(View.VISIBLE);
            holder1.rbOptionFour.setVisibility(View.GONE);

            holder1.rbOptionOne.setText(surveyResponse.get(position).getOptions().get(0).getOption());
        }

        holder1.rgAnswers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SurveyAnswerModel answerModel;
                switch (checkedId) {
                    case R.id.rb_option_one:
                        answerModel = new SurveyAnswerModel(String.valueOf(surveyResponse.get(position).getOptions().get(0).getQuid()), surveyResponse.get(position).getOptions().get(0).getOption());
                        try {

                            if (surveyAnswerBeans.get(position).getAnswer().equalsIgnoreCase("")) {
                                surveyAnswerBeans.set(position, answerModel);
                            }

                        } catch (IndexOutOfBoundsException e) {
                            surveyAnswerBeans.add(position, answerModel);
                        }

                        break;
                    case R.id.rb_option_two:
                        answerModel = new SurveyAnswerModel(String.valueOf(surveyResponse.get(position).getOptions().get(1).getQuid()), surveyResponse.get(position).getOptions().get(1).getOption());
                        try {

                            if (surveyAnswerBeans.get(position).getAnswer().equalsIgnoreCase("")) {
                                surveyAnswerBeans.set(position, answerModel);
                            }

                        } catch (IndexOutOfBoundsException e) {
                            surveyAnswerBeans.add(position, answerModel);
                        }

                        break;
                    case R.id.rb_option_three:
                        answerModel = new SurveyAnswerModel(String.valueOf(surveyResponse.get(position).getOptions().get(2).getQuid()), surveyResponse.get(position).getOptions().get(2).getOption());
                        try {

                            if (surveyAnswerBeans.get(position).getAnswer().equalsIgnoreCase("")) {
                                surveyAnswerBeans.set(position, answerModel);
                            }

                        } catch (IndexOutOfBoundsException e) {
                            surveyAnswerBeans.add(position, answerModel);
                        }

                        break;
                    case R.id.rb_option_four:
                        answerModel = new SurveyAnswerModel(String.valueOf(surveyResponse.get(position).getOptions().get(3).getQuid()), surveyResponse.get(position).getOptions().get(3).getOption());
                        try {

                            if (surveyAnswerBeans.get(position).getAnswer().equalsIgnoreCase("")) {
                                surveyAnswerBeans.set(position, answerModel);
                            }

                        } catch (IndexOutOfBoundsException e) {
                            surveyAnswerBeans.add(position, answerModel);
                        }

                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return surveyResponse.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_survey_question)
        MyTextview tvSurveyQuestion;
        @BindView(R.id.rb_option_one)
        AppCompatRadioButton rbOptionOne;
        @BindView(R.id.rb_option_two)
        AppCompatRadioButton rbOptionTwo;
        @BindView(R.id.rb_option_three)
        AppCompatRadioButton rbOptionThree;
        @BindView(R.id.rb_option_four)
        AppCompatRadioButton rbOptionFour;
        @BindView(R.id.rg_answers)
        RadioGroup rgAnswers;

        String selectedAnswer = "";

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
