package com.trackkers.tmark.adapter.fieldofficer;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.model.fieldofficer.SurveyAnswerModel;
import com.trackkers.tmark.views.activity.fieldofficer.SiteDetailsActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;
import com.trackkers.tmark.webApi.SurveyResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.trackkers.tmark.views.activity.fieldofficer.SiteDetailsActivity.CAMERA_REQUEST;
import static com.trackkers.tmark.views.activity.fieldofficer.SiteDetailsActivity.REQUEST_CODE_FOR_IMAGE;
import static com.trackkers.tmark.views.activity.fieldofficer.SiteDetailsActivity.currentLatitude;
import static com.trackkers.tmark.views.activity.fieldofficer.SiteDetailsActivity.currentLongitude;

public class SiteDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity context;
    ArrayList<ApiResponse.DataBean> siteDetails;
    ApiInterface apiInterface;
    ProgressView progressView;
    public static String commentText = "", commentImagePath = "", commType = "";
    public static File commentFile;
    Bitmap myBitmap = null;

    MyCallBack myCallback;

    public interface MyCallBack {
        void listenerMethod();
    }

    public SiteDetailsAdapter(AppCompatActivity context, ArrayList<ApiResponse.DataBean> siteDetails, MyCallBack myCallBack) {
        this.context = context;
        this.siteDetails = siteDetails;
        this.myCallback = myCallBack;

        progressView = new ProgressView(context);
        apiInterface = ApiClient.getClient(context).create(ApiInterface.class);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sites_details, parent, false);

        return new MyViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        MyViewholder viewholder = (MyViewholder) holder;

        if (!payloads.isEmpty() && payloads.get(0) instanceof SetImage) {

            myBitmap = BitmapFactory.decodeFile(commentFile.getAbsolutePath());
            viewholder.tvAttachImage.setImageBitmap(myBitmap);

        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewholder viewholder = (MyViewholder) holder;

        viewholder.tvSiteName.setText(context.getResources().getString(R.string.site_name) + siteDetails.get(position).getSiteName());
        viewholder.tvSiteAddress.setText(context.getResources().getString(R.string.site_address_qolon) + siteDetails.get(position).getAddress());

        if (siteDetails.get(position).isAlreadySiteVisitStarted()) {
            viewholder.cardViewComments.setVisibility(View.VISIBLE);
        } else {
            viewholder.cardViewComments.setVisibility(View.GONE);
        }

        viewholder.btnStartSiteVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (siteDetails.get(position).isAlreadySiteVisitStarted()) {
                    Toast.makeText(context, context.getResources().getString(R.string.visit_already_started), Toast.LENGTH_SHORT).show();
                } else {
                    PrefData.writeStringPref(PrefData.suid, String.valueOf(siteDetails.get(position).getSuid()));

                    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        sendTakePictureIntentForStartVisit(0);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.phone_doesnt_have_camer), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        viewholder.btnEndSiteVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (siteDetails.get(position).isAlreadySiteVisitStarted()) {

                   /* if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                        Toast.makeText(context, context.getResources().getString(R.string.unable_to_fetch_exact_location), Toast.LENGTH_SHORT).show();
                    } else {
                        connectApiToEndSiteVisit();
                    }*/

                    connectApiToFetchSurveyQuestions(String.valueOf(siteDetails.get(position).getSuid()), siteDetails.get(position).getVisitToken(), siteDetails.get(position).getSiteName());

                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.start_visit_first), Toast.LENGTH_SHORT).show();
                }
            }
        });


        viewholder.tvAttachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PrefData.writeStringPref(PrefData.site_attach_image_position, String.valueOf(position));

                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    sendTakePictureIntentForStartVisit(1);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.phone_doesnt_have_camer), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewholder.btnSubmitComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentText = viewholder.etComment.getText().toString();

                if (commentText.equalsIgnoreCase("") && commentImagePath.equalsIgnoreCase("")) {
                    Toast.makeText(context, R.string.write_comment, Toast.LENGTH_SHORT).show();
                } else if (!commentText.equalsIgnoreCase("") && commentImagePath.equalsIgnoreCase("")) {
                    commType = "Text";
                } else if (commentText.equalsIgnoreCase("") && !commentImagePath.equalsIgnoreCase("")) {
                    commType = "Image";
                } else if (!commentText.equalsIgnoreCase("") && !commentImagePath.equalsIgnoreCase("")) {
                    commType = "Both";
                }

                if (!commType.equalsIgnoreCase("")) {
                    connectApiToComment(commType, commentText, siteDetails.get(position).getVisitToken(), viewholder.etComment, viewholder.tvAttachImage);
                }
            }
        });


    }

    private void connectApiToFetchSurveyQuestions(String suid, String visitToken, String sitename) {
        if (CheckNetworkConnection.isConnection1(context, true)) {
            progressView.showLoader();
        }

        Call<SurveyResponse> call = apiInterface.getFoSiteSurvey(PrefData.readStringPref(PrefData.firebase_token), suid);
        call.enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(Call<SurveyResponse> call, Response<SurveyResponse> response) {
                progressView.hideLoader();
                try {

                    if (response.body().getStatus().equalsIgnoreCase(context.getResources().getString(R.string.success))) {

                        List<SurveyResponse.DataBean.AllQuestionsBean> surveyResponses = new ArrayList<>();

                        surveyResponses.clear();
                        surveyResponses.addAll(response.body().getData().get(0).getAllQuestions());

                        String surveyId = String.valueOf(response.body().getData().get(0).getSurvuid());
                        String surveyName = String.valueOf(response.body().getData().get(0).getSurveyname());

                        openDialogToTakeSurveyBeforeEndVisit(visitToken, surveyResponses, surveyId, surveyName, sitename);

                    } else {
                        if (response.body().getStatus().toLowerCase().equalsIgnoreCase("fail")) {
                            connectApiToEndSiteVisit(visitToken, "null", "null", null, false);
                        } else if (response.body().getStatus().toLowerCase().equalsIgnoreCase("error")) {
                            Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    if (response.code() == 400) {
                        Toast.makeText(context, context.getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 500) {
                        Toast.makeText(context, context.getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 404) {
                        Toast.makeText(context, context.getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<SurveyResponse> call, Throwable t) {
                progressView.hideLoader();
            }
        });


    }

    private void openDialogToTakeSurveyBeforeEndVisit(String visitToken, List<SurveyResponse.DataBean.AllQuestionsBean> surveyBeans, String surveyId, String surveyname, String sitename) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_survey);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((AppCompatActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = (int) (displaymetrics.widthPixels * 0.9);
        int height = (int) (displaymetrics.heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);

        RecyclerView surveyRecycler;
        SurveyAdapter mAdapter;
        MyButton cancel, submit;
        MyTextview surveyName, siteName;
        cancel = dialog.findViewById(R.id.btn_dialog_cancel);
        submit = dialog.findViewById(R.id.btn_dialog_submit);
        surveyRecycler = dialog.findViewById(R.id.recycler_survey);
        surveyName = dialog.findViewById(R.id.tv_survey_name);
        siteName = dialog.findViewById(R.id.tv_site_name);
        surveyName.setText(surveyname);
        siteName.setText(context.getResources().getString(R.string.site_name) + sitename);

        List<SurveyAnswerModel> surveyAnswerBeans = new ArrayList<>();
        surveyAnswerBeans.clear();

        for (int i = 0; i < surveyBeans.size(); i++) {
            SurveyAnswerModel models = new SurveyAnswerModel("", "");
            surveyAnswerBeans.add(models);
        }

        LinearLayoutManager manager = new LinearLayoutManager(context);
        surveyRecycler.setLayoutManager(manager);
        mAdapter = new SurveyAdapter(context, surveyBeans, surveyAnswerBeans);
        surveyRecycler.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (surveyBeans.size() == surveyAnswerBeans.size()) {

                    boolean allOkWithData = false;
                    for (int i = 0; i < surveyAnswerBeans.size(); i++) {

                        if (surveyAnswerBeans.get(i).getQuestionId().equalsIgnoreCase("") && surveyAnswerBeans.get(i).getAnswer().equalsIgnoreCase("")) {
                            allOkWithData = true;
                        }
                    }

                    if (!allOkWithData) {

                        try {

                            JSONObject mainObject = new JSONObject();
                            JSONArray jsonArray = new JSONArray();

                            for (int i = 0; i < surveyAnswerBeans.size(); i++) {
                                JSONObject uidObject = new JSONObject();
                                uidObject.put("quid", surveyAnswerBeans.get(i).getQuestionId());
                                uidObject.put("response", surveyAnswerBeans.get(i).getAnswer());
                                jsonArray.put(uidObject);
                            }
                            mainObject.put("data", jsonArray);
                            Log.e("data", mainObject.toString());
                            connectApiToEndSiteVisit(visitToken, mainObject.toString(), surveyId, dialog, true);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else {
                        Toast.makeText(context, "Please answer all the questions", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, "Please answer all the questions", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void sendTakePictureIntentForStartVisit(int flag) {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFileCheckin = null;
        try {
            pictureFileCheckin = getPictureFileStartVisit(flag);
            if (pictureFileCheckin != null) {
                Uri photoURI = FileProvider.getUriForFile(context, "com.trackkers.tmark.fileprovider", pictureFileCheckin);
                cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (flag == 0) {
                    context.startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else {
                    context.startActivityForResult(cameraIntent, REQUEST_CODE_FOR_IMAGE);
                }
            }
        } catch (IOException ex) {
            Toast.makeText(context, context.getResources().getString(R.string.photo_cant_be_created), Toast.LENGTH_SHORT).show();
        }
    }

    private File getPictureFileStartVisit(int Flag) throws IOException {

        String timeStamp = Utils.currentTimeStamp();
        File storageDir = context.getExternalFilesDir(null);
        File image = File.createTempFile(timeStamp, ".png", storageDir);
        if (Flag == 0) {
            SiteDetailsActivity.imagePath = image.getAbsolutePath();
        } else {
            commentImagePath = image.getAbsolutePath();
        }

        return image;

    }

    public void connectApiToComment(String commentType, String commText, String visitToken, TextInputEditText etComment, ImageView tvAttachImage) {

        if (CheckNetworkConnection.isConnection1(context, true)) {
            progressView.showLoader();
            MultipartBody.Part filePart = null;

            if (commentType.equalsIgnoreCase("TEXT")) {
                filePart = MultipartBody.Part.createFormData("image", "", RequestBody.create(MediaType.parse("text/plain"), ""));
            } else {
                filePart = MultipartBody.Part.createFormData("image", commentFile.getName(), RequestBody.create(MediaType.parse("image/*"), commentFile));
            }

            RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token) + "");
            RequestBody Type = RequestBody.create(MediaType.parse("text/plain"), commentType);
            RequestBody CommentText = RequestBody.create(MediaType.parse("text/plain"), commText);
            RequestBody visitedToken = RequestBody.create(MediaType.parse("text/plain"), visitToken);

            Call<ApiResponse> call = apiInterface.PostTextOrImage(
                    SecurityToken,
                    visitedToken,
                    Type,
                    CommentText,
                    filePart
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(context.getResources().getString(R.string.success))) {
                            Toast.makeText(context, context.getResources().getString(R.string.posted_successfully), Toast.LENGTH_SHORT).show();

                            etComment.setText("");
                            commType = "";
                            tvAttachImage.setImageResource(R.drawable.ic_add_box_black_24dp);
                            commentImagePath = "";

                        } else {
                            Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(context, context.getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(context, context.getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(context, context.getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();

                    }

                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });
        }


    }

    private void connectApiToEndSiteVisit(String visitiToken, String responseData, String surveyId, Dialog dialog, boolean flag) {

        if (CheckNetworkConnection.isConnection1(context, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.EndSiteVisit(
                    PrefData.readStringPref(PrefData.security_token),
                    visitiToken,
                    surveyId,
                    String.valueOf(currentLatitude),
                    String.valueOf(currentLongitude),
                    responseData
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    if (response.body().getStatus().equalsIgnoreCase(context.getString(R.string.success))) {

                        Toast.makeText(context, "Site Visit Ended Successfully", Toast.LENGTH_SHORT).show();

                        myCallback.listenerMethod();
                        if (flag) {
                            dialog.dismiss();
                        }


                    } else {
                        Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return siteDetails.size();
    }

    public class MyViewholder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_site_name)
        MyTextview tvSiteName;
        @BindView(R.id.tv_site_address)
        MyTextview tvSiteAddress;
        @BindView(R.id.card_site)
        CardView cardSite;
        @BindView(R.id.btn_start_site_visit)
        MyButton btnStartSiteVisit;
        @BindView(R.id.btn_end_site_visit)
        MyButton btnEndSiteVisit;
        @BindView(R.id.et_comment)
        TextInputEditText etComment;
        @BindView(R.id.tv_attach_image)
        ImageView tvAttachImage;
        @BindView(R.id.btn_submit_comments)
        Button btnSubmitComments;
        @BindView(R.id.card_view_comments)
        LinearLayout cardViewComments;

        public MyViewholder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class SetImage {
    }

}
