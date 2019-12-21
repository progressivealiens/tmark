package com.trackkers.tmark.webApi;

import java.util.List;

public class ApiResponse {

    private String status;
    private String msg;
    private String token;
    private String type;
    private String routeStartAddress;
    private String routeEndAddress;
    private String siteAddress;
    private String flag;
    private boolean isCheckedIn;
    private String distenceInMeter;
    private int currentRound;
    private String checkInTime;
    private String alarmInterval;
    private String guardName;
    private boolean isLiveTracking;
    private String companyName;
    private boolean isConveyanceAsked;
    private int employeeId;
    private String logo;
    private boolean liveTrackingEnabled;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public String getRouteStartAddress() {
        return routeStartAddress;
    }

    public void setRouteStartAddress(String routeStartAddress) {
        this.routeStartAddress = routeStartAddress;
    }

    public String getRouteEndAddress() {
        return routeEndAddress;
    }

    public void setRouteEndAddress(String routeEndAddress) {
        this.routeEndAddress = routeEndAddress;
    }

    public String getSiteAddress() {
        return siteAddress;
    }

    public void setSiteAddress(String siteAddress) {
        this.siteAddress = siteAddress;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public boolean isIsCheckedIn() {
        return isCheckedIn;
    }

    public void setIsCheckedIn(boolean isCheckedIn) {
        this.isCheckedIn = isCheckedIn;
    }

    public String getDistenceInMeter() {
        return distenceInMeter;
    }

    public void setDistenceInMeter(String distenceInMeter) {
        this.distenceInMeter = distenceInMeter;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public String getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(String alarmInterval) {
        this.alarmInterval = alarmInterval;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getGuardName() {
        return guardName;
    }

    public void setGuardName(String guardName) {
        this.guardName = guardName;
    }

    public boolean isIsLiveTracking() {
        return isLiveTracking;
    }

    public void setIsLiveTracking(boolean isLiveTracking) {
        this.isLiveTracking = isLiveTracking;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isIsConveyanceAsked() {
        return isConveyanceAsked;
    }

    public void setIsConveyanceAsked(boolean isConveyanceAsked) {
        this.isConveyanceAsked = isConveyanceAsked;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isLiveTrackingEnabled() {
        return liveTrackingEnabled;
    }

    public void setLiveTrackingEnabled(boolean liveTrackingEnabled) {
        this.liveTrackingEnabled = liveTrackingEnabled;
    }

    public static class DataBean {
        private int employeeId;
        private String name;
        private String dob;
        private String doj;
        private String mobile;
        private String nationality;
        private String address;
        private int routeId;
        private String routeName;
        private String siteName;
        private int checkPointId;
        private String checkPointName;
        private boolean checkPointIsVerified;
        private boolean assignedToMe;
        private boolean isVerified;
        private String administratorName;
        private String administratorMobile;
        private String checkInTime;
        private String checkOutTime;
        private String checkInAddress;
        private String checkOutAddress;
        private String workingHours;
        private String selfieImg;
        private String ESIC;
        private String Medical;
        private String UAN;
        private String empCode;
        private String AdhaarCard;
        private String DrivingLicence;
        private int suid;
        private String code;
        private boolean alreadySiteVisitStarted;
        private String visitToken;

        public String getEmpCode() {
            return empCode;
        }

        public void setEmpCode(String empCode) {
            this.empCode = empCode;
        }

        public int getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(int employeeId) {
            this.employeeId = employeeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getDoj() {
            return doj;
        }

        public void setDoj(String doj) {
            this.doj = doj;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getRouteId() {
            return routeId;
        }

        public void setRouteId(int routeId) {
            this.routeId = routeId;
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

        public int getCheckPointId() {
            return checkPointId;
        }

        public void setCheckPointId(int checkPointId) {
            this.checkPointId = checkPointId;
        }

        public String getCheckPointName() {
            return checkPointName;
        }

        public void setCheckPointName(String checkPointName) {
            this.checkPointName = checkPointName;
        }

        public boolean isCheckPointIsVerified() {
            return checkPointIsVerified;
        }

        public void setCheckPointIsVerified(boolean checkPointIsVerified) {
            this.checkPointIsVerified = checkPointIsVerified;
        }

        public boolean isAssignedToMe() {
            return assignedToMe;
        }

        public void setAssignedToMe(boolean assignedToMe) {
            this.assignedToMe = assignedToMe;
        }

        public boolean isIsVerified() {
            return isVerified;
        }

        public void setIsVerified(boolean isVerified) {
            this.isVerified = isVerified;
        }

        public String getAdministratorName() {
            return administratorName;
        }

        public void setAdministratorName(String administratorName) {
            this.administratorName = administratorName;
        }

        public String getAdministratorMobile() {
            return administratorMobile;
        }

        public void setAdministratorMobile(String administratorMobile) {
            this.administratorMobile = administratorMobile;
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

        public String getCheckOutAddress() {
            return checkOutAddress;
        }

        public void setCheckOutAddress(String checkOutAddress) {
            this.checkOutAddress = checkOutAddress;
        }

        public String getWorkingHours() {
            return workingHours;
        }

        public void setWorkingHours(String workingHours) {
            this.workingHours = workingHours;
        }

        public String getSelfieImg() {
            return selfieImg;
        }

        public void setSelfieImg(String selfieImg) {
            this.selfieImg = selfieImg;
        }

        public String getESIC() {
            return ESIC;
        }

        public void setESIC(String ESIC) {
            this.ESIC = ESIC;
        }

        public String getMedical() {
            return Medical;
        }

        public void setMedical(String Medical) {
            this.Medical = Medical;
        }

        public String getUAN() {
            return UAN;
        }

        public void setUAN(String UAN) {
            this.UAN = UAN;
        }

        public String getAdhaarCard() {
            return AdhaarCard;
        }

        public void setAdhaarCard(String AdhaarCard) {
            this.AdhaarCard = AdhaarCard;
        }

        public String getDrivingLicence() {
            return DrivingLicence;
        }

        public void setDrivingLicence(String DrivingLicence) {
            this.DrivingLicence = DrivingLicence;
        }

        public int getSuid() {
            return suid;
        }

        public void setSuid(int suid) {
            this.suid = suid;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isAlreadySiteVisitStarted() {
            return alreadySiteVisitStarted;
        }

        public void setAlreadySiteVisitStarted(boolean alreadySiteVisitStarted) {
            this.alreadySiteVisitStarted = alreadySiteVisitStarted;
        }

        public String getVisitToken() {
            return visitToken;
        }

        public void setVisitToken(String visitToken) {
            this.visitToken = visitToken;
        }
    }
}
