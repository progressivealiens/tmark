package com.trackkers.tmark.webApi;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ApiResponseHistoryOperational {

    private String status;
    private String msg;
    private List<DataBean> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean extends ExpandableGroup<DataBean.CommunicationsListDataBean> {

        private String checkInTime;
        private String checkOutTime;
        private String message;
        private String workingHours;
        private String startImageName;
        private List<CommunicationsListDataBean> communicationsListData;


        public DataBean(String title, List<CommunicationsListDataBean> items, String checkInTime, String checkOutTime, String message, String workingHours,String startImageName) {
            super(title, items);
            this.checkInTime = checkInTime;
            this.checkOutTime = checkOutTime;
            this.message = message;
            this.workingHours = workingHours;
            this.startImageName=startImageName;
        }

        public String getStartImageName() {
            return startImageName;
        }

        public void setStartImageName(String startImageName) {
            this.startImageName = startImageName;
        }

        public String getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(String checkInTime) {
            this.checkInTime = checkInTime;
        }

        public String getCheckOutTime() {
            return checkOutTime;
        }

        public void setCheckOutTime(String checkOutTime) {
            this.checkOutTime = checkOutTime;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getWorkingHours() {
            return workingHours;
        }

        public void setWorkingHours(String workingHours) {
            this.workingHours = workingHours;
        }

        public List<CommunicationsListDataBean> getCommunicationsListData() {
            return communicationsListData;
        }

        public void setCommunicationsListData(List<CommunicationsListDataBean> communicationsListData) {
            this.communicationsListData = communicationsListData;
        }

        public static class CommunicationsListDataBean implements Parcelable {

            private String type;
            private String text;
            private String image;
            private String time;

            public CommunicationsListDataBean(String type, String text, String image, String time) {
                this.type = type;
                this.text = text;
                this.image = image;
                this.time = time;
            }

            protected CommunicationsListDataBean(Parcel in) {
                type = in.readString();
                text = in.readString();
                image = in.readString();
                time = in.readString();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(type);
                dest.writeString(text);
                dest.writeString(image);
                dest.writeString(time);
            }

            public static final Creator<CommunicationsListDataBean> CREATOR = new Creator<CommunicationsListDataBean>() {
                @Override
                public CommunicationsListDataBean createFromParcel(Parcel in) {
                    return new CommunicationsListDataBean(in);
                }

                @Override
                public CommunicationsListDataBean[] newArray(int size) {
                    return new CommunicationsListDataBean[size];
                }
            };

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getImage() {
                return image;
            }

            public void setImage(String image) {
                this.image = image;
            }

            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }
        }
    }
}
