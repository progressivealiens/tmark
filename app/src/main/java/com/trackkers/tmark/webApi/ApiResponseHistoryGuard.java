package com.trackkers.tmark.webApi;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ApiResponseHistoryGuard {

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

    public static class DataBean extends ExpandableGroup<DataBean.CheckPointsScanDetailsBean> {
        private String guardName;
        private String empcode;
        private String date;
        private String checkInTime;
        private String checkOutTime;
        private String checkInAddress;
        private double checkInLatitude;
        private double checkInLongitude;
        private String checkOutAddress;
        private double checkOutLatitude;
        private double checkOutLongitude;
        private String startImageName;
        private String routeName;
        private String siteName;
        private List<CheckPointsScanDetailsBean> checkPointsScanDetails;

        public DataBean(String title, List<CheckPointsScanDetailsBean> items,String siteName,String routeName,String checkInTime,String checkOutTime,String startImageName) {
            super(title, items);
            this.routeName=routeName;
            this.siteName=siteName;
            this.checkInTime=checkInTime;
            this.checkOutTime=checkOutTime;
            this.startImageName=startImageName;
        }

        protected DataBean(Parcel in) {
            super(in);
        }

        public String getGuardName() {
            return guardName;
        }

        public void setGuardName(String guardName) {
            this.guardName = guardName;
        }

        public String getEmpcode() {
            return empcode;
        }

        public void setEmpcode(String empcode) {
            this.empcode = empcode;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
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

        public String getCheckInAddress() {
            return checkInAddress;
        }

        public void setCheckInAddress(String checkInAddress) {
            this.checkInAddress = checkInAddress;
        }

        public double getCheckInLatitude() {
            return checkInLatitude;
        }

        public void setCheckInLatitude(double checkInLatitude) {
            this.checkInLatitude = checkInLatitude;
        }

        public double getCheckInLongitude() {
            return checkInLongitude;
        }

        public void setCheckInLongitude(double checkInLongitude) {
            this.checkInLongitude = checkInLongitude;
        }

        public String getCheckOutAddress() {
            return checkOutAddress;
        }

        public void setCheckOutAddress(String checkOutAddress) {
            this.checkOutAddress = checkOutAddress;
        }

        public double getCheckOutLatitude() {
            return checkOutLatitude;
        }

        public void setCheckOutLatitude(double checkOutLatitude) {
            this.checkOutLatitude = checkOutLatitude;
        }

        public double getCheckOutLongitude() {
            return checkOutLongitude;
        }

        public void setCheckOutLongitude(double checkOutLongitude) {
            this.checkOutLongitude = checkOutLongitude;
        }

        public String getStartImageName() {
            return startImageName;
        }

        public void setStartImageName(String startImageName) {
            this.startImageName = startImageName;
        }

        public String getRouteName() {
            return routeName;
        }

        public void setRouteName(String routeName) {
            this.routeName = routeName;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public List<CheckPointsScanDetailsBean> getCheckPointsScanDetails() {
            return checkPointsScanDetails;
        }

        public void setCheckPointsScanDetails(List<CheckPointsScanDetailsBean> checkPointsScanDetails) {
            this.checkPointsScanDetails = checkPointsScanDetails;
        }

        public static class CheckPointsScanDetailsBean implements Parcelable {
            private String checkpointName;
            private String deviceID;
            private String scanDate;
            private String scanTime;
            private int trip;

            public CheckPointsScanDetailsBean(String name, String date, String time, int roundNo) {
                checkpointName=name;
                scanDate=date;
                scanTime=time;
                trip=roundNo;
            }

            public String getCheckpointName() {
                return checkpointName;
            }

            public void setCheckpointName(String checkpointName) {
                this.checkpointName = checkpointName;
            }

            public String getDeviceID() {
                return deviceID;
            }

            public void setDeviceID(String deviceID) {
                this.deviceID = deviceID;
            }

            public String getScanDate() {
                return scanDate;
            }

            public void setScanDate(String scanDate) {
                this.scanDate = scanDate;
            }

            public String getScanTime() {
                return scanTime;
            }

            public void setScanTime(String scanTime) {
                this.scanTime = scanTime;
            }

            public int getTrip() {
                return trip;
            }

            public void setTrip(int trip) {
                this.trip = trip;
            }


            public CheckPointsScanDetailsBean(Parcel in) {
                checkpointName = in.readString();
                scanDate = in.readString();
                scanTime = in.readString();
                trip = in.readInt();
            }
            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(checkpointName);
                dest.writeString(scanDate);
                dest.writeString(scanTime);
                dest.writeInt(trip);
            }

            @Override
            public int describeContents() {
                return 0;
            }
            public static final Creator<CheckPointsScanDetailsBean> CREATOR = new Creator<CheckPointsScanDetailsBean>() {
                @Override
                public CheckPointsScanDetailsBean createFromParcel(Parcel in) {
                    return new CheckPointsScanDetailsBean(in);
                }

                @Override
                public CheckPointsScanDetailsBean[] newArray(int size) {
                    return new CheckPointsScanDetailsBean[size];
                }
            };
        }
    }
}
